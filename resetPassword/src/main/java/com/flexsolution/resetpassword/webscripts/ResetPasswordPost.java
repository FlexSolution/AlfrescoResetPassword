package com.flexsolution.resetpassword.webscripts;

import com.flexsolution.resetpassword.util.WorkflowHelper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ResetPasswordPost extends DeclarativeWebScript {

    private WorkflowService workflowService;
    private PersonService personService;

    private static final Logger logger = Logger.getLogger(ResetPasswordPost.class);

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        final String userName = getUserNameFromRequest(req);

        // clear context - to avoid MT concurrency issue (causing domain mismatch)
        AuthenticationUtil.clearCurrentSecurityContext();

        final String tenantDomain = AuthenticationUtil.getUserTenant(userName).getSecond();

        TenantContextHolder.setTenantDomain(tenantDomain);

        NodeRef user = getUserByUserName(userName);

        WorkflowHelper.cancelPreviousWorkflows(userName);

        logger.debug("Try to start workflow with user " + userName);

        startWorkFlow(user, AuthenticationUtil.getUserTenant(userName).getSecond());

        logger.debug("Workflow has been started");

        return null;
    }

    private void startWorkFlow(final NodeRef user, final String tenantDomain) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {

                TenantContextHolder.setTenantDomain(tenantDomain);

                WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName("activiti$resetPassword");

                if (workflowDefinition == null) {
                    workflowDefinition = WorkflowHelper.deployResetPasswordWorkflow();
                }

                Map<QName, Serializable> param = new HashMap<>();

                logger.debug("Try to set assignee");

                param.put(WorkflowModel.ASSOC_ASSIGNEE, user);

                logger.debug("Assignee: " + param.get(WorkflowModel.ASSOC_ASSIGNEE));

                logger.debug("Try to set description");

                param.put(WorkflowModel.PROP_DESCRIPTION, "Request to change password");

                logger.debug("Try to set owner");

                param.put(ContentModel.PROP_OWNER, user);

                logger.debug("Try to start workflow...");

                workflowService.startWorkflow(workflowDefinition.getId(), param);

                return null;
            }
        });
    }

    private NodeRef getUserByUserName(final String userName) {

        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception {
                NodeRef user = personService.getPersonOrNull(userName);
                if (user == null) {
                    logger.error("Failed to find user with username " + userName);
                    throw new WebScriptException(404, "User with username " + userName + " not found");
                }
                return user;
            }
        });
    }

    private String getUserNameFromRequest(WebScriptRequest request) {
        Content content = request.getContent();

        String userName;

        try {
            JSONObject jsonObject = new JSONObject(content.getContent());

            userName = jsonObject.getString("userName");

        } catch (JSONException | IOException e) {
            logger.error(e);
            throw new WebScriptException(500, "Sorry. Internal error. Try later");
        }

        return userName;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
