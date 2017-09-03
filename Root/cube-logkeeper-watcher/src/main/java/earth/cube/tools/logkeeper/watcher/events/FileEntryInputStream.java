package earth.cube.tools.logkeeper.watcher.events;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import earth.cube.tools.logkeeper.watcher.IFunc;

public class FileEntryInputStream<T> extends FilterInputStream{
	
	private IFunc<T> _func;
	private T _obj;

	public FileEntryInputStream(InputStream in, IFunc<T> func, T obj) {
		super(in);
		_func = func;
		_obj = obj;
	}

	@Override
	public void close() throws IOException {
		_func.execute(_obj);
		super.close();
		
	}

}
