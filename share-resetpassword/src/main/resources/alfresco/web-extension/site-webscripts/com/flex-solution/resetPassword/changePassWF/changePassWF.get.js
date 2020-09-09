model.token = page.url.args["token"];

var jsonObj = {
    token: model.token
};

var connector = remote.connect("alfresco-noauth");

var resp = connector.post("/com/flex-solution/taskIsComplete", jsonUtils.toJSONString(jsonObj), "application/json");

model.showError = resp.status == 500;