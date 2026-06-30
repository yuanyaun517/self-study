layui.use(['form', 'layer'], function () {
    var form = layui.form;
    var layer = layui.layer;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    function getQueryId() {
        return Number(new URLSearchParams(window.location.search).get('id') || 0);
    }

    function fillForm(row) {
        form.val('noticeForm', {
            title: row.title || '',
            type: String(row.type == null ? '1' : row.type),
            content: row.content || ''
        });
        form.render('select');
    }

    function loadNotice(noticeId) {
        if (!noticeId) {
            form.render('select');
            return;
        }

        AdminApp.postJson('/manager/getAllNotice', {}).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || '公告通知数据加载失败');
                return;
            }

            var row = (result.data || []).find(function (item) {
                return Number(item.id) === noticeId;
            });

            if (!row) {
                layer.alert('未找到要编辑的公告通知');
                return;
            }

            fillForm(row);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    var noticeId = getQueryId();
    loadNotice(noticeId);

    form.on('submit(saveNotice)', function (data) {
        var field = data.field || {};
        var payload = {
            title: String(field.title || '').trim(),
            type: String(field.type || '1'),
            content: String(field.content || '').trim(),
            sendUser: AdminApp.getManagerId()
        };

        if (!payload.title) {
            layer.msg('标题不能为空');
            return false;
        }

        if (!payload.content) {
            layer.msg('内容不能为空');
            return false;
        }

        var url = '/manager/addNotice';
        if (noticeId) {
            payload.id = noticeId;
            url = '/manager/editNotice';
        }

        AdminApp.postJson(url, payload).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || '保存失败');
                return;
            }

            layer.msg('保存成功', function () {
                AdminApp.closeLayerOrRedirect('index.html');
            });
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });

        return false;
    });
});
