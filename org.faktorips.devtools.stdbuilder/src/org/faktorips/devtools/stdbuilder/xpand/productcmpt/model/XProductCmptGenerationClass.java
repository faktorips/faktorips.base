/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XMethod;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptGenJavaClassNameProvider;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.internal.ProductComponentGeneration;

public class XProductCmptGenerationClass extends XProductClass {

    private static final boolean CHANGE_OVER_TIME = true;

    private final IJavaClassNameProvider prodGenJavaClassNameProvider;

    public XProductCmptGenerationClass(IProductCmptType productCmptType, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(productCmptType, modelContext, modelService);
        prodGenJavaClassNameProvider = createProductCmptGenJavaClassNaming(isGeneratePublishedInterfaces(),
                getLanguageUsedInGeneratedSourceCode());
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        LinkedHashSet<String> extendedInterfaces = super.getExtendedInterfaces();
        if (!hasSupertype()) {
            extendedInterfaces.add(addImport(IProductComponentGeneration.class));
        }
        return extendedInterfaces;
    }

    @Override
    public String getName() {
        String implClassName = prodGenJavaClassNameProvider.getImplClassName(getIpsObjectPartContainer()
                .getIpsSrcFile());
        return QNameUtil.getUnqualifiedName(implClassName);
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
        return getJavaNamingConvention().getGetterMethodName(getName());
    }

    public String getMethodNameSetProductComponentGeneration() {
        return getJavaNamingConvention().getSetterMethodName(getName());
    }

    public Set<XMethod> getOptionalFormulas() {
        Set<XMethod> optionalMethods = new HashSet<XMethod>();
        Set<XMethod> methods = getMethods();
        for (XMethod xMethod : methods) {
            if (xMethod.isFormulaOptional() && !xMethod.isOverloadsFormula()) {
                optionalMethods.add(xMethod);
            }
        }

        return optionalMethods;
    }
}
