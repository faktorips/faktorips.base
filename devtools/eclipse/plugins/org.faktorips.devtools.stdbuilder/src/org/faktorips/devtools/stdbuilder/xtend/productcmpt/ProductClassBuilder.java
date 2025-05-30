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

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductClass;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xtend.XtendTypeBuilder;

public abstract class ProductClassBuilder<T extends XProductClass> extends XtendTypeBuilder<T> {

    public ProductClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService, LocalizedStringsSet localizedStringsSet) {
        super(interfaceBuilder, builderSet, modelContext, modelService, localizedStringsSet);

    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        if (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile())) {
            return true;
        }
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType polCmptType) {
            return isGeneratingArtifactsFor(polCmptType);
        } else {
            return false;
        }
    }

    protected boolean isGeneratingArtifactsFor(IPolicyCmptType polCmptType) {
        return polCmptType.isConfigurableByProductCmptType();
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        return switch (ipsObjectPartContainer.getIpsObject()) {
            case IProductCmptType productCmptType -> productCmptType;
            case IPolicyCmptType policyCmptType when policyCmptType.isConfigurableByProductCmptType() -> policyCmptType
                    .findProductCmptType(policyCmptType.getIpsProject());
            default -> null;
        };
    }

}
