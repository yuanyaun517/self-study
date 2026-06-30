layui.use(["form", "table", "layer", "jquery", "upload"], function () {
    var form = layui.form;
    var table = layui.table;
    var layer = layui.layer;
    var $ = layui.jquery;
    var upload = layui.upload;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var PHONE_PATTERN = /^1[3-9]\d{9}$/;
    var IDCARD_PATTERN = /^\d{17}[\dXx]$/;
    var IDCARD_WEIGHTS = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    var IDCARD_CHECK_CODES = ["1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"];
    var IDCARD_PROVINCES = {
        "11": true, "12": true, "13": true, "14": true, "15": true,
        "21": true, "22": true, "23": true,
        "31": true, "32": true, "33": true, "34": true, "35": true, "36": true, "37": true,
        "41": true, "42": true, "43": true, "44": true, "45": true, "46": true,
        "50": true, "51": true, "52": true, "53": true, "54": true,
        "61": true, "62": true, "63": true, "64": true, "65": true,
        "71": true, "81": true, "82": true, "91": true
    };
    var DEFAULT_RESET_PASSWORD = "123456";

    var usersCache = [];
    var currentAvatar = "";
    var currentUserId = getQueryId();

    function isListPage() {
        return !!document.getElementById("userTable");
    }

    function isAddPage() {
        return !!document.getElementById("userForm");
    }

    function getQueryId() {
        return Number(new URLSearchParams(window.location.search).get("id") || 0);
    }

    function isEditPage() {
        return currentUserId > 0;
    }

    function trimValue(value) {
        return String(value == null ? "" : value).trim();
    }

    function safeText(value) {
        var text = trimValue(value);
        return text ? AdminApp.escapeHtml(text) : "--";
    }

    function sortUsers(list) {
        return (list || []).slice().sort(function (left, right) {
            return Number(right && right.id ? right.id : 0) - Number(left && left.id ? left.id : 0);
        });
    }

    function decodeSex(value) {
        if (value === "male" || value === "\u7537") {
            return "\u7537";
        }
        if (value === "female" || value === "\u5973") {
            return "\u5973";
        }
        return "";
    }

    function encodeSex(value) {
        if (value === "female" || value === "\u5973") {
            return "female";
        }
        return "male";
    }

    function renderAvatarCell(icon) {
        var iconValue = trimValue(icon);
        if (!iconValue) {
            return '<span class="table-empty">\u672a\u4e0a\u4f20</span>';
        }

        var resolvedUrl = AdminApp.resolveFileUrl(iconValue);
        return '<div class="goods-cover-cell">' +
            '<img class="table-thumb js-user-avatar" src="' + AdminApp.escapeHtml(resolvedUrl) + '" data-image="' + AdminApp.escapeHtml(iconValue) + '" alt="avatar">' +
            "</div>";
    }

    function renderTable(list) {
        table.render({
            elem: "#userTable",
            id: "userTableReload",
            data: list || [],
            page: true,
            limit: 10,
            skin: "line",
            toolbar: "#userToolbar",
            cols: [[
                { field: "id", title: "ID", width: 80, sort: true },
                {
                    field: "icon",
                    title: "\u5934\u50cf",
                    width: 90,
                    templet: function (row) {
                        return renderAvatarCell(row.icon);
                    }
                },
                {
                    field: "account",
                    title: "\u8d26\u53f7",
                    minWidth: 140,
                    templet: function (row) {
                        return safeText(row.account);
                    }
                },
                {
                    field: "nickname",
                    title: "\u6635\u79f0",
                    minWidth: 140,
                    templet: function (row) {
                        return safeText(row.nickname);
                    }
                },
                {
                    field: "sex",
                    title: "\u6027\u522b",
                    width: 90,
                    templet: function (row) {
                        return safeText(row.sex);
                    }
                },
                {
                    field: "tel",
                    title: "\u624b\u673a\u53f7",
                    minWidth: 140,
                    templet: function (row) {
                        return safeText(row.tel);
                    }
                },
                {
                    field: "idcard",
                    title: "\u8eab\u4efd\u8bc1\u53f7",
                    minWidth: 200,
                    templet: function (row) {
                        return safeText(row.idcard);
                    }
                },
                {
                    field: "college",
                    title: "\u5b66\u9662",
                    minWidth: 140,
                    templet: function (row) {
                        return safeText(row.college);
                    }
                },
                {
                    field: "grade",
                    title: "\u73ed\u7ea7",
                    minWidth: 130,
                    templet: function (row) {
                        return safeText(row.grade);
                    }
                },
                {
                    field: "roomnumb",
                    title: "\u5bbf\u820d\u53f7",
                    minWidth: 110,
                    templet: function (row) {
                        return safeText(row.roomnumb);
                    }
                },
                { fixed: "right", title: "\u64cd\u4f5c", width: 250, toolbar: "#userBar" }
            ]]
        });
    }

    function loadUsers() {
        AdminApp.postJson("/manager/getAllUser", {}).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "\u7528\u6237\u6570\u636e\u52a0\u8f7d\u5931\u8d25");
                return;
            }

            usersCache = sortUsers(result.data || []);
            renderTable(usersCache);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function matchKeyword(user, keyword) {
        if (!keyword) {
            return true;
        }

        var values = [
            user.account,
            user.nickname,
            user.tel,
            user.idcard,
            user.college,
            user.grade,
            user.roomnumb
        ];

        for (var index = 0; index < values.length; index++) {
            if (String(values[index] == null ? "" : values[index]).toLowerCase().indexOf(keyword) > -1) {
                return true;
            }
        }
        return false;
    }

    function filterUsers() {
        var keyword = trimValue($("#keyword").val()).toLowerCase();
        renderTable(usersCache.filter(function (item) {
            return matchKeyword(item || {}, keyword);
        }));
    }

    function resetUserPassword(row) {
        var accountText = AdminApp.escapeHtml(trimValue(row.account) || "--");
        layer.confirm(
            "\u786e\u8ba4\u5c06\u8d26\u53f7 " + accountText + " \u7684\u5bc6\u7801\u91cd\u7f6e\u4e3a " + DEFAULT_RESET_PASSWORD + " \u5417\uff1f",
            function (index) {
                AdminApp.postJson("/manager/editUser", {
                    id: row.id,
                    password: DEFAULT_RESET_PASSWORD
                }).then(function (result) {
                    if (result.code !== 1) {
                        layer.alert(result.msg || "\u91cd\u7f6e\u5bc6\u7801\u5931\u8d25");
                        return;
                    }

                    layer.close(index);
                    layer.msg("\u5bc6\u7801\u5df2\u91cd\u7f6e\u4e3a " + DEFAULT_RESET_PASSWORD);
                    loadUsers();
                }).catch(function (xhr) {
                    AdminApp.handleHttpError(xhr);
                });
            }
        );
    }

    function deleteUser(row) {
        var accountText = AdminApp.escapeHtml(trimValue(row.account) || "--");
        layer.confirm("\u786e\u8ba4\u5220\u9664\u8d26\u53f7 " + accountText + " \u5417\uff1f", function (index) {
            AdminApp.postJson("/manager/delUser", {
                id: row.id
            }).then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || "\u5220\u9664\u7528\u6237\u5931\u8d25");
                    return;
                }

                layer.close(index);
                layer.msg("\u5220\u9664\u6210\u529f");
                loadUsers();
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
        });
    }

    function fillUserForm(row) {
        currentAvatar = trimValue(row && row.icon);
        form.val("userForm", {
            account: trimValue(row && row.account),
            password: "",
            nickname: trimValue(row && row.nickname),
            sex: encodeSex(row && row.sex),
            tel: trimValue(row && row.tel),
            idcard: normalizeIdcard(row && row.idcard),
            college: trimValue(row && row.college),
            grade: trimValue(row && row.grade),
            roomnumb: trimValue(row && row.roomnumb)
        });
        renderAvatarPreview();
        form.render("radio");
    }

    function loadUserForEdit() {
        if (!isEditPage()) {
            return;
        }

        AdminApp.postJson("/manager/getAllUser", {}).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "\u7528\u6237\u6570\u636e\u52a0\u8f7d\u5931\u8d25");
                return;
            }

            var row = (result.data || []).find(function (item) {
                return Number(item && item.id) === currentUserId;
            });
            if (!row) {
                layer.alert("\u672a\u627e\u5230\u8981\u4fee\u6539\u7684\u7528\u6237");
                return;
            }

            fillUserForm(row);
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
    }

    function bindAvatarPreviewClick() {
        $(document).off("click.userAvatar").on("click.userAvatar", ".js-user-avatar", function () {
            var imageValue = trimValue($(this).attr("data-image"));
            if (!imageValue) {
                return;
            }
            AdminApp.openImagePreview(imageValue);
        });
    }

    function initListPage() {
        if (!isListPage()) {
            return;
        }

        loadUsers();
        bindAvatarPreviewClick();
        window.reloadPageData = loadUsers;

        form.on("submit(searchUser)", function () {
            filterUsers();
            return false;
        });

        $("#resetSearch").on("click", function () {
            $("#keyword").val("");
            renderTable(usersCache);
        });

        table.on("toolbar(userTableFilter)", function (obj) {
            if (obj.event === "add") {
                AdminApp.openLayerPage("\u65b0\u589e\u7528\u6237", "add.html", ["860px", "760px"]);
            }
        });

        table.on("tool(userTableFilter)", function (obj) {
            var row = obj.data || {};
            if (obj.event === "edit") {
                AdminApp.openLayerPage("\u4fee\u6539\u7528\u6237\u4fe1\u606f", "add.html?id=" + row.id, ["860px", "760px"]);
                return;
            }
            if (obj.event === "resetPassword") {
                resetUserPassword(row);
                return;
            }
            if (obj.event === "delete") {
                deleteUser(row);
            }
        });
    }

    function syncAvatarFields() {
        $("#userIcon").val(currentAvatar);
        $("#userIconDisplay").val(currentAvatar);
    }

    function renderAvatarPreview() {
        var $preview = $("#userAvatarPreview");
        if (!$preview.length) {
            return;
        }

        syncAvatarFields();
        if (!currentAvatar) {
            $preview.text("+");
            return;
        }

        $preview.html('<img src="' + AdminApp.escapeHtml(AdminApp.resolveFileUrl(currentAvatar)) + '" alt="user avatar">');
    }

    function normalizeIdcard(value) {
        return trimValue(value).toUpperCase();
    }

    function isValidBirthday(value) {
        var year = Number(value.substring(0, 4));
        var month = Number(value.substring(4, 6));
        var day = Number(value.substring(6, 8));
        if (year < 1900) {
            return false;
        }

        var birthday = new Date(year, month - 1, day);
        if (
            birthday.getFullYear() !== year ||
            birthday.getMonth() !== month - 1 ||
            birthday.getDate() !== day
        ) {
            return false;
        }

        var today = new Date();
        today.setHours(0, 0, 0, 0);
        birthday.setHours(0, 0, 0, 0);
        return birthday.getTime() <= today.getTime();
    }

    function isValidIdcard(value) {
        var idcard = normalizeIdcard(value);
        if (!IDCARD_PATTERN.test(idcard)) {
            return false;
        }
        if (!IDCARD_PROVINCES[idcard.substring(0, 2)]) {
            return false;
        }
        if (!isValidBirthday(idcard.substring(6, 14))) {
            return false;
        }

        var sum = 0;
        for (var index = 0; index < IDCARD_WEIGHTS.length; index++) {
            sum += Number(idcard.charAt(index)) * IDCARD_WEIGHTS[index];
        }
        return IDCARD_CHECK_CODES[sum % 11] === idcard.charAt(17);
    }

    function buildUserPayload(field) {
        var payload = {
            account: trimValue(field.account),
            password: trimValue(field.password),
            nickname: trimValue(field.nickname),
            sex: decodeSex(field.sex),
            tel: trimValue(field.tel),
            idcard: normalizeIdcard(field.idcard),
            college: trimValue(field.college),
            grade: trimValue(field.grade),
            roomnumb: trimValue(field.roomnumb),
            icon: currentAvatar
        };

        if (!payload.roomnumb && !isEditPage()) {
            delete payload.roomnumb;
        }
        if (!payload.icon && !isEditPage()) {
            delete payload.icon;
        }
        return payload;
    }

    function validateUserPayload(payload) {
        if (!payload.account) {
            return "\u8bf7\u8f93\u5165\u8d26\u53f7";
        }
        if (!isEditPage() && !payload.password) {
            return "\u8bf7\u8f93\u5165\u5bc6\u7801";
        }
        if (!payload.nickname) {
            return "\u8bf7\u8f93\u5165\u6635\u79f0";
        }
        if (!payload.sex) {
            return "\u8bf7\u9009\u62e9\u6027\u522b";
        }
        if (!payload.tel) {
            return "\u8bf7\u8f93\u5165\u624b\u673a\u53f7";
        }
        if (!PHONE_PATTERN.test(payload.tel)) {
            return "\u8bf7\u8f93\u5165\u6b63\u786e\u7684\u624b\u673a\u53f7";
        }
        if (!payload.idcard) {
            return "\u8bf7\u8f93\u5165\u8eab\u4efd\u8bc1\u53f7";
        }
        if (!isValidIdcard(payload.idcard)) {
            return "\u8bf7\u8f93\u5165\u6b63\u786e\u7684\u8eab\u4efd\u8bc1\u53f7";
        }
        if (!payload.college) {
            return "\u8bf7\u8f93\u5165\u5b66\u9662";
        }
        if (!payload.grade) {
            return "\u8bf7\u8f93\u5165\u73ed\u7ea7";
        }
        return "";
    }

    function updateFormMode() {
        var isEditMode = isEditPage();
        var titleText = isEditMode ? "\u4fee\u6539\u7528\u6237\u4fe1\u606f" : "\u65b0\u589e\u7528\u6237";
        var descriptionText = isEditMode
            ? "\u53ef\u4fee\u6539\u8d26\u53f7\u3001\u6635\u79f0\u3001\u6027\u522b\u3001\u624b\u673a\u53f7\u3001\u8eab\u4efd\u8bc1\u53f7\u3001\u5b66\u9662\u3001\u73ed\u7ea7\u3001\u5bbf\u820d\u53f7\u548c\u5934\u50cf\u3002\u5bc6\u7801\u7559\u7a7a\u5219\u4fdd\u6301\u4e0d\u53d8\u3002"
            : "\u8d26\u53f7\u3001\u5bc6\u7801\u3001\u6635\u79f0\u3001\u6027\u522b\u3001\u624b\u673a\u53f7\u3001\u8eab\u4efd\u8bc1\u53f7\u3001\u5b66\u9662\u3001\u73ed\u7ea7\u4e3a\u5fc5\u586b\u9879\u3002\u5934\u50cf\u4e3a\u9009\u586b\u9879\uff0c\u5982\u9700\u586b\u5199\u8bf7\u901a\u8fc7\u4e0a\u4f20\u6309\u94ae\u9009\u62e9\u56fe\u7247\u3002";

        document.title = titleText;
        $("#userFormTitle").html(AdminApp.escapeHtml(titleText));
        $("#userFormDesc").html(AdminApp.escapeHtml(descriptionText));
        $("#saveUserSubmitBtn").html(isEditMode ? "\u4fdd\u5b58\u4fee\u6539" : "\u63d0\u4ea4\u65b0\u589e");
        $("#userPassword").attr("placeholder", isEditMode
            ? "\u5982\u9700\u4fee\u6539\u5bc6\u7801\u8bf7\u586b\u5199\u65b0\u5bc6\u7801\uff0c\u4e0d\u586b\u5219\u4fdd\u6301\u539f\u5bc6\u7801"
            : "\u8bf7\u8f93\u5165\u5bc6\u7801");
        $("#userPasswordTip").text(isEditMode ? "\u7f16\u8f91\u7528\u6237\u65f6\uff0c\u5bc6\u7801\u53ef\u4e0d\u586b\u3002" : "");
    }

    function initUpload() {
        upload.render({
            elem: "#uploadUserAvatarBtn",
            url: buildApiUrl("/common/upload"),
            accept: "images",
            exts: "jpg|jpeg|png|gif|webp",
            done: function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || "\u5934\u50cf\u4e0a\u4f20\u5931\u8d25");
                    return;
                }

                currentAvatar = result.data ? getDownloadUrl(result.data) : "";
                renderAvatarPreview();
                layer.msg("\u5934\u50cf\u4e0a\u4f20\u6210\u529f");
            },
            error: function () {
                alertHttpError("\u5934\u50cf\u4e0a\u4f20\u5931\u8d25");
            }
        });
    }

    function initAddPage() {
        if (!isAddPage()) {
            return;
        }

        currentAvatar = "";
        updateFormMode();
        renderAvatarPreview();
        initUpload();
        form.render("radio");
        loadUserForEdit();

        $("#previewUserAvatarBtn").on("click", function () {
            if (!currentAvatar) {
                layer.msg("\u8bf7\u5148\u4e0a\u4f20\u5934\u50cf");
                return;
            }
            AdminApp.openImagePreview(currentAvatar);
        });

        $("#clearUserAvatarBtn").on("click", function () {
            currentAvatar = "";
            renderAvatarPreview();
        });

        form.on("submit(saveUserForm)", function (data) {
            var field = data.field || {};
            var payload = buildUserPayload(field);
            var validationMessage = validateUserPayload(payload);
            if (validationMessage) {
                layer.msg(validationMessage);
                return false;
            }

            var requestUrl = "/manager/addUser";
            if (isEditPage()) {
                payload.id = currentUserId;
                if (!trimValue(field.password)) {
                    delete payload.password;
                }
                requestUrl = "/manager/editUser";
            }

            AdminApp.postJson(requestUrl, payload).then(function (result) {
                if (result.code !== 1) {
                    layer.alert(result.msg || (isEditPage() ? "\u4fee\u6539\u7528\u6237\u5931\u8d25" : "\u65b0\u589e\u7528\u6237\u5931\u8d25"));
                    return;
                }

                layer.msg(isEditPage() ? "\u4fee\u6539\u7528\u6237\u6210\u529f" : "\u65b0\u589e\u7528\u6237\u6210\u529f", function () {
                    AdminApp.closeLayerOrRedirect("index.html");
                });
            }).catch(function (xhr) {
                AdminApp.handleHttpError(xhr);
            });
            return false;
        });
    }

    initListPage();
    initAddPage();
});
