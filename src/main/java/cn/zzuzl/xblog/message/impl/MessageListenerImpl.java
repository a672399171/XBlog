package cn.zzuzl.xblog.message.impl;

import cn.zzuzl.xblog.common.Common;
import cn.zzuzl.xblog.message.MessageListener;
import cn.zzuzl.xblog.model.Article;
import cn.zzuzl.xblog.model.Attention;
import cn.zzuzl.xblog.model.Dynamic;
import cn.zzuzl.xblog.model.User;
import cn.zzuzl.xblog.service.DynamicService;
import cn.zzuzl.xblog.service.MailService;
import cn.zzuzl.xblog.service.MessageService;
import cn.zzuzl.xblog.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.zzuzl.xblog.model.message.NewArticleMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("myMessageListener")
public class MessageListenerImpl implements MessageListener {
    @Resource
    private MailService mailService;
    @Resource
    private UserService userService;
    @Resource
    private DynamicService dynamicService;
    @Resource
    private MessageService messageService;
    @Resource
    private VelocityEngine velocityEngine;

    private Logger logger = LogManager.getLogger(getClass());

    public void handleMessage(String message) {
        if (message == null) {
            logger.error("message is null");
        } else {
            logger.info(message);
            ObjectMapper mapper = new ObjectMapper();
            NewArticleMessage articleMessage = null;
            try {
                articleMessage = mapper.readValue(message, NewArticleMessage.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (articleMessage != null) {
                User user = new User();
                user.setUserId(articleMessage.getUserId());

                Article article = new Article();
                article.setArticleId(articleMessage.getArticleId());

                // 插入动态
                Dynamic dynamic = new Dynamic(user, article, Common.POST_OPERATOR, articleMessage.getDescription());
                dynamicService.insertDynamic(dynamic);

                // 给粉丝发站内消息
                List<Attention> attentionList = userService.getAllFans(articleMessage.getUserId());
                for (Attention attention : attentionList) {
                    User from = attention.getFrom();

                    Map<String, Object> model = new HashMap<String, Object>();
                    model.put("url", "http://xblog.zzuzl.cn/p/" + articleMessage.getArticleId());
                    model.put("nickname", articleMessage.getNickname());
                    model.put("title", articleMessage.getArticleTitle());

                    String title = articleMessage.getNickname() + "发表了新博客";
                    String content = VelocityEngineUtils.mergeTemplateIntoString(
                            velocityEngine, "fans.vm", "utf-8", model);
                    messageService.sendNewArticle(from, title, content);
                }

                logger.info("发送fans站内消息！");
            }
        }
    }
}