package com.flexsolution.resetpassword.webscripts;

import com.flexsolution.resetpassword.namespaces.ResetPasswordNameSpace;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.util.Map;

public class TaskIsCompletePost extends DeclarativeWebScript {

    private WorkflowService workflowService;

    private static final Logger logger = Logger.getLogger(TaskIsCompletePost.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        // clear context - to avoid MT concurrency issue (causing domain mismatch)
        AuthenticationUtil.clearCurrentSecurityContext();

        Content content = req.getContent();

        try {
            final JSONObject jsonObject = new JSONObject(content.getContent());

            final String userName = jsonObject.getString("user");
            final String domain = AuthenticationUtil.getUserTenant(userName).getSecond();

            TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Object>() {
                @Override
                public Object doWork() throws Exception {
                    TenantContextHolder.setTenantDomain(domain);
                    WorkflowTask task = workflowService.getTaskById(jsonObject.getString("taskId"));
                    if(task == null
                            || task.getState().equals(WorkflowTaskState.COMPLETED)
                            || !task.getProperties().get(ResetPasswordNameSpace.QNAME_TOKEN).equals(jsonObject.getString("token"))){
                        logger.warn("Invalid 'change password' request received");
                        if (task != null){
                            logger.warn("Details can be found in this task: " + task.toString());
                        }
                        throw new AlfrescoRuntimeException("Request to change password is not valid");
                    }
                    return null;
                }
            }, userName, domain);

        } catch (JSONException | IOException e) {
            logger.error(e);
            throw new AlfrescoRuntimeException("Failed to retrieve data from json");
        }

        return null;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
}