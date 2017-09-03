package earth.cube.tools.logkeeper.watcher.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class StringSplitTest {
	
	@Test
	public void test_1() {
		String s = "pattern:$1:date:'uuuu-MM-dd HH:mm:ss.SSS':abc";
		String[] t = new String[] { "+pattern", "+$1", "+date", "-uuuu-MM-dd HH:mm:ss.SSS", "+abc" };
		String sItemPattern = "(?:'((?:[^'\\\\]|\\\\.)*)'|((?<!')[^:']+)):?";
		Pattern p = Pattern.compile(sItemPattern);
		Matcher m = p.matcher(s);
		for(int i = 0; i < t.length; i++) {
			Assert.assertTrue(Integer.toString(i), m.find());
			Assert.assertEquals(t[i], m.group(1) != null ? "-" + m.group(1) : "+" + m.group(2));
		}
		Assert.assertFalse(m.find());
	}

}
