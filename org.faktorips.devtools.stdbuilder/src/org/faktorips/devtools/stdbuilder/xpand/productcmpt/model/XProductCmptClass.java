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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.runtime.internal.ProductComponent;

public class XProductCmptClass extends XClass {
    private final List<XProductAttribute> attributes;
    private final List<XProductAssociation> associations;

    public XProductCmptClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext model,
            ModelService modelService) {
        super(ipsObjectPartContainer, model, modelService);

        attributes = initNodes(getProductCmptType().getProductCmptTypeAttributes(), XProductAttribute.class);
        associations = initNodes(getProductCmptType().getProductCmptTypeAssociations(), XProductAssociation.class);
    }

    public List<XProductAttribute> getAttributes() {
        return new CopyOnWriteArrayList<XProductAttribute>(attributes);
    }

    public List<XProductAssociation> getAssociations() {
        return new CopyOnWriteArrayList<XProductAssociation>(associations);
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return (IProductCmptType)super.getIpsObjectPartContainer();
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponent.class);
    }

}
