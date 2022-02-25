package cn.edu.sysu.workflow.activiti.util;

import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis连接池类
 * @author: Gordan Lin
 * @create: 2019/10/9
 **/
public class JedisPoolUtil {

    private JedisPoolUtil() {}

    private static volatile JedisPool jedisPool = null;

    @Value("${jedis.redis.host}")
    public void setRedisHost(String redisHost) {
        REDIS_HOST = redisHost;
    }

    //服务器ip
    private static String REDIS_HOST;

    private static final int REDIS_PORT = 6379;

    public static JedisPool getJedisPoolInstance() {
        if (jedisPool == null) {
            synchronized (JedisPoolUtil.class) {
                if (jedisPool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxIdle(32);
                    poolConfig.setMaxTotal(1000);
                    poolConfig.setMaxWaitMillis(1000*10);
                    jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
                }
            }
        }
        return jedisPool;
    }

}