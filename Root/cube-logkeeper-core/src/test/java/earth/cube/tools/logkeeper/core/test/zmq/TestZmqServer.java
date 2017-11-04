package earth.cube.tools.logkeeper.core.test.zmq;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.zeromq.ZMQ;

import earth.cube.tools.logkeeper.core.Globals;
import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.utils.jackson.JsonParser;

public class TestZmqServer implements Runnable, Closeable {
	
	private Semaphore _serverReady = new Semaphore(0);
	private Semaphore _serverStopped = new Semaphore(0);
	
	private List<LogMessage> _msgs = Collections.synchronizedList(new ArrayList<LogMessage>());
	
	@Override
	public void run() {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.XSUB);
        socket.bind(String.format("tcp://127.0.0.1:%s", Globals.ZMQ_PORT));
        _serverReady.release();
        while(true) {
            String s = socket.recvStr ();
            ServerAction action = RequestedServerAction.probe(s);
            System.out.println("" + action);
            if(action == ServerAction.QUIT)
            	break;
            if(action == ServerAction.NONE) {
            	LogMessage msg = JsonParser.fromJson(s, LogMessage.class);
            	_msgs.add(msg);
        	}
        }
        socket.close ();
        context.term ();
        _serverStopped.release();
    }
	
	@Override
	public void close() throws IOException {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.PUSH);
        socket.connect(String.format("tcp://127.0.0.1:%s", Globals.ZMQ_PORT));
        
		socket.send(RequestedServerAction.create(ServerAction.QUIT));
        socket.close();
        context.term();
        try {
			_serverStopped.acquire();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	public void start() throws InterruptedException {
		new Thread(this).start();
		_serverReady.acquire();
	}
	
	public List<LogMessage> getMessages() {
		return _msgs;
	}
	
	public LogMessage findMessageAndPurge(String sMarker) {
		LogMessage msg = null;
		do {
			if(msg != null)
				System.out.println(String.format("discarding log message: %s ## %s - %s", sMarker, msg.getLevel(), msg.getMessage()));
			if(_msgs.isEmpty()) {
				System.out.println(String.format("missing log message: %s", sMarker));
				throw new IllegalStateException(String.format("missing log message: %s", sMarker));
			}
			msg = _msgs.remove(0);
		}
		while(!msg.getMessage().contains(sMarker));
		return msg;
	}

	public LogMessage findMessage(String sMarker) {
		LogMessage msg = null;
		for(int i = _msgs.size() - 1; i >= 0; i--) {
			msg = _msgs.get(i);
			if(msg.getMessage().contains(sMarker))
				return msg;
		}
		throw new IllegalStateException(String.format("missing log message: %s", sMarker));
	}

}
