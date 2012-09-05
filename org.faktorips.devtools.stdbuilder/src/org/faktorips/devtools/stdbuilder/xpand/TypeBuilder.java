/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.util.LocalizedStringsSet;

public abstract class TypeBuilder<T extends AbstractGeneratorModelNode> extends XpandBuilder<T> {

    private final boolean interfaceBuilder;

    public TypeBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, modelContext, modelService, localizedStringsSet);
        this.interfaceBuilder = interfaceBuilder;
    }

    @Override
    public boolean isGeneratsArtifactsFor(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)
                || ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE);
    }

    public boolean isInterfaceBuilder() {
        return interfaceBuilder;
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        try {
            if (!isBuilderFor(ipsObject.getIpsSrcFile())) {
                if (ipsObject instanceof IPolicyCmptType) {
                    IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
                    if (policyCmptType.isConfigurableByProductCmptType()) {
                        ipsObject = policyCmptType.findProductCmptType(getIpsProject());
                    } else {
                        return null;
                    }
                } else if (ipsObject instanceof IProductCmptType) {
                    IProductCmptType productCmptType = (IProductCmptType)ipsObject;
                    if (productCmptType.isConfigurationForPolicyCmptType()) {
                        ipsObject = productCmptType.findPolicyCmptType(getIpsProject());
                    } else {
                        return null;
                    }
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return ipsObject;
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return isInterfaceBuilder() || !getGeneratorModelContext().isGeneratePublishedInterfaces();
    }

    @Override
    protected boolean generatesInterface() {
        return isInterfaceBuilder();
    }

}