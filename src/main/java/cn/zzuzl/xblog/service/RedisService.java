package cn.zzuzl.xblog.service;

import cn.zzuzl.xblog.common.Common;
import cn.zzuzl.xblog.dao.ArticleDao;
import cn.zzuzl.xblog.dao.UserDao;
import cn.zzuzl.xblog.model.Article;
import cn.zzuzl.xblog.model.Category;
import cn.zzuzl.xblog.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis存储service
 */
@Service
public class RedisService implements InitializingBean {
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private ArticleDao articleDao;
    @Resource
    private CategoryService categoryService;
    @Resource
    private UserDao userDao;
    private final Logger logger = LogManager.getLogger(getClass());
    private long lastTime = System.currentTimeMillis();

    /**
     * 初始化超时时间
     */
    private void initExpire() {
        // 文章失效时间30分钟
        redisTemplate.boundHashOps(Common.KEY_ARTICLEDETAIL).expire(30, TimeUnit.MINUTES);
        // 用户排行超时时间10分钟
        redisTemplate.boundListOps(Common.KEY_USERRANK).expire(10, TimeUnit.MINUTES);
        // 同步分类缓存超时时间10分钟
        redisTemplate.boundListOps(Common.KEY_CATEGORY).expire(10, TimeUnit.MINUTES);
    }

    /**
     * 更新博客浏览量
     *
     * @param id
     */
    public void updateViewCount(int id) {
        Integer oldCount = (Integer) redisTemplate.boundHashOps("viewCount").get(String.valueOf(id));
        if (oldCount == null) {
            oldCount = 0;
        }
        redisTemplate.boundHashOps("viewCount").put(String.valueOf(id), oldCount + 1);

        long gap = (System.currentTimeMillis() - lastTime) / 1000;
        lastTime = System.currentTimeMillis();
        if (gap > 60) {
            backupData();
        }
    }

    /**
     * 备份redis到mysql
     */
    private synchronized void backupData() {
        Map<Object, Object> map = redisTemplate.boundHashOps("viewCount").entries();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            int articleId = Integer.parseInt((String) entry.getKey());
            int count = (Integer) entry.getValue();
            if (count > 0) {
                articleDao.updateViewCount(articleId, count);
                redisTemplate.boundHashOps("viewCount").put(entry.getKey(), 0);
            }
        }
    }

    /**
     * 存储验证码
     *
     * @param email
     * @param hash
     */
    public void addLink(String email, String hash) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("email", email);
        model.put("time", System.currentTimeMillis());
        model.put("hash", hash);

        redisTemplate.boundHashOps(Common.OPERATE_RESET_PWD).put(hash, model);
    }

    /**
     * 临时存储注册未完成的user
     *
     * @param user
     */
    public void addUser(String hash, User user, String password) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("time", System.currentTimeMillis());
        model.put("password", password);
        model.put("user", user);
        model.put("password", password);
        redisTemplate.boundHashOps(Common.REGISTER).put(hash, model);
    }

    /**
     * 返回user
     *
     * @param hash
     * @return
     */
    public Map<String, Object> getUserModel(String hash) {
        return (Map<String, Object>) redisTemplate.boundHashOps(Common.REGISTER).get(hash);
    }

    /**
     * 删除user model
     *
     * @param hash
     */
    public void deleteUserModel(String hash) {
        redisTemplate.boundHashOps(Common.REGISTER).delete(hash);
    }

    /**
     * 根据id获取验证码
     *
     * @param hash
     * @return
     */
    public Map<String, Object> getLink(String hash) {
        return (Map<String, Object>) redisTemplate.boundHashOps(Common.OPERATE_RESET_PWD).get(hash);
    }

    /**
     * 删除验证码
     *
     * @param hash
     */
    public void deleteLink(String hash) {
        redisTemplate.boundHashOps(Common.OPERATE_RESET_PWD).delete(hash);
    }

    /**
     * 获取所有的分类
     *
     * @return
     */
    public List<Category> getAllCategory() {
        List<Category> categories = new ArrayList<Category>();
        BoundListOperations<Object, Object> listOperations = redisTemplate.boundListOps(Common.KEY_CATEGORY);
        if (listOperations == null || listOperations.size() < 1) {
            categories = categoryService.listCategory();
            if (categories != null) {
                for (Category category : categories) {
                    redisTemplate.boundListOps(Common.KEY_CATEGORY).leftPushAll(category);
                }
            }
        } else {
            List<Object> objects = listOperations.range(0, listOperations.size());
            if (objects != null && objects.size() > 0) {
                for (Object o : objects) {
                    if (o instanceof Category) {
                        categories.add((Category) o);
                    }
                }
            }
        }

        return categories;
    }

    /**
     * 获取用户排行
     *
     * @return
     */
    public List<User> getUserRank() {
        List<User> users = new ArrayList<User>();
        BoundListOperations<Object, Object> userRank = redisTemplate.boundListOps(Common.KEY_USERRANK);
        if (userRank == null || userRank.size() < 1) {
            users = userDao.getUserRank(Common.DEFAULT_ITEM_COUNT);
            if (users != null) {
                for (User user : users) {
                    redisTemplate.boundListOps(Common.KEY_USERRANK).leftPushAll(user);
                }
            }
        } else {
            List<Object> objects = userRank.range(0, userRank.size());
            if (objects != null && objects.size() > 0) {
                for (Object o : objects) {
                    if (o instanceof User) {
                        users.add((User) o);
                    }
                }
            }
        }
        return users;
    }

    /**
     * 属性设置完成后初始化缓存超时时间
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        initExpire();
    }

    /**
     * 缓存中查文章，不存在则查库
     *
     * @param id
     * @return
     */
    public Article queryArticleFromCacheById(Integer id) {
        if (id == null) {
            return null;
        }
        Article article = (Article) redisTemplate.boundHashOps(Common.KEY_ARTICLEDETAIL).get(String.valueOf(id));
        if (article == null) {
            article = articleDao.detail(id);
            if (article != null) {
                redisTemplate.boundHashOps(Common.KEY_ARTICLEDETAIL).put(String.valueOf(id), article);
            }
        }
        return article;
    }

    /**
     * 删除缓存
     *
     * @param key
     * @param hashKey
     */
    public Long deleteHashCache(Object key, Object hashKey) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(hashKey)) {
            return null;
        }
        return redisTemplate.opsForHash().delete(String.valueOf(key), String.valueOf(hashKey));
    }
}
