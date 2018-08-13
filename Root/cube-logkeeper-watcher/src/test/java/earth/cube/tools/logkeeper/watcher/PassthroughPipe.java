package earth.cube.tools.logkeeper.watcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class PassthroughPipe extends OutputStream {
	
	private PipedInputStream _in;
	
	private PipedOutputStream _out;
	
	private Object _pendingMutex = new Object();
	
	private ByteArrayOutputStream _pending = new ByteArrayOutputStream();
	
	private BlockingQueue<byte[]> _sending = new LinkedBlockingQueue<>(); 
	
	private int _nState;
	
	private Thread _outThread;
	
	private boolean _bShutdown;
	
	private boolean _bFailed;
	
	
	protected class OutputWriter extends Thread {
		
		@Override
		public void run() {
			try {
				while(true) {
					byte[] buf = _sending.take();
					if(buf.length == 0 && _bShutdown)
						break;
					_out.write(buf);
				}
			} catch (InterruptedException e) {
			} catch (IOException e) {
				_bFailed = true;
				throw new RuntimeException(e);
			}
		}
		
	}
	
	
	public PassthroughPipe() throws IOException {
		_in = new PipedInputStream();
		_out = new PipedOutputStream(_in);
		_outThread = new OutputWriter();
		_outThread.start();
	}


	@Override
	public void write(int b) throws IOException {
		if(_bFailed)
			throw new IOException();
		synchronized(_pendingMutex) {
			_pending.write(b);
			if(b == 10)
				_nState = 1;
			else
				if(b == 13)
					_nState = 2;
				else
					if(_nState == 1)
						_nState = 2;
			if(_nState == 2) {
				_sending.add(_pending.toByteArray());
				_pending = new ByteArrayOutputStream();
			}
		}
	}
	
	
	@Override
	public void flush() throws IOException {
		synchronized(_pendingMutex) {
			_sending.add(_pending.toByteArray());
			_pending = new ByteArrayOutputStream();
		}
	}
	
	
	@Override
	public void close() throws IOException {
		flush();
		_bShutdown = true;
		_sending.add(new byte[0]);
		try {
			_outThread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public InputStream getInputStream() {
		return _in;
	}
	
	
	public OutputStream getOutputStream() {
		return _out;
	}

}
