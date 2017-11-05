package earth.cube.tools.logkeeper.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import earth.cube.tools.logkeeper.delegates.streams.StreamRedirector;

public class TomcatStreamRedirector implements LifecycleListener {

	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		switch(event.getType()) {

			case Lifecycle.BEFORE_START_EVENT:
				StreamRedirector.set();
				break;
				
			case Lifecycle.AFTER_STOP_EVENT:
				StreamRedirector.unset();
				break;
		}
		
	}

}
