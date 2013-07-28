package org.programus.robots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class TestRobot extends AdvancedRobot {
	private Point2D.Double enemyLocation = new Point2D.Double(-1, -1);

	@Override
	public void run() {
		this.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		while (true) {
			if (this.enemyLocation.x >= 0 && this.enemyLocation.y >= 0) {
				double dx = this.enemyLocation.x - this.getX();
				double dy = this.enemyLocation.y - this.getY();
				double enemyDis = Math.sqrt(dx * dx + dy * dy);
				double enemyAngle = Math.asin(dy / enemyDis);
				if (dx < 0) {
					enemyAngle = Math.PI - enemyAngle;
				}
				enemyAngle = Math.PI / 2 - enemyAngle;
				double gunAngle = Utils.normalRelativeAngle(enemyAngle - this.getGunHeadingRadians());
				double bodyAngle = Utils.normalRelativeAngle(enemyAngle - this.getHeadingRadians());
				this.setTurnGunRightRadians(Math.min(gunAngle, Rules.GUN_TURN_RATE_RADIANS));
				this.setTurnRightRadians(Math.min(bodyAngle, Rules.MAX_TURN_RATE_RADIANS));
				if (Math.abs(Utils.normalRelativeAngle(this.getHeadingRadians() - enemyAngle)) < Math.PI / 22 && enemyDis < 100) {
					this.setFire(this.getEnergy() / 10);
				}
			}
//			this.scan();
			this.execute();
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		double angle = this.getHeadingRadians() + event.getBearingRadians();
		double radarTurn = Utils.normalRelativeAngle(angle - this.getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(72. / event.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
		radarTurn += radarTurn < 0 ? -extraTurn : extraTurn;
		this.setTurnRadarRightRadians(radarTurn);
		
		this.enemyLocation.x = this.getX() + Math.sin(angle) * event.getDistance();
		this.enemyLocation.y = this.getY() + Math.cos(angle) * event.getDistance();
		System.out.println(this.enemyLocation);
		System.out.println(angle);
	}

	@Override
	public void onStatus(StatusEvent e) {
		System.out.println(this.getX());
		System.out.println(this.getY());
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.drawLine((int)this.enemyLocation.x, (int)this.enemyLocation.y, (int)this.getX(), (int)this.getY());
		g.fillRect((int)this.enemyLocation.x - 2, (int)this.enemyLocation.y - 2, 5, 5);
	}
}
