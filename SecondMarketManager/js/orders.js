layui.use(['form', 'table', 'layer', 'jquery'], function () {
    var form = layui.form;
    var table = layui.table;
    var layer = layui.layer;
    var $ = layui.jquery;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var ordersCache = [];
    var queryParams = new URLSearchParams(window.location.search);
    var detailOrderId = Number(queryParams.get('id') || 0);
    var hasSearchForm = $('#orderKeyword').length > 0 && $('#orderStateFilter').length > 0;
    var syncOrderTableTimer = 0;

    function getGoodsCover(goods) {
        if (!goods) {
            return '';
        }
        if (goods.icon) {
            return AdminApp.resolveFileUrl(goods.icon);
        }
        var images = AdminApp.splitImages(goods.imgs);
        return images.length ? AdminApp.resolveFileUrl(images[0]) : '';
    }

    function buildAddressText(address) {
        return [address.province, address.city, address.county, address.detail]
            .filter(function (value) {
                return !!value;
            })
            .join(' ') || '--';
    }

    function normalizeRating(value) {
        var rating = Number(value);
        return Number.isInteger(rating) && rating >= 1 && rating <= 5 ? rating : 0;
    }

    function buildRatingStars(rating) {
        if (rating <= 0) {
            return '';
        }
        return '★'.repeat(rating) + '☆'.repeat(5 - rating);
    }

    function buildRow(item) {
        var order = item || {};
        var goods = order.goods || {};
        var user = order.user || {};
        var address = order.address || {};
        var ratingNumber = normalizeRating(order.rating);
        var reviewContentText = String(order.reviewContent || '').trim();

        return $.extend({}, order, {
            goods: goods,
            user: user,
            address: address,
            goodsName: goods.name || '--',
            goodsCover: getGoodsCover(goods),
            buyerName: user.nickname || user.account || '--',
            buyerTel: user.tel || '--',
            addressText: buildAddressText(address),
            logisticsText: String(order.logistics || '').trim() || '--',
            ratingNumber: ratingNumber,
            ratingText: ratingNumber > 0 ? ratingNumber + '分' : '--',
            ratingStars: buildRatingStars(ratingNumber),
            reviewContentText: reviewContentText || '--'
        });
    }

    function buildRows(list) {
        return (Array.isArray(list) ? list : []).map(buildRow);
    }

    function buildReviewSummary(row) {
        var parts = [];
        if (row.ratingNumber > 0) {
            parts.push(row.ratingStars + ' ' + row.ratingText);
        }
        if (row.reviewContentText !== '--') {
            parts.push(row.reviewContentText);
        }
        return parts.length ? parts.join(' | ') : '暂无评价';
    }

    function getOrdersTableView() {
        return $('#ordersTable').next('.layui-table-view');
    }

    function syncFixedActionColumnHeights() {
        var tableView = getOrdersTableView();
        var mainRows;
        var rightRows;

        if (!tableView.length || tableView.find('.layui-table-fixed-r').hasClass('layui-hide')) {
            return;
        }

        mainRows = tableView.find('.layui-table-main tbody tr');
        rightRows = tableView.find('.layui-table-fixed-r tbody tr');

        if (!mainRows.length || !rightRows.length) {
            return;
        }

        mainRows.css('height', '');
        rightRows.css('height', '');

        mainRows.each(function (index) {
            var mainRow = $(this);
            var rightRow = rightRows.eq(index);
            var rowHeight;

            if (!rightRow.length) {
                return;
            }

            rowHeight = Math.max(mainRow.outerHeight(), rightRow.outerHeight());
            mainRow.css('height', rowHeight + 'px');
            rightRow.css('height', rowHeight + 'px');
        });
    }

    function scheduleOrderTableLayoutSync(delay) {
        window.clearTimeout(syncOrderTableTimer);
        syncOrderTableTimer = window.setTimeout(function () {
            table.resize('ordersTableReload');
            syncFixedActionColumnHeights();
        }, typeof delay === 'number' ? delay : 80);
    }

    function bindOrderTableImageLayoutSync() {
        var tableView = getOrdersTableView();

        if (!tableView.length) {
            return;
        }

        tableView.find('.preview-image').each(function () {
            var image = this;

            if (image.complete) {
                return;
            }

            $(image).one('load error', function () {
                scheduleOrderTableLayoutSync(0);
            });
        });
    }

    function renderTable(list) {
        table.render({
            elem: '#ordersTable',
            id: 'ordersTableReload',
            data: buildRows(list),
            page: true,
            limit: 15,
            skin: 'line',
            done: function () {
                bindOrderTableImageLayoutSync();
                scheduleOrderTableLayoutSync();
            },
            cols: [[
                { field: 'id', title: '订单ID', width: 90, sort: true },
                {
                    field: 'goodsCover',
                    title: '商品封面',
                    width: 120,
                    templet: function (row) {
                        if (!row.goodsCover) {
                            return '<div class="order-cover-cell order-cover-cell-empty"><span class="table-empty">无</span></div>';
                        }
                        return '<div class="order-cover-cell"><img class="table-thumb preview-image" data-src="' + AdminApp.escapeHtml(row.goodsCover) + '" src="' + AdminApp.escapeHtml(row.goodsCover) + '" alt="goods"></div>';
                    }
                },
                { field: 'goodsName', title: '商品名称', minWidth: 160 },
                { field: 'buyerName', title: '买家', minWidth: 120 },
                { field: 'buyerTel', title: '联系电话', minWidth: 130 },
                {
                    field: 'price',
                    title: '支付金额',
                    width: 110,
                    templet: function (row) {
                        return '¥' + AdminApp.formatMoney(row.price);
                    }
                },
                { field: 'number', title: '数量', width: 80 },
                {
                    field: 'state',
                    title: '状态',
                    width: 100,
                    templet: function (row) {
                        return AdminApp.orderStateText(row.state);
                    }
                },
                {
                    field: 'ratingText',
                    title: '评分',
                    width: 120,
                    templet: function (row) {
                        if (row.ratingNumber <= 0) {
                            return '<span class="table-empty">未评价</span>';
                        }
                        return '<span>' + AdminApp.escapeHtml(row.ratingText) + '</span>';
                    }
                },
                {
                    field: 'reviewContentText',
                    title: '订单评价',
                    minWidth: 220,
                    templet: function (row) {
                        return '<div class="table-text-2">' + AdminApp.escapeHtml(buildReviewSummary(row)) + '</div>';
                    }
                },
                {
                    field: 'logisticsText',
                    title: '物流单号',
                    minWidth: 150,
                    templet: function (row) {
                        return AdminApp.escapeHtml(row.logisticsText);
                    }
                },
                {
                    field: 'sendTime',
                    title: '下单时间',
                    minWidth: 170,
                    templet: function (row) {
                        return AdminApp.formatDate(row.sendTime);
                    }
                },
                { fixed: 'right', title: '操作', width: 190, toolbar: '#ordersBar' }
            ]]
        });
    }

    function getFilteredOrders() {
        var keyword = String($('#orderKeyword').val() || '').trim().toLowerCase();
        var state = String($('#orderStateFilter').val() || '').trim();
        return ordersCache.filter(function (item) {
            var goods = item.goods || {};
            var user = item.user || {};
            var matchKeyword = !keyword || [
                item.id,
                goods.name,
                user.nickname,
                user.account,
                user.tel,
                item.logistics,
                item.reviewContent
            ].some(function (value) {
                return String(value || '').toLowerCase().indexOf(keyword) > -1;
            });
            var matchState = !state || String(item.state || '') === state;
            return matchKeyword && matchState;
        });
    }

    function openOrderInfoPage(row) {
        if (!row || !row.id) {
            return;
        }

        var href = '../orders/info.html?id=' + encodeURIComponent(row.id);
        if (window !== parent && parent.layui && parent.layui.miniTab && typeof parent.layui.miniTab.openNewTabByIframe === 'function') {
            parent.layui.miniTab.openNewTabByIframe({
                href: href,
                title: '订单信息'
            });
            return;
        }

        window.location.href = href;
    }

    function isNoOrdersResult(result) {
        if (!result || result.code === 1) {
            return false;
        }
        var message = String(result.msg || '').trim().toLowerCase();
        return message === 'no orders found';
    }

    function loadOrders() {
        AdminApp.postJson('/orders/getAllOrder', {}).then(function (result) {
            if (result.code === 1) {
                ordersCache = Array.isArray(result.data) ? result.data : [];
            } else if (isNoOrdersResult(result)) {
                ordersCache = [];
            } else {
                layer.alert(result.msg || '订单数据加载失败');
                return;
            }

            if (hasSearchForm) {
                filterOrders();
            } else {
                renderTable(ordersCache);
            }

            if (detailOrderId > 0) {
                showDetailById(detailOrderId);
            }
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function filterOrders() {
        renderTable(getFilteredOrders());
    }

    function showOrderDetail(row) {
        var detail = buildRow(row);
        var goods = detail.goods || {};
        var user = detail.user || {};
        var address = detail.address || {};
        var reviewSection = detail.ratingNumber > 0 || detail.reviewContentText !== '--'
            ? [
                '<div><strong>订单评分：</strong>' + AdminApp.escapeHtml(detail.ratingStars || detail.ratingText) + (detail.ratingStars ? '（' + AdminApp.escapeHtml(detail.ratingText) + '）' : '') + '</div>',
                '<div style="margin-top: 8px;"><strong>订单评价：</strong><br>' + AdminApp.escapeHtml(detail.reviewContentText) + '</div>'
            ].join('')
            : '<div><strong>订单评价：</strong>暂无评价</div>';

        var goodsCoverHtml = detail.goodsCover
            ? '<div style="margin-bottom: 14px;"><img class="preview-image" data-src="' + AdminApp.escapeHtml(detail.goodsCover) + '" src="' + AdminApp.escapeHtml(detail.goodsCover) + '" alt="goods" style="width: 84px; height: 84px; object-fit: contain; border-radius: 12px; border: 1px solid #e5e7eb; background: #ffffff;"></div>'
            : '';

        var content = [
            '<div style="padding: 18px 20px; line-height: 1.9;">',
            goodsCoverHtml,
            '<div><strong>订单ID：</strong>' + AdminApp.escapeHtml(detail.id || '--') + '</div>',
            '<div><strong>商品名称：</strong>' + AdminApp.escapeHtml(goods.name || '--') + '</div>',
            '<div><strong>买家：</strong>' + AdminApp.escapeHtml(detail.buyerName) + '</div>',
            '<div><strong>联系电话：</strong>' + AdminApp.escapeHtml(detail.buyerTel) + '</div>',
            '<div><strong>订单状态：</strong>' + AdminApp.escapeHtml(AdminApp.orderStateText(detail.state)) + '</div>',
            '<div><strong>支付金额：</strong>¥' + AdminApp.formatMoney(detail.price) + '</div>',
            '<div><strong>购买数量：</strong>' + AdminApp.escapeHtml(detail.number || 0) + '</div>',
            '<div><strong>物流单号：</strong>' + AdminApp.escapeHtml(detail.logisticsText) + '</div>',
            '<div><strong>收货人：</strong>' + AdminApp.escapeHtml(address.name || '--') + '</div>',
            '<div><strong>收货电话：</strong>' + AdminApp.escapeHtml(address.tel || '--') + '</div>',
            '<div><strong>收货地址：</strong>' + AdminApp.escapeHtml(detail.addressText) + '</div>',
            '<div><strong>下单时间：</strong>' + AdminApp.escapeHtml(AdminApp.formatDate(detail.sendTime)) + '</div>',
            '<hr style="margin: 14px 0; border: none; border-top: 1px solid #eef2f7;">',
            reviewSection,
            '</div>'
        ].join('');

        layer.open({
            type: 1,
            title: '订单详情',
            area: ['760px', '640px'],
            shadeClose: true,
            content: content
        });
    }

    function showDetailById(orderId) {
        var list = hasSearchForm ? getFilteredOrders() : ordersCache;
        var matched = list.find(function (item) {
            return Number(item.id) === Number(orderId);
        }) || ordersCache.find(function (item) {
            return Number(item.id) === Number(orderId);
        });

        if (!matched) {
            layer.msg('未找到对应订单');
            return;
        }

        showOrderDetail(matched);
    }

    loadOrders();
    window.reloadPageData = loadOrders;

    $(document).on('click', '.preview-image', function () {
        AdminApp.openImagePreview($(this).data('src'));
    });

    $(window).off('resize.ordersTableLayout').on('resize.ordersTableLayout', function () {
        scheduleOrderTableLayoutSync();
    });

    if (hasSearchForm) {
        form.on('submit(searchOrders)', function () {
            filterOrders();
            return false;
        });

        $('#resetOrdersSearch').on('click', function () {
            $('#orderKeyword').val('');
            $('#orderStateFilter').val('');
            form.render('select');
            renderTable(ordersCache);
        });
    }

    table.on('tool(ordersTableFilter)', function (obj) {
        var row = obj.data || {};

        if (obj.event === 'view') {
            if (hasSearchForm) {
                showOrderDetail(row);
            } else {
                openOrderInfoPage(row);
            }
        }

        if (obj.event === 'delete') {
            layer.confirm('确认删除这笔订单吗？', function (index) {
                AdminApp.request({
                    url: '/orders/delOrder?id=' + encodeURIComponent(row.id),
                    method: 'POST',
                    data: '',
                    contentType: false
                }).then(function (result) {
                    if (result.code !== 1) {
                        layer.alert(result.msg || '删除失败');
                        return;
                    }
                    layer.close(index);
                    layer.msg('删除成功');
                    loadOrders();
                }).catch(function (xhr) {
                    AdminApp.handleHttpError(xhr);
                });
            });
        }
    });
});
