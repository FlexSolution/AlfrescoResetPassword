package com.flexsolution.resetpassword.namespaces;

import org.alfresco.service.namespace.QName;

public abstract class ResetPasswordNameSpace {

    private ResetPasswordNameSpace() {}

    private static final String MODEL_URI = "https://flex-solution.com/model/workflow/1.0";

    public static final QName QNAME_TOKEN = QName.createQName(MODEL_URI, "token");

    public static final QName QNAME_PASSWORD = QName.createQName(MODEL_URI, "password");

    public static final QName QNAME_CONFIRM_PASS = QName.createQName(MODEL_URI, "confirmPass");
}
