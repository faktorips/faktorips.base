/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.policycmpt;

import java.util.Set;

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xtend.XtendTypeBuilder;
import org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptInterfaceTmpl;
import org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyCmptTmpl;
import org.faktorips.runtime.IModelObject;

public class PolicyCmptClassBuilder extends XtendTypeBuilder<XPolicyCmptClass> {

    public PolicyCmptClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService) {
        super(interfaceBuilder, builderSet, modelContext, modelService,
                new LocalizedStringsSet(PolicyCmptClassBuilder.class));
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected Class<XPolicyCmptClass> getGeneratorModelRootType() {
        return XPolicyCmptClass.class;
    }

    @Override
    protected Set<String> getAllSuperTypeNames(IIpsSrcFile ipsSrcFile) {
        Set<String> allSuperTypeNames = super.getAllSuperTypeNames(ipsSrcFile);
        // this is the base interface containing STOP/CONTINUE_VALIDATION
        // we need to add it for the first build where the java type and it's hierarchy don't exist
        allSuperTypeNames.add(IModelObject.class.getName());
        return allSuperTypeNames;
    }

    @Override
    public String generateBody(IIpsObject ipsObject) {
        if (generatesInterface()) {
            return PolicyCmptInterfaceTmpl.body(getGeneratorModelRoot(ipsObject));
        } else {
            return PolicyCmptTmpl.body(getGeneratorModelRoot(ipsObject));

        }
    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        if (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile())) {
            return true;
        }
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IProductCmptType proCmptType) {
            return proCmptType.isConfigurationForPolicyCmptType();
        } else {
            return false;
        }
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        return switch (ipsObjectPartContainer.getIpsObject()) {
            case IPolicyCmptType policyCmptType -> policyCmptType;
            case IProductCmptType productCmptType when productCmptType
                    .isConfigurationForPolicyCmptType() -> productCmptType
                            .findPolicyCmptType(productCmptType.getIpsProject());
            default -> null;
        };
    }

}
