/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.productcmpttype.Messages;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class
 * <code>org.faktorips.devtools.core.model.productcmpttype.IProductCmptType</code> which are also
 * used in the creation wizard where the model object doesn't exist at the point of validation.
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

    // TODO internationalize messages
    public static Message validateSupertypeMustBeInHierarchy(IIpsProject ipsProject,
            IProductCmptType superType,
            IPolicyCmptType superPcType) throws CoreException {

        // FIXME AW: This method is completely broken!

        IProductCmptType productCmptTypeOfPolicyCmptSupertype = null;
        if (superPcType != null) {
            productCmptTypeOfPolicyCmptSupertype = superPcType.findProductCmptType(ipsProject);
            return null;
        }
        if (productCmptTypeOfPolicyCmptSupertype == null) {
            return null;
        }
        String msg = NLS.bind("The super type of the product component type must be {0} or a subtype of it",
                productCmptTypeOfPolicyCmptSupertype.getQualifiedName());
        if (superType == null) {
            return Message.newError("", msg); //$NON-NLS-1$
        }
        if (superType != null) {
            final Boolean[] holder = new Boolean[] { Boolean.FALSE };
            final IProductCmptType productCmptTypeOfPolicyCmptSupertypeFinal = productCmptTypeOfPolicyCmptSupertype;
            if (productCmptTypeOfPolicyCmptSupertype != null) {
                new TypeHierarchyVisitor(ipsProject) {
                    @Override
                    protected boolean visit(IType currentType) throws CoreException {
                        if (currentType.equals(productCmptTypeOfPolicyCmptSupertypeFinal)) {
                            holder[0] = Boolean.TRUE;
                            return false;
                        }
                        return true;
                    }
                }.start(superType);
                if (Boolean.FALSE.equals(holder[0])) {
                    return Message.newError("", msg); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

    // TODO internationalize messages
    public static Message validatePolicyCmptSuperTypeNeedsToBeX(IIpsProject ipsProject,
            String productCmptSuperType,
            String policyCmptSupertype) throws CoreException {

        if (!StringUtils.isEmpty(productCmptSuperType)) {
            IProductCmptType superType = ipsProject.findProductCmptType(productCmptSuperType);
            if (superType != null && superType.isConfigurationForPolicyCmptType()) {
                String msg = NLS.bind("The supertype of the configured policy component type must be {0}", superType
                        .getPolicyCmptType());
                if (StringUtils.isEmpty(policyCmptSupertype)) {
                    return Message.newError("", msg); //$NON-NLS-1$
                }
                IPolicyCmptType policyCmptType = ipsProject.findPolicyCmptType(policyCmptSupertype);
                if (policyCmptType == null) {
                    return Message.newError("", msg); //$NON-NLS-1$
                }
                if (!superType.getPolicyCmptType().equals(policyCmptType.getQualifiedName())) {
                    return Message.newError("", msg); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

}
