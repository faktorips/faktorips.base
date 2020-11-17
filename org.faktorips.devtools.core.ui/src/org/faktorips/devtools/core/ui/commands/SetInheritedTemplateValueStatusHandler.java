package org.faktorips.devtools.core.ui.commands;

import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

/**
 * A handler to set the template value status of property values to
 * {@link TemplateValueStatus#INHERITED}.
 */
public class SetInheritedTemplateValueStatusHandler extends SetTemplateValueStatusHandler {

    public SetInheritedTemplateValueStatusHandler() {
        super(TemplateValueStatus.INHERITED);
    }
}