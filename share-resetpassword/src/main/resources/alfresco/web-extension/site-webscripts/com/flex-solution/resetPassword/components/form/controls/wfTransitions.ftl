<@script type="text/javascript" src="${url.context}/res/extras/com/flex-solution/components/wfTransitions.js"/>

<#if form.mode == "edit" >
<script type="text/javascript">//<![CDATA[
(function()
{
    new Alfresco.Transitions.ChangePassword("${fieldHtmlId}").setOptions(
            {
                currentValue: "${field.value?js_string}"
            }).setMessages(${messages});
})();
//]]></script>

<div class="form-field suggested-actions" id="${fieldHtmlId}">
    <div id="${fieldHtmlId}-buttons">
    </div>
</div>
</#if>