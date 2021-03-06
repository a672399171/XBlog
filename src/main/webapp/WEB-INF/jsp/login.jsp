<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <title>XBlog登录</title>
    <%@include file="common/head.jsp" %>
    <script src="/resource/js/validator.min.js"></script>
    <style type="text/css">
        body {
            background: #dcdcdc;
        }

        #login-btn {
            width: 100%;
        }

        .login-box {
            width: 50%;
            height: 400px;
            border: solid 1px #afd9ee;
            margin: 0 auto;
            margin-top: 50px;
            margin-bottom: 20px;
            box-shadow: 10px 20px 40px #9b9b9d;
        }

        #form1 {
            width: 300px;
            margin: 0 auto;
        }

        .login-content {
            height: 300px;
            width: 100%;
            padding-top: 50px;
            background: white;
        }

        .login-title {
            padding: 20px 10px;
            background: #c0c0c0;
        }

        .nav-tabs li {
            width: 128px;
            text-align: center;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="login-box">
        <div class="login-title text-center">
            <h1>
                <small>登录</small>
            </h1>
        </div>
        <div class="login-content">
            <form id="form1">
                <div class="form-group">
                    <div class="input-group">
                                <span class="input-group-addon">
                                    <span class="glyphicon glyphicon-user"></span>
                                </span>
                        <input type="email" id="email" name="email" class="form-control" novalidate
                               placeholder="邮箱" required>
                    </div>
                    <div class="help-block with-errors"></div>
                </div>
                <div class="form-group">
                    <div class="input-group">
                                <span class="input-group-addon">
                                    <span class="glyphicon glyphicon-lock"></span>
                                </span>
                        <input type="password" id="password" name="password" class="form-control"
                               novalidate placeholder="密码" minlength="6" maxlength="32" required>
                    </div>
                    <div class="help-block with-errors"></div>
                </div>
                <div class="form-group form-actions" style="text-align: center">
                    <button type="submit" class="btn btn-info" id="login-btn">登录</button>
                </div>
                <div class="form-group">
                    <div class="alert alert-danger" role="alert" id="error"></div>
                </div>
                <div class="form-group">
                    <div class="col-xs-6">
                        <span>忘记密码</span>
                        <a href="/findPwd" target="_blank">找回密码</a>
                    </div>
                    <div class="col-xs-6 link">
                        <span>没有账号?</span>
                        <a href="/register" target="_blank">注册</a>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript" color="255,0,0" opacity='0.7' zIndex="-1" count="200"
        src="//cdn.bootcss.com/canvas-nest.js/1.0.1/canvas-nest.min.js"></script>
<script>
    /* 文档加载完后执行该函数 */
    $(function () {
        $("#error").hide();
        /* 为form1表单加上数据校验 */
        $('#form1').validator().on('submit', function (e) {
            /* e为javascript事件对象 */
            if (e.isDefaultPrevented()) {
                // handle the invalid form...
            } else {
                $("#login-btn").text("登录中...");
                $("#login-btn").attr('disabled', 'disabled');
                obj.login();
            }

            return false;
        });
    });

    var obj = {
        url: "/user/login",
        login: function () {
            /* jquery的post方法发送post请求到url */
            $.post(this.url, {
                email: $("#email").val(),
                password: $("#password").val()
            }, this.success, "JSON");
        },
        /* 请求响应成功时回调函数，data为响应的数据 */
        success: function (data) {
            if (data.success) {
                $("#error").hide();
                window.location = '${requestScope.returnUrl}';
            } else {
                $("#error").text(data.msg);
                $("#error").show();
                $("#login-btn").removeAttr('disabled');
                $("#login-btn").text("登录");
            }
        }
    };
</script>
</body>

</html>
