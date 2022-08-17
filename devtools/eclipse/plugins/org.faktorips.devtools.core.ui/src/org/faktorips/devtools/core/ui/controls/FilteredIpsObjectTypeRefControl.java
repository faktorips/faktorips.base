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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;

/**
 * Control to edit references to object types.
 */
public class FilteredIpsObjectTypeRefControl extends IpsObjectRefControl {

    private QualifiedNameType selectedIpsObject;
    private IpsObjectType[] applicableObjectTypes;
    private boolean excludeAbstractTypes;

    public FilteredIpsObjectTypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit,
            String controlTitle, String controlDescription, IpsObjectType[] applicableTypes,
            boolean excludeAbstractTypes) {

        super(project, parent, toolkit, controlTitle, controlDescription);
        applicableObjectTypes = applicableTypes;
        this.excludeAbstractTypes = excludeAbstractTypes;
    }

    @Override
    protected String getDefaultDialogFilterExpression() {
        return selectedIpsObject == null ? "?" : selectedIpsObject.getName(); //$NON-NLS-1$
    }

    @Override
    protected void updateTextControlAfterDialogOK(List<IIpsSrcFile> ipsSrcFiles) {
        setSelectedIpsSrcFile(ipsSrcFiles.get(0));
    }

    /**
     * Store the QualifiedNameType of the selected object.
     */
    public void setSelectedIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        selectedIpsObject = ipsSrcFile.getQualifiedNameType();
        String text = ipsSrcFile.getQualifiedNameType().getName() + " (" + ipsSrcFile.getIpsObjectType().getId() + ')'; //$NON-NLS-1$
        setText(text);
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() {
        if (getIpsProjects().isEmpty()) {
            return new IIpsSrcFile[0];
        }
        IIpsProject ipsProject = getIpsProjects().get(0);
        List<IIpsSrcFile> allowedIpsSrcFiles = ipsProject.findAllIpsSrcFiles(applicableObjectTypes);
        if (excludeAbstractTypes) {
            for (IIpsSrcFile type : allowedIpsSrcFiles) {
                if (Boolean.parseBoolean(type.getPropertyValue(IType.PROPERTY_ABSTRACT))) {
                    allowedIpsSrcFiles.remove(type);
                }
            }
        }
        return allowedIpsSrcFiles.toArray(new IIpsSrcFile[allowedIpsSrcFiles.size()]);
    }

    public IIpsSrcFile findSelectedIpsSrcFile() {
        if (getIpsProjects().isEmpty()) {
            return null;
        }
        IIpsProject ipsProject = getIpsProjects().get(0);
        return selectedIpsObject == null ? null : ipsProject.findIpsSrcFile(selectedIpsObject);
    }
}
