package earth.cube.tools.logkeeper.watcher;

public interface IConsumer {
	
	void start();
	
	void interrupt();
	
	void join() throws InterruptedException;
	
	void flush();
	
	void flushOverdue();
}
