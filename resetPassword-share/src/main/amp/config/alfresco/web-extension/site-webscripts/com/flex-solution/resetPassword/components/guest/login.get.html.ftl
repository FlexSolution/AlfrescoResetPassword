<@markup id="reset-pass-css" target="js" action="after">
<#-- CSS Dependencies -->
    <@link href="${url.context}/res/extras/com/flex-solution/components/ResetPassword.css" group="login"/>

<script type="text/javascript">
    new FlexSolution.component.ResetPassword("${args.htmlid}-resetPassword");
</script>
</@markup>

<@markup id="reset-pass-js">
<#-- JavaScript Dependencies -->
    <@script src="${url.context}/res/extras/com/flex-solution/components/ResetPassword.js" group="login"/>
</@markup>

<@markup id="reset-password" target="buttons" action="after" scope="page">
<div class="form-field">
    <input type="button" id="${args.htmlid}-resetPassword" class="login-button hidden"
           value="${msg("button.resetPassword")}"/>
</div>
</@>
