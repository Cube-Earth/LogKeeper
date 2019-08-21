package earth.cube.tools.logkeeper.watcher.health_check;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class SunWebServer implements IWebServer {

	private HttpServer _server;

    private static class Handler implements HttpHandler {
    	
    	protected void sendResponse(HttpExchange e, int nRC, String sResponse) throws IOException {
    		if(sResponse == null || sResponse.length() == 0)
    			sResponse = "no details available";
    		e.getResponseHeaders().add("Content-type", "text/plain");
            e.sendResponseHeaders(nRC, sResponse.length());
            OutputStream os = e.getResponseBody();
            os.write(sResponse.getBytes());
            os.close();
    	}
        
    	@Override
        public void handle(HttpExchange e) throws IOException {
    		if(HealthStatus.isHealthy())
    			sendResponse(e, 200, "Everythinhg is ok!");
    		else
    			sendResponse(e, 500, HealthStatus.getMessage());
        }
    }

	@Override
	public void start(int nPort) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(nPort), 0);
        server.createContext("/health", new Handler());
        server.setExecutor(null); // creates a default executor
        server.start();
        _server = server;
	}

	@Override
	public void stop() {
		_server.stop(0);
	}

}
