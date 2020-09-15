package com.flexsolution.resetpassword.webscripts;

import com.flexsolution.resetpassword.util.WorkflowHelper;
import org.activiti.engine.history.HistoricTaskInstance;
import org.alfresco.error.AlfrescoRuntimeException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskIsCompletePost extends DeclarativeWebScript {

    private static final Logger logger = LoggerFactory.getLogger(TaskIsCompletePost.class);

    @Override
    protected Map<String, Object> executeImpl (WebScriptRequest req, Status status, Cache cache) {

        try {
            Content content = req.getContent();
            final JSONObject jsonObject = new JSONObject(content.getContent());
            final String resetPasswordToken = jsonObject.getString("token");

            if(resetPasswordToken == null) {
                logger.error("Invalid 'change password' request received. User token is null");
                throw new AlfrescoRuntimeException("Request to change password is not valid");
            }

            List<HistoricTaskInstance> cnadidateTasks = WorkflowHelper.getResetPassTasksByUserTokenAcrossTenants(resetPasswordToken);

            if(cnadidateTasks.isEmpty()) {
                logger.error("Invalid 'change password' request received. Process by token={} does not exist or has been finished", resetPasswordToken);
                throw new AlfrescoRuntimeException("Request to change password is not valid");
            }
            if(cnadidateTasks.size() != 1) {
                logger.error("Found more than one process by token={}", resetPasswordToken);
                throw new AlfrescoRuntimeException("Request to change password is not valid");
            }
            return new HashMap<String, Object>(){{
                put("message", "OK");
            }};
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new AlfrescoRuntimeException("Failed to retrieve data from json");
        }
    }
}