/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

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
