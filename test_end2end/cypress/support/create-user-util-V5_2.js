import {transformTokenObject, transformUserIfExist, transformUserIdForToken} from '../support/visit-authorization.js'
import {CONTEXT_ALFRESCO} from '../support/global-variables'

export default class AlfrescoServise {

    checkIfUserExist = (filePath, admin, password, then) => {
        const cred = window.btoa(admin + ':' + password);
        cy
            .fixture(filePath)
            .then(filePath => {
                cy.request({
                    method: 'GET',
                    url: `${CONTEXT_ALFRESCO}/api/-default-/public/alfresco/versions/1/people`,
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
                        const updateData = transformUserIfExist(data.list, filePath)
                        then(updateData)
                    });
            });
    };


    createNewUser = (dataPath, name, password, userId, then) => {
        const cred = window.btoa(name + ':' + password);
        cy
            .fixture(dataPath)
            .then(dataPath => {

                if (userId === undefined) {
                    cy.request({
                        method: 'POST',
                        url: `${CONTEXT_ALFRESCO}/api/-default-/public/alfresco/versions/1/people`,
                        body: dataPath,
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
                            then(data.entry)
                        })
                } else {
                    then(dataPath)
                }
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


    reasignTask = (filePATH, taskId, name, password, then) => {

        const cred = window.btoa('admin' + ':' + 'admin');
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
                    .then(response => {
                        return response.body
                    })
                    .then(data => {
                        then(data)
                    })
            });

    }

}