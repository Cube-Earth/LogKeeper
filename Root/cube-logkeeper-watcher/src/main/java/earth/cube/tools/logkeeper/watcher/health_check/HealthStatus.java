package earth.cube.tools.logkeeper.watcher.health_check;

public class HealthStatus {
	
	public static volatile boolean _bHealthy = true;
	private static String _sMsg;
	
	public static void setError(String sMsg) {
		if(_bHealthy)
			synchronized(HealthStatus.class) {
				if(_bHealthy) {
					_sMsg = sMsg;
					_bHealthy = false;
				}
			}
	}
	
	public static boolean isHealthy() {
		return _bHealthy;
	}
	
	public static String getMessage() {
		return _sMsg;
	}

	public static void reset() {
		synchronized(HealthStatus.class) {
			_sMsg = null;
			_bHealthy = true;
		}
	}
	
}
