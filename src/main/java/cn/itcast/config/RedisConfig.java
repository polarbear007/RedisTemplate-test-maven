package cn.itcast.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	// 如果我们直接 new JedisConnectionFactory() 的话，默认服务器的 ip 是localhost , 端口6379
	// 你可以简单地认为这是一个连接池对象（JedisPool）
	@Bean
    public JedisConnectionFactory connectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("192.168.48.129", 6379);
		return new JedisConnectionFactory(config);
	}
	
	// 配置 RedisTemplate 
	// 你可以简单地认为这是一个连接对象或者说会话对象（Jedis）
	@Bean
	public RedisTemplate<Object, Object> redisTemplate(@Autowired JedisConnectionFactory connectionFactory){
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		// 我们需要手动开启对事务的支持
		//template.setEnableTransactionSupport(true);
		
		// 设置json 的序列化器， 因为我们懒得为每个实体类都创建一个对应的 redisTemplate 
		// 所以我们这里的直接使用 Object 泛型
		// 要使用这个序列化器，得另外导入 jackson-core 和  jackson-databind 依赖，否则会报错
		
		// 注意，我们只需要配置默认的序列化器为GenericJackson2JsonRedisSerializer，那么不管我们保存什么类型的数据
		// key - value 都会转成 json ===> 如果你不想全部都转成 json 的话，那么你可能得自己分别设置
		
		// 注意： 请不要使用 Jackson2JsonRedisSerializer ，使用这个序列化器的话，解析返回的是一个 LinkedHashMap 
		//       返回的数据我们还得把Map 转成 object ,比较麻烦 
		
		// GenericJackson2JsonRedisSerializer 为什么就可以自己转换呢？
		//        如果我们去客户端看保存的数据，会发现使用这个序列化器保存数据，默认会多一个key-value
		// 		  "@class": "cn.itcast.entity.Student"
		
		// Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
		template.setDefaultSerializer(serializer);
		// 一般来说，我们的 key 都用字符串就好了，不需要再转成 json
		template.setKeySerializer(new StringRedisSerializer());
		return template;
	}
	
	// 因为redis 的很多操作都是关于 String 类型的操作，所以我们可以配置一个专门的 模板对象
	@Bean
	public StringRedisTemplate stringRedisTemplate(@Autowired JedisConnectionFactory connectionFactory) {
		StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
		// 我们需要手动开启对事务的支持
		// template.setEnableTransactionSupport(true);
		return template;
	}
}	
