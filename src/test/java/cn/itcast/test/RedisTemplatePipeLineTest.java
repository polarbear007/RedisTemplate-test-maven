package cn.itcast.test;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.config.RedisConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {RedisConfig.class})
public class RedisTemplatePipeLineTest {
	@Autowired
	private RedisTemplate<Object, Object> template;
	@Autowired
	private StringRedisTemplate stringTemplate;
	
	// 如果有多个命令之间，没有相互依赖的关系。
	// 比如上一条的命令的结果，不影响下一条命令是否执行，或者如何执行。
	// 那么我们就可以使用管道来进行批量操作；
	// 如果上条命令的结果，会直接或间接影响下一条命令是否执行，如何执行，那么我们一定要使用
	// 事务 + watch 来处理。
	
	// 就形式而言，管道操作，几乎就跟事务操作没有什么区别
	@Test
	public void test() {
		List<Object> results = template.executePipelined(new RedisCallback<List<Object>>() {

			@Override
			public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
				// 开启管道
				connection.openPipeline();
				// 执行操作
				// 这个 connection 就是原始的连接对象，所有的参数都必须是字节数组类型
				for (int i = 0; i < 1000; i++) {
					connection.lPush("pipeKey".getBytes(), String.valueOf(i).getBytes());
				}
				
				// 【注意】 一定不要执行下面这句关闭管道，或者返回这个语句的返回值， 不知道为什么
				// List<Object> closePipeline = connection.closePipeline();
				
				// 【注意】 这里直接返回 null 就可以了，虽然这里返回 null ，但是外面的 template 是可以拿到具体的结果的
				return null;
			}
		});
		
		for (Object object : results) {
			System.out.println(object);
		}
	}
	
	@Test
	public void test2() {
		List<Object> results = stringTemplate.executePipelined(new SessionCallback<List<Object>>() {

			@Override
			public List<Object> execute(RedisOperations operations) throws DataAccessException {
				for (int i = 0; i < 1000; i++) {
					operations.opsForList().leftPush("pipeKey2", String.valueOf(i));
				}
				return null;
			}
		});
		
		System.out.println(results.size());
	}
}
