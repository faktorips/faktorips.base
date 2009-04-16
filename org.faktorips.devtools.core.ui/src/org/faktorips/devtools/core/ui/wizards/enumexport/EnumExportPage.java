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

package org.faktorips.devtools.core.ui.wizards.enumexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;

/**
 * Wizard page for configuring an Enum type or content for export.
 * 
 * @see {@link IEnumType}, {@link IEnumContent}
 * @author Roman Grutza
 */
public class EnumExportPage extends IpsObjectExportPage {

    
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // Override page title
        setTitle(Messages.EnumExportPage_messagearea_title);
    }


    public EnumExportPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.EnumExportPage_title);
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();                
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
    }


    @Override
    public IpsObjectRefControl createExportedIpsObjectRefControlWithLabel(UIToolkit toolkit, Composite parent) {
        toolkit.createFormLabel(parent, Messages.EnumExportPage_enum_label);
        return toolkit.createEnumRefControl(getIpsProject(), parent, false); 
    }

    /**
     * {@inheritDoc}
     */
    protected void validateObjectToExport() {
        try {
            IEnumValueContainer enumContainer = getEnum();
            if (enumContainer == null) {
                setErrorMessage(Messages.EnumExportPage_msgInvalidEnum);
                return;
            }
            if (!enumContainer.exists()) {
                setErrorMessage(Messages.EnumExportPage_msgNonExistingEnum);
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            setErrorMessage(Messages.EnumExportPage_msgValidateEnumError + e);
            return;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void setDefaults(IResource selectedResource) {
        try {
            if (selectedResource==null) {
                setEnum(null);
                return;
            }
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile) element;
                if (src.getIpsObjectType() == IpsObjectType.ENUM_TYPE
                    || src.getIpsObjectType() == IpsObjectType.ENUM_CONTENT) {
                    
                    IEnumValueContainer enumContainer = (IEnumValueContainer) src.getIpsObject();
                    setEnum(enumContainer);
                }
            } else if (element != null) {
                setIpsProject(element.getIpsProject());
            } else {
                setEnum(null);    
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }


    
    private void setEnum(IEnumValueContainer enumContainer) {
        if (enumContainer == null) {
            exportedIpsObjectControl.setText(""); //$NON-NLS-1$
            setIpsProject(null);
            return;
        }
        exportedIpsObjectControl.setText(enumContainer.getQualifiedName());
        setIpsProject(enumContainer.getIpsProject());
    }
    
    public IEnumValueContainer getEnum() throws CoreException {
        if (exportedIpsObjectControl instanceof EnumRefControl) {
            return ((EnumRefControl)exportedIpsObjectControl)
                .findEnum();
        }
        return null;
    }
}
