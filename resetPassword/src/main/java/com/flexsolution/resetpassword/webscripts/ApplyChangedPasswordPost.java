package com.flexsolution.resetpassword.webscripts;

import com.flexsolution.resetpassword.namespaces.ResetPasswordNameSpace;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApplyChangedPasswordPost extends DeclarativeWebScript{

    public static final String DOMAIN_PROP = "domain";
    public static final String TASK_ID = "taskId";
    public static final String NEW_PAS = "new-password";
    public static final String NEW_PAS_CONFIRM = "new-password-confirm";
    public static final String USER_NAME = "userName";
    public static final String USER_TOKEN = "userToken";

    private WorkflowService workflowService;

    private static final Logger logger = Logger.getLogger(ApplyChangedPasswordPost.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // clear context - to avoid MT concurrency issue (causing domain mismatch)
        AuthenticationUtil.clearCurrentSecurityContext();

        final Map<String,String> data = getDataFromRequest(req);

        TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Object>() {
                @Override
                public Object doWork() throws Exception {
                    TenantContextHolder.setTenantDomain(data.get(DOMAIN_PROP));

                    WorkflowTask task = workflowService.getTaskById(data.get(TASK_ID));

                    final Map<QName,Serializable> properties = task.getProperties();
                    properties.put(ResetPasswordNameSpace.QNAME_PASSWORD, data.get(NEW_PAS));
                    properties.put(ResetPasswordNameSpace.QNAME_CONFIRM_PASS, data.get(NEW_PAS_CONFIRM));

                    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
                        @Override
                        public Object doWork() throws Exception {
                            workflowService.updateTask(data.get(TASK_ID), properties, null, null);
                            workflowService.endTask(data.get(TASK_ID), null);
                            return null;
                        }
                    });
                    return null;
                }
            }, data.get(USER_NAME), data.get(DOMAIN_PROP));

        return new HashMap<>();
    }

    private Map<String, String> getDataFromRequest(WebScriptRequest req) {

        HashMap <String, String> result = new HashMap<>();

        Content content = req.getContent();

        try {
            JSONObject jsonObject = new JSONObject(content.getContent());

            String userName = jsonObject.getString(USER_TOKEN);

            result.put(USER_NAME, userName);
            result.put(DOMAIN_PROP, AuthenticationUtil.getUserTenant(userName).getSecond());

            result.put(NEW_PAS, jsonObject.getString(NEW_PAS));
            result.put(NEW_PAS_CONFIRM, jsonObject.getString(NEW_PAS_CONFIRM));
            result.put(TASK_ID, jsonObject.getString(TASK_ID));

        } catch (Exception e) {
            logger.error(e);
            throw new WebScriptException("Failed to get data from request. Please, contact system administrator");
        }

        return result;

    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
}