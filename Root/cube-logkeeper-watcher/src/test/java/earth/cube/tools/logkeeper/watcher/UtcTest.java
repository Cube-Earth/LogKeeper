package earth.cube.tools.logkeeper.watcher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.Test;

public class UtcTest {
	
	private DateTimeFormatter _df = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
	
	@Test
	public void test_1() {
		LocalDateTime dt = LocalDateTime.of(2012, 5, 12, 12, 00, 00);
		ZonedDateTime utc = dt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
		
		Assert.assertEquals(_df.format(dt.plusHours(-2)), _df.format(utc));  // Subtract 2 hours for time zone and daylight saving
	}

}
