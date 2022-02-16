/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpttype;

import java.text.MessageFormat;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

/**
 * A class that contains validations of the model class {@link IProductCmptType} which are also used
 * in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Peter Erzberger
 */
public class ProductCmptTypeValidations {

    private ProductCmptTypeValidations() {
        // do not instantiate
    }

    /**
     * Validates the rule that if a policy component type is abstract then the configuring product
     * component type needs to be abstract.
     * 
     * @param isPolicyCmptTypeAbstract the value of the property abstract of the policy component
     *            type
     * @param isProductCmptTypeAbstract the value of the property abstract of the product component
     *            type
     * @param thisProductCmptType the product component type instance if available if not
     *            <code>null</code> is an accepted value
     * @return a message instance if the validation fails otherwise <code>null</code>
     * 
     * @throws IpsException delegates raised exceptions
     */
    public static Message validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(boolean isPolicyCmptTypeAbstract,
            boolean isProductCmptTypeAbstract,
            IProductCmptType thisProductCmptType) {

        if (isPolicyCmptTypeAbstract && !isProductCmptTypeAbstract) {
            return new Message(IProductCmptType.MSGCODE_PRODUCTCMPTTYPE_ABSTRACT_WHEN_POLICYCMPTTYPE_ABSTRACT,
                    Messages.ProductCmptType_msgProductCmptTypeAbstractWhenPolicyCmptTypeAbstract, Message.ERROR,
                    thisProductCmptType != null ? new ObjectProperty[] { new ObjectProperty(thisProductCmptType,
                            IProductCmptType.PROPERTY_ABSTRACT) } : new ObjectProperty[0]);
        }
        return null;
    }

    /**
     * Validates the supertype property of the product component type to be validated.
     * <ol>
     * <li>If the policy component type that is configured by the product component type to be
     * validated has a supertype, the product component type must have a supertype as well
     * <li>If the product component type to be validated has a supertype, then the configured policy
     * component type must be the same policy component type that is configured by the super product
     * component type or one of it's direct descendants
     * </ol>
     * 
     * @param productCmptType The product component type to be validated or null if it doesn't exist
     *            yet
     * @param superProductCmptType The product component type that is the supertype of the product
     *            component type to be validated or null if none is found
     * @param policyCmptType The qualified name of the policy component type that is configured by
     *            the product component type to be validated
     * @param superPolicyCmptType The qualified name of the supertype of the policy component type
     *            that is configured by the product component type to be validated or an empty
     *            string if no such supertype exists
     * @param ipsProject The IPS project that is used for the validation
     * 
     * @throws IpsException If an error occurs during the validation
     */
    public static Message validateSupertype(IProductCmptType productCmptType,
            IProductCmptType superProductCmptType,
            String policyCmptType,
            String superPolicyCmptType,
            IIpsProject ipsProject) {

        Message message = null;
        ObjectProperty[] invalidObjectProperties = productCmptType == null ? new ObjectProperty[0]
                : new ObjectProperty[] { new ObjectProperty(productCmptType, IProductCmptType.PROPERTY_SUPERTYPE),
                        new ObjectProperty(productCmptType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE) };

        if (superProductCmptType == null) {
            IPolicyCmptType foundSuperPolicyCmptType = ipsProject.findPolicyCmptType(superPolicyCmptType);
            if (foundSuperPolicyCmptType != null && foundSuperPolicyCmptType.isConfigurableByProductCmptType()) {
                String text = Messages.ProductCmptType_MustInheritFromASupertype;
                message = new Message(IProductCmptType.MSGCODE_MUST_HAVE_SUPERTYPE, text, Message.ERROR,
                        invalidObjectProperties);
            }
        } else {
            String policyCmptTypeOfProductSupertype = superProductCmptType.getPolicyCmptType();
            if (!isConsistentHierarchy(policyCmptType, superPolicyCmptType, policyCmptTypeOfProductSupertype,
                    superProductCmptType, ipsProject)) {
                String text = Messages.ProductCmptType_InconsistentTypeHierarchies;
                message = new Message(IProductCmptType.MSGCODE_HIERARCHY_MISMATCH, text, Message.ERROR,
                        invalidObjectProperties);
            }
        }

        return message;
    }

    /**
     * Validates the changingOverTime property of the product component type.
     * <p>
     * If the product component type that has to be validated has a supertype, then the supertype
     * must have the same setting for changingOverTime property.
     * 
     * @param list The message list containing all validation messages.
     * @param productCmptType The product component type to be validated
     * @param superProductCmptType The product component type that is the supertype of the product
     *            component type that has to be validated or null if none is found
     * 
     */
    public static void validateSuperProductCmptTypeHasSameChangingOverTimeSetting(MessageList list,
            IProductCmptType productCmptType,
            IProductCmptType superProductCmptType) {
        if (superProductCmptType == null) {
            return;
        }
        if (productCmptType.isChangingOverTime() != superProductCmptType.isChangingOverTime()) {
            String changingOverTimePluralName = IIpsModelExtensions.get().getModelPreferences()
                    .getChangesOverTimeNamingConvention()
                    .getGenerationConceptNamePlural();
            String text = MessageFormat.format(
                    Messages.ProductCmptType_error_settingChangingOverTimeDiffersFromSettingInSupertype,
                    changingOverTimePluralName, superProductCmptType.getQualifiedName());
            list.newError(IProductCmptType.MSGCODE_SETTING_CHANGING_OVER_TIME_DIFFERS_FROM_SUPERTYPE, text,
                    productCmptType, IProductCmptType.PROPERTY_CHANGING_OVER_TIME);
        }

    }

    static boolean isConsistentHierarchy(String policyCmptType,
            String superPolicyCmptType,
            String policyCmptTypeOfProductSupertype,
            IProductCmptType superProductCmptType,
            IIpsProject ipsProject) {
        if (isPCTypeConfiguredByConsistentProductCmptType(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfProductSupertype)) {
            return true;
        } else {
            IPolicyCmptType foundSuperPolicyCmptType = ipsProject.findPolicyCmptType(superPolicyCmptType);
            return isPolicyAndProductCmptTypeNotConfigured(superProductCmptType, foundSuperPolicyCmptType);
        }
    }

    /**
     * Allow a product component type and its supertype to configure the same policy component type.
     */
    private static boolean isPCTypeConfiguredByConsistentProductCmptType(String policyCmptType,
            String superPolicyCmptType,
            String policyCmptTypeOfProductSupertype) {
        return policyCmptType.equals(policyCmptTypeOfProductSupertype)
                || superPolicyCmptType.equals(policyCmptTypeOfProductSupertype);
    }

    /**
     * Ensure that if the product component type is not configured, the given policy component type
     * is also not configured. The configured case is checked in
     * {@link #isPCTypeConfiguredByConsistentProductCmptType(String, String, String)}.
     */
    private static boolean isPolicyAndProductCmptTypeNotConfigured(IProductCmptType superProductCmptType,
            IPolicyCmptType foundSuperPolicyCmptType) {
        return !superProductCmptType.isConfigurationForPolicyCmptType()
                && (foundSuperPolicyCmptType == null || !foundSuperPolicyCmptType.isConfigurableByProductCmptType());
    }

}
