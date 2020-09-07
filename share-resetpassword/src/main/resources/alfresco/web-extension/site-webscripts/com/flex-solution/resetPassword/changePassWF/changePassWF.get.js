model.taskId = page.url.args["taskId"];
model.token = page.url.args["token"];
model.userToken = page.url.args["userToken"];

var jsonObj = {
    taskId: model.taskId,
    user: model.userToken,
    token: model.token
};

var connector = remote.connect("alfresco-noauth");

var resp = connector.post("/com/flex-solution/taskIsComplete", jsonUtils.toJSONString(jsonObj), "application/json");

model.showError = resp.status == 500;