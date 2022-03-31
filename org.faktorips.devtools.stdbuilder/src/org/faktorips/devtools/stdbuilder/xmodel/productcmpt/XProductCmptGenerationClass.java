/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XMethod;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute.GenerateValueSetType;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.productcmpt.ProductCmptGenJavaClassNameProvider;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.internal.ProductComponentGeneration;

public class XProductCmptGenerationClass extends XProductClass {

    private static final boolean CHANGE_OVER_TIME = true;

    private final ProductCmptGenJavaClassNameProvider prodGenJavaClassNameProvider;

    public XProductCmptGenerationClass(IProductCmptType productCmptType, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(productCmptType, modelContext, modelService);
        prodGenJavaClassNameProvider = createProductCmptGenJavaClassNaming(
                getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject()),
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
        String implClassName = prodGenJavaClassNameProvider
                .getImplClassName(getIpsObjectPartContainer().getIpsSrcFile());
        return QNameUtil.getUnqualifiedName(implClassName);
    }

    @Override
    public boolean isChangeOverTimeClass() {
        return CHANGE_OVER_TIME;
    }

    public static ProductCmptGenJavaClassNameProvider createProductCmptGenJavaClassNaming(
            boolean isGeneratePublishedInterface,
            Locale locale) {
        return new ProductCmptGenJavaClassNameProvider(isGeneratePublishedInterface, locale);
    }

    @Override
    public ProductCmptGenJavaClassNameProvider getJavaClassNameProvider() {
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
        Set<XMethod> optionalMethods = new HashSet<>();
        Set<XMethod> methods = getMethods();
        for (XMethod xMethod : methods) {
            if (xMethod.isFormulaOptional() && !xMethod.isOverloadsFormula()) {
                optionalMethods.add(xMethod);
            }
        }

        return optionalMethods;
    }

    @Override
    public Set<XPolicyAttribute> getConfiguredAttributes() {
        return getConfiguredAttributes(a -> a.isChangingOverTime() && !a.isAbstract());
    }

    public Set<XPolicyAttribute> getConfiguredAttributesIncludingAbstract() {
        return getConfiguredAttributes(XPolicyAttribute::isChangingOverTime);
    }

    public Set<XPolicyAttribute> attributesFromSupertypeWhenDifferentUnifyValueSetSettingsFor(
            GenerateValueSetType valueSetType) {
        return attributesFromSupertypeWhenDifferentUnifyValueSetSettingsFor(XPolicyAttribute::isChangingOverTime,
                valueSetType);
    }
}
