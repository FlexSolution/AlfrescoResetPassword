<import resource="classpath:alfresco/extension/templates/webscripts/com/flex-solution/reset-pass-lib.js">

var ResetPassword = function () {

    this.reviewCreate = function () {
        if (typeof bpm_workflowDueDate != 'undefined') task.dueDate = bpm_workflowDueDate;
        if (typeof bpm_workflowPriority != 'undefined') task.priority = bpm_workflowPriority;

        //Send Email
        this._sendEmail("reset-password.ftl", bpm_assignee.properties.email, {})
    };

    this._sendEmail = function (templateName, to) {
        try {
            var template = emailHelper.getLocalizedEmailTemplate("app:dictionary/app:email_templates/cm:workflownotification/cm:" + templateName);
        } catch (e) {
            throw e.javaException.getMessage();
        }
        
        var mail = actions.create("mail");
        var currentToken = token.genToken(bpm_assignee.properties.userName);

        mail.parameters.template = template;
        mail.parameters.to = to;
        mail.parameters.subject = template.properties.title || "Reset Password";
        mail.parameters.template_model = this.prepareTemplateProps(bpm_assignee, task, currentToken);
        task.setVariable("fs-reset:token", currentToken);

        try {

            mail.execute(bpm_assignee);

        } catch (ex) {

            throw "error.outBound";
        }

    };

    this.prepareTemplateProps = function (assignee, task, currentToken) {
        var templateProps = {};
        templateProps["assignee"] = {
            firstname: assignee.properties["firstName"],
            lastname: assignee.properties["lastName"]
        };
        templateProps["token"] = currentToken;
        return templateProps;
    }
};