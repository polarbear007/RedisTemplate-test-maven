package cn.itcast.utils;

public class RedisKeyUtils {
	public static String getKey(Integer id, Class<? extends Object> clazz) {
		return clazz.getName() + id;
	}
}
