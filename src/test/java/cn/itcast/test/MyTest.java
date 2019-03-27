package cn.itcast.test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;

import cn.itcast.entity.Student;

public class MyTest {
	@Test
	public void test() {
		Student stu = new Student(1, "eric", 12, "male");
		BeanMap map = BeanMap.create(stu);
		System.out.println(map);
	}
	
	@Test
	public void test2() throws IllegalAccessException, InvocationTargetException {
		HashMap<String, Object> map = new HashMap<>();
		map.put("sid", "1");
		map.put("sname", "eric");
		map.put("age", "12");
		map.put("gender", "male");
		
		Student stu = new Student();
		// 注意，一定要使用apache 的Beanutils ，spring 的 beanUtils 不支持从map 中取值
		// 而且要注意这两个 bean 参数的顺序是不一样的
		
		// apache: public static void copyProperties(final Object dest, final Object orig)
		// spring: public static void copyProperties(Object source, Object target)
		BeanUtils.copyProperties(stu, map);
		System.out.println(stu);
	}
	
	@Test
	public void test3() throws IllegalAccessException, InvocationTargetException {
		TestObject to = new TestObject();
		to.setArr(new Integer[]{1, 2, 3});
		to.setId("012");
		to.setList(Arrays.asList("1", "2" , "3"));
		HashMap<String, String> map = new HashMap<>();
		map.put("hello", "hello value");
		map.put("world", "world value");
		to.setMap(map);
		
		// beanMap 是spring 的一个工具类
		BeanMap beanMap = BeanMap.create(to);
		System.out.println(beanMap);
		
		// 再把这个 beanMap 转成一个 javaBean
		TestObject to2 = new TestObject();
		BeanUtils.copyProperties(to2, beanMap);
		System.out.println(to2);
	}
	
	// BeanUtilsHashMapper 也可以实现 map 和 javaBean 的相互转换
	// 但是如果javabean 中有list 或者 array ，只会拿其中的第一个元素
	@Test
	public void test4() throws IllegalAccessException, InvocationTargetException {
		TestObject to = new TestObject();
		to.setArr(new Integer[]{1, 2, 3});
		to.setId("012");
		to.setList(Arrays.asList("1", "2" , "3"));
		HashMap<String, String> map = new HashMap<>();
		map.put("hello", "hello value");
		map.put("world", "world value");
		to.setMap(map);
		
		BeanUtilsHashMapper<TestObject> mapper = new BeanUtilsHashMapper<>(TestObject.class);
		Map<String, String> hashMap = mapper.toHash(to);
		System.out.println(hashMap);
	}
	
	// 这个 ObjectHashMapper 是好用的
	// 但是Map 的泛型必须都是  byte[] , bean 转 map 还好
	// 如果是我们使用  template.opsForHash().entries()   返回的那个map，无法直接使用这个来转换
	@Test
	public void test5() {
		TestObject to = new TestObject();
		to.setArr(new Integer[]{1, 2, 3});
		to.setId("012");
		to.setList(Arrays.asList("1", "2" , "3"));
		HashMap<String, String> map = new HashMap<>();
		map.put("hello", "hello value");
		map.put("world", "world value");
		to.setMap(map);
		
		ObjectHashMapper mapper = new ObjectHashMapper();
		Map<byte[], byte[]> map2 = mapper.toHash(to);
		System.out.println(map2);
		
		Object obj = mapper.fromHash(map2);
		System.out.println(obj);
	}
	
	@Test
	public void test6() {
		int i = Integer.parseInt(null);
	}
}
