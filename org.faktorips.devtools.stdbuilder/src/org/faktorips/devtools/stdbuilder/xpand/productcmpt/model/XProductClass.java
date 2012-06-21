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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;

public abstract class XProductClass extends XClass {

    public XProductClass(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
    }

    protected List<IProductCmptTypeAssociation> getChangableAssociations() {
        return getProductAssociations(true);
    }

    protected List<IProductCmptTypeAssociation> getStaticAssociations() {
        return getProductAssociations(false);
    }

    protected List<IProductCmptTypeAssociation> getProductAssociations(boolean changableAssociations) {
        List<IProductCmptTypeAssociation> resultingAssociations = new ArrayList<IProductCmptTypeAssociation>();
        List<IProductCmptTypeAssociation> allAssociations = getProductCmptType().getProductCmptTypeAssociations();
        for (IProductCmptTypeAssociation assoc : allAssociations) {
            if (changableAssociations) {
                resultingAssociations.add(assoc);
            }
        }
        return resultingAssociations;
    }

    protected List<IProductCmptTypeAttribute> getChangeableAttributes() {
        return getProductAttributes(true);
    }

    protected List<IProductCmptTypeAttribute> getStaticAttributes() {
        return getProductAttributes(false);
    }

    protected List<IProductCmptTypeAttribute> getProductAttributes(boolean changableAttributes) {
        List<IProductCmptTypeAttribute> resultingAttributes = new ArrayList<IProductCmptTypeAttribute>();
        List<IProductCmptTypeAttribute> allAttributes = getProductCmptType().getProductCmptTypeAttributes();
        for (IProductCmptTypeAttribute attr : allAttributes) {
            if (changableAttributes == attr.isChangingOverTime()) {
                resultingAttributes.add(attr);
            }
        }
        return resultingAttributes;
    }

    private IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return (IProductCmptType)super.getIpsObjectPartContainer();
    }

    @Override
    public abstract List<XProductAttribute> getAttributes();

    @Override
    public abstract List<XProductAssociation> getAssociations();

}