package com.flexsolution.resetpassword.services.script;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.security.MutableAuthenticationService;

public class ScriptPasswordService extends BaseScopableProcessorExtension {

    private MutableAuthenticationService authenticationService;

    public void setPassword(String userName, String password) {
            authenticationService.setAuthentication(userName, password.toCharArray());
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}