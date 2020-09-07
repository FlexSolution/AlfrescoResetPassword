
(function () {

    Alfresco.component.ChangePassWF = function (htmlId) {
        Alfresco.component.ChangePassWF.superclass.constructor.call(this, "Alfresco.component.ChangePassWF", htmlId);

        return this;
    };

    YAHOO.extend(Alfresco.component.ChangePassWF, Alfresco.component.Base,
        {


            onCancel: function () {
                window.location.href = Alfresco.constants.URL_CONTEXT;
            },


            initForm: function () {

                var form = new Alfresco.forms.Form(this.id + "-form");
                var passField = YAHOO.util.Dom.get(this.id + "-new-password");
                var confPassField = YAHOO.util.Dom.get(this.id + "-new-password-confirm");

                Alfresco.util.createYUIButton(this, "button-ok", null,
                    {
                        type: "submit"
                    });

                Alfresco.util.createYUIButton(this, "button-cancel", this.onCancel);

                form.addValidation(passField.id, Alfresco.forms.validation.mandatory, null, "keyup,mouseover", Alfresco.util.message("form.field.mandatory"));
                form.addValidation(passField.id, Alfresco.forms.validation.length, {minLength: 3}, "keyup,mouseover", Alfresco.util.message("form.field.length"));
                form.addValidation(confPassField.id, Alfresco.forms.validation.passwordMatch, {el: passField}, "keyup,mouseover", Alfresco.util.message("form.field.confirm"));
                form.addValidation(confPassField.id, Alfresco.forms.validation.mandatory, null, "keyup,mouseover", Alfresco.util.message("form.field.mandatory"));

                form.setSubmitAsJSON(true);
                form.setAJAXSubmit(true,
                    {
                        successCallback: {
                            fn: function (obj) {

                                //redirect to home page on success
                                function onClickBut() {
                                    window.location.href = Alfresco.constants.URL_CONTEXT;
                                }

                                Alfresco.util.PopupManager.displayPrompt({
                                    text: Alfresco.util.message("form.success.message"),
                                    buttons: [{
                                        text: Alfresco.util.message("form.button.sign.in"),
                                        handler: onClickBut
                                    }]
                                });
                            },
                            scope: this
                        },

                        failureCallback: {
                            fn: function (obj) {
                                Alfresco.util.PopupManager.displayPrompt({
                                    text: !obj.json ? obj.serverResponse.statusText : obj.json.message
                                });
                            },
                            scope: this
                        }
                    });

                form.init();
            },


            onReady: function () {
                this.initForm();
            }
        }
    )
})();