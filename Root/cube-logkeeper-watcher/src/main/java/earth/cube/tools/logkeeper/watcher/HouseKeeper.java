package earth.cube.tools.logkeeper.watcher;

public class HouseKeeper extends Thread {
	
	private int _nWaitTime;
	private Application _app;
	private static boolean _bShutdown;

	private Thread _shutdownHook = new Thread() {
		public void run() {
			quit();
			System.err.println(getClass().getCanonicalName() + " daemon exited."); // TODO remove
		}
	};


	
	
	public HouseKeeper(int nWaitTime, Application app) {
		_nWaitTime = nWaitTime;
		_app = app;
	}
	
	
	@Override
	public void run() {
		Runtime.getRuntime().addShutdownHook(_shutdownHook);
		try {
			while(true) {
				try {
					Thread.sleep(_nWaitTime);
				} catch (InterruptedException e) {
					break;
				}
				_app.flushOverdue();
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	public void quit() {
		if(!_bShutdown) {
			_bShutdown = true;
			Runtime.getRuntime().removeShutdownHook(_shutdownHook);
			interrupt();
			try {
				join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}		
		}
	}
}
