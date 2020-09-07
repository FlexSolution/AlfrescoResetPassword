(function () {

    Alfresco.component.ConfirmPassword = function (htmlId) {
        Alfresco.component.ConfirmPassword.superclass.constructor.call(this, "Alfresco.component.ConfirmPassword", htmlId);

        return this;
    };

    YAHOO.extend(Alfresco.component.ConfirmPassword, Alfresco.component.Base,
        {


            getPassField: function () {
                return YAHOO.util.Dom.get(this.id.replace(/_prop_fs-reset_confirmPass/g, "_prop_fs-reset_password"));
            },

            addValidation: function (event, args) {
                args[1].runtime.addValidation(this.id, Alfresco.forms.validation.passwordMatch, {el: this.getPassField()}, "keyup,mouseover", Alfresco.util.message("form.field.confirm"));
            },

            onReady: function () {

                YAHOO.Bubbling.on("afterFormRuntimeInit", this.addValidation, this);
            }
        }
    )
})();