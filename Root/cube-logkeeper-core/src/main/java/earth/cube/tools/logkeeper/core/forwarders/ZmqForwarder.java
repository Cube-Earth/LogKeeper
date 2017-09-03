package earth.cube.tools.logkeeper.core.forwarders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.zeromq.ZMQ;

import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.utils.jackson.JsonEmitter;

public class ZmqForwarder implements ILogForwarder {
	
	private String _sHostName;
	private int _nPort;
	private ZMQ.Context _ctx;
	private ZMQ.Socket _socket;
	private JsonEmitter _emitter = new JsonEmitter();
	

	@Override
	public void setConnectInfo(String sHostName, int nPort) {
		_sHostName = sHostName;
		_nPort = nPort;
	}

	@Override
	public void connect() {
        _ctx = ZMQ.context(1);
        _socket = _ctx.socket(ZMQ.PUSH);
        _socket.connect("tcp://" + _sHostName + ':' + _nPort);
	}

	public void forward(LogMessage msg) {
        String sJson = _emitter.getJson(msg);
        try {
			_socket.send(sJson.getBytes("utf-8"), 0);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
//        String sResult = new String(socket.recv(0));
//       System.out.println("=" + sResult);

	}

	public void close() throws IOException {
		if(_socket != null) {
	        _socket.close();
	        _socket = null;
	        
	        _ctx.term();
	        _ctx = null;
		}
	}

}
