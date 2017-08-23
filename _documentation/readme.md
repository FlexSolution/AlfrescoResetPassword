###This addon support *Alfresco Multitenancy*

If you install this addon after creation tenants, you need:

 - to upload file **{alfresco.installation.folder}/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/extension/workflow/ResetPasswordProcess.bpmn20.xml** into alfresco repository **/Data Dictionary/Workflow Definitions** folder (into each tenant):
 - to upload file **{alfresco.installation.folder}/tomcat/webapps/alfresco/WEB-INF/classes/alfresco/module/resetPassword/bootstrap/reset-password.ftl** into alfresco repository **/Data Dictionary/Email Templates/Workflow Notification** folder (into each tenant):
