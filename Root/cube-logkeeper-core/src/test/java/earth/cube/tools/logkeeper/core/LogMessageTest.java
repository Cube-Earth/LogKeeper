package earth.cube.tools.logkeeper.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import earth.cube.tools.logkeeper.core.utils.jackson.JsonEmitter;


public class LogMessageTest {

	private Date getDate(int nYear, int nMonth, int nDay, int nHour, int nMinute, int nSecond) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, nYear);
		cal.set(Calendar.MONTH, nMonth-1);
		cal.set(Calendar.DAY_OF_MONTH, nDay);
		cal.set(Calendar.HOUR_OF_DAY, nHour);
		cal.set(Calendar.MINUTE, nMinute);
		cal.set(Calendar.SECOND, nSecond);
		return cal.getTime();
	}
	
	
	@Test
	public void test_1() {
		LogMessage msg = new LogMessage();
		msg.appendMsg("this is a message");
		msg.appendMsg("with to lines!");
//		msg.setDate(LocalDateTime.of(2016, 12, 31, 12, 30, 00));
		msg.setDate(getDate(2016, 12, 31, 12, 30, 00));
		msg.setFilePath(Paths.get("/tmp/my_log.txt"));
		msg.setSource("my_app");
		
		String sExpected = "{\"message\":\"this is a message\\nwith to lines!\",\"date\":\"2016-12-31 12:30:00.000\",\"source\":\"my_app\",\"level\":\"INFO\",\"thread\":\"main\",\"file_path\":\"/tmp/my_log.txt\"}";
		Assert.assertEquals(sExpected, JsonEmitter.toJson(msg));
	}

	private String toJsonString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		String s = sw.toString().replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t");
		System.out.println(s);
		Assert.assertTrue(s.matches(".*\\\\n\\\\tat .*"));
		return s;
	}
	
	@Test
	public void test_exception_1() {
		Throwable t = new IllegalStateException("This is an exception!");

		LogMessage msg = new LogMessage();
		msg.appendMsg("this is a message");
		msg.appendMsg("with to lines!");
//		msg.setDate(LocalDateTime.of(2016, 12, 31, 12, 30, 00));
		msg.setDate(getDate(2016, 12, 31, 12, 30, 00));
		msg.setFilePath(Paths.get("/tmp/my_log.txt"));
		msg.setSource("my_app");
		msg.setThrowable(t);
		
		String sExpected = String.format("{\"message\":\"this is a message\\nwith to lines!\",\"date\":\"2016-12-31 12:30:00.000\",\"source\":\"my_app\",\"level\":\"INFO\",\"thread\":\"main\",\"file_path\":\"/tmp/my_log.txt\","
				+ "\"throwable_type\":\"java.lang.IllegalStateException\",\"throwable_message\":\"This is an exception!\","
				+ "\"throwable_stacktrace\":\"%s\"" 
				+ "}", toJsonString(t));
		Assert.assertEquals(sExpected, JsonEmitter.toJson(msg));
	}

}
