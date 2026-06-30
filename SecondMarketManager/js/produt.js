layui.use(['form', 'table', 'layer', 'jquery'], function () {
    var form = layui.form;
    var table = layui.table;
    var layer = layui.layer;
    var $ = layui.jquery;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var goodsCache = [];
    var userCache = [];
    var categoryCache = [];
    var relationCache = [];

    function getUserMap() {
        return userCache.reduce(function (map, item) {
            map[String(item.id)] = item;
            return map;
        }, {});
    }

    function getCategoryMap() {
        return categoryCache.reduce(function (map, item) {
            map[String(item.cateid)] = item;
            return map;
        }, {});
    }

    function getRelationMap() {
        return relationCache.reduce(function (map, item) {
            map[String(item.goodid)] = item;
            return map;
        }, {});
    }

    function getGoodsCover(row) {
        if (row.icon) {
            return AdminApp.resolveFileUrl(row.icon);
        }
        var images = AdminApp.splitImages(row.imgs);
        return images.length ? AdminApp.resolveFileUrl(images[0]) : '';
    }

    function resolveSellerName(user, item) {
        return user.nickname || user.account || ('\u7528\u6237 ' + (item.sendUser || '--'));
    }

    function resolveSellerContact(user, item) {
        var sellerTel = String(item && item.sellerTel || '').trim();
        if (sellerTel) {
            return sellerTel;
        }
        var userTel = String(user && user.tel || '').trim();
        if (userTel) {
            return userTel;
        }
        return String(item && item.contactWay || '').trim();
    }

    function resolveGoodsContact(user, item) {
        var goodsContact = String(item && item.contactWay || '').trim();
        if (goodsContact) {
            return goodsContact;
        }
        return String(user && user.tel || '').trim();
    }

    function buildGoodsActions(row) {
        var manage = String(row && row.manage || '');
        var actions = [
            '<a class="layui-btn layui-btn-xs layui-btn-primary" lay-event="view">\u8be6\u60c5</a>'
        ];

        if (manage === '0') {
            actions.push('<a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="approve">\u901a\u8fc7</a>');
            actions.push('<a class="layui-btn layui-btn-xs layui-btn-warm" lay-event="reject">\u9a73\u56de</a>');
        } else if (manage === '1') {
            actions.push('<a class="layui-btn layui-btn-xs layui-btn-warm" lay-event="reject">\u9a73\u56de</a>');
            actions.push('<a class="layui-btn layui-btn-xs" lay-event="soldout">\u4e0b\u67b6</a>');
        }
        actions.push('<a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete">\u5220\u9664</a>');

        return actions.join('');
    }

    function buildGoodsRows(list) {
        var userMap = getUserMap();
        var categoryMap = getCategoryMap();
        var relationMap = getRelationMap();

        return (list || []).map(function (item) {
            var user = userMap[String(item.sendUser)] || {};
            var relation = relationMap[String(item.id)] || {};
            var category = categoryMap[String(relation.cateid)] || {};

            return $.extend({}, item, {
                cover: getGoodsCover(item),
                sellerName: resolveSellerName(user, item),
                sellerContact: resolveSellerContact(user, item) || '--',
                contactDisplay: resolveSellerContact(user, item) || resolveGoodsContact(user, item) || '--',
                goodsContact: resolveGoodsContact(user, item) || '--',
                categoryName: category.catename || '--',
                categoryId: relation.cateid || '',
                goodsImages: AdminApp.splitImages(item.imgs)
            });
        });
    }

    function renderTable(list) {
        table.render({
            elem: '#goodsTable',
            id: 'goodsTableReload',
            data: buildGoodsRows(list),
            page: true,
            limit: 15,
            skin: 'line',
            cols: [[
                { field: 'id', title: 'ID', width: 80, sort: true },
                {
                    field: 'cover',
                    title: '\u5c01\u9762',
                    width: 100,
                    templet: function (row) {
                        if (!row.cover) {
                            return '<div class="goods-cover-cell goods-cover-cell-empty"><span class="table-empty">\u65e0</span></div>';
                        }
                        return '<div class="goods-cover-cell"><img class="table-thumb preview-image" data-src="' + AdminApp.escapeHtml(row.cover) + '" src="' + AdminApp.escapeHtml(row.cover) + '" alt="goods"></div>';
                    }
                },
                { field: 'name', title: '\u5546\u54c1\u540d\u79f0', minWidth: 160, edit: 'text' },
                { field: 'sellerName', title: '\u53d1\u5e03\u4eba', minWidth: 120 },
                { field: 'categoryName', title: '\u5206\u7c7b', width: 130 },
                {
                    field: 'price',
                    title: '\u4ef7\u683c',
                    width: 100,
                    edit: 'text',
                    templet: function (row) {
                        return '\u00a5' + AdminApp.formatMoney(row.price);
                    }
                },
                { field: 'number', title: '\u5e93\u5b58', width: 90, edit: 'text' },
                { field: 'status', title: '\u6210\u8272', width: 100, edit: 'text' },
                { field: 'dealtypy', title: '\u4ea4\u6613\u65b9\u5f0f', minWidth: 150, edit: 'text' },
                { field: 'contactDisplay', title: '\u8054\u7cfb\u65b9\u5f0f', minWidth: 150 },
                {
                    field: 'manage',
                    title: '\u5ba1\u6838\u72b6\u6001',
                    width: 110,
                    templet: function (row) {
                        return AdminApp.goodsStatusText(row.manage);
                    }
                },
                {
                    field: 'updateTime',
                    title: '\u66f4\u65b0\u65f6\u95f4',
                    minWidth: 170,
                    templet: function (row) {
                        return AdminApp.formatDate(row.updateTime || row.sendTime);
                    }
                },
                {
                    field: 'describes',
                    title: '\u63cf\u8ff0',
                    minWidth: 220,
                    edit: 'text',
                    templet: function (row) {
                        return '<div class="table-text-2">' + AdminApp.escapeHtml(row.describes || '--') + '</div>';
                    }
                },
                {
                    fixed: 'right',
                    title: '\u64cd\u4f5c',
                    width: 260,
                    templet: function (row) {
                        return buildGoodsActions(row);
                    }
                }
            ]]
        });
    }

    function renderCategoryFilter() {
        var options = ['<option value="">\u5168\u90e8\u5206\u7c7b</option>'].concat(categoryCache.map(function (item) {
            return '<option value="' + item.cateid + '">' + AdminApp.escapeHtml(item.catename) + '</option>';
        }));
        $('#goodsCategoryFilter').html(options.join(''));
        form.render('select');
    }

    function loadGoodsData() {
        Promise.all([
            AdminApp.postJson('/manager/getAllGoods', {}),
            AdminApp.postJson('/manager/getAllUser', {}),
            AdminApp.postJson('/manager/getAllCategory', {}),
            AdminApp.postJson('/manager/getAllCatechild', {})
        ]).then(function (results) {
            var goodsResult = results[0] || {};
            var userResult = results[1] || {};
            var categoryResult = results[2] || {};
            var relationResult = results[3] || {};

            if (goodsResult.code !== 1) {
                layer.alert(goodsResult.msg || '\u5546\u54c1\u6570\u636e\u52a0\u8f7d\u5931\u8d25');
                return;
            }
            if (userResult.code !== 1) {
                layer.alert(userResult.msg || '\u7528\u6237\u6570\u636e\u52a0\u8f7d\u5931\u8d25');
                return;
            }
            if (categoryResult.code !== 1) {
                layer.alert(categoryResult.msg || '\u5206\u7c7b\u6570\u636e\u52a0\u8f7d\u5931\u8d25');
                return;
            }
            if (relationResult.code !== 1) {
                layer.alert(relationResult.msg || '\u5206\u7c7b\u5173\u8054\u6570\u636e\u52a0\u8f7d\u5931\u8d25');
                return;
            }

            goodsCache = goodsResult.data || [];
            userCache = userResult.data || [];
            categoryCache = categoryResult.data || [];
            relationCache = relationResult.data || [];
            renderCategoryFilter();
            renderTable(goodsCache);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function getFilteredGoods() {
        var keyword = String($('#goodsKeyword').val() || '').trim().toLowerCase();
        var manage = String($('#goodsManageFilter').val() || '').trim();
        var categoryId = String($('#goodsCategoryFilter').val() || '').trim();
        var relationMap = getRelationMap();
        var userMap = getUserMap();

        return goodsCache.filter(function (item) {
            var relation = relationMap[String(item.id)] || {};
            var user = userMap[String(item.sendUser)] || {};
            var matchKeyword = !keyword || [
                item.name,
                item.status,
                item.dealtypy,
                item.describes,
                item.contactWay,
                user.tel,
                user.account,
                user.nickname
            ].some(function (value) {
                return String(value || '').toLowerCase().indexOf(keyword) > -1;
            });
            var matchManage = !manage || String(item.manage || '') === manage;
            var matchCategory = !categoryId || String(relation.cateid || '') === categoryId;
            return matchKeyword && matchManage && matchCategory;
        });
    }

    function filterGoods() {
        renderTable(getFilteredGoods());
    }

    function saveGoodsField(row, field, value) {
        var payload = { id: row.id };
        payload[field === 'goodsContact' ? 'contactWay' : field] = value;
        return AdminApp.postJson('/manager/editGoods', payload);
    }

    function updateGoodsManage(id, manage) {
        return AdminApp.request({
            url: '/manager/checkGoods?id=' + encodeURIComponent(id) + '&manage=' + encodeURIComponent(manage),
            method: 'POST',
            data: '',
            contentType: false
        });
    }

    function showGoodsDetail(row) {
        var imagesHtml = (row.goodsImages || []).map(function (item) {
            var imageUrl = AdminApp.resolveFileUrl(item);
            return imageUrl
                ? '<img src="' + AdminApp.escapeHtml(imageUrl) + '" data-src="' + AdminApp.escapeHtml(imageUrl) + '">'
                : '';
        }).join('');

        var content = [
            '<div style="padding: 18px 20px; line-height: 1.9;">',
            '<div><strong>\u5546\u54c1\u540d\u79f0\uff1a</strong>' + AdminApp.escapeHtml(row.name || '--') + '</div>',
            '<div><strong>\u53d1\u5e03\u4eba\uff1a</strong>' + AdminApp.escapeHtml(row.sellerName || '--') + '</div>',
            '<div><strong>\u53d1\u5e03\u4eba\u8054\u7cfb\u65b9\u5f0f\uff1a</strong>' + AdminApp.escapeHtml(row.sellerContact || '--') + '</div>',
            '<div><strong>\u5206\u7c7b\uff1a</strong>' + AdminApp.escapeHtml(row.categoryName || '--') + '</div>',
            '<div><strong>\u4ef7\u683c\uff1a</strong>\u00a5' + AdminApp.formatMoney(row.price) + '</div>',
            '<div><strong>\u5e93\u5b58\uff1a</strong>' + AdminApp.escapeHtml(row.number || 0) + '</div>',
            '<div><strong>\u6210\u8272\uff1a</strong>' + AdminApp.escapeHtml(row.status || '--') + '</div>',
            '<div><strong>\u4ea4\u6613\u65b9\u5f0f\uff1a</strong>' + AdminApp.escapeHtml(row.dealtypy || '--') + '</div>',
            '<div><strong>\u5546\u54c1\u8054\u7cfb\u65b9\u5f0f\uff1a</strong>' + AdminApp.escapeHtml(row.goodsContact || '--') + '</div>',
            '<div><strong>\u5ba1\u6838\u72b6\u6001\uff1a</strong>' + AdminApp.escapeHtml(AdminApp.goodsStatusText(row.manage)) + '</div>',
            '<div style="margin-top: 8px;"><strong>\u63cf\u8ff0\uff1a</strong><br>' + AdminApp.escapeHtml(row.describes || '--') + '</div>',
            imagesHtml ? '<div class="goods-images-preview">' + imagesHtml + '</div>' : '',
            '</div>'
        ].join('');

        layer.open({
            type: 1,
            title: '\u5546\u54c1\u8be6\u60c5',
            area: ['720px', '560px'],
            shadeClose: true,
            content: content
        });
    }

    loadGoodsData();
    window.reloadPageData = loadGoodsData;

    $(document).on('click', '.preview-image', function () {
        AdminApp.openImagePreview($(this).data('src'));
    });

    $(document).on('click', '.goods-images-preview img', function () {
        AdminApp.openImagePreview($(this).data('src') || $(this).attr('src'));
    });

    form.on('submit(searchGoods)', function () {
        filterGoods();
        return false;
    });

    $('#resetGoodsSearch').on('click', function () {
        $('#goodsKeyword').val('');
        $('#goodsManageFilter').val('');
        $('#goodsCategoryFilter').val('');
        form.render('select');
        renderTable(goodsCache);
    });

    table.on('edit(goodsTableFilter)', function (obj) {
        var field = obj.field;
        var value = String(obj.value == null ? '' : obj.value).trim();

        if (field === 'price') {
            if (!/^\d+(\.\d{1,2})?$/.test(value)) {
                layer.msg('\u4ef7\u683c\u683c\u5f0f\u4e0d\u6b63\u786e');
                loadGoodsData();
                return;
            }
            value = Number(value);
        }

        if (field === 'number') {
            if (!/^\d+$/.test(value)) {
                layer.msg('\u5e93\u5b58\u5fc5\u987b\u662f\u975e\u8d1f\u6574\u6570');
                loadGoodsData();
                return;
            }
            value = Number(value);
        }

        saveGoodsField(obj.data, field, value).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || '\u4fdd\u5b58\u5931\u8d25');
                loadGoodsData();
                return;
            }
            layer.msg('\u4fdd\u5b58\u6210\u529f');
            loadGoodsData();
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
            loadGoodsData();
        });
    });

    table.on('tool(goodsTableFilter)', function (obj) {
        var row = obj.data || {};

        if (obj.event === 'view') {
            showGoodsDetail(row);
        }

        if (obj.event === 'approve') {
            if (String(row.manage || '') !== '0') {
                layer.msg('\u53ea\u6709\u5f85\u5ba1\u6838\u5546\u54c1\u624d\u80fd\u901a\u8fc7');
                return;
            }
            updateGoodsManage(row.id, '1').then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || '\u5ba1\u6838\u901a\u8fc7\u5931\u8d25');
                    return;
                }
                layer.msg('\u5df2\u5ba1\u6838\u901a\u8fc7');
                loadGoodsData();
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
        }

        if (obj.event === 'reject') {
            if (['0', '1'].indexOf(String(row.manage || '')) === -1) {
                layer.msg('\u53ea\u6709\u5f85\u5ba1\u6838\u6216\u5df2\u4e0a\u67b6\u5546\u54c1\u624d\u80fd\u9a73\u56de');
                return;
            }
            updateGoodsManage(row.id, '2').then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || '\u9a73\u56de\u5931\u8d25');
                    return;
                }
                layer.msg('\u5df2\u9a73\u56de');
                loadGoodsData();
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
        }

        if (obj.event === 'soldout') {
            if (String(row.manage || '') !== '1') {
                layer.msg('\u53ea\u6709\u5df2\u4e0a\u67b6\u5546\u54c1\u624d\u80fd\u4e0b\u67b6');
                return;
            }
            updateGoodsManage(row.id, '3').then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || '\u4e0b\u67b6\u5931\u8d25');
                    return;
                }
                layer.msg('\u5df2\u4e0b\u67b6');
                loadGoodsData();
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
        }

        if (obj.event === 'delete') {
            layer.confirm('\u786e\u8ba4\u5220\u9664\u8be5\u5546\u54c1\u5417\uff1f', function (index) {
                AdminApp.postJson('/manager/delGoods', { id: row.id }).then(function (result) {
                    if (result.code !== 1) {
                        layer.alert(result.msg || '\u5220\u9664\u5931\u8d25');
                        return;
                    }
                    layer.close(index);
                    layer.msg('\u5220\u9664\u6210\u529f');
                    loadGoodsData();
                }).catch(function (xhr) {
                    AdminApp.handleHttpError(xhr);
                });
            });
        }
    });
});
