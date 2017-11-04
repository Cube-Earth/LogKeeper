package earth.cube.tools.logkeeper.core.forwarders;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import earth.cube.tools.logkeeper.core.LogMessage;

public class LogDispatcher extends Thread {
	
	private static LogDispatcher _instance;

	private ILogForwarder _forwarder;
	
	private ArrayBlockingQueue<LogMessage> _msgs = new ArrayBlockingQueue<>(20000);
	
	private CountDownLatch _quitEvent = new CountDownLatch(1);
	

	static {
		try {
			_instance = new LogDispatcher(create());
			_instance.start();
			init();
		}
		catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}
	
	/** For JUnit tests only.
	 */
	public static void reinitalize() throws IOException {
		_instance._forwarder.close();
		_instance._forwarder = create();
		_instance._forwarder.connect();
	}
	
	private static void init() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				_instance.quit();
				System.err.println("Log Forwarder daemon exited."); // TODO remove
			}
		});
	}
	
	private static ILogForwarder create() {
		return LogForwarderFactory.create();
	}
	
	public static LogDispatcher get() {
		return _instance;
	}

	
	public LogDispatcher(ILogForwarder forwarder) {
		_forwarder = forwarder;
	}
	
	public static void add(LogMessage msg) {
		try {
			_instance._msgs.put(msg);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void quit() {
		_instance.interrupt();
		try {
			_quitEvent.await();
		} catch (InterruptedException e) {
			new IllegalStateException(e);
		}
	}
		
	@Override
	public void run() {
		_forwarder.connect();
		try {
			while(true) {
				_forwarder.forward(_msgs.take());
			}
		} catch (InterruptedException e) {
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
		finally {
			while(!_msgs.isEmpty()) {
				_forwarder.forward(_msgs.poll());
			}
			try {
				_forwarder.close();
			} catch (IOException e) {
				new IllegalStateException(e);
			}
			_quitEvent.countDown();
		}
	}
	
	public static boolean isEmpty() {
		return _instance._msgs.isEmpty();
	}
	
	public static void flush() {
		while(!_instance._msgs.isEmpty()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}

	}

}
