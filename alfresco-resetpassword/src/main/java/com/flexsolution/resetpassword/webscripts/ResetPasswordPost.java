package com.flexsolution.resetpassword.webscripts;

import com.flexsolution.resetpassword.util.WorkflowHelper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResetPasswordPost extends DeclarativeWebScript {

    @Autowired
    private ContentService contentService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private Repository repository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private NamespaceService namespaceService;

    private WorkflowService workflowService;
    private PersonService personService;

    private static final String RESET_PASS_EMAIL_TEMPLATE_XPATH = "app:dictionary/app:email_templates/cm:workflownotification/cm:reset-password.ftl";
    private static final String WORKFLOW_NOTIFICATION_XPATH = "app:dictionary/app:email_templates/cm:workflownotification";

    private static final String RESET_PASS_FILE_NAME = "reset-password.ftl";

    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordPost.class);

    @Override
    public Map<String, Object> executeImpl (WebScriptRequest req, Status status, Cache cache) {

        final String userName = getUserNameFromRequest(req);

        // clear context - to avoid MT concurrency issue (causing domain mismatch)
        AuthenticationUtil.clearCurrentSecurityContext();

        final String tenantDomain = AuthenticationUtil.getUserTenant(userName).getSecond();

        createEmailTemplateIfNotExists(tenantDomain);

        TenantContextHolder.setTenantDomain(tenantDomain);
        AuthenticationUtil.setRunAsUser(userName);

        NodeRef user = getUserByUserName(userName);

        WorkflowHelper.cancelPreviousWorkflows(userName);

        logger.debug("Try to start workflow with user " + userName);

        startWorkFlow(user);

        logger.debug("Workflow has been started");

        return null;
    }

    private void createEmailTemplateIfNotExists (String tenantDomain) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork () throws Exception {
                if(!emailTemplateExists(tenantDomain)) {
                    addEmailTemplate(tenantDomain);
                }
                return null;
            }
        });
    }

    private boolean emailTemplateExists (String tenantDomain) {

        TenantContextHolder.setTenantDomain(tenantDomain);

        NodeRef companyHome = repository.getCompanyHome();

        List<NodeRef> emailTemplateFiles = searchService.selectNodes(companyHome, RESET_PASS_EMAIL_TEMPLATE_XPATH, null, namespaceService, false);

        return !emailTemplateFiles.isEmpty();
    }

    private void addEmailTemplate (String tenantDomain) {

        TenantContextHolder.setTenantDomain(tenantDomain);

        NodeRef companyHome = repository.getCompanyHome();

        List<NodeRef> workflowNotificationFolder = searchService.selectNodes(companyHome, WORKFLOW_NOTIFICATION_XPATH, null, namespaceService, false);

        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, RESET_PASS_FILE_NAME);

        ChildAssociationRef newEmailTemplateCreated = nodeService.createNode(workflowNotificationFolder.get(0),
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(RESET_PASS_FILE_NAME)),
                ContentModel.TYPE_CONTENT,
                properties);
        ContentWriter invWriter = contentService.getWriter(newEmailTemplateCreated.getChildRef(), ContentModel.PROP_CONTENT, true);

        String adminTenant = "";

        TenantContextHolder.setTenantDomain(adminTenant);

        companyHome = repository.getCompanyHome();

        List<NodeRef> emailTemplateFiles = searchService.selectNodes(companyHome, RESET_PASS_EMAIL_TEMPLATE_XPATH, null, namespaceService, false);

        NodeRef emailTemplate = emailTemplateFiles.get(0);

        ContentReader reader = contentService.getReader(emailTemplate, ContentModel.PROP_CONTENT);

        TenantContextHolder.setTenantDomain(tenantDomain);

        invWriter.putContent(reader);

    }

    private void startWorkFlow (final NodeRef user) {

        WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName("activiti$resetPasswordFlex");

        if(workflowDefinition == null) {
            logger.error("Workflow definition is not found activiti$resetPasswordFlex");
            throw new WebScriptException(500, "Sorry. Internal error. Try later");
        }

        Map<QName, Serializable> param = new HashMap<>();

        logger.debug("Try to set assignee");

        param.put(WorkflowModel.ASSOC_ASSIGNEE, user);

        logger.debug("Assignee: {}", param.get(WorkflowModel.ASSOC_ASSIGNEE));

        logger.debug("Try to set description");

        param.put(WorkflowModel.PROP_DESCRIPTION, "Request to change password");

        logger.debug("Try to set owner");

        param.put(ContentModel.PROP_OWNER, AuthenticationUtil.getAdminUserName());

        logger.debug("Try to start workflow...");

        workflowService.startWorkflow(workflowDefinition.getId(), param);
    }

    private NodeRef getUserByUserName (final String userName) {

        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork () throws Exception {
                NodeRef user = personService.getPersonOrNull(userName);
                if(user == null) {
                    logger.error("Failed to find user with username={}", userName);
                    throw new WebScriptException(404, "error.userNotFound");
                }
                return user;
            }
        });
    }

    private String getUserNameFromRequest (WebScriptRequest request) {
        Content content = request.getContent();

        String userName;

        try {
            JSONObject jsonObject = new JSONObject(content.getContent());

            userName = jsonObject.getString("userName");

        } catch (JSONException | IOException e) {
            logger.error(e.getMessage());
            throw new WebScriptException(500, "Sorry. Internal error. Try later");
        }

        return userName;
    }

    public void setWorkflowService (WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setPersonService (PersonService personService) {
        this.personService = personService;
    }
}
