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

import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.XtendTypeBuilder;
import org.faktorips.devtools.stdbuilder.xtend.policycmpt.template.PolicyValidatorTmpl;
import org.faktorips.util.LocalizedStringsSet;

public class PolicyCmptValidatorBuilder extends XtendTypeBuilder<XPolicyCmptClass> {

    private final IJavaClassNameProvider javaClassNameProvider;

    public PolicyCmptValidatorBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(false, builderSet, modelContext, modelService,
                new LocalizedStringsSet(PolicyCmptValidatorBuilder.class));
        javaClassNameProvider = new ValidatorJavaClassNameProvider(builderSet.isGeneratePublishedInterfaces());
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        if (IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType())) {
            // make sure validator class is deleted if the type is removed
            if (!ipsSrcFile.exists()) {
                return true;
            }
            IPolicyCmptType cmpt = (IPolicyCmptType)ipsSrcFile.getIpsObject();
            // existing validator classes should be deprecated instead of deleted to
            // preserve manual calls and custom implementations
            return cmpt.isGenerateValidatorClass() || isValidatorClassAlreadyPresent(ipsSrcFile);
        } else {
            return false;
        }
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        return PolicyValidatorTmpl.body(getGeneratorModelRoot(ipsObject));
    }

    @Override
    protected Class<XPolicyCmptClass> getGeneratorModelRootType() {
        return XPolicyCmptClass.class;
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

    private boolean isValidatorClassAlreadyPresent(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        return getJavaFile(ipsSrcFile).exists();
    }
}
