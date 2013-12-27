package wangyuan.utils;

public class RoboUtils {
	public static double convertAngleRadians(double normal) {
		return Math.PI / 2 - normal;
	}
	
	public static double convertAngleDegrees(double normal) {
		return 90 - normal;
	}
}
