package com.flexsolution.resetpassword.util;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.WebScriptException;

import java.util.List;

public class WorkflowHelper {

    private static WorkflowService workflowService;

    private static final String SEARCH_QUERY = "TYPE:\"bpm:workflowdefinition\" AND =@cm\\:name:\"ResetPasswordProcess.bpmn20.xml\"";
    private static final String DEPLOY_DEFINITION_ERROR = "Failed to load 'activiti$resetPassword' definition: file 'ResetPasswordProcess.bpmn20.xml' not found";
    private static final QName ENGINE_ID = QName.createQName("http://www.alfresco.org/model/bpm/1.0", "engineId");

    private static Logger logger = Logger.getLogger(WorkflowHelper.class);

    private static SearchService searchService;
    private static Repository repository;
    private static NodeService nodeService;

    public static void cancelPreviousWorkflows(final String userName) {

        Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(userName);

        final String tenantDomain = userTenant.getSecond();

        TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                TenantContextHolder.setTenantDomain(tenantDomain);

                List<WorkflowTask> workflowTasks = getTasksInProgress(userName);

                if(logger.isDebugEnabled()) {
                    logger.debug("Found workflow tasks = " + workflowTasks.size());
                }

                for (WorkflowTask task : workflowTasks) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Current task: " + task.getName());
                    }

                    if ("fs-reset:review".equals(task.getName())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Try to end task " + task.toString());
                        }
                        workflowService.cancelWorkflow(task.getPath().getInstance().getId());
                    }
                }
                return null;
            }
        }, userName, tenantDomain);
    }

    public static WorkflowDefinition deployResetPasswordWorkflow() {

        workflowService.deployDefinition(getWorkflowDefinitionNodeRef());

        return workflowService.getDefinitionByName("activiti$resetPasswordFlex");
    }

    private static NodeRef getWorkflowDefinitionNodeRef() {

        ResultSet results = null;

        try {
            results = searchService.query(getSearchParameters());

            if (logger.isDebugEnabled()) {
                logger.debug("Result length " + results.length());
            }

            if (results.length() < 1) {
                logger.error(DEPLOY_DEFINITION_ERROR);
                throw new WebScriptException(404, DEPLOY_DEFINITION_ERROR);
            }

            NodeRef wfDefNode = results.getNodeRef(0);

            nodeService.setProperty(wfDefNode, ENGINE_ID, ActivitiConstants.ENGINE_ID);

            return wfDefNode;

        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    private static SearchParameters getSearchParameters() {

        SearchParameters parameters = new SearchParameters();

        parameters.addStore(repository.getCompanyHome().getStoreRef());

        parameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);

        parameters.setQuery(SEARCH_QUERY);

        return parameters;
    }

    private static List<WorkflowTask> getTasksInProgress(String userName) {

        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setEngineId(ActivitiConstants.ENGINE_ID);
        query.setTaskState(WorkflowTaskState.IN_PROGRESS);
        query.setActorId(userName);

        if (logger.isDebugEnabled()) {
            logger.debug("Try to delete workflows...");
        }

        return workflowService.queryTasks(query, true);
    }

    public void setWorkflowService(WorkflowService workflowService) {
        WorkflowHelper.workflowService = workflowService;
    }

    public void setSearchService(SearchService searchService) {
        WorkflowHelper.searchService = searchService;
    }

    public void setRepository(Repository repository) {
        WorkflowHelper.repository = repository;
    }

    public void setNodeService(NodeService nodeService) {
        WorkflowHelper.nodeService = nodeService;
    }
}