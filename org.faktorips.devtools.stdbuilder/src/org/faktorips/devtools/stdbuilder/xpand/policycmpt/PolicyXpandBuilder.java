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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.XpandBuilder;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.GPolicyCmpt;
import org.faktorips.util.LocalizedStringsSet;

public class PolicyXpandBuilder extends XpandBuilder {

    public PolicyXpandBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(PolicyXpandBuilder.class));
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    public Object getGeneratorModel() {
        return new GPolicyCmpt((IPolicyCmptType)getIpsObject(), this);
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        // TODO
        return false;
    }

    @Override
    public String getTemplate() {
        return "org::faktorips::devtools::stdbuilder::xpand::policycmpt::template::PolicyCmpt::main";
    }

}
