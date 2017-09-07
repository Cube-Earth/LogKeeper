package earth.cube.tools.logkeeper.core.utils;

import org.junit.Assert;
import org.junit.Test;


public class DynamicByteArrayTest {
	
	private DynamicByteArray _arr = new DynamicByteArray(3);
	
	@Test
	public void test_empty_1() {
		Assert.assertArrayEquals(new byte[] { }, _arr.get());
		Assert.assertEquals("", _arr.getAsString());
		Assert.assertEquals("", _arr.getAsString("utf-8"));
	}
	
	@Test
	public void test_1() {
		_arr.add((byte) 'a');
		_arr.add((byte) 'b');
		Assert.assertArrayEquals(new byte[] { 'a', 'b' }, _arr.get());
		Assert.assertEquals("ab", _arr.getAsString());
		Assert.assertEquals("ab", _arr.getAsString("utf-8"));
	}

	@Test
	public void test_2() {
		_arr.add((byte) 'a');
		_arr.add((byte) 'b');
		_arr.add((byte) 'c');
		Assert.assertArrayEquals(new byte[] { 'a', 'b', 'c' }, _arr.get());
		Assert.assertEquals("abc", _arr.getAsString());
		Assert.assertEquals("abc", _arr.getAsString("utf-8"));
	}

	@Test
	public void test_3() {
		_arr.add((byte) 'a');
		_arr.add((byte) 'b');
		_arr.add((byte) 'c');
		_arr.add((byte) 'd');
		Assert.assertArrayEquals(new byte[] { 'a', 'b', 'c', 'd' }, _arr.get());
		Assert.assertEquals("abcd", _arr.getAsString());
		Assert.assertEquals("abcd", _arr.getAsString("utf-8"));
	}

	@Test
	public void test_clear_1() {
		_arr.add((byte) 'a');
		_arr.add((byte) 'b');
		_arr.add((byte) 'c');
		_arr.add((byte) 'd');
		Assert.assertArrayEquals(new byte[] { 'a', 'b', 'c', 'd' }, _arr.get());

		_arr.clear();
		Assert.assertArrayEquals(new byte[] { }, _arr.get());
		Assert.assertEquals("", _arr.getAsString());
		Assert.assertEquals("", _arr.getAsString("utf-8"));
	}
	
}
