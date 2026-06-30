
// 更改密码
function changepsd() {
	layui.use(['form','miniTab'], function () {
	    var form = layui.form,
	        layer = layui.layer,
	        miniTab = layui.miniTab;
	
	    //监听提交
	    form.on('submit(saveBtn)', function (data) {
			data = data.field;
	        var index = layer.alert("确认更改密码吗？", {
	            title: '确认更改'
	        }, function () {
	
				var datas = {
				   "token": window.localStorage.getItem("token"),
				   "userid": window.localStorage.getItem("userid"),
				    "upsdold": data.old_password,
				    "upsdnew": data.again_password
				}
				
				$.ajax({
					url: baseurl+"user/changePsd",
					data: JSON.stringify(datas),
					type: "post",
					dataType: "json",
					headers: {
						'Content-Type': 'application/json;charset=utf-8'
					}, //接口json格式
					success: function(data) {
						if (data.code == "200") {
							layer.msg('修改成功', function() {
								layer.close(index);
								miniTab.deleteCurrentByIframe();
								window.localStorage.clear();
								window.location.replace('../../../');
							});
						} else {
							layer.alert(data.data.info);
						}
					},
					error: function(data) {
						alertHttpError();
					}
				});
				
	           
	        });
	        return false;
	    });
	
	});
}
