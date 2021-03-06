package cn.zzuzl.xblog.web;

import cn.zzuzl.xblog.common.annotation.Logined;
import cn.zzuzl.xblog.model.*;
import cn.zzuzl.xblog.service.*;
import cn.zzuzl.xblog.common.Common;
import cn.zzuzl.xblog.util.ConfigProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责页面跳转的controller
 */
@Controller
public class PageController {
    @Resource
    private CategoryService categoryService;
    @Resource
    private ArticleService articleService;
    @Resource
    private UserService userService;
    @Resource
    private CommentService commentService;
    @Resource
    private RedisService redisService;
    @Resource
    private ConfigProperty configProperty;
    private final Logger logger = LogManager.getLogger(getClass());

    /* 主页 */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        List<Category> list = redisService.getAllCategory();
        model.addAttribute("list", list);

        List<User> userRank = redisService.getUserRank();
        model.addAttribute("userRank", userRank);
        return "index";
    }

    /* 关于 */
    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String about() {
        return "about";
    }

    /* 登录 */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam(value = "returnUrl", required = false) String returnUrl, Model model) {
        if (StringUtils.isEmpty(returnUrl)) {
            returnUrl = "/";
        }
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    /* 注册 */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register() {
        return "zc";
    }

    /* 注册结果提示 */
    @RequestMapping(value = "/check", method = RequestMethod.GET)
    public String check() {
        return "check";
    }

    /* 找回密码 */
    @RequestMapping(value = "/findPwd", method = RequestMethod.GET)
    public String findPwd() {
        return "findPwd";
    }

    /* 用户个人信息 */
    @RequestMapping(value = "/setting/userInfo", method = RequestMethod.GET)
    public String blog(HttpSession session, Model model, HttpServletResponse response, @Logined User user) {
        model.addAttribute("host", configProperty.getRoot());
        return "setting/info";
    }

    /* 发表文章 */
    @RequestMapping(value = "/setting/editArticle", method = RequestMethod.GET)
    public String editArticle(Model model) {
        List<Category> list = redisService.getAllCategory();
        model.addAttribute("list", list);
        return "setting/editArticle";
    }

    /* 编辑文章 */
    @RequestMapping(value = "/setting/editArticle/{id}", method = RequestMethod.GET)
    public String editArticle(@PathVariable() Integer id, Model model, HttpSession session, @Logined User user) {
        List<Category> list = categoryService.listCategory();
        model.addAttribute("list", list);
        if (id != null && id > 0) {
            Article article = articleService.detail(id);
            if (user.getUserId() == article.getUser().getUserId()) {
                model.addAttribute("article", articleService.detail(id));
            }
        }

        return "setting/editArticle";
    }

    /* 修改密码 */
    @RequestMapping(value = "/setting/changePwd", method = RequestMethod.GET)
    public String changePwd() {
        return "setting/changePwd";
    }

    /* 修改头像 */
    @RequestMapping(value = "/setting/changePhoto", method = RequestMethod.GET)
    public String changePhoto() {
        return "setting/changePhoto";
    }

    /* 管理文章 */
    @RequestMapping(value = "/setting/manageArticle", method = RequestMethod.GET)
    public String manageArticle(Model model, HttpSession session, @Logined User user) {
        List<Article> list = articleService.listMyArticle(1, 100, user.getUserId());
        model.addAttribute("list", list);

        return "setting/manageArticle";
    }

    /* 收藏的文章 */
    @RequestMapping(value = "/setting/collectArticle", method = RequestMethod.GET)
    public String collectArticle(Model model, HttpSession session, @Logined User user) {
        List<Article> list = articleService.listMyArticle(1, 100, user.getUserId());
        model.addAttribute("list", list);

        return "setting/collectArticle";
    }

    /* 用户个人中心 */
    @RequestMapping(value = "/u/{url}", method = RequestMethod.GET)
    public String personalCenter(@PathVariable("url") String url, Model model, HttpSession session, HttpServletResponse response, @Logined User loginUser) {
        User user = userService.searchUserByUrl(url);
        model.addAttribute("host", configProperty.getRoot());

        if (user != null) {
            List<Attention> fans = userService.getAllFans(user.getUserId());
            model.addAttribute("fans", fans);
            model.addAttribute("attentions", userService.getAllAttentions(user.getUserId()));
            model.addAttribute(Common.USER, user);

            if (fans != null && loginUser != null) {
                for (Attention attention : fans) {
                    if (attention.getFrom().getUserId() == loginUser.getUserId()) {
                        model.addAttribute("attention", attention);
                        break;
                    }
                }
            }
        } else {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "personalCenter";
    }

    /* 用户的博客 */
    @RequestMapping(value = "/{url}", method = RequestMethod.GET)
    public String blog(@PathVariable("url") String url, Model model, HttpServletResponse response) {
        User user = userService.searchUserByUrl(url);
        if (user != null) {
            List<Article> list = articleService.listMyArticle(1, 100, user.getUserId());
            model.addAttribute("list", list);
            model.addAttribute(Common.USER, user);
        } else {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "blog";
    }

    /* 文章详情 */
    @RequestMapping(value = "/p/{id}", method = RequestMethod.GET)
    public String articleDetail(@PathVariable("id") String idStr, Model model, HttpSession session, HttpServletResponse response, @Logined User user) {
        if (!StringUtils.isNumeric(idStr)) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            } catch (IOException e) {
                logger.error(e);
            }
        }

        Integer id = Integer.parseInt(idStr);
        Article article = redisService.queryArticleFromCacheById(id);
        if (article != null) {
            String tag = article.getTag();
            String[] tags = new String[0];
            if (!StringUtils.isEmpty(tag)) {
                tags = tag.split("#");
            }
            model.addAttribute("tags", tags);
            model.addAttribute("article", article);
            model.addAttribute("comments", commentService.listArticleComments(id));

            if (user != null) {
                List<Like> likes = articleService.getLikes(user.getUserId(), id);
                if (likes != null && likes.size() > 0) {
                    model.addAttribute("like", likes.get(0));
                }

                model.addAttribute("attention", userService.getOneAttention(user.getUserId(), article.getUser().getUserId()));
            }

            // 记录浏览量
            List<Integer> array = (List<Integer>) session.getAttribute(Common.ARTICLE_ARRAY);
            if (array == null) {
                array = new ArrayList<Integer>();
            }
            if (!array.contains(id)) {
                array.add(id);
                redisService.updateViewCount(id);
            }
            session.setAttribute(Common.ARTICLE_ARRAY, array);
        } else {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                logger.error(e);
            }
        }

        return "articleDetail";
    }

    /* 搜索文章结果页面 */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        return "search";
    }

}
