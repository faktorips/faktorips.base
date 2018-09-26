/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.XtendTypeBuilder;
import org.faktorips.util.LocalizedStringsSet;

public abstract class ProductClassBuilder<T extends XProductClass> extends XtendTypeBuilder<T> {

    public ProductClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService, LocalizedStringsSet localizedStringsSet) {
        super(interfaceBuilder, builderSet, modelContext, modelService, localizedStringsSet);

    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        try {
            if (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile())) {
                return true;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            IPolicyCmptType polCmptType = (IPolicyCmptType)ipsObject;
            return polCmptType.isConfigurableByProductCmptType();
        } else {
            return false;
        }
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IProductCmptType) {
            return ipsObject;
        } else if (ipsObject instanceof IPolicyCmptType) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
            if (policyCmptType.isConfigurableByProductCmptType()) {
                try {
                    return policyCmptType.findProductCmptType(policyCmptType.getIpsProject());
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }
        return null;
    }

}