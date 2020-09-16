package com.flexsolution.resetpassword.util;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

import java.util.List;

public class WorkflowHelper {

    private static WorkflowService workflowService;

    private static Logger logger = Logger.getLogger(WorkflowHelper.class);

    private static HistoryService activitiHistoryService;

    public static void cancelPreviousWorkflows(final String userName) {

        Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(userName);

        final String tenantDomain = userTenant.getSecond();

        TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {

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

    public static List<HistoricTaskInstance> getResetPassTasksByUserTokenAcrossTenants (String token){
        HistoricTaskInstanceQuery query = activitiHistoryService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .unfinished()
                .processVariableValueEquals("fs-reset:token", token);

        return query.list();
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

    public void setActivitiHistoryService (HistoryService activitiHistoryService) {
        WorkflowHelper.activitiHistoryService = activitiHistoryService;
    }
}