package cn.itcast.test;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.config.RedisConfig;
import cn.itcast.utils.RedisKeyUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {RedisConfig.class})
public class RedisTemplateTransactionTest {
	@Autowired
	private RedisTemplate<Object, Object> template;
	
	// redisTemplate 的事务操作
	@Test
	public void test() {
		List<Object> results = template.execute(new SessionCallback<List<Object>>() {
			@Override
			public  List<Object> execute(RedisOperations operations) throws DataAccessException {
				// 开启事务
				operations.multi();
				// 开始进行增删改操作，事务中一般是不会进行查询操作，因为没有什么意义 
				// redis 的事务是把命令先保存在一个队列中，然后当我们发送 exec 命令，一起发送给redis 服务端
				// 服务端统一执行，统一返回结果
				for (int i = 0; i < 10000; i++) {
					operations.opsForList().leftPush("txKey", String.valueOf(i));
				}
				
				// 最后发送 exec 命令
				return operations.exec();
			}

		});
		
		System.out.println(results.size());
	}
	
	// 上面的事务操作，其实跟批量操作没有太大的区别
	// 实际上事务经常是跟watch 一起使用， watch 命令可以监视某个key 或者多个 key 
	// 当这个key 的值发生改变的时候，就会自动取消事务操作。 从而避免丢失更新问题。
	
	// 注意： 本身 incr key / decr key 这些命令都是原子性操作，不需要在事务中处理的
	//       这里只是为了演示方便，直接使用 set 命令
	@Test
	public void test2() {
		while(true) {
			List<Object> results = template.execute(new SessionCallback<List<Object>>() {
				@Override
				public List<Object> execute(RedisOperations operations) throws DataAccessException {
					// 首先，我们应该先监视某个 key 的值
					operations.watch("txCount");
					
					// 然后，我们可以查看一下这个txCount  的值，根据这个值来决定进行什么操作
					String txCount = (String)operations.opsForValue().get("txCount");
					Integer count = null;
					try {
						count = Integer.parseInt(txCount);
						// 检查一下库存量，如果小于或者等于零，那么也不需要进行 下面的操作了
						if(count <= 0) {
							// 取消监视
							operations.unwatch();
							// 手动抛异常，如果异常了，那么循环会中止，事务也不会执行
							throw new RuntimeException("库存量不足！无法创建订单");
						}
					} catch (NumberFormatException e) {
						operations.opsForValue().set("txCount", "0");
						throw new RuntimeException("无法读取库量，已经将库存量改为0");
					}
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// 开启事务，如果被监视的key 被前面的代码修改了，或者被其他的客户端修改了，那么这里的事务都不会执行
					operations.multi();
					// 如果这个key 是数字字符串，而已可以转换成整数的话，那么我们就执行自减操作
					if(count > 0) {
						operations.opsForValue().set("txCount", String.valueOf(count - 1));
					}
					return operations.exec();
				}
			});
			
			// 如果事务 exec() 方法返回 null ，那么就说明事务被取消了，本次自减操作没有成功
			// 我们重复上一次操作
			
			// 如果事务 exec() 方法返回的值不是null ，那么说明事务执行成功，我们退出循环
			if(results != null && results.size() > 0) {
				System.out.println("成功下单");
				break;
			}
			System.out.println("下单失败，重新再来！");
		}
	}
}
