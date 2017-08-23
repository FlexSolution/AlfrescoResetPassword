/**
 * Transitions form component.
 *
 * @namespace Alfresco
 * @class Alfresco.Transitions
 */
(function () {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML;

    /**
     * Transitions constructor.
     *
     * @param {String} htmlId The HTML id of the parent element
     * @return {Alfresco.Transitions} The new Transitions instance
     * @constructor
     */
    Alfresco.Transitions.ChangePassword = function (htmlId) {
        Alfresco.Transitions.superclass.constructor.call(this, "Alfresco.Transitions.ChangePassword", htmlId, ["button", "container"]);

        return this;
    };

    YAHOO.extend(Alfresco.Transitions.ChangePassword, Alfresco.Transitions,
        {

            onClick: function Transitions_onClick(e, p_obj) {

                //listen form validation event
                YAHOO.Bubbling.on("formValidationError", function () {
                    p_obj.set("disabled", false);
                });

                //MNT-2196 fix, disable transition button to prevent multiple execution
                p_obj.set("disabled", true);

                // determine what button was pressed by it's id
                var buttonId = p_obj.get("id");
                var transitionId = buttonId.substring(this.id.length + 1);

                // get the hidden field
                var hiddenField = this._getHiddenField();

                // set the hidden field value
                Dom.setAttribute(hiddenField, "value", transitionId);

                if (Alfresco.logger.isDebugEnabled())
                    Alfresco.logger.debug("Set transitions hidden field to: " + transitionId);

                // attempt to submit the form
                Alfresco.util.submitForm(p_obj.getForm());
            },
        });
})();