layui.use(['form', 'layer'], function () {
    var form = layui.form,
        layer = layui.layer,
        $ = layui.jquery,
        $form = $('form');

    var password = null;
    var id = null;
    $(function () {
        $.ajax({
            type : "post",
            url : "/getSession",
            dataType : "json",
            success : function(result) {
                $("#id").attr("value",result.id);
                $("#name").attr("value",result.name);
                password = result.password;
                id=result.id;
            },
            error : function() {
                alert("請求失敗");
            }
        });
    });
    //添加验证规则verify
    form.verify({
        pass: [
            /^[\S]{6,16}$/
            , '密码必须6到16位，且不能出现空格'
        ],
        oldPwd: function (value, item) {
            $.ajax({
                type : "post",
                url : "/getMd5",
                data : {"password":value},
                dataType : "json",
                success : function(result) {
                    if (result != password) {
                        return "密码错误，请重新输入！";
                    }
                },
                error : function() {
                    alert("請求失敗");
                }
            });

        },
        confirmPwd: function (value, item) {
            if (!new RegExp($("#oldPwd").val()).test(value)) {
                return "两次输入密码不一致，请重新输入！";
            }
        },
    });
    //修改密码
    form.on("submit(changePwd)", function () {
        var newPwd = $("#oldPwd").val();
        var index = layer.msg('提交中，请稍候', {icon: 16, time: false, shade: 0.8});
        $.ajax({
            type : "post",
            url : "/updatePwd",
            data : {"password":newPwd,"id":id},
            dataType : "json",
            success : function(result) {
                if(result==0){
                    setTimeout(function () {
                        layer.close(index);
                        layer.msg("密码修改成功！");
                        $(".pwd").val('');
                    }, 2000);
                }
            },
            error : function() {
                alert("請求失敗");
            }
        });
        return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。
    })

});

