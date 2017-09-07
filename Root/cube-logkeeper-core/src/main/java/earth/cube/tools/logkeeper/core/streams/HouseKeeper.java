package earth.cube.tools.logkeeper.core.streams;

public class HouseKeeper extends Thread {
	
	private int _nWaitTime;

	private Thread _shutdownHook = new Thread() {
		public void run() {
			quit();
			System.err.println(getClass().getCanonicalName() + " daemon exited."); // TODO remove
		}
	};

	
	
	public HouseKeeper(int nWaitTime) {
		_nWaitTime = nWaitTime;
	}
	
	
	@Override
	public void run() {
		Runtime.getRuntime().addShutdownHook(_shutdownHook);
		
		while(true) {
			try {
				Thread.sleep(_nWaitTime);
			} catch (InterruptedException e) {
				break;
			}
			StreamRedirector.flushOverdue();
		}
	}

	public void quit() {
		Runtime.getRuntime().removeShutdownHook(_shutdownHook);
		interrupt();
		try {
			join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
	}
}
