layui.use(["jquery", "layer", "echarts", "miniTab"], function () {
    var $ = layui.jquery;
    var layer = layui.layer;
    var echarts = layui.echarts;
    var miniTab = layui.miniTab;
    var charts = [];

    if (!AdminApp.ensureLogin()) {
        return;
    }

    function safeNumber(value) {
        var amount = Number(value);
        return Number.isFinite(amount) ? amount : 0;
    }

    function formatInteger(value) {
        return String(Math.round(safeNumber(value)));
    }

    function formatMoney(value) {
        return "¥" + safeNumber(value).toFixed(2);
    }

    function sumNumbers(values) {
        return (values || []).reduce(function (total, item) {
            return total + safeNumber(item);
        }, 0);
    }

    function setText(id, value) {
        $("#" + id).text(value);
    }

    function createStatusChip(label, value, tone) {
        var toneClass = tone ? " status-chip-" + tone : "";
        return [
            '<span class="status-chip' + toneClass + '">',
            "  <em>" + AdminApp.escapeHtml(label) + "</em>",
            "  <strong>" + AdminApp.escapeHtml(String(value)) + "</strong>",
            "</span>"
        ].join("");
    }

    function renderStatusChips(containerId, items) {
        var html = (items || []).map(function (item) {
            return createStatusChip(item.label, item.value, item.tone);
        });
        $("#" + containerId).html(html.join(""));
    }

    function initChart(id) {
        var element = document.getElementById(id);
        if (!element) {
            return null;
        }
        var chart = echarts.init(element);
        charts.push(chart);
        return chart;
    }

    function renderTrendChart(data) {
        var chart = initChart("trendChart");
        if (!chart) {
            return;
        }

        chart.setOption({
            color: ["#2d8cf0", "#4ec9b0", "#f5a623", "#7a8aa0"],
            tooltip: {
                trigger: "axis"
            },
            legend: {
                top: 0,
                itemWidth: 10,
                itemHeight: 10,
                textStyle: {
                    color: "#5f6b7a"
                }
            },
            grid: {
                left: 10,
                right: 18,
                top: 46,
                bottom: 10,
                containLabel: true
            },
            xAxis: {
                type: "category",
                boundaryGap: false,
                data: data.trendDates || [],
                axisLine: {
                    lineStyle: {
                        color: "#d7e2ee"
                    }
                },
                axisLabel: {
                    color: "#7a8796"
                }
            },
            yAxis: [
                {
                    type: "value",
                    name: "数量",
                    splitLine: {
                        lineStyle: {
                            color: "#eef3f8"
                        }
                    },
                    axisLabel: {
                        color: "#7a8796"
                    }
                },
                {
                    type: "value",
                    name: "金额",
                    splitLine: {
                        show: false
                    },
                    axisLabel: {
                        color: "#7a8796"
                    }
                }
            ],
            series: [
                {
                    name: "商品",
                    type: "line",
                    smooth: true,
                    symbol: "circle",
                    symbolSize: 7,
                    lineStyle: {
                        width: 3
                    },
                    areaStyle: {
                        color: "rgba(45, 140, 240, 0.10)"
                    },
                    data: data.goodsTrend || []
                },
                {
                    name: "帖子",
                    type: "line",
                    smooth: true,
                    symbol: "circle",
                    symbolSize: 7,
                    lineStyle: {
                        width: 3
                    },
                    areaStyle: {
                        color: "rgba(78, 201, 176, 0.10)"
                    },
                    data: data.forumTrend || []
                },
                {
                    name: "订单",
                    type: "line",
                    smooth: true,
                    symbol: "circle",
                    symbolSize: 7,
                    lineStyle: {
                        width: 3
                    },
                    data: data.orderTrend || []
                },
                {
                    name: "成交额",
                    type: "bar",
                    yAxisIndex: 1,
                    barWidth: 16,
                    itemStyle: {
                        borderRadius: [6, 6, 0, 0],
                        color: "#7a8aa0"
                    },
                    data: data.paidAmountTrend || []
                }
            ]
        });
    }

    function renderOrderChart(data) {
        var chart = initChart("orderChart");
        if (!chart) {
            return;
        }

        chart.setOption({
            color: ["#2d8cf0", "#4ec9b0", "#f5a623", "#6c7a89", "#d9e2ec"],
            tooltip: {
                trigger: "item"
            },
            legend: {
                bottom: 0,
                itemWidth: 10,
                itemHeight: 10,
                textStyle: {
                    color: "#5f6b7a"
                }
            },
            series: [
                {
                    name: "订单状态",
                    type: "pie",
                    radius: ["48%", "72%"],
                    center: ["50%", "42%"],
                    avoidLabelOverlap: true,
                    itemStyle: {
                        borderColor: "#ffffff",
                        borderWidth: 4
                    },
                    label: {
                        color: "#5f6b7a",
                        formatter: "{b}\n{c}"
                    },
                    labelLine: {
                        length: 10,
                        length2: 8
                    },
                    data: [
                        { value: safeNumber(data.unpaidOrderCount), name: "待支付" },
                        { value: safeNumber(data.paidOrderCount), name: "待发货" },
                        { value: safeNumber(data.shippingOrderCount), name: "待收货" },
                        { value: safeNumber(data.completedOrderCount), name: "已完成" },
                        { value: safeNumber(data.cancelledOrderCount), name: "已取消" }
                    ]
                }
            ]
        });
    }

    function renderCharts(data) {
        renderTrendChart(data);
        renderOrderChart(data);
    }

    function renderOverview(data) {
        setText("goodsStatusSummary", "通过 " + formatInteger(data.goodsApprovedCount) + " / 待审核 " + formatInteger(data.goodsPendingCount));
        setText("forumStatusSummary", "通过 " + formatInteger(data.forumApprovedCount) + " / 待审核 " + formatInteger(data.forumPendingCount));
        setText("orderProgressSummary", "待支付 " + formatInteger(data.unpaidOrderCount) + " / 已完成 " + formatInteger(data.completedOrderCount));
        setText("contentSummary", "分类 " + formatInteger(data.categoryCount) + " / 公告 " + formatInteger(data.noticeCount));

        renderStatusChips("goodsStatusChips", [
            { label: "已通过", value: formatInteger(data.goodsApprovedCount), tone: "primary" },
            { label: "待审核", value: formatInteger(data.goodsPendingCount), tone: "warning" },
            { label: "已驳回", value: formatInteger(data.goodsRejectedCount), tone: "danger" },
            { label: "已下架", value: formatInteger(data.goodsSoldOutCount), tone: "muted" }
        ]);

        renderStatusChips("forumStatusChips", [
            { label: "已通过", value: formatInteger(data.forumApprovedCount), tone: "primary" },
            { label: "待审核", value: formatInteger(data.forumPendingCount), tone: "warning" },
            { label: "已驳回", value: formatInteger(data.forumRejectedCount), tone: "danger" }
        ]);

        renderStatusChips("orderStatusChips", [
            { label: "待支付", value: formatInteger(data.unpaidOrderCount), tone: "danger" },
            { label: "待发货", value: formatInteger(data.paidOrderCount), tone: "primary" },
            { label: "待收货", value: formatInteger(data.shippingOrderCount), tone: "success" },
            { label: "已取消", value: formatInteger(data.cancelledOrderCount), tone: "muted" }
        ]);

        renderStatusChips("contentStatusChips", [
            { label: "帖子", value: formatInteger(data.forumCount), tone: "primary" },
            { label: "分类", value: formatInteger(data.categoryCount), tone: "success" },
            { label: "公告", value: formatInteger(data.noticeCount), tone: "warning" },
            { label: "轮播图", value: formatInteger(data.bannerCount), tone: "muted" }
        ]);
    }

    function renderMetrics(data) {
        var recentPaidAmount = sumNumbers(data.paidAmountTrend);

        setText("paidAmount", formatMoney(data.paidAmount));
        setText("userCount", formatInteger(data.userCount));
        setText("goodsCount", formatInteger(data.goodsCount));
        setText("orderCount", formatInteger(data.orderCount));
        setText("forumCount", formatInteger(data.forumCount));

        setText("paidAmountMeta", "近 7 日成交额 " + formatMoney(recentPaidAmount));
        setText("goodsCountMeta", "待审核 " + formatInteger(data.goodsPendingCount) + " · 已通过 " + formatInteger(data.goodsApprovedCount));
        setText("orderCountMeta", "待发货 " + formatInteger(data.paidOrderCount) + " · 待收货 " + formatInteger(data.shippingOrderCount));
        setText("forumCountMeta", "待审核 " + formatInteger(data.forumPendingCount) + " · 已通过 " + formatInteger(data.forumApprovedCount));
    }

    function renderManagerAvatar() {
        var avatar = AdminApp.getManagerAvatar();
        var $box = $("#managerAvatarBox");
        if (!$box.length) {
            return;
        }

        if (avatar) {
            $box.html('<img src="' + AdminApp.escapeHtml(AdminApp.resolveFileUrl(avatar)) + '" alt="manager avatar">');
        } else {
            $box.text(AdminApp.getManagerInitial());
        }
    }

    function renderPageMeta() {
        var now = new Date();
        var dateText = [
            now.getFullYear(),
            String(now.getMonth() + 1).padStart(2, "0"),
            String(now.getDate()).padStart(2, "0")
        ].join("-");

        setText("managerAccountText", AdminApp.getManagerAccount());
        setText("currentDateText", "更新日期 " + dateText);
        renderManagerAvatar();
    }

    function openQuickAction(href, title) {
        if (!href) {
            return;
        }

        if (window !== parent && miniTab && typeof miniTab.openNewTabByIframe === "function") {
            miniTab.openNewTabByIframe({
                href: href,
                title: title
            });
            return;
        }

        window.location.href = href;
    }

    function openMenuAction(menuTitle, fallbackHref) {
        if (window === parent || !parent.layui || !parent.layui.$) {
            return false;
        }

        var $parent = parent.layui.$;
        var targetTitle = $.trim(String(menuTitle || ""));
        var $target = $();

        if (targetTitle) {
            $target = $parent(".layuimini-menu-left [layuimini-href]").filter(function () {
                return $parent.trim($parent(this).text()) === targetTitle;
            }).first();
        }

        if (!$target.length && fallbackHref) {
            $target = $parent('.layuimini-menu-left [layuimini-href="' + fallbackHref + '"]').first();
        }

        if (!$target.length) {
            return false;
        }

        $target.trigger("click");
        return true;
    }

    function bindQuickActions() {
        $(".dashboard-quick-actions").on("click", "[layuimini-content-href]", function (event) {
            event.preventDefault();

            var $card = $(this);
            var href = $card.attr("layuimini-content-href");
            var title = $card.attr("data-title") || $.trim($card.find("strong").text()) || "快捷入口";
            var menuTitle = $card.attr("data-menu-title");

            if (menuTitle && openMenuAction(menuTitle, href)) {
                return;
            }
            openQuickAction(href, title);
        });
    }

    function loadDashboard() {
        AdminApp.postJson("/manager/dashboard", {}).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "首页数据加载失败");
                return;
            }

            var data = result.data || {};
            renderPageMeta();
            renderMetrics(data);
            renderOverview(data);
            renderCharts(data);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    window.addEventListener("resize", function () {
        charts.forEach(function (chart) {
            if (chart && typeof chart.resize === "function") {
                chart.resize();
            }
        });
    });

    bindQuickActions();
    window.refreshManagerProfileUi = function () {
        renderPageMeta();
    };
    AdminApp.refreshManagerSession().then(function () {
        loadDashboard();
    }).catch(function () {
        loadDashboard();
    });
});
