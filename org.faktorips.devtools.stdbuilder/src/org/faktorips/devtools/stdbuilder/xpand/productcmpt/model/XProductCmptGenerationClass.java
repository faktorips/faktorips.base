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

import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenJavaClassNameProvider;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.internal.ProductComponentGeneration;

public class XProductCmptGenerationClass extends XProductClass {

    private static final boolean CHANGE_OVER_TIME = true;

    private final IJavaClassNameProvider prodGenJavaClassNameProvider;

    public XProductCmptGenerationClass(IProductCmptType productCmptType, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(productCmptType, modelContext, modelService);
        prodGenJavaClassNameProvider = createProductCmptGenJavaClassNaming(
                modelContext.isGeneratePublishedInterfaces(), getLanguageUsedInGeneratedSourceCode());
    }

    @Override
    public boolean isChangeOverTimeClass() {
        return CHANGE_OVER_TIME;
    }

    public static ProductCmptGenJavaClassNameProvider createProductCmptGenJavaClassNaming(boolean isGeneratePublishedInterface,
            Locale locale) {
        return new ProductCmptGenJavaClassNameProvider(isGeneratePublishedInterface, locale);
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return prodGenJavaClassNameProvider;
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return super.getIpsObjectPartContainer();
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    public XProductCmptClass getProductCmptClassNode() {
        return getModelNode(getProductCmptType(), XProductCmptClass.class);
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponentGeneration.class.getName());
    }

    @Override
    public Set<XProductCmptGenerationClass> getClassHierarchy() {
        return super.getClassHierarchy(XProductCmptGenerationClass.class);
    }

    public String getMethodNameGetProductComponentGeneration() {
        return getJavaNamingConvention().getGetterMethodName(getImplClassName());
    }
}
