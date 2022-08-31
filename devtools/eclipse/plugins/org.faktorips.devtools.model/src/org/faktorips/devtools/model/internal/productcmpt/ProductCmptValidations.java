/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.text.MessageFormat;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.model.internal.ipsobject.DeprecationValidation;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.Messages;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

/**
 * A class that contains validations of the model class <code>IProductCmpt</code> that are also used
 * in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Jan Ortmann
 * 
 * @since 2.3
 */
public class ProductCmptValidations {

    private ProductCmptValidations() {
        // avoid public constructor for utility class
    }

    /**
     * Checks if the product component type exists and is not abstract or deprecated.
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
            String text = MessageFormat.format(Messages.ProductCmptValidations_typeDoesNotExist, productCmptTypeName);
            list.add(new Message(IProductCmpt.MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE, text, Message.ERROR, productCmpt,
                    IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
            return null;
        }
        if (!productCmpt.isProductTemplate() && type.isAbstract()) {
            String text = MessageFormat.format(Messages.ProductCmptValidations_typeIsAbstract, productCmptTypeName);
            list.add(new Message(IProductCmpt.MSGCODE_ABSTRACT_PRODUCT_CMPT_TYPE, text, Message.ERROR, productCmpt,
                    IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE));
            return null;
        }
        DeprecationValidation.validateProductCmptTypeIsNotDeprecated(productCmpt, productCmpt.getQualifiedName(), type,
                ipsProject, list);
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
     * @param productCmpt The product component to validate
     * @param productCmptType The type of the product component
     * @param list The message list to add potential error messages to
     * @param ipsProject The project that should be used to search for other objects
     * @return The template of the product component if it was found
     */
    public static final IProductCmpt validateTemplate(IProductCmpt productCmpt,
            IProductCmptType productCmptType,
            MessageList list,
            IIpsProject ipsProject) {
        String templateName = productCmpt.getTemplate();

        if (IpsStringUtils.isNotEmpty(templateName)) {
            IProductCmpt template = ipsProject.findProductTemplate(templateName);
            if (template != null) {
                ObjectProperty typeProperty = new ObjectProperty(productCmpt, IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
                ObjectProperty templateProperty = new ObjectProperty(productCmpt, IProductCmpt.PROPERTY_TEMPLATE);
                validateTemplateType(productCmptType, list, ipsProject, template, typeProperty, templateProperty);
                validateValidFrom(list, template, productCmpt);
            } else {
                String text = MessageFormat.format(Messages.ProductCmptValidations_error_invalidTemplate, templateName);
                list.newError(IProductCmpt.MSGCODE_INVALID_TEMPLATE, text, productCmpt, IProductCmpt.PROPERTY_TEMPLATE);
            }
            return template;
        } else {
            return null;
        }
    }

    private static void validateTemplateType(IProductCmptType productCmptType,
            MessageList list,
            IIpsProject ipsProject,
            IProductCmpt template,
            ObjectProperty... invalidProperties) {
        IProductCmptType templateCmptType = findProductCmptType(template.getProductCmptType(), ipsProject);
        if (templateCmptType != null) {
            if (!isSubtypeOrSame(productCmptType, templateCmptType, ipsProject)) {
                list.newError(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_TYPE,
                        Messages.ProductCmptValidations_error_inconsistentTemplateType, invalidProperties);
            }
        }
    }

    protected static void validateValidFrom(MessageList list, IProductCmpt template, IProductCmpt productCmpt) {
        if (productCmpt.getValidFrom().before(template.getValidFrom())) {
            list.newError(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_VALID_FROM,
                    Messages.ProductCmptValidations_error_validFromTemplate, productCmpt,
                    IProductCmpt.PROPERTY_TEMPLATE);
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
