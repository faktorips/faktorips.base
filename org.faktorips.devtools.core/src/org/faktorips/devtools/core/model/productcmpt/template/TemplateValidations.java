/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt.template;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValueContainer;
import org.faktorips.devtools.core.model.productcmpt.Messages;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

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
            String text = NLS.bind(Messages.TemplateValidations_error_templateCycle, template);
            ObjectProperty templateProperty = new ObjectProperty(template, IProductCmpt.PROPERTY_TEMPLATE);
            list.newError(IProductCmpt.MSGCODE_TEMPLATE_CYCLE, text, templateProperty);
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
