package earth.cube.tools.logkeeper.watcher.expressions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import earth.cube.tools.logkeeper.watcher.expressions.Context;
import earth.cube.tools.logkeeper.watcher.expressions.MapLookup;
import earth.cube.tools.logkeeper.watcher.expressions.MatcherLookup;


public class ContextTest {
	
	private static Context _ctx;
	private static Map<String,String> _map = new HashMap<>();

	@BeforeClass
	public static void setUp() {
		_ctx = new Context();
		_ctx.addScope("map", new MapLookup(_map));
	}
	
	@Test
	public void test_resolve_env_1() {
		Assert.assertEquals('x' + System.getenv("PATH") + 'x', _ctx.resolve("x${env:PATH}x"));
	}

	@Test
	public void test_resolve_sys_1() {
		Assert.assertEquals('x' + System.getProperty("user.home") + 'x', _ctx.resolve("x${sys:user.home}x"));
	}


	@Test
	public void test_resolve_pattern_1() {
		String s = "123abcdef";
		Pattern p = Pattern.compile("([0-9]*)([a-z]*)(.*)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(s);
		_ctx.addScope("matcher", new MatcherLookup(m));
		
		Assert.assertTrue(m.matches());
		
		Assert.assertEquals("-abcdef-123-", _ctx.resolve("-${matcher:2}-${matcher:1}-"));
	}

	@Test
	public void test_resolve_map_1() {
		_map.put("a", "123");
		_map.put("b","456");
		
		Assert.assertEquals("-456-123-", _ctx.resolve("-${map:b}-${map:a}-"));
	}

	@Test
	public void test_resolve_map_convert_1() {
		_map.put("d", "12/31/2016 12:30:00");
		_map.put("s","abc");
		
		Assert.assertEquals("#2016-12-31 12:30:00.000#abc#", _ctx.resolve("#${map:d, date, 'MM/dd/uuuu HH:mm:ss'}#${map:s, string}#"));
	}

}
