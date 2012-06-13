/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;

public class XPolicyCmptClass extends XClass {

    private final ArrayList<XPolicyAttribute> attributes;

    public XPolicyCmptClass(IPolicyCmptType policyCmptType, GeneratorModelContext context, ModelService modelService) {
        super(policyCmptType, context, modelService);
        attributes = new ArrayList<XPolicyAttribute>();
        initAttributeNodes();
        initAssociationNodes();
    }

    private void initAttributeNodes() {
        List<IPolicyCmptTypeAttribute> policyAttributes = getPolicyCmptType().getPolicyCmptTypeAttributes();
        for (IPolicyCmptTypeAttribute attr : policyAttributes) {
            attributes.add(createAttributeNode(attr));
        }
    }

    private XPolicyAttribute createAttributeNode(IPolicyCmptTypeAttribute attribute) {
        return getModelService().createModelNode(attribute, XPolicyAttribute.class, getModelContext());
    }

    private void initAssociationNodes() {
        // TODO Auto-generated method stub

    }

    @Override
    public IPolicyCmptType getIpsObjectPartContainer() {
        return (IPolicyCmptType)super.getIpsObjectPartContainer();
    }

    /**
     * @return Returns the policyCmptType.
     */
    public IPolicyCmptType getPolicyCmptType() {
        return getIpsObjectPartContainer();
    }

    public boolean hasSupertype() {
        return getPolicyCmptType().hasSupertype();
    }

    public boolean isConfigured() {
        return getPolicyCmptType().isConfigurableByProductCmptType();
    }

    public String getProductCmptClassName() {
        try {
            IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(
                    getIpsObjectPartContainer().getIpsProject());
            return addImport(getModelContext().getQualifiedClassName(productCmptType));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isAggregateRoot() {
        try {
            return getPolicyCmptType().isAggregateRoot();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isImplementsInterface() {
        return !getImplementedInterface().isEmpty();
    }

    public List<String> getImplementedInterface() {
        ArrayList<String> list = new ArrayList<String>();
        if (getModelContext().isGeneratePropertyChange() && !hasSupertype()) {
            list.add(addImport(INotificationSupport.class));
        }
        return list;
    }

    public String getSuperclassName() {
        try {
            if (getPolicyCmptType().hasSupertype()) {
                IType superType = getPolicyCmptType().findSupertype(getIpsProject());
                return addImport(getModelContext().getQualifiedClassName(superType));
            } else {
                if (isConfigured()) {
                    return addImport(AbstractConfigurableModelObject.class);
                } else {
                    return addImport(AbstractModelObject.class);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public List<XPolicyAttribute> getAttributes() {
        return new ArrayList<XPolicyAttribute>(attributes);
    }

    public List<XPolicyAssociation> getAssociations() {
        // TODO Auto-generated method stub
        return null;
    }
}
