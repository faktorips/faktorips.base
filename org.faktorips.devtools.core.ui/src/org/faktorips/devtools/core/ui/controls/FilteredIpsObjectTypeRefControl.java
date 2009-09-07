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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Control to edit references to object types.
 */
public class FilteredIpsObjectTypeRefControl extends IpsObjectRefControl {

    private QualifiedNameType selectedIpsObject;
    private IpsObjectType[] applicableObjectTypes;
    private boolean excludeAbstractTypes;

    /**
     * Constructor
     * 
     * @param project
     * @param parent
     * @param toolkit
     * @param controlTitle
     * @param controlDescription
     * @param applicableTypes
     * @param excludeAbstractTypes
     */
    public FilteredIpsObjectTypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit,
            String controlTitle, String controlDescription, IpsObjectType[] applicableTypes,
            boolean excludeAbstractTypes) {
        super(project, parent, toolkit, controlTitle, controlDescription);
        applicableObjectTypes = applicableTypes;
        this.excludeAbstractTypes = excludeAbstractTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultDialogFilterExpression() {
        if (selectedIpsObject == null) {
            return null;
        }
        return selectedIpsObject.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateTextControlAfterDialogOK(List<IIpsSrcFile> ipsSrcFiles) {
        setSelectedIpsSrcFile(ipsSrcFiles.get(0));
    }

    /**
     * Store the QualifiedNameType of the selected object.
     * 
     * @param ipsSrcFile
     */
    public void setSelectedIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        selectedIpsObject = ipsSrcFile.getQualifiedNameType();
        String text = ipsSrcFile.getQualifiedNameType().getName() + " (" + ipsSrcFile.getIpsObjectType().getId() + ')'; //$NON-NLS-1$ 
        setText(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }
        List<IIpsSrcFile> allowedIpsSrcFiles = new ArrayList<IIpsSrcFile>();
        getIpsProject().findAllIpsSrcFiles(allowedIpsSrcFiles, applicableObjectTypes);
        if (excludeAbstractTypes) {
            for (IIpsSrcFile type : allowedIpsSrcFiles) {
                if (Boolean.valueOf(type.getPropertyValue(IType.PROPERTY_ABSTRACT)).booleanValue()) {
                    allowedIpsSrcFiles.remove(type);
                }
            }
        }
        return allowedIpsSrcFiles.toArray(new IIpsSrcFile[allowedIpsSrcFiles.size()]);
    }

    public IIpsSrcFile findSelectedIpsSrcFile() throws CoreException {
        return getIpsProject().findIpsSrcFile(selectedIpsObject);
    }
}
