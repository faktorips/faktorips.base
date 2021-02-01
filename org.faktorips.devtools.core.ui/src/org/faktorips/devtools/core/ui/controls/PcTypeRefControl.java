/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;

/**
 * A control to edit policy component type references.
 */
public class PcTypeRefControl extends IpsObjectRefControl {

    public PcTypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.PcTypeRefControl_title, Messages.PcTypeRefControl_description);
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }
        return getIpsProject().findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
    }

    /**
     * Returns the policy component type identified by the qualified name found in this control's
     * text value. Returns <code>null</code> if the text value does not identify a policy component
     * type.
     * 
     * @throws CoreException if an error occurs while searching for the type.
     */
    public IPolicyCmptType findPcType() throws CoreException {
        IIpsProject project = getIpsProject();
        if (project == null) {
            return null;
        }
        return project.findPolicyCmptType(getText());
    }

}
