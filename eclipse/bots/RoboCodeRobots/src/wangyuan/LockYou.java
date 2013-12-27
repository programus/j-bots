package wangyuan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class LockYou extends AdvancedRobot {
	
	private Point2D.Double enemyPosition = new Point2D.Double();

	@Override
	public void run() {
		// let the radar move freely.
		this.setAdjustRadarForGunTurn(true);
		this.setAdjustRadarForRobotTurn(true);
		// set the scan color.
		this.setScanColor(Color.YELLOW);
		// turn the radar.
		this.turnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// get the relative polar coordinates of enemy.
		double enemyAngle = this.getHeadingRadians() + event.getBearingRadians();
		double enemyDistance = event.getDistance();
		
		// calculate the x,y coordinates of the enemy.
		this.calculateEnemyPosition(enemyAngle, enemyDistance);
		out.println(String.format("(%.2f, %.2f)", this.enemyPosition.x, this.enemyPosition.y));
		
		// calculate the angle we should turn the radar.
		double radarTurn = enemyAngle - this.getRadarHeadingRadians();
		// turn radar.
		this.setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
		
		// calculate the angle we should turn the gun.
		double gunTurn = enemyAngle - this.getGunHeadingRadians();
		// turn gun towards enemy. (actually, previous position)
		this.setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
		
		// Fire!
		if (gunTurn < Math.PI / 18) {
			this.setFire(this.getEnergy() > Rules.MAX_BULLET_POWER * 3 ? Rules.MAX_BULLET_POWER : this.getEnergy() / 2);
		}
		
		// keep scanning...
		this.scan();
	}
	
	private void calculateEnemyPosition(double angleRedians, double distance) {
		this.enemyPosition.x = this.getX() + Math.sin(angleRedians) * distance;
		this.enemyPosition.y = this.getY() + Math.cos(angleRedians) * distance;
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		Color oldColor = g.getColor();
		g.setColor(Color.YELLOW);
		g.drawLine((int)this.getX(), (int)this.getY(), (int)this.enemyPosition.x, (int)this.enemyPosition.y);
		g.setColor(Color.RED);
		g.fillOval((int)(this.enemyPosition.x - 3), (int)(this.enemyPosition.y - 3), 7, 7);
		g.drawOval((int)(this.enemyPosition.x - 10), (int)(this.enemyPosition.y - 10), 21, 21);
		g.setColor(Color.GREEN);
		g.drawString(String.format("(%.2f, %.2f)", this.enemyPosition.x, this.enemyPosition.y), (float)(this.enemyPosition.x + 30), (float)(this.enemyPosition.y));
		g.setColor(oldColor);
	}

}
