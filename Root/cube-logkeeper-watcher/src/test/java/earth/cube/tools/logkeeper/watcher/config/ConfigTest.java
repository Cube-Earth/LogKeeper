package earth.cube.tools.logkeeper.watcher.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import earth.cube.tools.logkeeper.watcher.LogTest;
import earth.cube.tools.logkeeper.watcher.config.Config;
import earth.cube.tools.logkeeper.watcher.config.LinePatternConfig;
import earth.cube.tools.logkeeper.watcher.config.LogConfig;

public class ConfigTest {
	
	private static Config _config;
	
	private static Path _logDir;

	@BeforeClass
	public static void setUp() throws JsonParseException, JsonMappingException, IOException {
		_logDir = Paths.get(Files.createTempDirectory("junit-" + LogTest.class.getSimpleName()).toString(),"logkeeper.tmp");
		// TODO delete _logdir recursively
		
		System.setProperty("LOG_DIR", _logDir.toString());
		String sResName = ConfigTest.class.getPackage().getName().replace('.', '/') + "/LogTracker.yml";
		_config = Config.read(ConfigTest.class.getClassLoader().getResourceAsStream(sResName));
		Assert.assertNotNull(_config);
	}
	
	@Test
	public void test_1() {
		Assert.assertEquals(1, _config.getLogConfigs().size());
		
		LogConfig c = _config.getLogConfigs().get(0);
		Assert.assertEquals(Paths.get("abc"), c.getDirectory());
		Assert.assertEquals("iso-8859-1", c.getEncoding());
		Assert.assertEquals(".*\\.log", c.getGlobString().toString());
	}

        @Test
	public void test_2() {
		Assert.assertEquals(1, _config.getLogConfigs().get(0).getLineRules().size());
		
		LinePatternConfig c = _config.getLogConfigs().get(0).getLineRules().get(0);
		Assert.assertEquals("([0-9]{4}-[0-9]{2}-[0-9]{2}) ([a-zA-Z]) (.*)", c.getTextPattern().toString());
		Assert.assertFalse(c.shouldSkip());
	}

	@Test
	public void test_3() {
		Map<String, String> c = _config.getLogConfigs().get(0).getLineRules().get(0).getFields();
		Assert.assertEquals(3, c.size());
		Assert.assertEquals("$1, date, yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm:ss", c.get("date"));
		Assert.assertEquals("$2", c.get("level"));
		Assert.assertEquals("$3", c.get("msg"));
	}
	
	@Test
	public void test_4() throws IOException {
		Assert.assertEquals("joerg", Files.getOwner(_logDir).getName());
		
		Set<PosixFilePermission> perms = new HashSet<>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);		

        Assert.assertSame(perms, Files.getPosixFilePermissions(_logDir));
	}
}
