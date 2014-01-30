/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.policycmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.TypeBuilder;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.util.LocalizedStringsSet;

public class PolicyCmptClassBuilder extends TypeBuilder<XPolicyCmptClass> {

    public PolicyCmptClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService) {
        super(interfaceBuilder, builderSet, modelContext, modelService, new LocalizedStringsSet(
                PolicyCmptClassBuilder.class));
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected Class<XPolicyCmptClass> getGeneratorModelNodeClass() {
        return XPolicyCmptClass.class;
    }

    @Override
    public String getTemplate() {
        if (isInterfaceBuilder()) {
            return "org::faktorips::devtools::stdbuilder::xpand::policycmpt::template::PolicyCmptInterface::main";
        } else {
            return "org::faktorips::devtools::stdbuilder::xpand::policycmpt::template::PolicyCmpt::main";
        }
    }

    @Override
    public boolean isGenerateingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        try {
            if (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile())) {
                return true;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IProductCmptType) {
            IProductCmptType proCmptType = (IProductCmptType)ipsObject;
            return proCmptType.isConfigurationForPolicyCmptType();
        } else {
            return false;
        }
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            return ipsObject;
        } else if (ipsObject instanceof IProductCmptType) {
            IProductCmptType productCmptType = (IProductCmptType)ipsObject;
            if (productCmptType.isConfigurationForPolicyCmptType()) {
                try {
                    return productCmptType.findPolicyCmptType(productCmptType.getIpsProject());
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }
        return null;
    }

}
