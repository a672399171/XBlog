<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- 兼容ie8 --%>
<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
<meta name="renderer" content="webkit">

<!--[if lt IE 9]>
<script src="/resource/js/html5.js"></script>
<script src="/resource/js/respond.min.js"></script>
<![endif]-->

<link href="//cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
<link href="//cdn.bootcss.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="/resource/css/common.css">
<link rel="stylesheet" href="/resource/css/index.css">
<script src="//cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<script src="//cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script type="application/javascript">
    // 适配页面高度
    $(function () {
        //浏览器当前窗口可视区域高度
        var height = window.innerHeight;

        $('#container').css('min-height', height + 'px');
    });
</script>