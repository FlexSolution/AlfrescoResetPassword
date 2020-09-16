package com.flexsolution.resetpassword.util;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class Sudo extends BaseScopableProcessorExtension {

    private final Logger logger = Logger.getLogger(Sudo.class);

    public Object su(final Function func) {

        final Scriptable scope = getScope();

        return su(func, scope);
    }

    public Object su(final Function func, final Scriptable scope, final Object... objects) {

        final Context cx = Context.getCurrentContext();

        AuthenticationUtil.RunAsWork<Object> raw = new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                return func.call(cx, ScriptableObject.getTopLevelScope(scope), scope, objects);
            }
        };

        return AuthenticationUtil.runAs(raw, AuthenticationUtil.getSystemUserName());
    }

    public Object asTenant(final String requestName, final Function func, final Scriptable scope) {

        if(logger.isDebugEnabled()) {
            logger.debug("AsTenant starts . Username: " + requestName);
            logger.debug("AsTenant function " + func.toString());
        }

        Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(requestName);
        final String tenantDomain = userTenant.getSecond();

        if (StringUtils.isBlank(tenantDomain)) {
            return su(func, scope);
        }

        TenantUtil.TenantRunAsWork<Object> work = new TenantUtil.TenantRunAsWork<Object>() {
            public Object doWork() throws Exception {
                return su(func, scope);
            }
        };

        return TenantUtil.runAsUserTenant(work, requestName, tenantDomain);
    }
}
