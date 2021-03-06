package cn.zzuzl.xblog.web;

import cn.zzuzl.xblog.common.annotation.Auth;
import cn.zzuzl.xblog.model.vo.Result;
import cn.zzuzl.xblog.service.CommentService;
import cn.zzuzl.xblog.model.Comment;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 评论相关controller
 */
@Controller
@RequestMapping("comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    /* 查询文章对应评论 */
    /*@RequestMapping(value = "/article/{articleId}", method = RequestMethod.GET)
    @ResponseBody
    public List<Comment> list(@PathVariable("articleId") Integer articleId) {
        return commentService.listArticleComments(articleId);
    }*/

    /* 发表评论 */
    @Auth
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public Result postComment(@Valid @ModelAttribute("comment") Comment comment, BindingResult bindingResult) {
        return commentService.insertComment(comment);
    }
}
