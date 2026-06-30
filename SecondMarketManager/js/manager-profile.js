layui.use(["form", "layer", "jquery", "upload"], function () {
    var form = layui.form;
    var layer = layui.layer;
    var $ = layui.jquery;
    var upload = layui.upload;

    if (!AdminApp.ensureLogin()) {
        return;
    }

    var currentAvatar = "";
    var loadingProfile = false;

    function renderAvatarPreview() {
        var $preview = $("#managerAvatarPreview");
        if (!currentAvatar) {
            $preview.text(AdminApp.getManagerInitial());
            return;
        }
        $preview.html('<img src="' + AdminApp.escapeHtml(AdminApp.resolveFileUrl(currentAvatar)) + '" alt="manager avatar">');
    }

    function fillProfile(manager) {
        var currentManager = manager || {};
        form.val("profileForm", {
            account: currentManager.account || ""
        });
        $("#managerIdText").val(currentManager.id || AdminApp.getManagerId() || "");
        currentAvatar = currentManager.avatar || "";
        AdminApp.setManagerSession(currentManager);
        renderAvatarPreview();
    }

    function loadProfile() {
        if (loadingProfile) {
            return;
        }
        loadingProfile = true;

        AdminApp.postJson("/manager/profile", {
            id: AdminApp.getManagerId()
        }).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "加载管理员资料失败");
                return;
            }
            fillProfile(result.data || {});
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        }).finally(function () {
            loadingProfile = false;
        });
    }

    function syncParentUi() {
        if (!parent || parent === window) {
            return;
        }

        try {
            if (typeof parent.refreshManagerHeader === "function") {
                parent.refreshManagerHeader();
            }
        } catch (error) {
            // Ignore cross-frame UI sync failures.
        }

        try {
            for (var i = 0; i < parent.frames.length; i++) {
                var frameWindow = parent.frames[i];
                if (frameWindow && typeof frameWindow.refreshManagerProfileUi === "function") {
                    frameWindow.refreshManagerProfileUi();
                }
            }
        } catch (error) {
            // Ignore iframe refresh failures.
        }
    }

    upload.render({
        elem: "#uploadAvatarBtn",
        url: buildApiUrl("/common/upload"),
        accept: "images",
        exts: "jpg|jpeg|png|gif|webp",
        done: function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "头像上传失败");
                return;
            }
            currentAvatar = result.data;
            renderAvatarPreview();
            layer.msg("头像上传成功");
        },
        error: function () {
            alertHttpError("头像上传失败");
        }
    });

    $("#previewAvatarBtn").on("click", function () {
        if (!currentAvatar) {
            layer.msg("当前还没有头像");
            return;
        }
        AdminApp.openImagePreview(currentAvatar);
    });

    $("#clearAvatarBtn").on("click", function () {
        currentAvatar = "";
        renderAvatarPreview();
    });

    $("#reloadProfileBtn").on("click", function () {
        loadProfile();
    });

    form.on("submit(saveProfile)", function (data) {
        var field = data.field || {};
        var account = String(field.account || "").trim();
        if (!account) {
            layer.msg("管理员账号不能为空");
            return false;
        }

        AdminApp.postJson("/manager/updateProfile", {
            id: AdminApp.getManagerId(),
            account: account,
            avatar: currentAvatar
        }).then(function (result) {
            if (result.code !== 1) {
                layer.alert(result.msg || "保存管理员资料失败");
                return;
            }
            AdminApp.setManagerSession(result.data || {});
            fillProfile(result.data || {});
            syncParentUi();
            layer.msg("管理员资料已保存");
        }).catch(function (xhr) {
            AdminApp.handleHttpError(xhr);
        });
        return false;
    });

    loadProfile();
});
