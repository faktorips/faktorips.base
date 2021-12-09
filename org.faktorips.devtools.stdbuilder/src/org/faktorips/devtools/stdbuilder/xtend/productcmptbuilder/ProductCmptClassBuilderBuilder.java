/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.productcmptbuilder.XProductBuilder;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.XtendTypeBuilder;
import org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.BuilderJavaClassNameProvider;
import org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template.ProductBuilderTmpl;
import org.faktorips.util.LocalizedStringsSet;

public class ProductCmptClassBuilderBuilder extends XtendTypeBuilder<XProductBuilder> {

    private IJavaClassNameProvider javaClassNameProvider;

    public ProductCmptClassBuilderBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(false, builderSet, modelContext, modelService,
                new LocalizedStringsSet(ProductCmptClassBuilderBuilder.class));
        javaClassNameProvider = new BuilderJavaClassNameProvider();
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        String configProperty = getBuilderSet().getConfig()
                .getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR);

        if (StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_ALL.equals(configProperty)
                || StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_PRODUCT.equals(configProperty)) {
            return IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
        } else {
            return false;
        }
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        return ProductBuilderTmpl.body(getGeneratorModelRoot(ipsObject));
    }

    @Override
    protected Class<XProductBuilder> getGeneratorModelRootType() {
        return XProductBuilder.class;
    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        try {
            return isBuilderFor(ipsObjectPartContainer.getIpsSrcFile());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IProductCmptType) {
            return ipsObject;
        } else {
            return null;
        }
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return true;
    }

}
