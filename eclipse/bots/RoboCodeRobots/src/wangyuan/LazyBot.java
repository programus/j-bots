package wangyuan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class LazyBot extends AdvancedRobot {
	public class Enemy {
		private double x;
		private double y;
		private double distance = -1;
		private double angle = -1;
		public double getDistance() {
			if (this.distance < 0) {
				double dx = getX() - x;
				double dy = getY() - y;
				this.distance = Math.sqrt(dx * dx + dy * dy);
			}
			return this.distance;
		}
		public double getAngle() {
			if (this.angle < 0) {
				double dx = getX() - x;
				double dy = getY() - y;
				angle = Math.acos(dy / this.getDistance());
				if (dx < 0) {
					angle = Math.PI * 2 - angle;
				}
			}
			return angle;
		}
		public void setPolarValue(double distance, double angle) {
			this.x = getX() + Math.sin(angle) * distance;
			this.y = getY() + Math.cos(angle) * distance;
			this.distance = distance;
			this.angle = angle;
		}
		public void setPosition(double x, double y) {
			this.x = x;
			this.y = y;
			this.distance = this.angle = -1;
		}
	}
	
	private final static double MAX_AIM_DISTANCE = 500;
	
	private Enemy scannedEnemy = new Enemy();
	private Enemy guessedEnemy = new Enemy();
	private double enemyEnergy;
	private int direction = 1;	// direction: 1 - ahead, -1 - back
	private boolean hit;
	private Rectangle2D.Double battleField;
	private Rectangle2D.Double moveField;

	@Override
	public void onBulletHit(BulletHitEvent event) {
		// to monitor enemy's energy
		enemyEnergy = event.getEnergy();
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		// to monitor enemy's energy
		enemyEnergy += event.getPower() * 3;
		this.hit = true;
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		// to monitor enemy's energy
		enemyEnergy -= 0.6;
	}

	@Override
	public void run() {
		this.init();
		// turn the radar.
		this.turnRadarRightRadians(Double.POSITIVE_INFINITY);
	}
	
	private void init() {
		// let the radar move freely.
		this.setAdjustRadarForGunTurn(true);
		this.setAdjustRadarForRobotTurn(true);
		// initialize battle field bound.
		this.battleField = new Rectangle2D.Double(0, 0, this.getBattleFieldWidth(), this.getBattleFieldHeight());
		this.moveField = new Rectangle2D.Double(
				this.getWidth(), 
				this.getHeight(), 
				this.getBattleFieldWidth() - this.getWidth() * 2, 
				this.getBattleFieldHeight() - this.getHeight() * 2);
		// set the scan color.
		this.setScanColor(Color.YELLOW);
		// our bullets are fire balls
		this.setBulletColor(Color.ORANGE);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// get the relative polar coordinates of enemy.
		this.scannedEnemy.setPolarValue(event.getDistance(), Utils.normalAbsoluteAngle(this.getHeadingRadians() + event.getBearingRadians()));
		
		// ======= radar =======
		// calculate the angle we should turn the radar.
		double radarTurn = this.scannedEnemy.getAngle() - this.getRadarHeadingRadians();
		// turn radar.
		this.setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
		
		// ======= movement =======
		// always keep this angle so that we could escape as fast as we can
		double heading = this.scannedEnemy.getAngle() + Math.PI / 2;
		double bodyTurn = heading - this.getHeadingRadians();
		this.setTurnRightRadians(Utils.normalRelativeAngle(bodyTurn));
		double energyChange = enemyEnergy - event.getEnergy();
		// keep monitoring the enemy's energy
		enemyEnergy = event.getEnergy();
		if (energyChange >= 0.1 && energyChange <= 3.001) {
			// enemy fired!
			double moveDistance = this.getHeight() * 1.2;
			if (this.hit) {
				this.hit = false;
				this.direction = -this.direction;
			}
			Point2D.Double nextPos = new Point2D.Double(
					this.getX() + this.direction * moveDistance * Math.sin(heading), 
					this.getY() + this.direction * moveDistance * Math.cos(heading));
			if (!this.moveField.contains(nextPos)) {
				this.direction = -this.direction;
			}
			this.setAhead(moveDistance * this.direction);
		}
		
		// ======= targeting =======
		// use more power on near target
		double power = Rules.MAX_BULLET_POWER - this.scannedEnemy.getDistance() * (Rules.MAX_BULLET_POWER / 2) / MAX_AIM_DISTANCE;
		if (this.getEnergy() < Rules.MAX_BULLET_POWER) {
			// if no enough energy, use the min power
			power = Rules.MIN_BULLET_POWER;
		}
		// guess where the enemy would be
		this.guessEnemyMove(event, power);
		// calculate the angle the gun should turn
		double gunTurn = this.guessedEnemy.getAngle() - this.getGunHeadingRadians();
		// turn gun towards the point the enemy would be
		this.setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
		
		if (gunTurn < Rules.GUN_TURN_RATE_RADIANS && this.battleField.contains(this.guessedEnemy.x, this.guessedEnemy.y)) {
			// if the gun could turn to the target and the target point is in the battle field
			// Fire!
			this.setFire(power);
		}
		
		// keep scanning...
		this.scan();
	}
	
	private void guessEnemyMove(ScannedRobotEvent e, double power) {
		double alpha = Utils.normalRelativeAngle((Math.PI - (e.getHeadingRadians() - this.scannedEnemy.getAngle())));
		double v = e.getVelocity();
		double vb = 20 - 3 * power;
		double beta = (Math.asin(v * Math.sin(alpha) / vb));
		double gamma = Math.PI - alpha - beta;
		double sinGamma = Math.sin(gamma);
		double distance = sinGamma == 0 ? 
				this.scannedEnemy.getDistance() / (vb - v) * vb :
				this.scannedEnemy.getDistance() * Math.sin(alpha) /  sinGamma;
		double angle = Utils.normalAbsoluteAngle(this.scannedEnemy.getAngle() + beta);
		this.guessedEnemy.setPolarValue(distance, angle);
		out.println(String.format("enemyHeading=%.2f, enemyAngle=%.2f", Math.toDegrees(e.getHeadingRadians()), Math.toDegrees(this.scannedEnemy.getAngle())));
		out.println(String.format("alpha=%.2f, beta=%.2f, gamma=%.2f, D=%.2f", Math.toDegrees(alpha), Math.toDegrees(beta), Math.toDegrees(gamma), this.scannedEnemy.getDistance()));
		out.println(String.format("(%.2f, %.2f) -> (%.2f, %.2f)", this.scannedEnemy.x, this.scannedEnemy.y, this.guessedEnemy.x, this.guessedEnemy.y));
	}
	
	private void drawGunPredict() {
		Graphics2D g = this.getGraphics();
		Color oldColor = g.getColor();
		g.setColor(Color.PINK);
		g.fillOval((int)(this.guessedEnemy.x - this.getWidth() / 2), (int)(this.guessedEnemy.y - this.getHeight() / 2), (int)this.getWidth(), (int)this.getHeight());
		g.setColor(Color.RED);
		g.drawLine((int)this.getX(), (int)this.getY(), (int)this.guessedEnemy.x, (int)this.guessedEnemy.y);
		g.setColor(Color.GRAY);
		g.drawLine((int)this.guessedEnemy.x, (int)this.guessedEnemy.y, (int)this.scannedEnemy.x, (int)this.scannedEnemy.y);
		g.setColor(oldColor);
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		Color oldColor = g.getColor();
		g.setColor(Color.YELLOW);
		g.drawLine((int)this.getX(), (int)this.getY(), (int)this.scannedEnemy.x, (int)this.scannedEnemy.y);
		g.setColor(Color.RED);
		g.fillOval((int)(this.scannedEnemy.x - 3), (int)(this.scannedEnemy.y - 3), 7, 7);
		g.drawOval((int)(this.scannedEnemy.x - 10), (int)(this.scannedEnemy.y - 10), 21, 21);
		g.setColor(Color.GREEN);
		g.drawString(String.format("(%.2f, %.2f)", this.scannedEnemy.x, this.scannedEnemy.y), (float)(this.scannedEnemy.x + 30), (float)(this.scannedEnemy.y));
		g.setColor(oldColor);
		this.drawGunPredict();
	}
}
