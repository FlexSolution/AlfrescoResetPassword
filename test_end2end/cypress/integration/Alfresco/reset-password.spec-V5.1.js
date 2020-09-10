import {visitTenatePageAndCreateUser} from '../../support/visit-authorization.js'
import AlfrescoServise from '../../support/create-user-util-V5_1.js'
import {CONTEXT_SHARE} from '../../support/global-variables'

const alfrescoService = new AlfrescoServise();

describe("PASSWORD RESET CHECK V_5.1 && 5.0", () => {

    let createdUserId;

    let testName;
    let testPassword = 'test';
    let tenantName = "test@tenant3";
    let tenantPassword = "tenant3";

    const adminUser = 'admin';
    const adminPassword = 'admin';
    let tenantAdminName = "admin@tenant3";
    let tenantAdminPassword = "tenant3";


    const resetButton = () => cy.get('form').find('.form-field').eq(3).find('button').contains('Forgot Password?');
    const signInButton = () => cy.get('form').find('.form-field').eq(2).find('button').contains('Login');
    const resetPasswordField = () => cy.get('.formField').find('input[type="password"]');
    const resetInput = () => cy.get('#userInput').find('input[type="text"]');
    const resetPasswordConfirm = () => cy.get('button').contains('Reset');
    const submitButton = () => cy.get('button').contains('Submit');
    const logoutButton = () => cy.get('#HEADER_USER_MENU_LOGOUT_text');
    const userPassword = () => cy.get('input[name="password"]');
    const logoutPanel = () => cy.get('#HEADER_USER_MENU_BAR');
    const userInput = () => cy.get('input[name="username"]');
    const alert = () => cy.get(".bd");


    before(() => {
        alfrescoService.getOrCreateUser('test-user-V5_1.json', adminUser, adminPassword, (userName) => {
            testName = userName.id;
        });
    });

    it('Tenant create', () => {
        visitTenatePageAndCreateUser();
        alfrescoService.getOrCreateUser('tenant-user-V5_1.json', tenantAdminName, tenantAdminPassword, (tetantObject) => {
            tenantName = tetantObject.userName;
        });
    });


    describe('Main reset tests', function () {
        beforeEach(() => {
            cy.reload();
            cy.visit(`${CONTEXT_SHARE}/page/`);
        });


        it('User friendly exception is shown on invalid username', () => {
            cy.reload();
            cy.visit(`${CONTEXT_SHARE}/page/`).wait(1000);
            resetButton().click();
            resetInput().type(testName + '1234');
            resetPasswordConfirm().click().wait(1000);
            alert().should('not.have.text', "Please check your email. You should receive email with instructions how to reset password")
        });


        it('Success message is shown on valid username', () => {
            cy.reload();
            cy.visit(`${CONTEXT_SHARE}/page/`).wait(1000);
            resetButton().click().wait(2000);
            resetInput().type(testName).wait(2000);
            resetPasswordConfirm().click().wait(2000);
            alert().should('have.text', "Please check your email. You should receive email with instructions how to reset password")
        });


        it('Reset password and login using new password', () => {
            //test reset verification check and reset for token
            resetButton().click().wait(2000);
            resetInput().type(testName).wait(2000);
            resetPasswordConfirm().click().wait(1000);
            alert().should('have.text', "Please check your email. You should receive email with instructions how to reset password")

            cy.visit(`${CONTEXT_SHARE}/page/`);

            //tenant reset verification check and reset for token
            resetButton().click().wait(2000);
            resetInput().type(tenantName).wait(1000);
            resetPasswordConfirm().click().wait(1000);
            alert().should('have.text', "Please check your email. You should receive email with instructions how to reset password");


            alfrescoService.getResetPassTaskId(testName, testPassword, (id) => {
                createdUserId = id;
                alfrescoService.getToken(testName, testPassword, createdUserId, (currentToken) => {

                    let newToken = currentToken;
                    let newPassword = testPassword;
                    cy.visit(`${CONTEXT_SHARE}/page/changePassWF?token=${newToken}`);
                    resetPasswordField().first().type(newPassword);
                    resetPasswordField().last().type(newPassword);
                    submitButton().click().wait(1000);
                    alert().should('have.text', "Password has been changedxx");


                    cy.visit(`${CONTEXT_SHARE}/page/`);
                    userInput().clear().type(testName);
                    userPassword().clear().type(testPassword).wait(3000);
                    signInButton().click().wait(3000);
                    cy.url().should('contain', 'dashboard');
                    logoutPanel().click();
                    logoutButton().click();


                });

                //enter old credentials for tenant but it still work
                userInput().type(tenantName);
                userPassword().type(tenantPassword);
                signInButton().click().wait(3000);
                cy.url().should('contain', 'dashboard');
                logoutPanel().click();
                logoutButton().click();
            });

        });


        it('Check authentication bypass is forbidden', () => {
            resetButton().click().wait(1000);
            resetInput().type(testName);
            resetPasswordConfirm().click().wait(2000);
            alfrescoService.getResetPassTaskId(testName, testPassword, (id) => {
                createdUserId = id;
                alfrescoService.getToken(testName, testPassword, createdUserId, (currentToken) => {
                    let newTokenForReasign = currentToken;

                    alfrescoService.reasignTask('reasign-user.json', createdUserId, testName, testPassword);

                    cy.visit(`${CONTEXT_SHARE}/page/changePassWF?token=${newTokenForReasign}`);
                    resetPasswordField().first().type(testPassword + '123');
                    resetPasswordField().last().type(testPassword + '123');
                    submitButton().click().wait(1000);
                    alert().should('not.have.text', "Password has been changedxx")

                })
            })

        })


    });
});