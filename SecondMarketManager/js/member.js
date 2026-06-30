// 用户管理
function member() {

	layui.use(['form', 'table'], function() {
		var $ = layui.jquery,
			form = layui.form,
			table = layui.table;
			
			
		
		
		// var datas = {};
		// // 异步加载会员类型
		// $.ajax({
		// 	url: baseurl + "product/getAllPtype",
		// 	data: JSON.stringify(datas),
		// 	type: "post",
		// 	dataType: "json",
		// 	headers: {
		// 		'Content-Type': 'application/json;charset=utf-8'
		// 	}, //接口json格式
		// 	success: function(data) {
		// 		if (data.code == "200") {
		// 			var list = data.data.info;
		
		// 			for (var i = 0; i < list.length; i++) {
		// 				var option = document.createElement(
		// 					"option"); // 创建添加option属性
		// 				option.setAttribute("value", list[i].ptid); // 给option的value添加值
		// 				option.innerText = list[i].ptname; // 打印option对应的纯文本 
		// 				s_ptype.appendChild(option); //给select添加option子标签
		// 				form.render("select"); // 刷性select，显示出数据
		
		// 			}
		
		
		
		// 		} else {
		// 			layer.alert(data.data.info);
		// 		}
		// 	},
		// 	error: function(data) {
		// 		layer.alert(JSON.stringify(data), {
		// 			title: data
		// 		});
		// 	}
		// });
		
		
		
		
		// var datas = {};
		// // 异步加载折扣信息
		// $.ajax({
		// 	url: baseurl + "product/getAllDiscount",
		// 	data: JSON.stringify(datas),
		// 	type: "post",
		// 	dataType: "json",
		// 	headers: {
		// 		'Content-Type': 'application/json;charset=utf-8'
		// 	}, //接口json格式
		// 	success: function(data) {
		// 		if (data.code == "200") {
		// 			var list = data.data.info;
		
		// 			for (var i = 0; i < list.length; i++) {
		// 				var option = document.createElement(
		// 					"option"); // 创建添加option属性
		// 				option.setAttribute("value", list[i].did); // 给option的value添加值
		// 				option.innerText = list[i].dname + " " + list[i].val +
		// 				"折"; // 打印option对应的纯文本 
		// 				s_dtype.appendChild(option); //给select添加option子标签
		// 				form.render("select"); // 刷性select，显示出数据
		// 			}
		
		
		
		
		
		
		// 			// $("#dval").val("" + list[0].val);
		
		// 			//  form.on('select(dtype)', function(data){
		// 			//       $("#dval").val("" + list[data.did].val);
		// 			//       });
		
		
		
		
		// 		} else {
		// 			layer.alert(data.data.info);
		// 		}
		// 	},
		// 	error: function(data) {
		// 		layer.alert(JSON.stringify(data), {
		// 			title: data
		// 		});
		// 	}
		// });
		
		
		
		
		
		
		

		var datas = {
			"token": window.localStorage.getItem("token"),
			"userid": window.localStorage.getItem("userid"),
		};

		$.ajax({
			url: baseurl + "member/getAllMember",
			data: JSON.stringify(datas),
			type: "post",
			dataType: "json",
			headers: {
				'Content-Type': 'application/json;charset=utf-8'
			}, //接口json格式
			success: function(data) {
				if (data.code == "200") {


					var d = data.data.info;

					table.render({
						elem: '#currentTableId',
						data: d,
						toolbar: '#toolbarDemo',
						defaultToolbar: ['filter', 'exports', 'print', {
							title: '提示',
							layEvent: 'LAYTABLE_TIPS',
							icon: 'layui-icon-tips'
						}],
						cols: [
							[{
									field: 'mid',
									width: 120,
									title: '会员编号'
								},
								{
									field: 'mname',
									width: 120,
									title: '会员名称',
									edit: 'text',
									align: "center"
								},
								{
									field: 'tel',
									title: '电话号码',
									width:200,
									edit: 'text',
									align: "center"
								},
								{
									field: 'idcard',
									width: 240,
									title: '身份证号码',
									align: "center",
									edit: 'text'
								},
								{
									field: 'score',
									width: 250,
									title: '积分',
									edit: 'text',
									align: "center"
								},
								{
									field: 'priceall',
									width: 250,
									title: '消费总金额',
									edit: 'text',
									align: "center"
								},
								{
								field: 'createdatetime',
									width: 250,
									title: '会员注册日期',
									edit: 'text',
									align: "center"
								},
								{
									title: '操作',
									minWidth: 300,
									toolbar: '#currentTableBar',
									align: "center"
								}
							]
						],
						limits: [10, 15, 20, 25, 50, 100],
						limit: d.length,
						page: true,
						skin: 'line'
					});


				} else {
					layer.alert(data.data.info);
				}
			},
			error: function(data) {
				alertHttpError();
			}
		});




		/**
		 * toolbar监听事件
		 */
		table.on('toolbar(currentTableFilter)', function(obj) {
			if (obj.event === 'add') { // 监听添加操作
				var index = layer.open({
					title: '添加',
					type: 2,
					shade: 0.2,
					maxmin: true,
					shadeClose: true,
					area: ['100%', '100%'],
					content: 'add.html',
				});
				$(window).on("resize", function() {
					layer.full(index);
				});
			}

		});


        // 监听搜索操作
        form.on('submit(data-search-btn)', function (data) {
            var result = JSON.stringify(data.field);
			
           var datas = {
			   "token": window.localStorage.getItem("token"),
			   "userid": window.localStorage.getItem("userid"),
			   "s_tel":data.field.s_tel,
			   };
           $.ajax({
           	url: baseurl + "member/queryMemberByConditions",
           	data: JSON.stringify(datas),
           	type: "post",
           	dataType: "json",
           	headers: {
           		'Content-Type': 'application/json;charset=utf-8'
           	}, //接口json格式
           	success: function(data) {
           		if (data.code == "200") {
           			
					
			
					
					
					var d = data.data.info;
					
					table.render({
						elem: '#currentTableId',
						data: d,
						toolbar: '#toolbarDemo',
						defaultToolbar: ['filter', 'exports', 'print', {
							title: '提示',
							layEvent: 'LAYTABLE_TIPS',
							icon: 'layui-icon-tips'
						}],
						cols: [
							[{
									field: 'mid',
									width: 120,
									title: '会员编号'
								},
								{
									field: 'mname',
									width: 120,
									title: '会员名称',
									edit: 'text',
									align: "center"
								},
								{
									field: 'tel',
									title: '电话号码',
									width:200,
									edit: 'text',
									align: "center"
								},
								{
									field: 'idcard',
									width: 240,
									title: '身份证号码',
									align: "center",
									edit: 'text'
								},
								{
									field: 'score',
									width: 250,
									title: '积分',
									edit: 'text',
									align: "center"
								},
								{
									field: 'priceall',
									width: 250,
									title: '消费总金额',
									edit: 'text',
									align: "center"
								},
								{
								field: 'createdatetime',
									width: 250,
									title: '会员注册日期',
									edit: 'text',
									align: "center"
								},
								{
									title: '操作',
									minWidth: 300,
									toolbar: '#currentTableBar',
									align: "center"
								}
							]
						],
						limits: [10, 15, 20, 25, 50, 100],
						limit: d.length,
						page: true,
						skin: 'line'
					});
					
					
					
					
					
					
					
           		} else {
           			layer.alert(data.data.info);
           		}
           	},
           	error: function(data) {
           		alertHttpError();
           	}
           });
		   



            return false;
        });
		
		

		//监听单元格编辑
		table.on('edit(currentTableFilter)', function(obj) {
			var value = obj.value //得到修改后的值
				,
				data = obj.data //得到所在行所有键值
				,
				field = obj.field; //得到字段

			var datas = {
				"token": window.localStorage.getItem("token"),
				"userid": window.localStorage.getItem("userid"),
				"tablename": "member",
				"valkey": field,
				"valval": value,
				"wheval": "mid",
				"inid": data.mid,
			};

			$.ajax({
				url: baseurl + "public/edit",
				data: JSON.stringify(datas),
				type: "post",
				dataType: "json",
				headers: {
					'Content-Type': 'application/json;charset=utf-8'
				}, //接口json格式
				success: function(data) {
					if (data.code == "200") {
						layer.msg('修改成功', function() {
							// obj.del();
							// layer.close(index);
							location.reload(1);
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



		table.on('tool(currentTableFilter)', function(obj) {
			var data = obj.data;
			if (obj.event === 'edit') {
				layer.confirm('直接点击你要编辑的字段即可实现编辑保存');
				return false;
			} else if (obj.event === 'delete') {
				// 执行删除用户操作
				layer.confirm('真的要删除这个会员吗？', function(index) {


					var datas = {
						"token": window.localStorage.getItem("token"),
						"userid": window.localStorage.getItem("userid"),
						"mid": data.mid
					};

					$.ajax({
						url: baseurl + "member/delMember",
						data: JSON.stringify(datas),
						type: "post",
						dataType: "json",
						headers: {
							'Content-Type': 'application/json;charset=utf-8'
						}, //接口json格式
						success: function(data) {
							if (data.code == "200") {
								layer.msg('删除成功', function() {
									obj.del();
									layer.close(index);
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
			}



		});




	});
}















// 添加
function add() {




	layui.use(['form',"upload"], function() {
		var form = layui.form,
			layer = layui.layer,
			upload = layui.upload,
			$ = layui.$;
			
			


		//监听提交
		form.on('submit(saveBtn)', function(data) {
			var data = data.field;
			var index = layer.alert("确认添加吗???", {
				title: '确认添加'
			}, function() {
				
					var datas = {
						"token": window.localStorage.getItem("token"),
						"userid": window.localStorage.getItem("userid"),
						        "mname":data.mname,
						        "tel":data.tel,
						        "idcard":data.idcard
					};
					
					$.ajax({
						url: baseurl + "member/addMember",
						data: JSON.stringify(datas),
						type: "post",
						dataType: "json",
						headers: {
							'Content-Type': 'application/json;charset=utf-8'
						}, //接口json格式
						success: function(data) {
							if (data.code == "200") {
								layer.msg('添加成功', function() {
									// 关闭弹出层
									layer.close(index);
					
									var iframeIndex = parent.layer
										.getFrameIndex(window.name);
									parent.layer.close(iframeIndex);
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
