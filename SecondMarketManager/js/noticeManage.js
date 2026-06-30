layui.use(['form', 'table', 'layer', 'jquery'], function () {
    var form = layui.form;
    var table = layui.table;
    var layer = layui.layer;
    var $ = layui.jquery;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var noticesCache = [];
    var activeNoticeId = 0;
    var activeNoticeLayerIndex = null;

    function isListPage() {
        return !!document.getElementById('noticeTable');
    }

    function renderTable(list) {
        table.render({
            elem: '#noticeTable',
            id: 'noticeTableReload',
            data: list,
            page: true,
            limit: 15,
            skin: 'line',
            toolbar: '#noticeToolbar',
            cols: [[
                { field: 'id', title: 'ID', width: 80, sort: true },
                { field: 'title', title: '标题', minWidth: 180 },
                {
                    field: 'type',
                    title: '类型',
                    width: 100,
                    templet: function (row) {
                        return AdminApp.noticeTypeText(row.type);
                    }
                },
                {
                    field: 'content',
                    title: '内容',
                    minWidth: 280,
                    templet: function (row) {
                        return '<div class="table-text-2">' + AdminApp.escapeHtml(row.content || '--') + '</div>';
                    }
                },
                {
                    field: 'sendUser',
                    title: '发布人',
                    width: 100,
                    templet: function (row) {
                        return row.sendUser || '--';
                    }
                },
                {
                    field: 'sendTime',
                    title: '发布时间',
                    minWidth: 170,
                    templet: function (row) {
                        return AdminApp.formatDate(row.sendTime);
                    }
                },
                { fixed: 'right', title: '操作', width: 180, toolbar: '#noticeBar' }
            ]]
        });
    }

    function loadNotices() {
        AdminApp.postJson('/manager/getAllNotice', {}).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || '公告通知数据加载失败');
                return;
            }
            noticesCache = result.data || [];
            renderTable(noticesCache);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function filterNotices() {
        var keyword = String($('#noticeKeyword').val() || '').trim().toLowerCase();
        var type = String($('#noticeType').val() || '').trim();
        var filteredList = noticesCache.filter(function (item) {
            var matchKeyword = !keyword || [item.title, item.content].some(function (value) {
                return String(value || '').toLowerCase().indexOf(keyword) > -1;
            });
            var matchType = !type || String(item.type || '') === type;
            return matchKeyword && matchType;
        });
        renderTable(filteredList);
    }

    function fillForm(row) {
        form.val('noticeForm', {
            title: row.title || '',
            type: row.type || '1',
            content: row.content || ''
        });
    }

    function resetNoticeForm() {
        activeNoticeId = 0;
        var noticeFormElement = document.getElementById('noticeForm');
        if (noticeFormElement) {
            noticeFormElement.reset();
        }
        form.val('noticeForm', {
            title: '',
            type: '1',
            content: ''
        });
        form.render('select');
    }

    function openNoticeFormDialog(row) {
        var templateElement = document.getElementById('noticeFormTemplate');
        if (!templateElement) {
            return;
        }

        activeNoticeId = row && row.id ? Number(row.id) : 0;
        activeNoticeLayerIndex = layer.open({
            type: 1,
            title: activeNoticeId ? '编辑公告通知' : '发布公告通知',
            area: ['760px', '620px'],
            shadeClose: true,
            content: templateElement.innerHTML,
            success: function () {
                resetNoticeForm();
                if (row) {
                    activeNoticeId = Number(row.id);
                    fillForm(row);
                }
            },
            end: function () {
                activeNoticeLayerIndex = null;
                activeNoticeId = 0;
            }
        });
    }

    function initListPage() {
        if (!isListPage()) {
            return;
        }

        loadNotices();
        window.reloadPageData = loadNotices;

        form.on('submit(searchNotice)', function () {
            filterNotices();
            return false;
        });

        $('#resetNoticeSearch').on('click', function () {
            $('#noticeKeyword').val('');
            $('#noticeType').val('');
            form.render('select');
            renderTable(noticesCache);
        });

        table.on('toolbar(noticeTableFilter)', function (obj) {
            if (obj.event === 'add') {
                openNoticeFormDialog();
            }
        });

        table.on('tool(noticeTableFilter)', function (obj) {
            var row = obj.data || {};
            if (obj.event === 'edit') {
                openNoticeFormDialog(row);
            }

            if (obj.event === 'delete') {
                layer.confirm('确认删除这条公告通知吗？', function (index) {
                    AdminApp.postJson('/manager/delNotice', { id: row.id }).then(function (result) {
                        if (result.code !== 1) {
                            layer.alert(result.msg || '删除失败');
                            return;
                        }
                        layer.close(index);
                        layer.msg('删除成功');
                        loadNotices();
                    }).catch(function (xhr) {
                        AdminApp.handleHttpError(xhr);
                    });
                });
            }
        });
    }

    function bindFormEvents() {
        form.on('submit(saveNotice)', function (data) {
            var field = data.field || {};
            var payload = {
                title: field.title,
                type: field.type,
                content: field.content,
                sendUser: AdminApp.getManagerId()
            };
            var url = '/manager/addNotice';

            if (activeNoticeId) {
                payload.id = activeNoticeId;
                url = '/manager/editNotice';
            }

            AdminApp.postJson(url, payload).then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || '保存失败');
                    return;
                }
                layer.msg('保存成功', function () {
                    if (activeNoticeLayerIndex !== null) {
                        layer.close(activeNoticeLayerIndex);
                    }
                    loadNotices();
                });
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
            return false;
        });
    }

    initListPage();
    bindFormEvents();
});
