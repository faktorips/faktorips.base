/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.util.LocalizedStringsSet;

public class ProductCmptGenerationClassBuilder extends ProductClassBuilder<XProductCmptGenerationClass> {

    private final IJavaClassNameProvider javaClassNameProvider;

    public ProductCmptGenerationClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService) {
        super(interfaceBuilder, builderSet, modelContext, modelService, new LocalizedStringsSet(
                ProductCmptGenerationClassBuilder.class));
        javaClassNameProvider = XProductCmptGenerationClass.createProductCmptGenJavaClassNaming(
                modelContext.isGeneratePublishedInterfaces(builderSet.getIpsProject()),
                getLanguageUsedInGeneratedSourceCode());
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected Class<XProductCmptGenerationClass> getGeneratorModelNodeClass() {
        return XProductCmptGenerationClass.class;
    }

    @Override
    public String getTemplate() {
        if (isInterfaceBuilder()) {
            return "org::faktorips::devtools::stdbuilder::xpand::productcmpt::template::ProductComponentGenInterface::main";
        } else {
            return "org::faktorips::devtools::stdbuilder::xpand::productcmpt::template::ProductComponentGen::main";
        }
    }

    @Override
    protected boolean generatesInterface() {
        return isInterfaceBuilder();
    }

}
