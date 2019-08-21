package earth.cube.tools.logkeeper.pipe_sender.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

public class DataReceiverTest {
	
	private void compareMap(Map<String, Object> expected, Map<String, Object> actual) {
		Map<String, Object> expected2 = new HashMap<>(expected);
		for(Entry<String, Object> e : actual.entrySet()) {
			Assert.assertTrue("Unexpected key '" + e.getKey() + "'", expected2.containsKey(e.getKey()));
			Assert.assertEquals("Unexpected value for key '" + e.getKey() + "'", expected2.get(e.getKey()), e.getValue());
			expected2.remove(e.getKey());
		}
		for(Entry<String, Object> e : expected2.entrySet()) {
			Assert.fail("Unexpected key '" + e.getKey() + "' with value '" + e.getValue());
		}
	}

	@Test
	public void test_1() throws IOException {
		Map<String,Object> map = new HashMap<>();
		map.put("int", Integer.MIN_VALUE);
		map.put("str", "any\nvalue");
		map.put("date", new Date());
		map.put("bool", true);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		DataSender sender = new DataSender(Collections.unmodifiableMap(map), baos);
		sender.send();
		
		byte[] buf = baos.toByteArray();
		
		Assert.assertTrue(buf.length > 5);
		
		DataReceiver receiver = new DataReceiver(new ByteArrayInputStream(buf));
		
		Map<String,Object> map2 = receiver.read();
		
		compareMap(map, map2);
		
	}


}
