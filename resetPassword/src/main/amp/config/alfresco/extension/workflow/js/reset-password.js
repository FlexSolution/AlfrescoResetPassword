<import resource="classpath:alfresco/extension/templates/webscripts/com/flex-solution/reset-pass-lib.js">

var ResetPassword = function () {

    this.reviewCreate = function () {
        if (typeof bpm_workflowDueDate != 'undefined') task.dueDate = bpm_workflowDueDate;
        if (typeof bpm_workflowPriority != 'undefined') task.priority = bpm_workflowPriority;

        //Send Email
        this._sendEmail("reset-password.ftl", bpm_assignee.properties.email, {})
    };

    this.reviewComplete = function () {

        var pass = task.getVariable("fs-reset_password");
        pass2 = task.getVariable("fs-reset_confirmPass");

        if (!isValid(pass, pass2)) {
            throw "Confirm password does not match password or password length too short";
        }

        var password = task.getVariableLocal("fs-reset_password"),
            userName = bpm_assignee.properties.userName;

        passwordService.setPassword(userName, password);
    };

    this._sendEmail = function (templateName, to) {
        
        
        try {
            var template = emailHelper.getLocalizedEmailTemplate("app:dictionary/app:email_templates/cm:workflownotification/cm:" + templateName);
        } catch (e) {
            throw e.javaException.getMessage();
        }
        
        var mail = actions.create("mail");
        var currentToken = token.genToken();

        mail.parameters.template = template;
        mail.parameters.to = to;
        mail.parameters.subject = template.properties.title || "Reset Password";
        mail.parameters.template_model = this.prepareTemplateProps(bpm_assignee, task, currentToken);
        task.setVariable("fs-reset:token", currentToken);

        try {

            mail.execute(bpm_assignee);

        } catch (ex) {

            throw "Failed to send email. Please, check outbound email configuration.";
        }

    };

    this.prepareTemplateProps = function (assignee, task, currentToken) {
        var templateProps = {};
        templateProps["userToken"] = assignee.properties["userName"];
        templateProps["assignee"] = {
            firstname: assignee.properties["firstName"],
            lastname: assignee.properties["lastName"]
        };
        templateProps["taskId"] = "activiti$" + task.id;
        templateProps["token"] = currentToken;
        return templateProps;
    }
};