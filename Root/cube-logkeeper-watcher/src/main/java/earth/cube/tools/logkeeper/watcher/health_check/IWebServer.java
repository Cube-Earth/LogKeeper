package earth.cube.tools.logkeeper.watcher.health_check;

import java.io.IOException;

public interface IWebServer {
	
	void start(int nPort) throws IOException;
	
	void stop();

}
