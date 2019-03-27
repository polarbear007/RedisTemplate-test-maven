package cn.itcast.test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.config.RedisConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {RedisConfig.class})
public class RedisTemplateTest {
	@Autowired 
	private StringRedisTemplate stringTemplate;
	
	@Autowired
	private RedisTemplate<Object, Object> template;
	
	// 先看一下 这两个对象有没有创建成功
	@Test
	public void test() {
		System.out.println(stringTemplate);
		System.out.println(template);
	}
	
	// 使用 stringRedisTemplate 执行一些通用的命令
	@Test
	public void test2() {
		// keys pattern 命令，查看当前数据库中有多少键
		Set<String> keys = stringTemplate.keys("*");
		System.out.println(keys);
		
		// expire key   给某个键设置生命周期, 如果成功返回true ,失败返回 false
		// 但是似乎没有那个 ttl  key 命令
		Boolean expire = stringTemplate.expire("msg", 60, TimeUnit.SECONDS);
		System.out.println("expire:" + expire);
		
		// persist key   使某个键永久存在 
		Boolean persist = stringTemplate.persist("msg");
		System.out.println("persist:" + persist);
		
		// del key 删除某个键
		Boolean delete = stringTemplate.delete("msg3");
		System.out.println("delete:" + delete);
		
		// del key1 key2 key3...   删除多个键
		Long deletes = stringTemplate.delete(Arrays.asList("key1", "key2","key3"));
		System.out.println("deletes: " + deletes);
	}
	
	// redis 数据库支持以下几种数据类型的键值存储：
	//    字符串类型（String）
	//    散列类型(Hash)
	//    列表类型（List）
	//    集合类型（Set）
	//    有序集合类型（Zset）
	
	// springdata 提供了对应的API进行相应的操作
	//     	template.opsForValue()    ---->   String
	//		stringTemplate.opsForHash()---->   Hash
	// 		stringTemplate.opsForList()---->   List
	//		stringTemplate.opsForSet()---->   Set
	// 		stringTemplate.opsForZSet()---->   Zset
	
	// 下面我们就先看看最基础的 String 类型对应的 api 吧 
	@Test
	public void test3() {
		// set key value 命令
		stringTemplate.opsForValue().set("key1", "value1");
		
		// mset key1 value1 key2 value2....
		// 这里需要传入一个 map 集合
		HashMap<String, String> map = new HashMap<>();
		map.put("key2", "value2");
		map.put("key3", "value3");
		map.put("key4", "value4");
		stringTemplate.opsForValue().multiSet(map);
		
		// get key 
		String value1 = stringTemplate.opsForValue().get("key1");
		System.out.println(value1);
		
		// mget key1 key2 key3...
		List<String> multiGet = stringTemplate.opsForValue().multiGet(Arrays.asList("key2", "key3", "key4"));
		if(multiGet != null && multiGet.size() > 0) {
			for (String value : multiGet) {
				System.out.println(value);
			}
		}
		
		// 我们也可以在保存的时候，直接设置 key 的生命周期
		// set key value ex timeout
		stringTemplate.opsForValue().set("key5", "value5", Duration.ofSeconds(60));
		// 因为 springdata 没有对就 ttl 命令，所以查看效果可能不是很方便，我们可以通过客户端工具查看验证
		
		// 注意： 默认的set 方法是不会返回原来的值的， 如果我们想要拿到原来的key 值
		//      那么应该使用这个方法
		String oldValue = stringTemplate.opsForValue().getAndSet("key1", "newValue");
		System.out.println(oldValue);
		
		// append key value    // 拼接字符串,返回新字符串的长度
		//                        如果原来的那个 key 不存在，那么就会直接创建一个新的key
		Integer append = stringTemplate.opsForValue().append("key20", " hello world");
		System.out.println(append);
		
		// incr key      // 给指定的 Key 增加1 ， 返回增加后结果
		//                  如果指定的 key 对应的值不是数字字符串或者不是整数，会报错
//		Long increment = stringTemplate.opsForValue().increment("count");
//		System.out.println(increment);
		
		// incr key  increment(Long)     如果指定的 key 对应的值不是数字字符串或者不是整数，会报错
//		Long increment2 = stringTemplate.opsForValue().increment("count", 10L);
//		System.out.println(increment2);
		
		// incr key increment(Double)    如果指定的 key 对应的值不是数字字符串，会报错
		Double increment3 = stringTemplate.opsForValue().increment("count", 20.5);
		System.out.println(increment3);
		
		// decr key   
		Long decrement = stringTemplate.opsForValue().decrement("count2");
		System.out.println(decrement);
		
		// decr key decrement   
		// 注意，decrement()  方法并不支持浮点数，但是我们可以使用 increment() 实现，只要加上一个负数即可
		Long decrement2 = stringTemplate.opsForValue().decrement("count2", 100L);
		System.out.println(decrement2);
	}
		
	
	// 下面我们看看 StringRedisTemplate 关于 Hash 的常用api 吧
	@Test
	public void test4() {
		// hset key field  value   
		stringTemplate.opsForHash().put("hashkey:001", "field11", "value11");
		stringTemplate.opsForHash().put("hashkey:001", "field12", "value12");
		stringTemplate.opsForHash().put("hashkey:001", "field13", "value13");
		
		// hmset key field1 value1 field2 value2....
		HashMap<String, String> map = new HashMap<>();
		map.put("field21", "value21");
		map.put("field22", "value22");
		map.put("field23", "value23");
		stringTemplate.opsForHash().putAll("hashkey:002", map);
		
		// hget key field
		Object value1 = stringTemplate.opsForHash().get("hashkey:001", "field11");
		Object value2 = stringTemplate.opsForHash().get("hashkey:001", "field12");
		Object value3 = stringTemplate.opsForHash().get("hashkey:001", "field13");
		System.out.println(value1 + "---" + value2 + "---" + value3);
		
		// hmget key field1 field2...
		List<Object> multiGet = stringTemplate.opsForHash().multiGet("hashkey:002", Arrays.asList("field21", "field22","field23"));
		if(multiGet != null && !multiGet.isEmpty()) {
			for (Object object : multiGet) {
				System.out.println(object);
			}
		}
		
		// hgetAll key    一次性获取全部的字段名 + 字段值
		Map<Object, Object> entries = stringTemplate.opsForHash().entries("hashKey:002");
		// 如果我们需要把 entries 转成一个javabean ，可以使用apache 的beanutils.copyproperties()
		// spring 本身的 beanutils 不支持从 map 拷贝值
		System.out.println(entries);
		
		// 获取一个散列的所有字段名。     
		// hkeys key      
		Set<Object> keys = stringTemplate.opsForHash().keys("hashkey:002");
		System.out.println(keys);
		
		// 判断某个散列数据是否有某个字段
		// hexists key field
		Boolean hasKey = stringTemplate.opsForHash().hasKey("hashkey:002", "field1");
		System.out.println(hasKey);
		
		// 获取所有的值，不要字段
		// hvals key 
		List<Object> values = stringTemplate.opsForHash().values("hashkey:002");
		System.out.println(values);
		
		// hlen key   获取字段的数量
		Long size = stringTemplate.opsForHash().size("hashkey:002");
		System.out.println(size);
		
		// hdel key field1 field2...
		Long delete = stringTemplate.opsForHash().delete("hashkey:002", "field1", "field2", "field3");
		System.out.println(delete);
	}
	
	// 接下来我们看看 stringRedisTemplate  关于 list 类型的常用api 吧 
	// 因为 list 集合是允许重复的，所以插入、删除，可能会造成一些问题。 如果下面的方法报错，就注释掉一些方法，或者一个一个地去试
	@Test
	public void test5() {
		// lpush key value
		 stringTemplate.opsForList().leftPush("listKey:001", "001");
		
		// lpush key value1 value2 value3
		 stringTemplate.opsForList().leftPushAll("listKey:002", "002", "003", "004");
		
		// linsert key Before firstFindValue insertValue   // 相当于插入值，具体流程可以看redis笔记
		 Long leftPush = stringTemplate.opsForList().leftPush("listKey:002", "002", "leftInsertValue");
		 System.out.println(leftPush);
		
		// 查看一下刚才插入的效果
		// lrange key start end     // 支持负数索引 ，包左又包右
		List<String> range = stringTemplate.opsForList().range("listKey:002", 0, -1);
		System.out.println(range);
		
		// 获取指定索引位置的元素
		// lindex key index
		String value = stringTemplate.opsForList().index("listKey:002", 2);
		System.out.println(value);
		
		// 这条命令其实挺复杂的，根据 count 值的不同，会产生不同的效果
		// 如果 count 值是正数，那么从左开始搜索， 删除 count 个  值等于 value 的元素
		// 如果 count 值等于0， 那么删除列表中所有值等于 value 的元素
		// 如果 count 值小于0， 那么会从右开始搜索，删除count 个值等于 value 的元素
		
		// 返回实际删除的元素数量
		//  lrem key count value 
		Long remove = stringTemplate.opsForList().remove("listKey:002", 2, "002");
		System.out.println(remove);
		
		// 设置指定索引位置的的值
		// lset key index value
		stringTemplate.opsForList().set("listKey:002", 1, "setNewValue");
		// 查看一下修改的效果
		List<String> range2 = stringTemplate.opsForList().range("listKey:002", 0, -1);
		System.out.println(range2);
		
		// 从左侧弹出一个元素
		// lpop key 
		String leftPop = stringTemplate.opsForList().leftPop("listKey:002");
		System.out.println(leftPop);
		
		// 我们还可以阻塞式地弹出一个元素, 如果redis 中没有这个队列，那么就会等待指定的时长
		// 如果时间到了，还是没有元素可以弹出，那么就会返回 null
		// Blpop key timeout
		
		// 【注意】 stringTemplate 并没有提供   Blpop key1 key2 ..  timeout 
		//       如果我们想要实现优先级队列的效果，可能需要考虑其他方式了
		String leftPop2 = stringTemplate.opsForList().leftPop("listKey:002", 10, TimeUnit.SECONDS);
		System.out.println(leftPop2);
		
		// 从右侧插入、从右侧弹出的api 这里就不演示了
		// 补充一下，查看列表的长度命令
		// llen key 
		Long size = stringTemplate.opsForList().size("listKey:002");
		System.out.println(size);
	}
	
	
	// 演示一下 stringRedisTemplate  关于 set 集合类型的相关 api
	@Test
	public  void test6() {
		// sadd key value1 value2 value3...
		Long add = stringTemplate.opsForSet().add("setKey:001", "001", "002", "003", "004");
		System.out.println(add);
		
		// srem key value1 value2 value3...
		Long remove = stringTemplate.opsForSet().remove("setKey:001", "004");
		System.out.println(remove);
		
		// sisMember key value 
		Boolean isMember = stringTemplate.opsForSet().isMember("setKey:001", "001");
		System.out.println(isMember);
		
		// smembers  key    
		Set<String> members = stringTemplate.opsForSet().members("setKey:001");
		System.out.println(members);
		
		// SrandomMember key   随机从集合中返回一个元素
		String randomMember = stringTemplate.opsForSet().randomMember("setKey:001");
		System.out.println(randomMember);
		
		// SrandomMember key count    
		// 这个命令比较复杂，要看 count 的取值和集合本身的长度
		//  如果count 值大于0， 而且count 值大于集合长度，那么返回集合中所有的元素
		//  如果 count 值大于0， 而且count 值小于集合长度，那么返回count 个元素（不重复）
		//  如果 count 值等于0，那么不返回任何元素
		//  如果count 值小于0， 那么返回 |count| 个元素（元素可能会重复）
		
		// 这个方法中的 count 不可以是负数，但是返回的结果可能是重复的
		List<String> randomMembers = stringTemplate.opsForSet().randomMembers("setKey:001", 10);
		System.out.println(randomMembers);
		
		// 这个方法中的 count 同样不可以是负数，返回的结果不会是重复的，而且如果count 大于集合长度，只返回集合本身
		Set<String> distinctRandomMembers = stringTemplate.opsForSet().distinctRandomMembers("setKey:001", 10);
		System.out.println(distinctRandomMembers);
		
		// Scard key   获取集合中元素的个数 
		Long size = stringTemplate.opsForSet().size("setKey:001");
		System.out.println(size);
		
		// 为了演示集合的运算，我们再创建两个 集合
		Long add2 = stringTemplate.opsForSet().add("setKey:002", "001", "010", "022", "003", "005");
		System.out.println(add2);
		
		Long add3 = stringTemplate.opsForSet().add("setKey:003", "001", "042", "032", "003", "007");
		System.out.println(add3);
		
		// 求 setKey:001 中独有，而 setKey:002 没有的部分
		// sdiff  key1 key2 key3....
		Set<String> difference = stringTemplate.opsForSet().difference("setKey:001", "setKey:002");
		System.out.println(difference);
	
		// 求 setKey:003 中独有，而 setKey:001 和 setKey:002 中没有部分
		Set<String> difference2 = stringTemplate.opsForSet().difference("setKey:003", Arrays.asList("setKey:002", "setKey:001"));
		System.out.println(difference2);
		
		// 求 setKey:002 中独有，而 setKey:001 和 setKey:003 中没有的部分，并把结果保存到 setKey:store 中
		Long store = stringTemplate.opsForSet().differenceAndStore("setKey:002", 
				                                      Arrays.asList("setKey:001", "setKey:003"), 
				                                      "setKey:store");
		System.out.println(store);
		
		// 求 setKey:001 和  setKey:002 的交集
		// sinter key1 key2 key3....
		Set<String> intersect = stringTemplate.opsForSet().intersect("setKey:001", "setKey:002");
		System.out.println(intersect);
		
		// 求 setKey:001 和  setKey:002 ， setKey:003 三个集合的交集
		Set<String> intersect2 = stringTemplate.opsForSet().intersect("setKey:001", Arrays.asList("setKey:002", "setKey:003"));
		System.out.println(intersect2);
		
		// 求 setKey:001 和  setKey:002 ， setKey:003 三个集合的交集, 并保存到 setKey:store2
		Long intersectAndStore = stringTemplate.opsForSet().intersectAndStore("setKey:001", 
				                                      Arrays.asList("setKey:002", "setKey:003"), 
				                                      "setKey:store2");
		System.out.println(intersectAndStore);
		
		// 求 setKey:001 与 setKey:002 的并集
		// sunion key1 key2 key3....
		Set<String> union = stringTemplate.opsForSet().union("setKey:001", "setKey:002");
		System.out.println(union);
		
		// 求三个集合的并集
		Set<String> union2 = stringTemplate.opsForSet().union("setKey:001", Arrays.asList("setKey:003", "setKey:002"));
		System.out.println(union2);
		
		// 求三个集合的并集，并保存到指定的 key 中，准备继续运算
		Long unionAndStore = stringTemplate.opsForSet().unionAndStore("setKey:001", 
				                                                      Arrays.asList("setKey:002", "setKey:003"), 
				                                                      "setKey:store3");
		System.out.println(unionAndStore);
	}
	
	// 演示一下 stringRedisTemplate  关于 Zset 有序集合类型的相关 api
	@Test
	public  void test7() {
		// zadd key score value
		// 顺序跟原生的命令可能有点不一样
		Boolean add = stringTemplate.opsForZSet().add("zsetKey:001", "eric", 88);
		System.out.println(add);
		
		// 为了后面的演示，我们这里多加一些数据
		stringTemplate.opsForZSet().add("zsetKey:001", "rose", 98);
		stringTemplate.opsForZSet().add("zsetKey:001", "jack", 84);
		stringTemplate.opsForZSet().add("zsetKey:001", "tom", 75);
		stringTemplate.opsForZSet().add("zsetKey:001", "jerry", 69);
		stringTemplate.opsForZSet().add("zsetKey:001", "mary", 92);
		
		// 删除一个或者多个元素
		// zrem key value1 value2
		Long remove = stringTemplate.opsForZSet().remove("zsetKey:001", "mary");
		System.out.println(remove);
		
		Long remove2 = stringTemplate.opsForZSet().remove("zsetKey:001", "mary", "tina");
		System.out.println(remove2);
		
		// 返回集合的长度
		// zcard key 
		Long zCard = stringTemplate.opsForZSet().zCard("zsetKey:001");
		System.out.println(zCard);
		
		// 不知道这个方法跟上面的方法有什么区别 
		Long size = stringTemplate.opsForZSet().size("zsetKey:001");
		System.out.println(size);
		
		// 返回某个元素对应的分数
		// zscore key value  
		Double score = stringTemplate.opsForZSet().score("zsetKey:001", "eric");
		System.out.println(score);
		
		// 返回某个元素对应的排名
		// zrank key value
		Long rank = stringTemplate.opsForZSet().rank("zsetKey:001", "eric");
		System.out.println(rank);
		
		// 根据分数从小到大排序的排名范围，来获取元素，支持负数，包左包右
		// 如果是 [0, -1]    那么就是返回所有的元素
		// zrange key start end 
		Set<String> range = stringTemplate.opsForZSet().range("zsetKey:001", 0, -1);
		System.out.println(range);
		
		// 根据分数从小到大排序的排名范围来获取元素，支持负数，包左包右，结果带有分数
		// zrange key start end  withscores
		Set<TypedTuple<String>> rangeWithScores = stringTemplate.opsForZSet().rangeWithScores("zsetKey:001", 0, -1);
		System.out.println(rangeWithScores);
		
		// 根据分数从大到小排序的排名范围来获取元素，支持负数，包左包右，结果不带分数（带分数的不演示了）
		// zRevRange key start [withscores]
		Set<String> reverseRange = stringTemplate.opsForZSet().reverseRange("zsetKey:001", 0, -1);
		System.out.println(reverseRange);
		
		// 根据分数的范围来获取元素, 不带分数
		// zrangeByScore key min max [withscores]
		Set<String> rangeByScore = stringTemplate.opsForZSet().rangeByScore("zsetKey:001", 60.0, 85.0);
		System.out.println(rangeByScore);
		
		// 带分数
		Set<TypedTuple<String>> rangeByScoreWithScores = stringTemplate.opsForZSet().rangeByScoreWithScores("zsetKey:001", 60.5, 85.0);
		if(rangeByScoreWithScores != null && rangeByScoreWithScores.size() > 0) {
			for (TypedTuple<String> typedTuple : rangeByScoreWithScores) {
				System.out.println(typedTuple.getValue() + ": " + typedTuple.getScore());
			}
		}
		
		// 根据分数范围来获取元素，从高到低排序
		// zRevRangeByScore  key max min [withscores] [limit offset count]
		 Set<String> reverseRangeByScore = stringTemplate.opsForZSet().reverseRangeByScore("zsetKey:001", 60.0, 85.0);
		 System.out.println(reverseRangeByScore);
		 
		 // 根据分数范围来获取元素，从高到低排序，还可以携带分页参数
		 Set<String> reverseRangeByScore2 = stringTemplate.opsForZSet().reverseRangeByScore("zsetKey:001", 0.0, 200.0, 0, 3);
		 System.out.println(reverseRangeByScore2);
		 
		 // 统计某个分数段中元素的数量
		 // zcount key min max
		 Long count = stringTemplate.opsForZSet().count("zsetKey:001", 60.0, 85.0);
		 System.out.println(count);
		 
		 // 根据分数段来删除元素
		 // zRemRangeByScore  key min max
		 Long removeRangeByScore = stringTemplate.opsForZSet().removeRangeByScore("zsetKey:001", 0.0, 50.0);
		 System.out.println(removeRangeByScore);
		 
		 // 根据排名的范围来删除元素
		 // zRemRangeByRank key start end
		 Long removeRange = stringTemplate.opsForZSet().removeRange("zsetKey:001", 0, 2);
		 System.out.println(removeRange);
		 
		 // 有序集合其实还包含了并集交集操作，但是这里就不再演示了，太烦了
	}
	
}
