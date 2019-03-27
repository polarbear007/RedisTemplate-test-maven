package cn.itcast.test;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.config.RedisConfig;
import cn.itcast.entity.Student;
import cn.itcast.entity.Teacher;
import cn.itcast.utils.RedisKeyUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {RedisConfig.class})
public class RedisTemplateSerializerTest {
	@Autowired 
	private StringRedisTemplate stringTemplate;
	
	@Autowired
	private RedisTemplate<Object, Object> template;
	
	// 前面我们虽然演示了使用 stringRedisTemplate 对常见类型的各种命令api 
	// 但是我们存的所有数据都是简单的字符串,而真实的开发需求可能还需要存储java 对象
	
	// 以前我们学习 io 流的时候就已经讲过了， java 对象本身是保存在内存中的数据，更加确切地说是保存在 java 虚拟机的工作内存中
	// 也就是说，我们只能在本程序中访问到对象数据。出了这个程序，或者程序关闭以后，你就无法访问这些对象数据了。
	
	// 如果我们想要把对象中的数据保存到其他地方，或者在网络中传输，那么一般有以下几种思路：
	//   1、 使用序列化和反序列化，把对象数据直接转成字节数据。
	//   2、 使用 Json 工具，把java 对象转化成 json 字符串。
	
	//   3、 手动拆分java 对象的数据，把一个java 对象拆解成各种 字符串类型的 key-value 对。（自己瞎想的）
	//       比如我们要把一个 java 对象保存到关系型数据库，我们一般是没有搞什么序列化和反序列化
	//       这是因为我们把java 对象的数据拆解成各种最简单的key-value 对，然后让数据库中的列与java 对象中的成员变量
	//       形成一一对应的映射关系。  反过来，读取数据库的数据以后，又需要把分散的数据封装成一个java 对象。
	//       一般这些工作，我们都是直接让 orm 框架自己来完成的。
	
	// 当我们把 redis 当成一个数据库来使用的时候，我们存储数据的思路其实跟上面差不多：
	//   1、 直接把一个对象通过序列化处理，或者 json 化处理，然后整体保存到redis 数据库中。 
	//        从数据库读取数据后，再通过反序列化或者json解析，把数据还原成对象。 
	//   2、 还是拆解数据，以字符串的形式一条一条地保存到redis数据库中。
	 //       从数据库读取数据后，再把这些数据按照特定的规则封装成java 对象。
	
	
	// 之前我们演示，全部使用的是  stringRedisTemplate , 这个模板对象要求我们输入的 key 和  value 全部都是字符串
	// 使用这个模板对象的好处就在于，我们不需要考虑什么序列化或者 json 处理
	// 这里统一说一下，我们说redis 数据库目前支持五种数据类型，主要指的是 value 支持的类型可以是 string/hash/list/set/zset
	// 而 key 本身的数据类型一般都是字符串！！！！
	
	// 再说回springdata 的模板对象   RedisTemplate ，这个对象其实是带有泛型的： RedisTemplate<K, V>
	// 其中的 K 就对应着数据库里面的 key ， V 就对应着数据库里面的 Value
	// 因为 key 和  value 都是字符串的场景很普遍，所以springdata 直接提供了 stringRedisTemplate 模板，方便我们操作
	
	// 如果我们要使用 RedisTemplate<K, V> 模板的话，因为 K 和 V 本身都可能是对象，而不是简单的字符串
	// 所以我们就必须要考虑对象的序列化和反序列化
	// 当我们使用 RedisTemplate<K, V> 往redis 数据库里面存一条数据的时候，redisTemplate 就会调用我们配置的
	// 序列化器分别对 K 和  V 进行序列化，然后才能把数据保存到 redis 数据库中
	// 当我们使用 RedisTemplate<K, V> 从redis 数据库里面查询数据的时候，redisTemplate 就会调用我们配置的
	// 序列化器分别对 K 和 V 进行反序列化，然后把数据还原成java 对象
	
	// 如果我们不指定 RedisTemplate 的序列化器的话，那么默认会使用 JdkSerializationRedisSerializer
	// 也就是JDK自带的序列化器，这个序列化器要求对象本身一定要实现  Serializable 接口
	
	
	@Test
	public void test() {
		// 创建 Student 对象
		Student stu = new Student(1, "张三", 12, "男");
		// 我们直接以 stu 对象的 id 作为键，然后把整个 stu 对象作为值
		// 保存的时候，key 是Integer 对象， 而 value 是 Student 对象
		// 所以需要分别对这两个对象进行序列化，转成二进制数据
		// 保存成功以后，我们可以使用客户端工具查看一下刚才保存的数据
		//  key: \xAC\xED\x00\x05sr\x00\x11java.lang.Integer\x12\xE2\xA0\xA4\...
		//  value: \xAC\xED\x00\x05sr\x00\x18cn.itcast.entity.Student\xB6c\x089\xC6...
		template.opsForValue().set(stu.getSid(), stu);
	}
	
	@Test
	public void test2() {
		// 使用 redisTemplate 去获取数据，这里我们是通过 
		//  get key      命令去获取刚才保存的数据
		//  redisTemplate 会把我们传入的 key 先进行序列化，转成二进制数据===> 这样子才能跟redis 数据库中保存的key对应上
		//  如果找到对应的数据，redis 就会传回查询到的数据
		//  redisTemplate 会根据我们配置的序列化器再对这些数据进行反序列化，还原成一个 java 对象
		
		// 【注意】 虽然我们在保存的时候，值传入的是一个 Student 类型
		// 但是因为我们配置 RedisTemplate 的时候，泛型指定的是 object ，所以这里返回值是Object 类型
		// 因为我们知道这个 object 其实是一个 Student 类型，所以这里可以直接强转成 Student 类型
		Student stu = (Student) template.opsForValue().get(1);
		System.out.println(stu);
	}
	
	
	
	@Test
	public void test3() {
		// 这里我们再保存一个 Teacher 对象
		Teacher t1 = new Teacher(1, "老王", "男", 25);
		// key 就使用 t1 对象的 id ，value 就使用 t1 对象本身 
		template.opsForValue().set(t1.getTid(), t1);
		
		
		// 保存完以后，我们再次使用 RedisTemplate 对象
		Teacher t2 = (Teacher) template.opsForValue().get(t1.getTid());
		System.out.println(t2);
		// t1 和 t2 是虽然值相同，但是却是两个不同的对象
		System.out.println(t1 == t2);
	}
	
	// 这里可能会有一个问题：
	//   我们保存 student 对象的时候，key 直接用的是 stu 对象的 id ，前面stu 对象的 id 值是1
	//   然后我们又保存 teacher 对象，key 同样是用 t1 对象的 id， 刚好值也是1 
	
	//  那么redis 数据库是否会帮我们区分 stu 对象的 1 和  t1 对象的1 呢？
	@Test
	public void test4() {
		// 这一次我们想要读取之前保存的那么stu 对象
		// 结果发现报了如下的异常：
		// ClassCastException: cn.itcast.entity.Teacher cannot be cast to cn.itcast.entity.Student】
		// 也就是说，因为key 的取值相同，后面插入的 teacher 对象覆盖了前面的插入的 student 对象
		Student stu = (Student) template.opsForValue().get(1);
		System.out.println(stu);
	}
	
	
	// 为了防止出现上面这种问题，我们一般在设置key 的时候，并不会简单地把key 设置成实体类的 id 
	// 因为那样子太容易重复了。 我们可以自己想一个办法，比如说 类名 + id
	// 生成 key ，我们可以写一个专门的 util类 
	@Test
	public void test5() {
		// 保存两个 id 都为  2 的学生对象和 教师对象
		Student stu = new Student(2, "李四", 12, "女");
		template.opsForValue().set(RedisKeyUtils.getKey(stu.getSid(), Student.class), stu);
		
		Teacher teacher = new Teacher(2, "老李", "男", 24);
		template.opsForValue().set(RedisKeyUtils.getKey(teacher.getTid(), Teacher.class), teacher);
		
		// 因为我们有使用工具类生成特定的 key ，那么按理说，虽然这两个对象的 id 都是2
		// 但是生成的 key 应该不是重复的
		Student stu2 = (Student) template.opsForValue()
				       .get(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()));
		System.out.println(stu2);
		
		Teacher teacher2 = (Teacher) template.opsForValue()
						 .get(RedisKeyUtils.getKey(teacher.getTid(), teacher.getClass()));
		System.out.println(teacher2);
	}
	
	// 上面演示了使用 redisTemplate 直接保存java 对象
	// 虽然存取都没有什么问题，但是如果我们使用客户端工具去查看数据库的话，会发现保存的都是二进制数据
	// 但是阅读性几乎为零
	// 所以springdata 还提供了另一种序列化思路： 就是把 key-value 都转成json 字符串
	// 我们知道json 格式来源于 javascript 的对象格式，只要我们的对象符合javabean 规范，那么都是可以很方便地转成json 数据的
	
	// 如果我们想要把对象数据转成 json 字符串的话，那么可以使用springdata 提供的 Jackson2JsonRedisSerializer
	// 一般我们都是在配置类上，直接配置这个序列化器为默认的序列化器
	
	// 我们把序列化器设置好了以后，重新执行一下下面的代码，然后去数据库看一下效果
	@Test
	public void test6() {
		// 保存两个 id 都为  2 的学生对象和 教师对象
		Student stu = new Student(2, "李四", 12, "女");
		template.opsForValue().set(RedisKeyUtils.getKey(stu.getSid(), Student.class), stu);
		
		Teacher teacher = new Teacher(2, "老李", "男", 24);
		template.opsForValue().set(RedisKeyUtils.getKey(teacher.getTid(), Teacher.class), teacher);
		
		// 获取数据
		Student stu2 = (Student)template.opsForValue()
				          .get(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()));
		System.out.println(stu2);
		
		Teacher teacher2 = (Teacher) template.opsForValue()
				 			.get(RedisKeyUtils.getKey(teacher.getTid(), teacher.getClass()));
		System.out.println(teacher2);
	}
	
	
	// 上面我们是把整个json 数据直接保存到一个 字符串类型的value 中
	//  其实那样子的话并不太好，我们之前学 redis 的时候就有讲过，其实对象最好保存在散列类型中
	//  这样子粒度更小。 
	//  比如，我们需要修改一个学生的年龄：
	//       如果整个学生对象保存成一个json 字符串，那么我们需要获取整个学生对象，然后修改完，再把整个对象保存回去 
	//       如果我们把学生对象保存成一个散列的类型，那么每个成员变量都可以独立获取，
	 //         我们需要改年龄，就直接拿年龄这个字段的值，修改以后，再保存回去就可以了。
	
	//  当然，因为redis 的散列类型数据，value 只支持字符串。所以如果我们的某个成员变量，还是一个包装类型，
	//   那么我们同样需要把这个成员变量转成 json 字符串，进行保存。不能无限细化下去。
	@Test
	public void test7() throws Exception {
		Student stu = new Student(3, "王五", 15, "男");
		// 这里我们最好不要一个一个成员变量去设置
		// 而是直接把javabean 转成一个 map 集合，然后批量存储
		//  一来比较方便，直接用一条命令就可以完成了，不来连续发送好几条命令
		BeanMap map = BeanMap.create(stu);
		
		template.opsForHash().putAll(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()), map);
		
		// 然后我们试一下，只获取这个学生的对象的年龄
		Object age = template.opsForHash().get(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()), "age");
		System.out.println(age.getClass());
		System.out.println(age);
		
		// 我们再试一下，只修改一下这个学生的年龄
		template.opsForHash().put(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()), "age", 22);
		
		// 我们再试一下，获取整合学生对象，会不会受影响
		Map<Object, Object> entries = template.opsForHash().entries(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()));
		Student stu2 = new Student();
		// 注意，一定要使用apache 的Beanutils ，spring 的 beanUtils 不支持从map 中取值
		// 而且要注意这两个 bean 参数的顺序是不一样的
		
		// apache: public static void copyProperties(final Object dest, final Object orig)
		// spring: public static void copyProperties(Object source, Object target)
		BeanUtils.copyProperties(stu2, entries);
		System.out.println(stu2);
	}
	
	// StringRedisTemplate 默认key 和 value 都是使用  StringRedisSerializer
	// 这个序列化工具其实就干一件事儿： 把你输入的字符串值改成 指定的 编码。（默认是 utf-8）
	
	// StringRedisTemplate 要求你输入的 key 和   value 都是字符串，所以如果你想保存对象数据的话
	// 最好不要使用 StringRedisTemplate ，因为你在保存之前得自己先把值改成字符串类型
	// 比如说，你可以自己调用  json 工具类，把对象转成 json 
	// 比如说，你可以先自己调用 jdk 序列化工具，把对象转成二进制数据，然后再转成字符串。。。。
	// 这里就不演示了，因为没有人会给自己找这样的麻烦 
	
	
	// 我们来看一下，如果一个类是个pojo的包装类， redisTemplate 能不能正确保存和解析
	// 把这个对象保存到 string 类型中
	@Test
	public void test8() {
		Student stu = new Student(5, "小明", 14, "男");
		stu.setTeacher(new Teacher(1, "rose", "female", 25));
		
		template.opsForValue().set(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()), stu);
		
		// 再次获取这个 对象
		Object stu2 = template.opsForValue().get(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()));
		System.out.println(stu2);
	}
	
	// 把这个包装类对象保存到hash 类型中
	@Test
	public void test9() throws Exception {
		Student stu = new Student(6, "小花", 14, "女");
		stu.setTeacher(new Teacher(1, "rose", "female", 25));
		
		// 把对象保存到 hash 类型数据中去
		template.opsForHash().putAll(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()), BeanMap.create(stu));
		
		// 再次获取数据
		Map<Object, Object> entries = template.opsForHash().entries(RedisKeyUtils.getKey(stu.getSid(), stu.getClass()));
		
		Student stu2 = new Student();
		// 使用 beanutils 工具把值复制到 javabean中
		BeanUtils.copyProperties(stu2, entries);
		System.out.println(stu2);
	}
	
}
