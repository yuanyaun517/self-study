layui.use(['form', 'table', 'layer', 'jquery', 'upload'], function () {
    var form = layui.form;
    var table = layui.table;
    var layer = layui.layer;
    var $ = layui.jquery;
    var upload = layui.upload;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var bannersCache = [];
    var currentBannerImage = '';

    function getQueryId() {
        return Number(new URLSearchParams(window.location.search).get('id') || 0);
    }

    function renderTable(list) {
        table.render({
            elem: '#bannerTable',
            id: 'bannerTableReload',
            data: list,
            page: true,
            limit: 15,
            skin: 'line',
            cols: [[
                { field: 'id', title: 'ID', width: 80, sort: true },
                {
                    field: 'imgUrl',
                    title: '图片',
                    width: 180,
                    templet: function (row) {
                        var imageUrl = AdminApp.resolveFileUrl(row.imgUrl);
                        if (!imageUrl) {
                            return '<span class="table-empty">无</span>';
                        }
                        return '<img class="banner-table-thumb preview-image" data-src="' + AdminApp.escapeHtml(imageUrl) + '" src="' + AdminApp.escapeHtml(imageUrl) + '" alt="banner">';
                    }
                },
                { field: 'sendUser', title: '发布人', minWidth: 120, templet: function (row) { return row.sendUser || '--'; } },
                {
                    field: 'sendTime',
                    title: '创建时间',
                    minWidth: 170,
                    templet: function (row) {
                        return AdminApp.formatDate(row.sendTime);
                    }
                },
                {
                    field: 'updateTime',
                    title: '更新时间',
                    minWidth: 170,
                    templet: function (row) {
                        return AdminApp.formatDate(row.updateTime);
                    }
                },
                { fixed: 'right', title: '操作', width: 180, toolbar: '#bannerBar' }
            ]]
        });
    }

    function loadBanners() {
        AdminApp.postJson('/manager/getAllBanner', {}).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || '轮播图数据加载失败');
                return;
            }
            bannersCache = result.data || [];
            renderTable(bannersCache);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function initListPage() {
        if (!document.getElementById('bannerTable')) {
            return;
        }

        loadBanners();
        window.reloadPageData = loadBanners;

        $(document).on('click', '.preview-image', function () {
            AdminApp.openImagePreview($(this).data('src'));
        });

        table.on('tool(bannerTableFilter)', function (obj) {
            var row = obj.data || {};

            if (obj.event === 'edit') {
                AdminApp.openLayerPage('编辑轮播图', 'add.html?id=' + row.id, ['720px', '520px']);
            }

            if (obj.event === 'delete') {
                layer.confirm('确认删除这张轮播图吗？', function (index) {
                    AdminApp.postJson('/manager/delBanner', { id: row.id }).then(function (result) {
                        if (result.code !== 1) {
                            layer.alert(result.msg || '删除失败');
                            return;
                        }
                        layer.close(index);
                        layer.msg('删除成功');
                        loadBanners();
                    }).catch(function (xhr) {
                        AdminApp.handleHttpError(xhr);
                    });
                });
            }
        });
    }

    function fillBannerForm(row) {
        currentBannerImage = row && row.imgUrl ? row.imgUrl : '';
        form.val('bannerForm', {
            sendUser: row && row.sendUser ? row.sendUser : AdminApp.getManagerAccount()
        });
        if (currentBannerImage) {
            $('#bannerPreview').attr('src', AdminApp.resolveFileUrl(currentBannerImage)).removeClass('layui-hide');
        }
    }

    function initFormPage() {
        if (!document.getElementById('bannerForm')) {
            return;
        }

        var bannerId = getQueryId();
        if (!bannerId) {
            layer.msg('This page has been removed', function () {
                AdminApp.closeLayerOrRedirect('index.html');
            });
            return;
        }

        form.val('bannerForm', {
            sendUser: AdminApp.getManagerAccount()
        });

        upload.render({
            elem: '#uploadBannerBtn',
            url: buildApiUrl('/common/upload'),
            accept: 'images',
            exts: 'jpg|jpeg|png|gif|webp',
            done: function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || '上传失败');
                    return;
                }
                currentBannerImage = result.data;
                $('#bannerPreview').attr('src', getDownloadUrl(result.data)).removeClass('layui-hide');
                layer.msg('上传成功');
            },
            error: function () {
                alertHttpError('图片上传失败');
            }
        });

        if (bannerId) {
            AdminApp.postJson('/manager/getAllBanner', {}).then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || '轮播图数据加载失败');
                    return;
                }
                var row = (result.data || []).find(function (item) {
                    return Number(item.id) === bannerId;
                });
                if (!row) {
                    layer.alert('未找到要编辑的轮播图');
                    return;
                }
                fillBannerForm(row);
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
        }

        form.on('submit(saveBanner)', function (data) {
            var field = data.field || {};
            if (!currentBannerImage) {
                layer.msg('请先上传轮播图');
                return false;
            }

            var payload = {
                imgUrl: /^https?:\/\//i.test(currentBannerImage) ? currentBannerImage : getDownloadUrl(currentBannerImage),
                sendUser: field.sendUser || AdminApp.getManagerAccount()
            };
            var url = '/manager/addBanner';
            if (bannerId) {
                payload.id = bannerId;
                url = '/manager/editBanner';
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
    }

    initListPage();
    initFormPage();
});
