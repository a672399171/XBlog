package cn.zzuzl.xblog.service;

import cn.zzuzl.xblog.dao.UserDao;
import cn.zzuzl.xblog.exception.ErrorCode;
import cn.zzuzl.xblog.exception.ServiceException;
import cn.zzuzl.xblog.model.vo.Result;
import cn.zzuzl.xblog.model.Attention;
import cn.zzuzl.xblog.model.User;
import cn.zzuzl.xblog.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户相关service
 */
@Service
public class UserService {
    @Resource
    private UserDao userDao;
    @Resource
    private RedisService redisService;
    private Logger logger = LogManager.getLogger(getClass());

    /**
     * 使用email登录
     *
     * @param email
     * @param password
     * @return
     */
    public User login(String email, String password) {
        User user = userDao.searchUserByEmail(email);
        if (user == null) {
            return null;
        } else {
            String hash = Utils.MD5(password, user.getSalt());
            if (hash.equals(user.getHash())) {
                user.setSalt(null);
                user.setHash(null);
                return user;
            } else {
                return null;
            }
        }
    }

    /**
     * 根据email查询用户
     *
     * @param email
     * @return
     */
    public User searchUserByEmail(String email) {
        User user = userDao.searchUserByEmail(email);
        if (user != null) {
            user.setSalt(null);
            user.setHash(null);
        }
        return user;
    }

    /**
     * 根据url查找用户，找不到返回null
     *
     * @param url
     * @return
     */
    public User searchUserByUrl(String url) {
        return userDao.searchUserByUrl(url);
    }

    /**
     * 用户注册，主键自动存于userId
     *
     * @param user
     * @return
     */
    public Result register(User user, String password, String hash) {
        Result result = new Result(true);
        try {
            if (Utils.isEmpty(user.getEmail(), user.getNickname(), password)) {
                result.setSuccess(false);
                result.setMsg("参数错误");
            } else if (searchUserByEmail(user.getEmail()) == null) {
                user.setPwd(password);
                user.setUrl(Utils.MD5(user.getEmail()));
                if (userDao.addUser(user) <= 0) {
                    result.setSuccess(false);
                    result.setMsg("注册失败");
                } else {
                    redisService.deleteUserModel(hash);
                    result.setMsg("恭喜你，注册成功!");
                }
            } else {
                result.setSuccess(false);
                result.setMsg("已被注册");
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return result;
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param newPassword
     */
    public void changePwd(int userId, String newPassword) {
        String salt = Utils.randomString();
        String hash = Utils.MD5(newPassword, salt);
        userDao.changePwd(userId, salt, hash);
    }

    /**
     * 根据id查找用户
     *
     * @param id
     * @return
     */
    public User getUserById(int id) {
        return userDao.getUserById(id);
    }


    /**
     * 修改用户头像
     *
     * @param photoSrc
     * @param userId
     * @return
     */
    public Result changePhoto(String photoSrc, int userId) {
        Result result = new Result();

        if (userId < 1) {
            result.setMsg("用户身份错误");
        } else {
            if (userDao.changePhoto(photoSrc, userId) > 0) {
                result.setSuccess(true);
            } else {
                result.setMsg("数据操作错误");
            }
        }
        return result;
    }

    /**
     * 返回一个用户的所有粉丝
     *
     * @param userId
     * @return
     */
    public List<Attention> getAllFans(int userId) {
        if (userId < 1) {
            return null;
        }
        return userDao.getAllFans(userId);
    }

    /**
     * 获取一个用户所有关注的人
     *
     * @param userId
     * @return
     */
    public List<Attention> getAllAttentions(int userId) {
        if (userId < 1) {
            return null;
        }
        return userDao.getAllAttentions(userId);
    }

    /**
     * 获取指定的关注
     *
     * @param from
     * @param to
     * @return
     */
    public Attention getOneAttention(int from, int to) {
        if (from < 1 || to < 1) {
            return null;
        }
        return userDao.getOneAttention(from, to);
    }

    /**
     * 添加关注
     *
     * @param from
     * @param to
     * @return
     */
    @Transactional
    public Result insertAttention(int from, int to) {
        Result result = new Result();

        if (from < 1 || to < 1) {
            result.setMsg("用户验证失败");
        } else {
            if (userDao.insertAttention(from, to) > 0 &&
                    userDao.updateAttentionCount(from, 1) > 0 &&
                    userDao.updateFansCount(to, 1) > 0) {
                result.setSuccess(true);
            } else {
                throw new ServiceException(ErrorCode.DATA_ERROR, ErrorCode.DATA_ERROR.getDefaultMsg());
            }
        }

        return result;
    }

    /**
     * 删除关注
     *
     * @param from
     * @param to
     * @return
     */
    @Transactional
    public Result deleteAttention(int from, int to) {
        Result result = new Result();

        if (from < 1 || to < 1) {
            result.setMsg("用户验证失败");
        } else {
            if (userDao.deleteAttention(from, to) > 0 &&
                    userDao.updateAttentionCount(from, -1) > 0 &&
                    userDao.updateFansCount(to, -1) > 0) {
                result.setSuccess(true);
            } else {
                throw new ServiceException(ErrorCode.DATA_ERROR, ErrorCode.DATA_ERROR.getDefaultMsg());
            }
        }

        return result;
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    public Result updateUser(User user) {
        Result result = user.valid();
        if (!result.isSuccess()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getDefaultMsg());
        }
        if (userDao.updateUser(user) < 1) {
            throw new ServiceException(ErrorCode.DATA_ERROR, ErrorCode.DATA_ERROR.getDefaultMsg());
        }
        return result;
    }

    /**
     * 重新设置关注和粉丝数量
     *
     * @param userId
     * @return
     */
    public int resetCounts(int userId) {
        if (userId < 1) {
            return 0;
        }
        return userDao.resetCounts(userId);
    }
}
