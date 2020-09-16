package com.flexsolution.resetpassword.util;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Properties;
import java.util.UUID;


public class TokenGenerator extends BaseScopableProcessorExtension {
    private static final String SALT_KEY = "fs.passreset.salt";
    private static final Logger logger = LoggerFactory.getLogger(TokenGenerator.class);

    private Properties globalProps;

    public String genToken (String userName) {
        String salt = globalProps.getProperty(SALT_KEY);
        if(salt == null || salt.equals("")) {
            logger.error("Property fs.passreset.salt is missing");
            throw new AlfrescoRuntimeException("Reset Password addon is not configured");
        }
        String token = BCrypt.hashpw(userName, salt).substring(salt.length());
        return token + UUID.randomUUID().toString().substring(0, 14);
    }

    public String getHashFromToken (String token) {
        return token.substring(0, token.length() - 14);
    }

    public void setGlobalProps (Properties globalProps) {
        this.globalProps = globalProps;
    }
}


