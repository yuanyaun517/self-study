layui.use(['form', 'table', 'layer', 'jquery'], function () {
    var form = layui.form;
    var table = layui.table;
    var layer = layui.layer;
    var $ = layui.jquery;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var categoriesCache = [];
    var relationCache = [];

    function getQueryId() {
        return Number(new URLSearchParams(window.location.search).get('id') || 0);
    }

    function getRelationCount(cateid) {
        return relationCache.filter(function (item) {
            return Number(item.cateid) === Number(cateid);
        }).length;
    }

    function renderTable(list) {
        table.render({
            elem: '#categoryTable',
            id: 'categoryTableReload',
            data: list.map(function (item) {
                return {
                    cateid: item.cateid,
                    catename: item.catename,
                    relationCount: getRelationCount(item.cateid)
                };
            }),
            page: true,
            limit: 15,
            skin: 'line',
            cols: [[
                { field: 'cateid', title: '分类ID', width: 100, sort: true },
                { field: 'catename', title: '分类名称', minWidth: 220 },
                { field: 'relationCount', title: '关联商品数', width: 130, sort: true },
                { fixed: 'right', title: '操作', width: 180, toolbar: '#categoryBar' }
            ]]
        });
    }

    function loadCategories() {
        Promise.all([
            AdminApp.postJson('/manager/getAllCategory', {}),
            AdminApp.postJson('/manager/getAllCatechild', {})
        ]).then(function (results) {
            var categoryResult = results[0] || {};
            var relationResult = results[1] || {};
            if (categoryResult.code !== 1) {
                layer.alert(categoryResult.msg || '分类数据加载失败');
                return;
            }
            if (relationResult.code !== 1) {
                layer.alert(relationResult.msg || '分类关联数据加载失败');
                return;
            }
            categoriesCache = categoryResult.data || [];
            relationCache = relationResult.data || [];
            renderTable(categoriesCache);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function filterCategories() {
        var keyword = String($('#categoryKeyword').val() || '').trim().toLowerCase();
        if (!keyword) {
            renderTable(categoriesCache);
            return;
        }

        renderTable(categoriesCache.filter(function (item) {
            return String(item.catename || '').toLowerCase().indexOf(keyword) > -1;
        }));
    }

    function fillForm(row) {
        form.val('categoryForm', {
            catename: row.catename || ''
        });
    }

    function initListPage() {
        if (!document.getElementById('categoryTable')) {
            return;
        }

        loadCategories();
        window.reloadPageData = loadCategories;

        form.on('submit(searchCategory)', function () {
            filterCategories();
            return false;
        });

        $('#resetCategorySearch').on('click', function () {
            $('#categoryKeyword').val('');
            renderTable(categoriesCache);
        });

        table.on('tool(categoryTableFilter)', function (obj) {
            var row = obj.data || {};
            if (obj.event === 'edit') {
                AdminApp.openLayerPage('编辑分类', 'edit.html?id=' + row.cateid, ['620px', '300px']);
            }

            if (obj.event === 'delete') {
                layer.confirm('确认删除该分类吗？删除后会清理分类商品关联。', function (index) {
                    AdminApp.postJson('/manager/delCategory', { cateid: row.cateid }).then(function (result) {
                        if (result.code !== 1) {
                            layer.alert(result.msg || '删除失败');
                            return;
                        }
                        layer.close(index);
                        layer.msg('删除成功');
                        loadCategories();
                    }).catch(function (xhr) {
                        AdminApp.handleHttpError(xhr);
                    });
                });
            }
        });
    }

    function initFormPage() {
        if (!document.getElementById('categoryForm')) {
            return;
        }

        var categoryId = getQueryId();
        if (categoryId) {
            AdminApp.postJson('/manager/getAllCategory', {}).then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || '分类数据加载失败');
                    return;
                }
                var row = (result.data || []).find(function (item) {
                    return Number(item.cateid) === categoryId;
                });
                if (!row) {
                    layer.alert('未找到要编辑的分类');
                    return;
                }
                fillForm(row);
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
        }

        form.on('submit(saveCategory)', function (data) {
            var payload = {
                catename: (data.field.catename || '').trim()
            };
            if (!payload.catename) {
                layer.msg('分类名称不能为空');
                return false;
            }

            var url = '/manager/addCategory';
            if (categoryId) {
                payload.cateid = categoryId;
                url = '/manager/editCategory';
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
