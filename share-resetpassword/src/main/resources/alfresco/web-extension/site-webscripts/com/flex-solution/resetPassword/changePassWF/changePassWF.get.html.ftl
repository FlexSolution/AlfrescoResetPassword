<#if !showError>

    <#assign id="rp-comp"/>


    <@link href="${url.context}/res/extras/com/flex-solution/components/changePassWF.css" group="form"/>
    <@script type="text/javascript" src="${url.context}/res/components/form/form.js" group="form"/>
    <@script type="text/javascript" src="${url.context}/res/extras/com/flex-solution/components/changePassWF.js"/>



<script type="text/javascript">//<![CDATA[

new Alfresco.component.ChangePassWF("${id}");

//]]></script>


<div id="${id}-parent" class="parent-block">

<div id="${id}" class="fields">
    <form id="${id}-form" class="formaa"
          action="${url.context}/proxy/alfresco-noauth/com/flex-solution/applyChangedPassword"
          method="post">
        <div id="comp-logo" class="theme-company-logo logo-com"></div>
        <div class="row">
                        <span class="label"><label for="${id}-new-password">${msg("form.label.new-password")}
                            :</label></span>
            <span class="formField"><input type="password" maxlength="255" id="${id}-new-password" name="new-password"/></span>
        </div>
        <div class="row">
                        <span class="label"><label
                                for="${id}-new-password-confirm">${msg("form.label.new-password-confirm")}
                            :</label></span>
            <span class="formField"><input type="password" maxlength="255" id="${id}-new-password-confirm"
                                           name="new-password-confirm"/></span>
        </div>
        <input type="hidden" name="userToken" value="${token}">

        <div class="buttons">
            <button id="${id}-button-ok" name="save">${msg("form.button.submit")}</button>
            <button id="${id}-button-cancel" name="cancel">${msg("form.button.cancel")}</button>
        </div>
    </form>
</div>
</div>


<#else>
<script type="application/javascript">
    window.location.href = Alfresco.constants.URL_CONTEXT + "page/error404";
</script>
</#if>







