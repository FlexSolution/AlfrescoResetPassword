import {transformTokenObject, transformUserIfExist, transformUserIdForToken} from '../support/visit-authorization.js'
import {CONTEXT_ALFRESCO} from '../support/global-variables'

export default class AlfrescoServise {

    getOrCreateUser = (filePath, admin, password, then) => {
        const cred = window.btoa(admin + ':' + password);
        cy
            .fixture(filePath)
            .then(reqBody => {
                cy.request({
                    method: 'GET',
                    url: `${CONTEXT_ALFRESCO}/s/api/people`,
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'Authorization': 'Basic ' + cred
                    }
                })
                    .then(response => {
                        return response.body
                    })
                    .then(data => {
                        data = data.people.find((el) => {
                            return el.userName === reqBody.userName
                        });

                        if (!data) {
                            cy.request({
                                method: 'POST',
                                url: `${CONTEXT_ALFRESCO}/s/api/people`,
                                body: reqBody,
                                headers: {
                                    'Accept': 'application/json',
                                    'Content-Type': 'application/json',
                                    'Authorization': 'Basic ' + cred
                                }
                            }).then(then(reqBody.userName))

                        } else {
                            then(reqBody)
                        }
                    });
            });
    };


    getResetPassTaskId = (userName, userPass, then) => {
        const cred = window.btoa(userName + ':' + userPass);
        cy.request({
            method: 'GET',
            url: `${CONTEXT_ALFRESCO}/api/-default-/public/workflow/versions/1/tasks`,
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Basic ' + cred
            }
        })
            .then(response => response.body)
            .then(data => {
                const updateData = transformUserIdForToken(data.list, userName);
                then(updateData);
            })
    };


    getToken = (adminUser, adminPassword, id, then) => {
        const cred = window.btoa(adminUser + ':' + adminPassword);
        cy.request({
            method: 'GET',
            url: `${CONTEXT_ALFRESCO}/api/-default-/public/workflow/versions/1/tasks/${id}/variables`,
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Basic ' + cred
            }
        })
            .then(response => response.body)
            .then(data => {
                let updateData = transformTokenObject(data.list);
                then(updateData);
            })
    };


    reasignTask = (filePATH, taskId, name, password) => {
        const cred = window.btoa(name + ':' + password);
        cy
            .fixture(filePATH)
            .then(filePATH => {
                cy.request({
                    method: 'PUT',
                    url: `${CONTEXT_ALFRESCO}/s/api/task-instances/activiti$${taskId}`,
                    body: filePATH,
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'Authorization': 'Basic ' + cred
                    }
                })
            });
    }

}