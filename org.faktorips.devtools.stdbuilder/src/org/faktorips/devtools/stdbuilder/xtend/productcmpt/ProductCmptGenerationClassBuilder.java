/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductComponentGenInterfaceTmpl;
import org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductComponentGenTmpl;
import org.faktorips.datatype.util.LocalizedStringsSet;

public class ProductCmptGenerationClassBuilder extends ProductClassBuilder<XProductCmptGenerationClass> {

    private final IJavaClassNameProvider javaClassNameProvider;

    public ProductCmptGenerationClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService) {
        super(interfaceBuilder, builderSet, modelContext, modelService,
                new LocalizedStringsSet(ProductCmptGenerationClassBuilder.class));
        javaClassNameProvider = XProductCmptGenerationClass.createProductCmptGenJavaClassNaming(
                modelContext.getBaseGeneratorConfig().isGeneratePublishedInterfaces(builderSet.getIpsProject()),
                getLanguageUsedInGeneratedSourceCode());
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        if (IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType())) {
            return isChangingOverTime(ipsSrcFile) || isGenerateDeprecatedGeneration(ipsSrcFile);
        }
        return false;

    }

    @Override
    protected boolean isGeneratingArtifactsFor(IPolicyCmptType polCmptType) {
        if (polCmptType.isConfigurableByProductCmptType()) {
            IProductCmptType productCmptType = polCmptType.findProductCmptType(polCmptType.getIpsProject());
            return productCmptType.isChangingOverTime();
        }
        return false;
    }

    /**
     * Returns whether the product component type of the given {@link IIpsSrcFile} is changing over
     * time or not. If the {@link IIpsSrcFile} does not exists we assume it was changing over time
     * to delete previously created java files.
     */
    private boolean isChangingOverTime(IIpsSrcFile ipsSrcFile) {
        return !ipsSrcFile.exists()
                || Boolean.valueOf(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_CHANGING_OVER_TIME));
    }

    /**
     * If the generated java file already exists we need to build the generation also if the type
     * currently is not changing over time. This is used to generate &#64;deprecated annotations in
     * the generation class files.
     */
    private boolean isGenerateDeprecatedGeneration(IIpsSrcFile ipsSrcFile) {
        return getJavaFile(ipsSrcFile).exists();
    }

    @Override
    protected Class<XProductCmptGenerationClass> getGeneratorModelRootType() {
        return XProductCmptGenerationClass.class;
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        if (generatesInterface()) {
            return ProductComponentGenInterfaceTmpl.body(getGeneratorModelRoot(ipsObject));
        } else {
            return ProductComponentGenTmpl.body(getGeneratorModelRoot(ipsObject));

        }
    }

}
