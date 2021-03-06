<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <title>修改个人信息</title>
    <%@include file="../common/head.jsp" %>
    <link rel="stylesheet" href="/resource/css/info.css">
    <script src="/resource/js/validator.min.js"></script>
</head>
<body>
<%@include file="../common/title.jsp" %>

<div class="container" style="margin-top: 80px" id="container">
    <div class="row">
        <div class="col-xs-3">
            <%@include file="../common/leftList.jsp" %>
        </div>
        <div class="col-xs-9" style="margin-top: 10px">
            <form id="form" role="form" novalidate>
                <div class="form-group">
                    <label for="email">Email</label>
                    <p class="form-control-static" id="email">
                        ${sessionScope.user.email}
                    </p>
                </div>
                <div class="form-group">
                    <label for="url">博客地址</label>
                    <p class="form-control-static" id="url">
                        <a href="/${sessionScope.user.url}" target="_blank">
                            ${host}/${sessionScope.user.url}
                        </a>
                    </p>
                </div>
                <div class="form-group">
                    <label for="sex">性别</label>
                    <select id="sex" class="form-control" name="sex" required>
                        <option value="男">男</option>
                        <option value="女">女</option>
                    </select>
                    <div class="help-block with-errors"></div>
                </div>
                <div class="form-group">
                    <label for="nickname">昵称</label>
                    <input type="text" class="form-control" id="nickname" data-minlength="1"
                           data-maxlength="30" required name="nickname" placeholder="昵称"
                           value="${sessionScope.user.nickname}">
                    <div class="help-block with-errors"></div>
                </div>
                <div class="form-group">
                    <label for="motto">个性签名</label>
                    <input type="text" class="form-control" id="motto" name="motto"
                           data-maxlength="50" required placeholder="个性签名"
                           value="${sessionScope.user.motto}">
                    <div class="help-block with-errors"></div>
                </div>
                <div class="form-group">
                    <label for="interest">兴趣</label>
                    <input type="text" class="form-control" id="interest" name="interest"
                           data-maxlength="100" required placeholder="兴趣（多个兴趣用'#'隔开）"
                           value="${sessionScope.user.interest}">
                    <div class="help-block with-errors"></div>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 200px;margin: 0 auto;display: block">
                    保存修改
                </button>
            </form>
        </div>
    </div>
</div>

<%@include file="../common/footer.jsp" %>
<script type="application/javascript">
    $(function () {
        $('#sex').val('${sessionScope.user.sex}');
        $('#info-item').addClass('active');

        $('#form').validator().on('submit', function (e) {
            if (e.isDefaultPrevented()) {
                // handle the invalid form...
            } else {
                updateInfo();
            }

            return false;
        });
    });

    function updateInfo() {
        $.ajax('/user', {
            data: {
                userId: '${sessionScope.user.userId}',
                email: '${sessionScope.user.email}',
                sex: $('#sex').val(),
                nickname: $('#nickname').val(),
                motto: $('#motto').val(),
                interest: $('#interest').val()
            },
            dataType: 'JSON',
            type: 'PUT',
            success: function (data) {
                if (data.success) {
                    layer.msg("修改成功");
                    window.location.reload();
                } else {
                    layer.msg(data.msg);
                }
            }
        });
    }
</script>
</body>
</html>
