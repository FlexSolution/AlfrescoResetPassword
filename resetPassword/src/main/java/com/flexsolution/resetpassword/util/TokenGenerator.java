package com.flexsolution.resetpassword.util;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;

import java.util.UUID;


public class TokenGenerator extends BaseScopableProcessorExtension {

    public String genToken() {
        return UUID.randomUUID().toString();
    }
}


