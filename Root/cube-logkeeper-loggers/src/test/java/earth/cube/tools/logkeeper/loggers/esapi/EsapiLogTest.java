package earth.cube.tools.logkeeper.loggers.esapi;

import org.junit.Test;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.LogFactory;
import org.owasp.esapi.reference.Log4JLogFactory;

public class EsapiLogTest {
	
	@Test
	public void test_1() {
		LogFactory l = Log4JLogFactory.getInstance();
		ESAPI.encoder().encodeForHTML("abc");		
	}

}
