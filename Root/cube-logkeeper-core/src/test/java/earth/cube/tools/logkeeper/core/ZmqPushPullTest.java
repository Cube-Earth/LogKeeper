package earth.cube.tools.logkeeper.core;

import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Test;
import org.zeromq.ZMQ;

public class ZmqPushPullTest {
	
	private String _s;
	private String _t;
	private Semaphore _serverReady = new Semaphore(0);
	private Thread _t1;
	private Thread _t2;
	
	protected class Server extends Thread {
		
		@Override
		public void run() {
	        ZMQ.Context context = ZMQ.context(1);
	        ZMQ.Socket socket = context.socket(ZMQ.XSUB);
	        socket.bind("tcp://127.0.0.1:2120");
	        _serverReady.release();
	        System.out.println("Server started");
            _t = socket.recvStr();
	        System.out.println("Server received data");
	        socket.close ();
	        context.term ();		
	        System.out.println("Server stopped");
	    }
		
	}
	
	
	protected class Client extends Thread {

		@Override
		public void run() {
	        System.out.println("Server is waiting ...");
			try {
				_serverReady.acquire();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
	        ZMQ.Context context = ZMQ.context(1);
	        ZMQ.Socket socket = context.socket(ZMQ.PUSH);
	        socket.connect("tcp://127.0.0.1:2120");
	        System.out.println("Client started");
			socket.send(_s);
	        System.out.println("Client sent data");
	        socket.close();
	        context.term();		
	        System.out.println("Client stopped");
	    }
	}
	
	
	@Test
	public void test_1() throws InterruptedException {
		
		_s = "test";
		_t1 = new Server();
		_t2 = new Client();
		_t1.start();
		_t2.start();
		_t2.join();
		_t1.join();
		Assert.assertEquals(_s, _t);
	}

}
