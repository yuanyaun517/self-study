layui.use(["form", "layer", "jquery"], function () {
    var form = layui.form;
    var layer = layui.layer;
    var $ = layui.jquery;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var forumsCache = [];

    function getForumCover(row) {
        if (row.icon) {
            return AdminApp.resolveFileUrl(row.icon);
        }
        var images = AdminApp.splitImages(row.imgs);
        return images.length ? AdminApp.resolveFileUrl(images[0]) : "";
    }

    function buildRows(list) {
        return (list || []).map(function (item) {
            return $.extend({}, item, {
                cover: getForumCover(item)
            });
        });
    }

    function renderManageStatus(value) {
        switch (String(value || "")) {
            case "1":
                return '<span style="color:#16b777;">审核通过 / 正常</span>';
            case "2":
                return '<span style="color:#ff5722;">审核驳回</span>';
            case "0":
                return '<span style="color:#ffb800;">待审核</span>';
            default:
                return '<span style="color:#999999;">--</span>';
        }
    }

    function renderForumTable(list) {
        var rows = buildRows(list);
        var html = [];
        var index;
        var row;

        html.push('<div class="forum-table-meta">共 ' + rows.length + " 条帖子</div>");

        if (!rows.length) {
            html.push('<div class="forum-manage-empty">当前没有可显示的帖子</div>');
            $("#forumTableContainer").html(html.join(""));
            return;
        }

        html.push('<div class="forum-table-scroll">');
        html.push('<table class="layui-table" lay-size="sm">');
        html.push("<colgroup>");
        html.push('<col style="width:80px;">');
        html.push('<col style="width:120px;">');
        html.push('<col style="width:160px;">');
        html.push('<col style="width:260px;">');
        html.push('<col style="width:130px;">');
        html.push('<col style="width:180px;">');
        html.push('<col style="width:140px;">');
        html.push('<col style="width:340px;">');
        html.push("</colgroup>");
        html.push("<thead><tr>");
        html.push("<th>ID</th>");
        html.push("<th>封面</th>");
        html.push("<th>标题</th>");
        html.push("<th>内容</th>");
        html.push("<th>分类</th>");
        html.push("<th>发布时间</th>");
        html.push("<th>帖子状态</th>");
        html.push("<th>操作</th>");
        html.push("</tr></thead>");
        html.push("<tbody>");

        for (index = 0; index < rows.length; index += 1) {
            row = rows[index];
            html.push("<tr>");
            html.push('<td style="text-align:center;">' + AdminApp.escapeHtml(row.id) + "</td>");
            html.push('<td style="text-align:center;">' + renderCoverCell(row) + "</td>");
            html.push("<td>" + AdminApp.escapeHtml(row.title || "--") + "</td>");
            html.push('<td><div class="table-text-2">' + AdminApp.escapeHtml(row.content || "--") + "</div></td>");
            html.push('<td style="text-align:center;">' + AdminApp.escapeHtml(row.type || "--") + "</td>");
            html.push('<td style="text-align:center;">' + AdminApp.escapeHtml(AdminApp.formatDate(row.sendTime)) + "</td>");
            html.push('<td style="text-align:center;">' + renderManageStatus(row.manage) + "</td>");
            html.push('<td style="text-align:center;">' + renderActionButtons(row) + "</td>");
            html.push("</tr>");
        }

        html.push("</tbody></table></div>");
        $("#forumTableContainer").html(html.join(""));
    }

    function renderCoverCell(row) {
        if (!row.cover) {
            return '<span class="forum-manage-cover-empty">无</span>';
        }
        return '<img class="forum-manage-cover preview-image" data-src="' + AdminApp.escapeHtml(row.cover) + '" src="' + AdminApp.escapeHtml(row.cover) + '" alt="forum">';
    }

    function renderActionButtonsLegacy(row) {
        var id = AdminApp.escapeHtml(row.id);
        return [
            '<div class="forum-manage-actions">',
            '  <button type="button" class="layui-btn layui-btn-xs layui-btn-primary forum-action-btn" data-event="view" data-id="' + id + '">详情</button>',
            '  <button type="button" class="layui-btn layui-btn-xs layui-btn-primary forum-action-btn" data-event="comments" data-id="' + id + '">评论</button>',
            '  <button type="button" class="layui-btn layui-btn-xs layui-btn-normal forum-action-btn" data-event="approve" data-id="' + id + '">通过</button>',
            '  <button type="button" class="layui-btn layui-btn-xs layui-btn-warm forum-action-btn" data-event="reject" data-id="' + id + '">驳回</button>',
            '  <button type="button" class="layui-btn layui-btn-danger layui-btn-xs forum-action-btn" data-event="delete" data-id="' + id + '">删除</button>',
            "</div>"
        ].join("");
    }

    function renderActionButtons(row) {
        var id = AdminApp.escapeHtml(row.id);
        var manage = String(row.manage || "");
        var buttons = [
            '<div class="forum-manage-actions">',
            '  <button type="button" class="layui-btn layui-btn-xs layui-btn-primary forum-action-btn" data-event="view" data-id="' + id + '">详情</button>',
            '  <button type="button" class="layui-btn layui-btn-xs layui-btn-primary forum-action-btn" data-event="comments" data-id="' + id + '">评论</button>'
        ];

        if (manage !== "1") {
            buttons.push('  <button type="button" class="layui-btn layui-btn-xs layui-btn-normal forum-action-btn" data-event="approve" data-id="' + id + '">通过</button>');
        }

        if (manage !== "0") {
            buttons.push('  <button type="button" class="layui-btn layui-btn-xs layui-btn-warm forum-action-btn" data-event="reject" data-id="' + id + '">驳回</button>');
        }

        buttons.push(
            '  <button type="button" class="layui-btn layui-btn-danger layui-btn-xs forum-action-btn" data-event="delete" data-id="' + id + '">删除</button>',
            "</div>"
        );

        return buttons.join("");
    }

    function loadForums() {
        AdminApp.postJson("/manager/getAllForum", {}).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "帖子数据加载失败");
                return;
            }
            forumsCache = result.data || [];
            renderForumTable(forumsCache);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function filterForums() {
        var keyword = String($("#forumKeyword").val() || "").trim().toLowerCase();
        if (!keyword) {
            renderForumTable(forumsCache);
            return;
        }

        renderForumTable(forumsCache.filter(function (item) {
            return [item.content, item.title, item.type].some(function (value) {
                return String(value || "").toLowerCase().indexOf(keyword) > -1;
            });
        }));
    }

    function updateForumManage(id, manage) {
        return AdminApp.request({
            url: "/manager/checkForum?id=" + encodeURIComponent(id) + "&manage=" + encodeURIComponent(manage),
            method: "POST",
            data: "",
            contentType: false
        });
    }

    function loadForumComments(forumId) {
        return AdminApp.request({
            url: "/manager/getForumComments?forumId=" + encodeURIComponent(forumId),
            method: "POST",
            data: "",
            contentType: false
        });
    }

    function loadForumDetail(forumId) {
        return AdminApp.request({
            url: "/manager/getForumDetail?id=" + encodeURIComponent(forumId),
            method: "POST",
            data: "",
            contentType: false
        });
    }

    function deleteForumComment(commentId) {
        return AdminApp.postJson("/manager/delForumComment", { id: commentId });
    }

    function getUserDisplayName(user) {
        if (!user) {
            return "未知用户";
        }
        return user.nickname || user.account || ("用户 #" + (user.id || "--"));
    }

    function renderReplyTo(replyToUser) {
        if (!replyToUser || (!replyToUser.id && !replyToUser.nickname && !replyToUser.account)) {
            return "";
        }
        return '<div class="forum-comment-reply-target">回复 @' + AdminApp.escapeHtml(getUserDisplayName(replyToUser)) + "</div>";
    }

    function renderCommentItem(comment, isReply) {
        var itemClass = isReply ? "forum-comment-reply" : "forum-comment-card";
        var buttonClass = isReply ? "layui-btn-danger layui-btn-xs" : "layui-btn layui-btn-danger layui-btn-xs";
        var repliesHtml = "";

        if (!isReply && comment.replies && comment.replies.length) {
            repliesHtml = '<div class="forum-comment-replies">' + comment.replies.map(function (reply) {
                return renderCommentItem(reply, true);
            }).join("") + "</div>";
        }

        return [
            '<div class="' + itemClass + '">',
            '  <div class="forum-comment-card-head">',
            '    <div class="forum-comment-author">',
            "      <strong>" + AdminApp.escapeHtml(getUserDisplayName(comment.user)) + "</strong>",
            "      <span>" + AdminApp.escapeHtml(AdminApp.formatDate(comment.createTime)) + " · ID " + AdminApp.escapeHtml(comment.id) + "</span>",
            "    </div>",
            '    <button type="button" class="' + buttonClass + ' delete-comment-btn" data-id="' + AdminApp.escapeHtml(comment.id) + '">删评</button>',
            "  </div>",
            renderReplyTo(comment.replyToUser),
            '  <div class="forum-comment-content">' + AdminApp.escapeHtml(comment.content || "--") + "</div>",
            repliesHtml,
            "</div>"
        ].join("");
    }

    function buildCommentPanelHtml(row, comments) {
        var title = row.title || "未命名帖子";
        var content = row.content || "暂无帖子内容";
        var commentHtml = (comments || []).length
            ? comments.map(function (comment) {
                return renderCommentItem(comment, false);
            }).join("")
            : '<div class="forum-comment-empty">当前帖子暂无评论</div>';

        return [
            '<div class="forum-comment-panel">',
            '  <div class="forum-comment-meta">',
            "    <h3>" + AdminApp.escapeHtml(title) + "</h3>",
            "    <p>" + AdminApp.escapeHtml(content) + "</p>",
            "  </div>",
            '  <div class="forum-comment-list">',
            commentHtml,
            "  </div>",
            "</div>"
        ].join("");
    }

    function showForumDetail(row) {
        loadForumDetail(row.id).then(function (result) {
            if (result.code !== 1 || !result.data) {
                layer.alert((result && result.msg) || "帖子详情加载失败");
                return;
            }

            var detail = result.data || {};
            var user = detail.user || {};
            var authorName = user.nickname || user.account || ("用户 #" + (detail.sendUser || "--"));
            var images = Array.isArray(detail.imgList) ? detail.imgList : AdminApp.splitImages(detail.imgs || "");
            var imagesHtml = images.map(function (item) {
                var imageUrl = AdminApp.resolveFileUrl(item);
                return imageUrl
                    ? '<img src="' + AdminApp.escapeHtml(imageUrl) + '" data-src="' + AdminApp.escapeHtml(imageUrl) + '">'
                    : "";
            }).join("");

            var content = [
                '<div style="padding: 18px 20px; line-height: 1.9;">',
                "<div><strong>帖子标题：</strong>" + AdminApp.escapeHtml(detail.title || "--") + "</div>",
                "<div><strong>帖子分类：</strong>" + AdminApp.escapeHtml(detail.type || "--") + "</div>",
                "<div><strong>帖子状态：</strong>" + AdminApp.escapeHtml(AdminApp.forumStatusText(detail.manage)) + "</div>",
                "<div><strong>发布用户：</strong>" + AdminApp.escapeHtml(authorName) + "</div>",
                "<div><strong>发布用户ID：</strong>" + AdminApp.escapeHtml(detail.sendUser || "--") + "</div>",
                "<div><strong>评论数量：</strong>" + AdminApp.escapeHtml(detail.commentCount || 0) + "</div>",
                "<div><strong>发布时间：</strong>" + AdminApp.escapeHtml(AdminApp.formatDate(detail.sendTime)) + "</div>",
                "<div><strong>更新时间：</strong>" + AdminApp.escapeHtml(AdminApp.formatDate(detail.updateTime || detail.sendTime)) + "</div>",
                '<div style="margin-top: 8px;"><strong>帖子内容：</strong><br>' + AdminApp.escapeHtml(detail.content || "--") + "</div>",
                imagesHtml ? '<div class="goods-images-preview">' + imagesHtml + "</div>" : "",
                "</div>"
            ].join("");

            layer.open({
                type: 1,
                title: "帖子详情",
                area: ["720px", "560px"],
                shadeClose: true,
                content: content
            });
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function openCommentsDialog(row) {
        loadForumComments(row.id).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "评论加载失败");
                return;
            }

            var comments = result.data || [];
            var panelHtml = buildCommentPanelHtml(row, comments);
            var currentLayerIndex = layer.open({
                type: 1,
                title: "帖子评论管理",
                area: ["860px", "78vh"],
                shadeClose: true,
                maxmin: true,
                content: panelHtml
            });

            $(document).off("click.forumCommentDelete").on("click.forumCommentDelete", ".delete-comment-btn", function () {
                var commentId = Number($(this).data("id") || 0);
                if (!commentId) {
                    return;
                }

                layer.confirm("确认删除这条评论吗？删除根评论会同时删除其回复。", function (confirmIndex) {
                    deleteForumComment(commentId).then(function (deleteResult) {
                        if (deleteResult.code !== 1) {
                            layer.alert(deleteResult.msg || "删评失败");
                            return;
                        }
                        layer.close(confirmIndex);
                        layer.close(currentLayerIndex);
                        layer.msg("删评成功");
                        openCommentsDialog(row);
                    }).catch(function (xhr) {
                        AdminApp.handleHttpError(xhr);
                    });
                });
            });
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function findForumById(id) {
        var targetId = Number(id);
        return buildRows(forumsCache).find(function (item) {
            return Number(item.id) === targetId;
        }) || null;
    }

    function handleAction(eventName, row) {
        if (!row) {
            return;
        }

        if (eventName === "view") {
            showForumDetail(row);
            return;
        }

        if (eventName === "comments") {
            openCommentsDialog(row);
            return;
        }

        if (eventName === "approve") {
            updateForumManage(row.id, "1").then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || "审核通过失败");
                    return;
                }
                layer.msg("已审核通过");
                loadForums();
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
            return;
        }

        if (eventName === "reject") {
            updateForumManage(row.id, "2").then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || "驳回失败");
                    return;
                }
                layer.msg("已驳回");
                loadForums();
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
            return;
        }

        if (eventName === "delete") {
            layer.confirm("确认删除该帖子吗？", function (index) {
                AdminApp.postJson("/manager/delForum", { id: row.id }).then(function (result) {
                    if (result.code !== 1) {
                        layer.alert(result.msg || "删除失败");
                        return;
                    }
                    layer.close(index);
                    layer.msg("删除成功");
                    loadForums();
                }).catch(function (xhr) {
                    AdminApp.handleHttpError(xhr);
                });
            });
        }
    }

    loadForums();
    window.reloadPageData = loadForums;

    $(document).on("click", ".preview-image", function () {
        AdminApp.openImagePreview($(this).data("src"));
    });

    $(document).on("click", ".goods-images-preview img", function () {
        AdminApp.openImagePreview($(this).data("src") || $(this).attr("src"));
    });

    $(document).on("click", ".forum-action-btn", function () {
        var eventName = $(this).data("event");
        var forumId = $(this).data("id");
        handleAction(eventName, findForumById(forumId));
    });

    form.on("submit(searchForum)", function () {
        filterForums();
        return false;
    });
});
