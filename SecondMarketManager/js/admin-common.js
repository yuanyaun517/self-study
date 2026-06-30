(function (window) {
    var layerCallbackWrapped = false;

    function getJQuery() {
        if (window.layui && layui.$) {
            return layui.$;
        }
        if (window.jQuery) {
            return window.jQuery;
        }
        if (window.$) {
            return window.$;
        }
        throw new Error("jQuery is not available");
    }

    function getLayer() {
        if (window.layui && layui.layer) {
            return layui.layer;
        }
        return window.layer;
    }

    function wrapLayerMsgForImmediateCallback() {
        if (layerCallbackWrapped) {
            return;
        }

        var layer = getLayer();
        if (!layer || typeof layer.msg !== "function") {
            return;
        }

        var originalMsg = layer.msg;
        layer.msg = function (content, options, end) {
            var callback = null;

            if (typeof options === "function") {
                callback = options;
                options = undefined;
                end = undefined;
            } else if (typeof end === "function") {
                callback = end;
                end = undefined;
            }

            if (callback) {
                var finished = false;
                var runCallback = function () {
                    if (finished) {
                        return;
                    }
                    finished = true;
                    callback();
                };

                var msgIndex;
                if (options === undefined) {
                    msgIndex = originalMsg.call(layer, content, runCallback);
                } else {
                    msgIndex = originalMsg.call(layer, content, options, runCallback);
                }
                runCallback();
                return msgIndex;
            }

            if (options === undefined && end === undefined) {
                return originalMsg.call(layer, content);
            }
            if (end === undefined) {
                return originalMsg.call(layer, content, options);
            }
            return originalMsg.call(layer, content, options, end);
        };

        layerCallbackWrapped = true;
    }

    function scheduleLayerMsgWrapper() {
        wrapLayerMsgForImmediateCallback();
        if (layerCallbackWrapped) {
            return;
        }
        if (window.layui && typeof layui.use === "function") {
            layui.use(["layer"], function () {
                wrapLayerMsgForImmediateCallback();
            });
        }
    }

    scheduleLayerMsgWrapper();

    function buildAjaxOptions(options) {
        var requestOptions = options || {};
        var method = String(requestOptions.method || "POST").toUpperCase();
        var isJson = requestOptions.contentType !== false;
        var ajaxData = requestOptions.data == null ? null : requestOptions.data;

        if (isJson && ajaxData && typeof ajaxData !== "string" && method !== "GET") {
            ajaxData = JSON.stringify(ajaxData);
        }

        return {
            url: buildApiUrl(requestOptions.url || ""),
            type: method,
            data: ajaxData,
            dataType: requestOptions.dataType || "json",
            headers: requestOptions.headers || {},
            contentType: isJson ? "application/json;charset=utf-8" : undefined,
            xhrFields: {
                withCredentials: true
            },
            crossDomain: true
        };
    }

    var AdminApp = {
        request: function (options) {
            wrapLayerMsgForImmediateCallback();
            var $ = getJQuery();
            return new Promise(function (resolve, reject) {
                var ajaxOptions = buildAjaxOptions(options);
                ajaxOptions.success = function (result) {
                    resolve(result || {});
                };
                ajaxOptions.error = function (xhr) {
                    reject(xhr);
                };
                $.ajax(ajaxOptions);
            });
        },

        postJson: function (url, data) {
            return this.request({
                url: url,
                method: "POST",
                data: data
            });
        },

        postForm: function (url, data) {
            return this.request({
                url: url,
                method: "POST",
                data: data,
                contentType: false
            });
        },

        ensureLogin: function () {
            if (!window.localStorage.getItem("managerId")) {
                window.location.replace("../../index.html");
                return false;
            }
            return true;
        },

        logout: function () {
            window.localStorage.removeItem("managerId");
            window.localStorage.removeItem("managerAccount");
            window.localStorage.removeItem("managerAvatar");
            window.location.replace("../../index.html");
        },

        setManagerSession: function (manager) {
            var currentManager = manager || {};
            window.localStorage.setItem("managerId", currentManager.id || "");
            window.localStorage.setItem("managerAccount", currentManager.account || "");
            window.localStorage.setItem("managerAvatar", currentManager.avatar || "");
        },

        getManagerId: function () {
            return Number(window.localStorage.getItem("managerId") || 0);
        },

        getManagerAccount: function () {
            return window.localStorage.getItem("managerAccount") || "管理员";
        },

        getManagerAvatar: function () {
            return window.localStorage.getItem("managerAvatar") || "";
        },

        getManagerDisplayName: function () {
            return this.getManagerAccount() || "Admin";
        },

        getManagerInitial: function () {
            return String(this.getManagerDisplayName() || "A").charAt(0).toUpperCase();
        },

        refreshManagerSession: function () {
            var managerId = this.getManagerId();
            var self = this;
            if (!managerId) {
                return Promise.resolve(null);
            }

            return this.postJson("/manager/profile", {
                id: managerId
            }).then(function (result) {
                if (result && result.code === 1 && result.data) {
                    self.setManagerSession(result.data);
                    return result.data;
                }
                return null;
            });
        },

        resolveFileUrl: function (value) {
            return resolveFileUrl(value);
        },

        splitImages: function (value) {
            return String(value || "")
                .split(",")
                .map(function (item) {
                    return item.trim();
                })
                .filter(function (item) {
                    return !!item;
                });
        },

        escapeHtml: function (value) {
            return String(value == null ? "" : value)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#39;");
        },

        formatDate: function (value) {
            if (value == null || value === "") {
                return "--";
            }

            var date = value instanceof Date ? value : new Date(value);
            if (isNaN(date.getTime())) {
                return String(value);
            }

            var year = date.getFullYear();
            var month = String(date.getMonth() + 1).padStart(2, "0");
            var day = String(date.getDate()).padStart(2, "0");
            var hour = String(date.getHours()).padStart(2, "0");
            var minute = String(date.getMinutes()).padStart(2, "0");
            var second = String(date.getSeconds()).padStart(2, "0");
            return [year, month, day].join("-") + " " + [hour, minute, second].join(":");
        },

        formatMoney: function (value) {
            var amount = Number(value);
            if (!Number.isFinite(amount)) {
                return "--";
            }
            return amount.toFixed(2);
        },

        goodsStatusText: function (value) {
            switch (String(value || "")) {
                case "0":
                    return "待审核";
                case "1":
                    return "已上架";
                case "2":
                    return "已驳回";
                case "3":
                    return "已下架";
                default:
                    return "--";
            }
        },

        forumStatusText: function (value) {
            switch (String(value || "")) {
                case "0":
                    return "待审核";
                case "1":
                    return "已通过";
                case "2":
                    return "已驳回";
                default:
                    return "--";
            }
        },

        orderStateText: function (value) {
            switch (String(value || "")) {
                case "1":
                    return "待支付";
                case "2":
                    return "待发货";
                case "3":
                    return "待收货";
                case "4":
                    return "已完成";
                case "5":
                    return "已取消";
                default:
                    return "--";
            }
        },

        noticeTypeText: function (value) {
            return String(value || "") === "1" ? "公告" : "通知";
        },

        openImagePreview: function (url) {
            var imageUrl = this.resolveFileUrl(url);
            if (!imageUrl) {
                return;
            }

            var layer = getLayer();
            if (!layer) {
                window.open(imageUrl, "_blank");
                return;
            }

            layer.open({
                type: 1,
                title: false,
                area: ["auto", "auto"],
                shadeClose: true,
                closeBtn: 0,
                skin: "layui-layer-nobg",
                content: '<div class="preview-container"><img src="' + this.escapeHtml(imageUrl) + '" alt="preview"></div>'
            });
        },

        showResponseMessage: function (result, successText) {
            var layer = getLayer();
            var message = result && (result.msg || result.data) ? (result.msg || result.data) : successText;
            if (layer && layer.msg) {
                layer.msg(message || successText || "操作成功");
            }
        },

        handleHttpError: function (xhr) {
            var message = "请求失败，请稍后重试。";
            if (xhr && xhr.responseJSON && xhr.responseJSON.msg) {
                message = xhr.responseJSON.msg;
            }
            alertHttpError(message);
        },

        openLayerPage: function (title, url, area) {
            var layer = getLayer();
            if (!layer) {
                window.location.href = url;
                return;
            }
            return layer.open({
                type: 2,
                title: title,
                shadeClose: true,
                shade: 0.2,
                area: area || ["720px", "560px"],
                content: url
            });
        },

        closeLayerOrRedirect: function (fallbackUrl) {
            try {
                if (parent && parent !== window && parent.reloadPageData) {
                    parent.reloadPageData();
                }

                if (parent && parent.layer && window.name) {
                    var frameIndex = parent.layer.getFrameIndex(window.name);
                    if (frameIndex !== undefined) {
                        parent.layer.close(frameIndex);
                        return;
                    }
                }
            } catch (error) {
                // Ignore layer close errors and fallback to redirect.
            }

            if (fallbackUrl) {
                window.location.replace(fallbackUrl);
                return;
            }
            window.location.reload();
        }
    };

    window.AdminApp = AdminApp;
})(window);
