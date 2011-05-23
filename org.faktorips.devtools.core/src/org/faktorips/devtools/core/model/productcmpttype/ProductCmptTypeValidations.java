/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.Messages;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class {@link IProductCmptType} which are also used
 * in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Peter Erzberger
 */
public class ProductCmptTypeValidations {

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
     * @throws CoreException delegates raised exceptions
     */
    public static Message validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(boolean isPolicyCmptTypeAbstract,
            boolean isProductCmptTypeAbstract,
            IProductCmptType thisProductCmptType) throws CoreException {

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
     * @throws CoreException If an error occurs during the validation
     */
    public static Message validateSupertype(IProductCmptType productCmptType,
            IProductCmptType superProductCmptType,
            String policyCmptType,
            String superPolicyCmptType,
            IIpsProject ipsProject) throws CoreException {

        Message message = null;
        ObjectProperty[] invalidObjectProperties = productCmptType == null ? new ObjectProperty[0]
                : new ObjectProperty[] { new ObjectProperty(productCmptType, IProductCmptType.PROPERTY_SUPERTYPE),
                        new ObjectProperty(productCmptType, IProductCmptType.PROPERTY_POLICY_CMPT_TYPE) };

        if (superProductCmptType == null) {
            if (superPolicyCmptType.length() > 0) {
                String text = Messages.ProductCmptType_MustInheritFromASupertype;
                message = new Message(IProductCmptType.MSGCODE_MUST_HAVE_SUPERTYPE, text, Message.ERROR,
                        invalidObjectProperties);
            }
        } else {
            String policyCmptTypeOfSupertype = superProductCmptType.getPolicyCmptType();
            if (!policyCmptType.equals(policyCmptTypeOfSupertype)
                    && !superPolicyCmptType.equals(policyCmptTypeOfSupertype)) {
                String text = Messages.ProductCmptType_InconsistentTypeHierarchies;
                message = new Message(IProductCmptType.MSGCODE_HIERARCHY_MISMATCH, text, Message.ERROR,
                        invalidObjectProperties);
            }
        }

        return message;
    }

}
