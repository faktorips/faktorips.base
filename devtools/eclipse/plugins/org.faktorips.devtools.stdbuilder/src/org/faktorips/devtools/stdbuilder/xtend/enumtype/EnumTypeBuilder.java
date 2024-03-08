/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.enumtype;

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xtend.XtendBuilder;
import org.faktorips.devtools.stdbuilder.xtend.enumtype.template.EnumTypeTmpl;

public class EnumTypeBuilder extends XtendBuilder<XEnumType> {

    private IJavaClassNameProvider javaClassNameProvider;

    public EnumTypeBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(builderSet, modelContext, modelService, new LocalizedStringsSet(EnumTypeBuilder.class));
        javaClassNameProvider = XEnumType.createEnumJavaClassNameProvider(
                modelContext.getBaseGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject()));
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        return EnumTypeTmpl.body(getGeneratorModelRoot(ipsObject));
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.ENUM_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected Class<XEnumType> getGeneratorModelRootType() {
        return XEnumType.class;
    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        return isBuilderFor(ipsObjectPartContainer.getIpsSrcFile());
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IEnumType) {
            return ipsObject;
        } else {
            return null;
        }
    }

    @Override
    protected boolean isBuildingPublishedSourceFile() {
        return true;
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

}
