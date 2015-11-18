/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <code>IProductCmpt</code> that are also used
 * in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Jan Ortmann
 * 
 * @since 2.3
 */
public class ProductCmptValidations {

    /**
     * Checks if the product component type exists and is not abstract.
     * 
     * @param productCmpt The product component that is checked or <code>null</code> if it does not
     *            exist, yet.
     * @param productCmptTypeName Qualified name of the product component type.
     * @param list The list error messages are added to.
     * @param ipsProject The IPS project used to search.
     * 
     * @return The product component type if it exists and is not abstract.
     * 
     */
    /* Tests can be found in ProductCmptTest */
    public static final IProductCmptType validateProductCmptType(IProductCmpt productCmpt,
            String productCmptTypeName,
            MessageList list,
            IIpsProject ipsProject) {
        IProductCmptType type = findProductCmptType(productCmptTypeName, ipsProject);
        if (type == null) {
            String text = NLS.bind(
                    org.faktorips.devtools.core.model.productcmpt.Messages.ProductCmptValidations_typeDoesNotExist,
                    productCmptTypeName);
            list.add(new Message(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE, text, Message.ERROR, productCmpt,
                    IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
            return null;
        }
        if (!productCmpt.isTemplate() && type.isAbstract()) {
            String text = NLS.bind(
                    org.faktorips.devtools.core.model.productcmpt.Messages.ProductCmptValidations_typeIsAbstract,
                    productCmptTypeName);
            list.add(new Message(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE, text, Message.ERROR, productCmpt,
                    IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
            return null;
        }
        return type;
    }

    /**
     * Validates that the template of a product component:
     * <ul>
     * <li>First validates that the template exists if the template name is not empty</li>
     * <li>Second validates that the product component type of a product component is covariant to
     * the product component type of its template.</li>
     * </ul>
     * 
     * @param productCmptType The product component type of the product component that should be
     *            validated
     * @param templateName The name of the template that is referenced by the product component
     * @param list The message list to add the potential error message
     * @param ipsProject The project that should be used to search for other objects
     * @return The template of the product component if it was found
     */
    public static final IProductCmpt validateTemplate(IProductCmptType productCmptType,
            String templateName,
            ObjectProperty templateObjectProperty,
            MessageList list,
            IIpsProject ipsProject) {
        if (StringUtils.isNotEmpty(templateName)) {
            IProductCmpt template = ipsProject.findProductTemplate(templateName);
            if (template != null) {
                IProductCmptType templateCmptType = findProductCmptType(template.getProductCmptType(), ipsProject);
                if (templateCmptType != null) {
                    if (!isSubtypeOrSame(productCmptType, templateCmptType, ipsProject)) {
                        list.newError(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE,
                                Messages.ProductCmptValidations_error_inconsistentTemplateType, templateObjectProperty);
                    }
                }
            } else {
                list.newError(IProductCmpt.MSGCODE_INVALID_TEMPLATE,
                        NLS.bind(Messages.ProductCmptValidations_error_invalidTemplate, templateName),
                        templateObjectProperty);
            }
            return template;
        } else {
            return null;
        }
    }

    private static boolean isSubtypeOrSame(IProductCmptType productCmptType,
            IProductCmptType templateCmptType,
            IIpsProject ipsProject) {
        return productCmptType.isSubtypeOrSameType(templateCmptType, ipsProject);
    }

    private static IProductCmptType findProductCmptType(String productCmptTypeName, IIpsProject ipsProject) {
        return ipsProject.findProductCmptType(productCmptTypeName);
    }
}
