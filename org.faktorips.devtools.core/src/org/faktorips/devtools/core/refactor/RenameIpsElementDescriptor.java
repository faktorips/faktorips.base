/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;

public final class RenameIpsElementDescriptor extends IpsRefactoringDescriptor {

    private static final String TYPE_ARGUMENT = "policyCmptType";

    private static final String PART_ARGUMENT = "policyCmptTypeAttribute";

    private IIpsElement ipsElement;

    public RenameIpsElementDescriptor(String contributionId) {
        super(contributionId);
    }

    public RenameIpsElementDescriptor(String contributionId, String project, String description, String comment,
            int flags) {

        super(contributionId, project, description, comment, flags);
    }

    public void setTypeArgument(IPolicyCmptType policyCmptType) {
        getArguments().put(TYPE_ARGUMENT, policyCmptType.getQualifiedName());
    }

    public void setPartArgument(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        getArguments().put(PART_ARGUMENT, policyCmptTypeAttribute.getName());
    }

    public String getTypeArgument() {
        return getArguments().get(TYPE_ARGUMENT);
    }

    public String getPartArgument() {
        return getArguments().get(PART_ARGUMENT);
    }

    @Override
    public void internalInit() throws CoreException {
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(getProject());
        if (getID().equals(IIpsRefactorings.RENAME_POLICY_CMPT_TYPE_ATTRIBUTE)) {
            IPolicyCmptType policyCmptType = ipsProject.findPolicyCmptType(getTypeArgument());
            ipsElement = policyCmptType.getPolicyCmptTypeAttribute(getPartArgument());
        }
    }

    public IIpsElement getIpsElement() {
        return ipsElement;
    }
}
