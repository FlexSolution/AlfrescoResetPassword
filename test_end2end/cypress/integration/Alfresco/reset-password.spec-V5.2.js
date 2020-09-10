import {visitTenatePageAndCreateUser} from '../../support/visit-authorization.js'
import AlfrescoServise from '../../support/create-user-util-V5_2.js'
import {CONTEXT_SHARE} from '../../support/global-variables'
import {newPasswordGenerator} from '../../support/visit-authorization.js'

const alfrescoService = new AlfrescoServise();


describe("PASSWORD RESET CHECK V_5.2 && 6.1 && 6.2", () => {

    let testName;
    let testPassword;
    let userId;

    let createdUserId;

    const adminUser = 'admin';
    const adminPassword = 'admin';
    let tenantUser = "admin@tenant1"; //tenant credentials for registration
    let tenantUserNew = 'test@tenant1';//new created tenant name
    let tenantPassword = "tenant1";//tenant password

    let newPasswordForReset = 'testnew';

    before(() => {
        alfrescoService.checkIfUserExist('test-user-V5_2.json', adminUser, adminPassword, (userObject) => {
            userId = userObject && userObject[0] && userObject[0].entry.id;
            alfrescoService.createNewUser('test-user-V5_2.json', adminUser, adminPassword, userId, (userName) => {
                testName = userName.id;
                testPassword = userName.password;
            })
        });
    });


    const resetButton = () => cy.get('form').find('.form-field').eq(3).find('button').contains('Forgot Password?');
    const signInButton = () => cy.get('form').find('.form-field').eq(2).find('button').contains('Sign In');
    const resetPasswordField = () => cy.get('.formField').find('input[type="password"]');
    const resetInput = () => cy.get('#userInput').find('input[type="text"]');
    const resetPasswordConfirm = () => cy.get('button').contains('Reset');
    const submitButton = () => cy.get('button').contains('Submit');
    const logoutButton = () => cy.get('#HEADER_USER_MENU_LOGOUT_text');
    const userPassword = () => cy.get('input[name="password"]');
    const logoutPanel = () => cy.get('#HEADER_USER_MENU_BAR');
    const userInput = () => cy.get('input[name="username"]');
    const alert = () => cy.get(".bd");

    it('Tenant create', () => {
        visitTenatePageAndCreateUser();
        alfrescoService.checkIfUserExist('tenant-user-V5_2.json', tenantUser, tenantPassword, (userObject) => {
            userId = userObject && userObject[0] && userObject[0].entry.id;
            alfrescoService.createNewUser('tenant-user-V5_2.json', tenantUser, tenantPassword, userId, (userName) => {
                tenantUser = userName.id;
                tenantPassword = userName.password;
            })
        });
    });


    describe('Main reset tests', function () {
        beforeEach(() => {
            cy.reload();
            cy.visit(`${CONTEXT_SHARE}/page/`);

        });


        it('User friendly exception is shown on invalid username', () => {
            resetButton().click()
            resetInput().type(testName + '123');
            resetPasswordConfirm().click().wait(2000);
            alert().should('have.text', "Seems, there is no user with appropriate username")
        });


        it(' Success message is shown on valid username', () => {
            resetButton().click().wait(1000);
            resetInput().type(testName).wait(1000);
            resetPasswordConfirm().click().wait(3000);
            alert().should('have.text', "Please check your email. You should receive email with instructions how to reset password")
        });


        it('Reset password and login using new password', () => {
            resetButton().click()
            resetInput().type(testName);
            resetPasswordConfirm().click().wait(1000);
            cy.visit(`${CONTEXT_SHARE}/page/`);


            resetButton().click();
            resetInput().type(tenantUserNew);
            resetPasswordConfirm().click().wait(1000);
            alert().should('have.text', "Please check your email. You should receive email with instructions how to reset password");


            alfrescoService.getResetPassTaskId(testName, testPassword, (id) => {
                createdUserId = id;
                alfrescoService.getToken(testName, testPassword, createdUserId, (currentToken) => {
                    let newToken = currentToken;

                    cy.visit(`${CONTEXT_SHARE}/page/changePassWF?token=${newToken}`).wait(3000);
                    resetPasswordField().first().type(newPasswordForReset);
                    resetPasswordField().last().type(newPasswordForReset);
                    submitButton().click().wait(1000);
                    alert().should('have.text', "Password has been changedxx");

                    cy.visit(`${CONTEXT_SHARE}/page/`);
                    userInput().type(testName);
                    userPassword().type(newPasswordForReset);
                    signInButton().click();
                    cy.url().should('contain', 'dashboard');
                    logoutPanel().click();
                    logoutButton().click();

                });

                //endter old credentials for tenant but it still work
                userInput().type('test@tenant1');
                userPassword().type('tenant1');
                signInButton().click();
                cy.url().should('contain', 'dashboard');
                logoutPanel().click();
                logoutButton().click();
            });


        });


        it('Check authentication bypass is forbidden', () => {
            resetButton().click()
            resetInput().type(testName);
            resetPasswordConfirm().click().wait(2000);
            alfrescoService.getResetPassTaskId(testName, newPasswordForReset, (id) => {
                createdUserId = id;
                alfrescoService.getToken(testName, newPasswordForReset, createdUserId, (currentToken) => {
                    let newTokenForReasign = currentToken;
                    alfrescoService.reasignTask('reasign-user.json', createdUserId, testName, newPasswordForReset, (data) => {

                        cy.visit(`${CONTEXT_SHARE}/page/changePassWF?token=${newTokenForReasign}`);
                        resetPasswordField().first().type(newPasswordForReset + '123');
                        resetPasswordField().last().type(newPasswordForReset + '123');
                        submitButton().click().wait(1000);
                        alert().should('not.have.text', "Password has been changedxx")
                    })
                })
            })

        })
    });
});
