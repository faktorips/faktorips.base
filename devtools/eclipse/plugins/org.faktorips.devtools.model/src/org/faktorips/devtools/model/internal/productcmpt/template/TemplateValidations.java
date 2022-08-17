/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import java.text.MessageFormat;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.Messages;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateHierarchyVisitor;
import org.faktorips.runtime.MessageList;

public class TemplateValidations {

    private TemplateValidations() {
        // Utility class that should not be instantiated
    }

    /**
     * Validates that given template's template hierarchy does not contain a cycle. Adds a message
     * to the list if it does.
     * 
     * @param template the template to validate
     * @param list the message list to which a message is added if a cycle is found
     * @param ipsProject the template's project
     */
    public static void validateTemplateCycle(IProductCmpt template, MessageList list, IIpsProject ipsProject) {
        if (template == null || !template.isProductTemplate()) {
            return;
        }
        TemplateCycleDetectionVisitor visitor = new TemplateCycleDetectionVisitor(ipsProject);
        visitor.start(template);
        if (visitor.cycleDetected()) {
            String text = MessageFormat.format(Messages.TemplateValidations_error_templateCycle, template);
            list.newError(IProductCmpt.MSGCODE_TEMPLATE_CYCLE, text, template, IProductCmpt.PROPERTY_TEMPLATE);
        }
    }

    /** Visitor that detects a cycle in a template's template hierarchy. */
    private static class TemplateCycleDetectionVisitor extends TemplateHierarchyVisitor<ITemplatedValueContainer> {

        public TemplateCycleDetectionVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(ITemplatedValueContainer currentType) {
            return true;
        }

    }
}
