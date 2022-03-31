/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder;

import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.policycmptbuilder.XPolicyBuilder;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.XtendTypeBuilder;
import org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder.template.PolicyBuilderTmpl;
import org.faktorips.util.LocalizedStringsSet;

public class PolicyCmptClassBuilderBuilder extends XtendTypeBuilder<XPolicyBuilder> {

    private IJavaClassNameProvider javaClassNameProvider;

    public PolicyCmptClassBuilderBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(false, builderSet, modelContext, modelService,
                new LocalizedStringsSet(PolicyCmptClassBuilderBuilder.class));
        javaClassNameProvider = new BuilderJavaClassNameProvider();
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        String configProperty = getBuilderSet().getConfig()
                .getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR);
        if (StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_ALL.equals(configProperty)
                || StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_POLICY.equals(configProperty)) {
            return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
        } else {
            return false;
        }
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        return PolicyBuilderTmpl.body(getGeneratorModelRoot(ipsObject));
    }

    @Override
    protected Class<XPolicyBuilder> getGeneratorModelRootType() {
        return XPolicyBuilder.class;
    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        return isBuilderFor(ipsObjectPartContainer.getIpsSrcFile());
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
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
