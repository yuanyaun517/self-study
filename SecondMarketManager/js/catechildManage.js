layui.use(['form', 'table', 'layer', 'jquery'], function () {
    var form = layui.form;
    var table = layui.table;
    var layer = layui.layer;
    var $ = layui.jquery;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var relationCache = [];
    var categoryCache = [];
    var goodsCache = [];

    function getQueryId() {
        return Number(new URLSearchParams(window.location.search).get('id') || 0);
    }

    function getCategoryMap() {
        return categoryCache.reduce(function (map, item) {
            map[String(item.cateid)] = item.catename;
            return map;
        }, {});
    }

    function getGoodsMap() {
        return goodsCache.reduce(function (map, item) {
            map[String(item.id)] = item;
            return map;
        }, {});
    }

    function buildRelationRows(list) {
        var categoryMap = getCategoryMap();
        var goodsMap = getGoodsMap();
        return list.map(function (item) {
            var goods = goodsMap[String(item.goodid)] || {};
            return {
                childid: item.childid,
                cateid: item.cateid,
                goodid: item.goodid,
                childname: item.childname || goods.name || '',
                image: item.image || goods.icon || '',
                categoryName: categoryMap[String(item.cateid)] || '--',
                goodsStatus: AdminApp.goodsStatusText(goods.manage)
            };
        });
    }

    function renderTable(list) {
        table.render({
            elem: '#catechildTable',
            id: 'catechildTableReload',
            data: buildRelationRows(list),
            page: true,
            limit: 15,
            skin: 'line',
            toolbar: '#catechildToolbar',
            cols: [[
                { field: 'childid', title: '关联ID', width: 90, sort: true },
                { field: 'categoryName', title: '分类名称', minWidth: 140 },
                { field: 'goodid', title: '商品ID', width: 100 },
                {
                    field: 'image',
                    title: '商品封面',
                    width: 100,
                    templet: function (row) {
                        var imageUrl = AdminApp.resolveFileUrl(row.image);
                        if (!imageUrl) {
                            return '<div class="catechild-cover-cell catechild-cover-cell-empty"><span class="table-empty">无</span></div>';
                        }
                        return '<div class="catechild-cover-cell"><img class="table-thumb preview-image" data-src="' + AdminApp.escapeHtml(imageUrl) + '" src="' + AdminApp.escapeHtml(imageUrl) + '" alt="goods"></div>';
                    }
                },
                { field: 'childname', title: '商品名称', minWidth: 180 },
                { field: 'goodsStatus', title: '商品状态', width: 110 },
                { fixed: 'right', title: '操作', width: 180, toolbar: '#catechildBar' }
            ]]
        });
    }

    function loadAllData() {
        Promise.all([
            AdminApp.postJson('/manager/getAllCatechild', {}),
            AdminApp.postJson('/manager/getAllCategory', {}),
            AdminApp.postJson('/manager/getAllGoods', {})
        ]).then(function (results) {
            var relationResult = results[0] || {};
            var categoryResult = results[1] || {};
            var goodsResult = results[2] || {};

            if (relationResult.code !== 1) {
                layer.alert(relationResult.msg || '关联数据加载失败');
                return;
            }
            if (categoryResult.code !== 1) {
                layer.alert(categoryResult.msg || '分类数据加载失败');
                return;
            }
            if (goodsResult.code !== 1) {
                layer.alert(goodsResult.msg || '商品数据加载失败');
                return;
            }

            relationCache = relationResult.data || [];
            categoryCache = categoryResult.data || [];
            goodsCache = goodsResult.data || [];
            renderTable(relationCache);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function filterRelations() {
        var keyword = String($('#catechildKeyword').val() || '').trim().toLowerCase();
        if (!keyword) {
            renderTable(relationCache);
            return;
        }

        renderTable(relationCache.filter(function (item) {
            var goods = (getGoodsMap())[String(item.goodid)] || {};
            var categoryName = (getCategoryMap())[String(item.cateid)] || '';
            return [item.childname, categoryName, goods.name, item.goodid]
                .some(function (value) {
                    return String(value || '').toLowerCase().indexOf(keyword) > -1;
                });
        }));
    }

    function renderFormSelects() {
        var categoryOptions = categoryCache.map(function (item) {
            return '<option value="' + item.cateid + '">' + AdminApp.escapeHtml(item.catename) + '</option>';
        }).join('');
        var goodsOptions = goodsCache.map(function (item) {
            return '<option value="' + item.id + '">' + AdminApp.escapeHtml((item.name || '未命名商品') + '（ID:' + item.id + '）') + '</option>';
        }).join('');

        $('#cateSelect').html('<option value="">请选择分类</option>' + categoryOptions);
        $('#goodsSelect').html('<option value="">请选择商品</option>' + goodsOptions);
        form.render('select');
    }

    function initListPage() {
        if (!document.getElementById('catechildTable')) {
            return;
        }

        loadAllData();
        window.reloadPageData = loadAllData;

        $(document).on('click', '.preview-image', function () {
            AdminApp.openImagePreview($(this).data('src'));
        });

        form.on('submit(searchCatechild)', function () {
            filterRelations();
            return false;
        });

        $('#resetCatechildSearch').on('click', function () {
            $('#catechildKeyword').val('');
            renderTable(relationCache);
        });

        table.on('toolbar(catechildTableFilter)', function (obj) {
            if (obj.event === 'add') {
                AdminApp.openLayerPage('新增分类商品关联', 'add.html', ['720px', '420px']);
            }
        });

        table.on('tool(catechildTableFilter)', function (obj) {
            var row = obj.data || {};
            if (obj.event === 'edit') {
                AdminApp.openLayerPage('编辑分类商品关联', 'add.html?id=' + row.childid, ['720px', '420px']);
            }

            if (obj.event === 'delete') {
                layer.confirm('确认删除这条分类商品关联吗？', function (index) {
                    AdminApp.postJson('/manager/delCatechild', { childid: row.childid }).then(function (result) {
                        if (result.code !== 1) {
                            layer.alert(result.msg || '删除失败');
                            return;
                        }
                        layer.close(index);
                        layer.msg('删除成功');
                        loadAllData();
                    }).catch(function (xhr) {
                        AdminApp.handleHttpError(xhr);
                    });
                });
            }
        });
    }

    function initFormPage() {
        if (!document.getElementById('catechildForm')) {
            return;
        }

        var childId = getQueryId();

        Promise.all([
            AdminApp.postJson('/manager/getAllCategory', {}),
            AdminApp.postJson('/manager/getAllGoods', {}),
            childId ? AdminApp.postJson('/manager/getAllCatechild', {}) : Promise.resolve({ code: 1, data: [] })
        ]).then(function (results) {
            var categoryResult = results[0] || {};
            var goodsResult = results[1] || {};
            var relationResult = results[2] || {};

            if (categoryResult.code !== 1) {
                layer.alert(categoryResult.msg || '分类数据加载失败');
                return;
            }
            if (goodsResult.code !== 1) {
                layer.alert(goodsResult.msg || '商品数据加载失败');
                return;
            }

            categoryCache = categoryResult.data || [];
            goodsCache = goodsResult.data || [];
            renderFormSelects();

            if (childId) {
                var row = (relationResult.data || []).find(function (item) {
                    return Number(item.childid) === childId;
                });
                if (!row) {
                    layer.alert('未找到要编辑的关联数据');
                    return;
                }
                form.val('catechildForm', {
                    cateid: row.cateid,
                    goodid: row.goodid
                });
            }
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });

        form.on('submit(saveCatechild)', function (data) {
            var field = data.field || {};
            if (!field.cateid || !field.goodid) {
                layer.msg('请选择分类和商品');
                return false;
            }

            var requestPromise;
            if (childId) {
                requestPromise = AdminApp.postJson('/manager/editCatechild', {
                    childid: childId,
                    cateid: Number(field.cateid),
                    goodid: String(field.goodid)
                });
            } else {
                requestPromise = AdminApp.request({
                    url: '/manager/addCatechild?cateid=' + encodeURIComponent(field.cateid) + '&goodsid=' + encodeURIComponent(field.goodid),
                    method: 'POST',
                    data: '',
                    contentType: false
                });
            }

            requestPromise.then(function (result) {
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
