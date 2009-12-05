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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

public final class RenameIpsElementDescriptor extends IpsRefactoringDescriptor {

    public static final String POLICY_CMPT_TYPE_ARGUMENT = "policyCmptType";

    public static final String POLICY_CMPT_TYPE_ATTRIBUTE_ARGUMENT = "policyCmptTypeAttribute";

    private IIpsElement ipsElement;

    public RenameIpsElementDescriptor(String id, Map<String, String> arguments) {
        super(id, arguments);
    }

    public RenameIpsElementDescriptor(String id, String project, String description, String comment,
            Map<String, String> arguments, int flags) {

        super(id, project, description, comment, arguments, flags);
    }

    @Override
    protected void initArguments() throws CoreException {
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(getProject());
        if (getID().equals(IIpsRefactorings.RENAME_POLICY_CMPT_TYPE_ATTRIBUTE)) {
            IPolicyCmptType policyCmptType = ipsProject.findPolicyCmptType(getArguments()
                    .get(POLICY_CMPT_TYPE_ARGUMENT));
            ipsElement = policyCmptType.getPolicyCmptTypeAttribute(getArguments().get(
                    POLICY_CMPT_TYPE_ATTRIBUTE_ARGUMENT));
        }
    }

    public IIpsElement getIpsElement() {
        return ipsElement;
    }
}
