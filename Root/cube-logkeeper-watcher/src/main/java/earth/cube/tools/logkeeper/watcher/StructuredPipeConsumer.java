package earth.cube.tools.logkeeper.watcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.Map;

import earth.cube.tools.logkeeper.core.LogMessage;
import earth.cube.tools.logkeeper.core.forwarders.LogForwarder;
import earth.cube.tools.logkeeper.pipe_sender.data.DataReceiver;
import earth.cube.tools.logkeeper.watcher.config.LogConfigStructuredPipe;
import earth.cube.tools.logkeeper.watcher.health_check.HealthCheck;

public class StructuredPipeConsumer extends Thread implements IConsumer {
	
	private InputStream _in;
	private RandomAccessFile _file;

	public StructuredPipeConsumer(LogConfigStructuredPipe config) throws IOException {
		_file = new RandomAccessFile(config.getPath().toFile(), "rw");
		FileChannel channel = _file.getChannel();
	    _in = Channels.newInputStream(channel);
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				try {
					DataReceiver r = new DataReceiver(_in);
					Map<String,Object> map = r.read();
					map.put("type", "json");
					LogMessage msg = new LogMessage(map);
					LogForwarder.get().forward(msg);
					HealthCheck.getInstance().verify(msg);
				}
				catch(Throwable e) {
					if(e.getCause() instanceof InterruptedException || e instanceof InterruptedIOException)
						break;
					else
						if(!(e.getCause() instanceof ClosedByInterruptException))
							e.printStackTrace();
				}
			}
		}
		finally {
			try {
				_in.close();
			} catch (IOException e) {
			}
			try {
				_file.close();
			} catch (IOException e) {
			}
		}
	}
	
	
	public void flush() {
	}
	
	public void flushOverdue() {
	}

}
