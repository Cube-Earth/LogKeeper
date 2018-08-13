package earth.cube.tools.logkeeper.watcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

import earth.cube.tools.logkeeper.watcher.appenders.StdInMessageConcentrator;
import earth.cube.tools.logkeeper.watcher.config.LogConfig;

public class StdInConsumer extends Thread {
	
	private StdInMessageConcentrator _concentrator;

	public StdInConsumer(LogConfig config) {
		_concentrator = new StdInMessageConcentrator(config);
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

		try(BufferedReader in = new BufferedReader(
			    new InputStreamReader(System.in))) {
			String sLine = in.readLine();
			while(sLine != null) {
				_concentrator.append(sLine);
				sLine = in.readLine();
			}
		} catch (IOException e) {
			if(!(e.getCause() instanceof InterruptedException || e instanceof InterruptedIOException))
				throw new RuntimeException(e);
		}
	}
	
	
	public void flush() {
		_concentrator.flush();
	}
	
	public void flushOverdue() {
		_concentrator.flushOverdue();
	}

}
