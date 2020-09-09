package com.flexsolution.resetpassword.webscripts;

import com.flexsolution.resetpassword.util.TokenGenerator;
import com.flexsolution.resetpassword.util.WorkflowHelper;
import org.activiti.engine.history.HistoricTaskInstance;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplyChangedPasswordPost extends DeclarativeWebScript {

    public static final String NEW_PAS = "new-password";
    public static final String NEW_PAS_CONFIRM = "new-password-confirm";
    public static final String TOKEN = "userToken";


    private WorkflowService workflowService;
    private MutableAuthenticationService authenticationService;
    private TokenGenerator tokenGenerator;

    private static final Logger logger = LoggerFactory.getLogger(ApplyChangedPasswordPost.class);

    @Override
    protected Map<String, Object> executeImpl (WebScriptRequest req, Status status, Cache cache) {

        final Map<String, String> data = getDataFromRequest(req);

        String pass = data.get(NEW_PAS);
        String confirmPass = data.get(NEW_PAS_CONFIRM);

        if(!Objects.equals(pass, confirmPass)) {
            logger.error("Password and confirm password are not equal");
            throw new AlfrescoRuntimeException("Password and confirm password are not equal");
        }

        String incomingToken = data.get(TOKEN);
        List<HistoricTaskInstance> candidateTasks = WorkflowHelper.getResetPassTasksByUserTokenAcrossTenants(incomingToken);

        if(candidateTasks.isEmpty()) {
            logger.error("Invalid 'change password' request received. Process by token={} does not exist or has been finished", incomingToken);
            throw new AlfrescoRuntimeException("Request to change password is not valid");
        }
        if(candidateTasks.size() != 1) {
            logger.error("Found more than one process by token={}", incomingToken);
            throw new AlfrescoRuntimeException("Request to change password is not valid");
        }
        final HistoricTaskInstance historicTaskInstance = candidateTasks.get(0);

        String assignee = historicTaskInstance.getAssignee();
        String hashForCurrentAssignee = tokenGenerator.getHashFromToken(tokenGenerator.genToken(assignee));

        if(!hashForCurrentAssignee.equals(tokenGenerator.getHashFromToken(incomingToken))) {
            logger.error("Invalid 'change password' request received. Token={} is not valid for user {}", incomingToken, assignee);
            throw new AlfrescoRuntimeException("Request to change password is not valid");
        }

        String tenant_domain = (String) historicTaskInstance.getProcessVariables().get(ActivitiConstants.VAR_TENANT_DOMAIN);

        if(tenant_domain == null) {
            tenant_domain = "";
        }

        // clear context - to avoid MT concurrency issue (causing domain mismatch)
        AuthenticationUtil.clearCurrentSecurityContext();

        TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Object>() {

            @Override
            public Object doWork () throws Exception {
                String activitiTaskId = "activiti$" + historicTaskInstance.getId();
                authenticationService.setAuthentication(assignee, pass.toCharArray());
                workflowService.endTask(activitiTaskId, null);
                return null;
            }
        }, tenant_domain);

        return new HashMap<String, Object>() {{
            put("message", "OK");
        }};
    }

    private Map<String, String> getDataFromRequest (WebScriptRequest req) {

        HashMap<String, String> result = new HashMap<>();
        Content content = req.getContent();

        try {
            JSONObject jsonObject = new JSONObject(content.getContent());

            result.put(TOKEN, jsonObject.getString(TOKEN));
            result.put(NEW_PAS, jsonObject.getString(NEW_PAS));
            result.put(NEW_PAS_CONFIRM, jsonObject.getString(NEW_PAS_CONFIRM));

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new WebScriptException("Failed to get data from request. Please, contact system administrator");
        }
        return result;
    }

    public void setWorkflowService (WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setAuthenticationService (MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setTokenGenerator (TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }
}