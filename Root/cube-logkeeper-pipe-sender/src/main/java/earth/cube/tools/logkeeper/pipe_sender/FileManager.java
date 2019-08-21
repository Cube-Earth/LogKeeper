package earth.cube.tools.logkeeper.pipe_sender;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import earth.cube.tools.logkeeper.pipe_sender.data.DataSender;

public class FileManager {
	
	public final static FileManager INSTANCE = new FileManager();
	
	private Map<File,OutputStream> _streams = new HashMap<>();

	private Map<File,RandomAccessFile> _files = new HashMap<>();
	
	
	protected OutputStream getStream(File file) throws IOException {
		synchronized(getClass()) {
			OutputStream out = _streams.get(file);
			if(out == null) {
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				_files.put(file, raf);
				
				FileChannel channel = raf.getChannel();
			    out = Channels.newOutputStream(channel);
				_streams.put(file, out);
			}
			return out;
		}
	}
		
	public void write(File file, LogMessage msg) {
		try {
			OutputStream out = getStream(file);
			synchronized(out) {
				new DataSender(msg.conclude(), out).send();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void write(File file, String sMsg, String sCharSet) {
		try {
			OutputStream out = getStream(file);
			synchronized(out) {
				out.write((sMsg + "\n").getBytes(sCharSet));
				out.flush();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		for(OutputStream os : _streams.values())
			try {
				os.close();
			} catch (IOException e) {
			}
		for(RandomAccessFile f : _files.values())
			try {
				f.close();
			} catch (IOException e) {
			}
	}
	
	

}
