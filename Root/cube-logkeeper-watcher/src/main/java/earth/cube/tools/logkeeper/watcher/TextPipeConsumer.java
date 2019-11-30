package earth.cube.tools.logkeeper.watcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;

import earth.cube.tools.logkeeper.watcher.appenders.TextPipeMessageConcentrator;
import earth.cube.tools.logkeeper.watcher.config.LogConfigTextPipe;

public class TextPipeConsumer extends Thread implements IConsumer {
	
	private TextPipeMessageConcentrator _concentrator;
	private InputStream _in;
	private boolean _bCloseIn;
	private RandomAccessFile _file;

	public TextPipeConsumer(LogConfigTextPipe config) throws IOException {
		_concentrator = new TextPipeMessageConcentrator(config);
		switch(config.getPath().toString()) {
			case "/dev/stdin":
				_in = System.in;
				_bCloseIn = false;
				break;
				
			default:
				_file = new RandomAccessFile(config.getPath().toFile(), "rw");
				FileChannel channel = _file.getChannel();
			    _in = Channels.newInputStream(channel);				
				_bCloseIn = true;
		}
	}
	
	@Override
	public void run() {
/*		
		try(BufferedReader in = new BufferedReader(
			    new InputStreamReader(Channels.newInputStream((
			    new FileInputStream(FileDescriptor.in)).getChannel())))) {
			String sLine = in.readLine();
			while(sLine != null) {
				_concentrator.append(sLine);
				sLine = in.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
*/

		try(BufferedReader in = new BufferedReader(new InputStreamReader(_in))) {
			String sLine = in.readLine();
			while(sLine != null) {
				try {
					_concentrator.append(sLine);
					sLine = in.readLine();
				}
				catch(Throwable e) {
					if(e.getCause() instanceof InterruptedException || e instanceof InterruptedIOException)
						break;
					else
						if(!(e.getCause() instanceof ClosedByInterruptException))
							e.printStackTrace();
				}
			}
		} catch (IOException e) {
			if(!(e.getCause() instanceof InterruptedException || e instanceof InterruptedIOException))
				throw new RuntimeException(e);
		}
		finally {
			if(_bCloseIn) {
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
	}
	
	
	public void flush() {
		_concentrator.flush();
	}
	
	public void flushOverdue() {
		_concentrator.flushOverdue();
	}

}
