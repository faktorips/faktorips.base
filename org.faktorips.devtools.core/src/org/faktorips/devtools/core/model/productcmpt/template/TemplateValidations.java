/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt.template;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedPropertyContainer;
import org.faktorips.devtools.core.model.productcmpt.Messages;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.message.Message;
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

    /**
     * Validates that the given template does not have the same type as its parent template if it
     * has a parent template. Adds a message to the list if it has the same type as its parent
     * template.
     * 
     * @param template the template to validate
     * @param list the message list to which a message is added
     * @param ipsProject the template's project
     */
    public static void validateTemplateTypeDiffersFromParentTemplate(IProductCmpt template,
            MessageList list,
            IIpsProject ipsProject) {
        if (template == null || !template.isProductTemplate()) {
            return;
        }

        IProductCmpt parentTemplate = template.findTemplate(ipsProject);
        if (parentTemplate == null) {
            return;
        }

        String text = NLS.bind(Messages.TemplateValidations_warning_mutlipleTemplatesWithSameType, template.getName());
        Message warning = Message.newWarning(IProductCmpt.MSGCODE_MULTIPLE_TEMPLATES_WITH_SAME_TYPE, text, template,
                IProductCmpt.PROPERTY_TEMPLATE);
        validateTemplateTypeDiffers(template.findProductCmptType(ipsProject), parentTemplate, warning, list, ipsProject);
    }

    /**
     * Validates that a new template of the given type would not have the same type as the given
     * parent template. Adds a message to the list if it would have the same type.
     * 
     * @param templateType the type the new template would have
     * @param templateName the name the new template would have
     * @param parentTemplate the parent template the new template would have
     * @param list the message list to which a message is added
     * @param ipsProject the new template's project
     */
    public static void validateTemplateTypeDiffersFromParentTemplate(IProductCmptType templateType,
            String templateName,
            IProductCmpt parentTemplate,
            MessageList list,
            IIpsProject ipsProject) {
        if (parentTemplate == null || !parentTemplate.isProductTemplate()) {
            return;
        }

        String text = NLS.bind(Messages.TemplateValidations_warning_mutlipleTemplatesWithSameType,
                StringUtils.trimToEmpty(templateName));
        Message warning = Message.newWarning(IProductCmpt.MSGCODE_MULTIPLE_TEMPLATES_WITH_SAME_TYPE, text, null, null);
        validateTemplateTypeDiffers(templateType, parentTemplate, warning, list, ipsProject);
    }

    private static void validateTemplateTypeDiffers(IProductCmptType templateType,
            IProductCmpt parentTemplate,
            Message warning,
            MessageList list,
            IIpsProject ipsProject) {
        if (templateType == null || parentTemplate == null) {
            return;
        }

        IProductCmptType parentTemplateType = parentTemplate.findProductCmptType(ipsProject);
        if (sameType(templateType, parentTemplateType)) {
            list.add(warning);
        }

    }

    private static boolean sameType(IProductCmptType type1, IProductCmptType type2) {
        return type1 != null && type2 != null && type1.equals(type2);

    }

    /** Visitor that detects a cycle in a template's template hierarchy. */
    private static class TemplateCycleDetectionVisitor extends TemplateHierarchyVisitor<ITemplatedPropertyContainer> {

        public TemplateCycleDetectionVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(ITemplatedPropertyContainer currentType) {
            return true;
        }

    }
}
