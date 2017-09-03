package earth.cube.tools.logkeeper.watcher.events;

public interface IPublishLineFunc {
	
	void publishLine(byte[] buf, int nOfs, int nLen);

}
