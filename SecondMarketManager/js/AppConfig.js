(function (window) {
    var DEFAULT_HOST = "127.0.0.1";
    var DEFAULT_PORT = "9088";
    var BASE_URL_STORAGE_KEY = "managerServerBaseUrl";
    var HOST_STORAGE_KEY = "managerServerHost";

    function trimSlashes(value) {
        return String(value || "").replace(/\/+$/, "");
    }

    function getRuntimeHost() {
        var storedHost = trimSlashes(window.localStorage.getItem(HOST_STORAGE_KEY));
        if (storedHost) {
            return storedHost;
        }

        if (window.location && window.location.hostname) {
            return window.location.hostname;
        }

        return DEFAULT_HOST;
    }

    function getBaseUrl() {
        var storedBaseUrl = trimSlashes(window.localStorage.getItem(BASE_URL_STORAGE_KEY));
        if (storedBaseUrl) {
            return storedBaseUrl;
        }

        var protocol = window.location && /^https?:$/.test(window.location.protocol)
            ? window.location.protocol
            : "http:";
        return protocol + "//" + getRuntimeHost() + ":" + DEFAULT_PORT;
    }

    function buildApiUrl(path) {
        var requestPath = String(path || "").replace(/^\/+/, "");
        return trimSlashes(getBaseUrl()) + "/" + requestPath;
    }

    function getDownloadUrl(name) {
        return buildApiUrl("common/download?name=" + encodeURIComponent(String(name || "").trim()));
    }

    function resolveFileUrl(value) {
        var text = String(value || "").trim();
        if (!text) {
            return "";
        }

        if (/^https?:\/\//i.test(text)) {
            return text;
        }

        if (/^common\/download/i.test(text) || /^\/common\/download/i.test(text)) {
            return buildApiUrl(text);
        }

        return getDownloadUrl(text);
    }

    function alertHttpError(message) {
        var tip = message || "无法连接到服务端，请检查接口地址、端口与网络状态。";
        if (window.layer && window.layer.alert) {
            window.layer.alert(tip, {
                title: "请求失败"
            });
            return;
        }
        window.alert(tip);
    }

    window.host = getRuntimeHost();
    window.port = DEFAULT_PORT;
    window.pname = "";
    window.baseurl = trimSlashes(getBaseUrl()) + "/";
    window.getBaseUrl = getBaseUrl;
    window.buildApiUrl = buildApiUrl;
    window.getDownloadUrl = getDownloadUrl;
    window.resolveFileUrl = resolveFileUrl;
    window.alertHttpError = alertHttpError;
})(window);
