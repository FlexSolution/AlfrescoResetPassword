import {CONTEXT_ALFRESCO, CONTEXT} from '../support/global-variables'


export function visitTenatePageAndCreateUser() {
    cy.visit(`${CONTEXT_ALFRESCO}/s/admin/admin-tenantconsole`, {
        auth: {
            username: 'admin',
            password: 'admin'
        }
    });
    const tenantRegInput = () => cy.get(`input[name="tenant-cmd"]`);
    const executeInput = () => cy.get(`input[type="button"]`).should('have.value', 'Execute');
    tenantRegInput().type('create tenant3 tenant3 /usr/tenantstores/tenant3');
    executeInput().click();
}


export function transformTokenObject(obj) {

    let tokenObj = obj.entries.filter((el) => {
        return el.entry.name === "fs-reset:token";
    });

    return tokenObj[0].entry.value;

}

export function transformUserIfExist(obj, objResponse) {
    return obj.entries.filter((el) => {
        return el.entry.id === objResponse.id;
    })
}

export function transformUserIdForToken(obj, currentUser) {
    return obj.entries.find(function (e) {
        return e.entry.assignee === currentUser;
    }).entry.id;
}


export function newPasswordGenerator(oldPassword) {
    var abc = "abcdefghijklmnopqrstuvwxyz";
    var rs = "";
    while (rs.length < 6) {
        rs += abc[Math.floor(Math.random() * abc.length)];
    }
    return oldPassword + rs;
}



