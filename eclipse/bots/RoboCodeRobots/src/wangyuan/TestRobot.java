package wangyuan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import robocode.AdvancedRobot;
import robocode.RobocodeFileWriter;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wangyuan.utils.RoboUtils;

public class TestRobot extends AdvancedRobot {
	final public static int MARGIN = 10;
	private Point2D.Double enemyLocation = new Point2D.Double(-1, -1);
	private Rectangle2D.Double field;
	
	private void init() {
		this.setAdjustGunForRobotTurn(true);
		this.setAdjustRadarForGunTurn(true);
		this.setAdjustGunForRobotTurn(true);
		this.field = new Rectangle2D.Double(
				this.getWidth() / 2 + MARGIN, 
				this.getHeight() / 2 + MARGIN, 
				this.getBattleFieldWidth() - this.getWidth() - MARGIN * 2, 
				this.getBattleFieldHeight() - this.getHeight() - MARGIN * 2);
	}

	@Override
	public void run() {
		this.init();
		this.setBodyColor(Color.YELLOW);
		
		try {
			RobocodeFileWriter writer = new RobocodeFileWriter(this.getDataFile("test.txt"));
			writer.write("Test");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.getDataFile("test.txt")));
			out.println(reader.readLine());
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (true) {
			if (this.enemyLocation.x >= 0 && this.enemyLocation.y >= 0) {
				double dx = this.enemyLocation.x - this.getX();
				double dy = this.enemyLocation.y - this.getY();
				double enemyDis = Math.sqrt(dx * dx + dy * dy);
				double enemyAngle = Math.asin(dy / enemyDis);
				if (dx < 0) {
					enemyAngle = Math.PI - enemyAngle;
				}
				enemyAngle = RoboUtils.convertAngleRadians(enemyAngle);
				double gunAngle = Utils.normalRelativeAngle(enemyAngle - this.getGunHeadingRadians());
				double bodyAngle = Utils.normalRelativeAngle(enemyAngle - this.getHeadingRadians());
				this.setTurnGunRightRadians(Math.min(gunAngle, Rules.GUN_TURN_RATE_RADIANS));
				this.setTurnRightRadians(Math.min(bodyAngle, Rules.MAX_TURN_RATE_RADIANS));
				if (Math.abs(Utils.normalRelativeAngle(this.getHeadingRadians() - enemyAngle)) < Math.PI / 22) {
					this.setAhead(Rules.MAX_VELOCITY);
					if (bodyAngle < Math.atan(15. / enemyDis)) {
						this.setFire(100 / enemyDis);
					}
				}
			}
			this.execute();
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		double angle = this.getHeadingRadians() + event.getBearingRadians();
		double radarTurn = Utils.normalRelativeAngle(angle - this.getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(30. / event.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
		radarTurn += radarTurn < 0 ? -extraTurn : extraTurn;
		this.setTurnRadarRightRadians(radarTurn);
		
		this.enemyLocation.x = this.getX() + Math.sin(angle) * event.getDistance();
		this.enemyLocation.y = this.getY() + Math.cos(angle) * event.getDistance();
		// System.out.println(this.enemyLocation);
		// System.out.println(angle);
	}

	@Override
	public void onStatus(StatusEvent e) {
		this.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		// System.out.println(this.getX());
		// System.out.println(this.getY());
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.drawLine((int)this.enemyLocation.x, (int)this.enemyLocation.y, (int)this.getX(), (int)this.getY());
		g.drawString("This is King!", (int)this.getX(), (int)this.getY() + 30);
		g.fillRect((int)this.enemyLocation.x - 2, (int)this.enemyLocation.y - 2, 5, 5);
	}
}
