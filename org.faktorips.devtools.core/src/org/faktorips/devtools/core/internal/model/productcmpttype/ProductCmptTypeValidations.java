/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;

public class ProductCmptTypeValidations {

    public Message validateSupertypeMustBeInHierarchy(IIpsProject ipsProject, IProductCmptType superType,
            IPolicyCmptType superPcType) throws CoreException {
        IProductCmptType productCmptTypeOfPolicyCmptSupertype = null;        
        if(superPcType != null){
            productCmptTypeOfPolicyCmptSupertype = superPcType.findProductCmptType(ipsProject);
            return null;
        }
        if(productCmptTypeOfPolicyCmptSupertype == null){
            return null;
        }
        String msg = NLS.bind("The super type of the product component type must be {0} or a subtype of it", productCmptTypeOfPolicyCmptSupertype.getQualifiedName());
        if(superType == null){
            return Message.newError("", msg);
        }
        if (superType != null) {
            final Boolean[] holder = new Boolean[] { Boolean.FALSE };
            final IProductCmptType productCmptTypeOfPolicyCmptSupertypeFinal = productCmptTypeOfPolicyCmptSupertype; 
            if (productCmptTypeOfPolicyCmptSupertype != null) {
                new TypeHierarchyVisitor(ipsProject) {
                    protected boolean visit(IType currentType) throws CoreException {
                        if (currentType.equals(productCmptTypeOfPolicyCmptSupertypeFinal)) {
                            holder[0] = Boolean.TRUE;
                            return false;
                        }
                        return true;
                    }
                }.start(superType);
                if (Boolean.FALSE.equals(holder[0])) {
                    return Message.newError("", msg);
                }
            }
        }
        return null;
    }

    public Message validatePolicyCmptSuperTypeNeedsToBeX(IIpsProject ipsProject,
            String productCmptSuperType,
            String policyCmptSupertype) throws CoreException {
        if (!StringUtils.isEmpty(productCmptSuperType)) {
            IProductCmptType superType = ipsProject.findProductCmptType(productCmptSuperType);
            if (superType != null && superType.isConfigurationForPolicyCmptType()) {
                String msg = NLS.bind("The supertype of the configured policy component type must be {0}", superType
                        .getPolicyCmptType());
                if (StringUtils.isEmpty(policyCmptSupertype)) {
                    return Message.newError("", msg);
                }
                IPolicyCmptType policyCmptType = ipsProject.findPolicyCmptType(policyCmptSupertype);
                if (policyCmptType == null) {
                    return Message.newError("", msg);
                }
                if (!superType.getPolicyCmptType().equals(policyCmptType.getQualifiedName())) {
                    return Message.newError("", msg);
                }
            }
        }
        return null;
    }
    
}
