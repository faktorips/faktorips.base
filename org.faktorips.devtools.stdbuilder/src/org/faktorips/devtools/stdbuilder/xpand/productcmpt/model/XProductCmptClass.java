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

import java.util.Set;

import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.internal.ProductComponent;

public class XProductCmptClass extends XProductClass {

    private static final boolean CHANGE_OVER_TIME = false;

    public XProductCmptClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
    }

    @Override
    public boolean isChangeOverTimeClass() {
        return CHANGE_OVER_TIME;
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponent.class);
    }

    @Override
    public Set<XProductCmptClass> getClassHierarchy() {
        return super.getClassHierarchy(XProductCmptClass.class);
    }

    protected XProductCmptGenerationClass getProductCmptGenerationNode() {
        XProductCmptGenerationClass productCmptGenerationClass = getModelNode(getProductCmptType(),
                XProductCmptGenerationClass.class);
        return productCmptGenerationClass;
    }

    public String getProductCmptGenClassName() {
        XProductCmptGenerationClass productCmptGenerationClass = getProductCmptGenerationNode();
        return productCmptGenerationClass.getSimpleName(BuilderAspect.INTERFACE);
    }

    public String getProductCmptGenClassName(BuilderAspect aspect) {
        XProductCmptGenerationClass productCmptGenerationClass = getProductCmptGenerationNode();
        return productCmptGenerationClass.getSimpleName(aspect);
    }

    public String getMethodNameGetProductComponentGeneration() {
        XProductCmptGenerationClass productCmptGenerationClass = getProductCmptGenerationNode();
        return getJavaNamingConvention().getGetterMethodName(
                productCmptGenerationClass.getSimpleName(BuilderAspect.IMPLEMENTATION));
    }

}
