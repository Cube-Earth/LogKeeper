package earth.cube.tools.logkeeper.core.forwarders;

public class TestForwarderTest {
	
	static {
		System.setProperty("logkeeper.forwarder", TestForwarder.class.getCanonicalName());
	}


}
