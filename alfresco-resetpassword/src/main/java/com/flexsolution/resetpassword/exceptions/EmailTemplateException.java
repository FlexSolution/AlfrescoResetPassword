package com.flexsolution.resetpassword.exceptions;

import org.alfresco.error.AlfrescoRuntimeException;

public class EmailTemplateException extends AlfrescoRuntimeException {

    public EmailTemplateException(String msgId) {
        super(msgId);
    }
}
