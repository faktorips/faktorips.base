/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
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
import org.faktorips.util.message.MessageList;

/**
 * Wizard page for configuring an enum type or enum content for export.
 * 
 * @see IEnumType
 * @see IEnumContent
 * 
 * @author Roman Grutza
 */
public class EnumExportPage extends IpsObjectExportPage {

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
    public void createControl(Composite parent) {
        super.createControl(parent);

        // Override page title
        setTitle(Messages.EnumExportPage_messagearea_title);
    }

    @Override
    public IpsObjectRefControl createExportedIpsObjectRefControlWithLabel(UIToolkit toolkit, Composite parent) {
        toolkit.createFormLabel(parent, Messages.EnumExportPage_enum_label);
        return toolkit.createEnumRefControl(getIpsProject(), parent, true, false);
    }

    @Override
    protected void validateObjectToExport() {
        if (exportedIpsObjectControl.getText().length() == 0) {
            setErrorMessage(Messages.EnumExportPage_msgEnumEmpty);
            return;
        }
        try {
            IEnumValueContainer enumValueContainer = getEnum();
            if (enumValueContainer == null) {
                setErrorMessage(Messages.EnumExportPage_msgNonExistingEnum);
                return;
            }

            if (!enumValueContainer.exists()) {
                setErrorMessage(Messages.EnumExportPage_msgNonExistingEnum);
                return;
            }

            if (enumValueContainer.validate(enumValueContainer.getIpsProject()).containsErrorMsg()) {
                setErrorMessage(Messages.EnumExportPage_msgEnumNotValid);
                return;
            }

            if (enumValueContainer instanceof IEnumType) {
                IEnumType enumType = (IEnumType)enumValueContainer;
                if (enumType.isAbstract()) {
                    setErrorMessage(Messages.EnumExportPage_msgAbstractEnumType);
                    return;
                }
            }

            IEnumType enumType = enumValueContainer.findEnumType(enumValueContainer.getIpsProject());
            if (enumType == null || !enumType.exists()) {
                setErrorMessage(Messages.EnumExportPage_msgNonExistingEnumType);
                return;
            }
            if (enumValueContainer instanceof IEnumContent) {
                MessageList enumTypeValidationMessages = enumType.validate(enumType.getIpsProject());
                removeVersionFormatValidation(enumTypeValidationMessages);
                if (enumTypeValidationMessages.containsErrorMsg()) {
                    setWarningMessage(Messages.EnumExportPage_msgEnumTypeNotValid);
                } else {
                    clearWarningMessage();
                }
            }

            boolean includeLiteralName = enumValueContainer instanceof IEnumType;
            if (enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralName) > MAX_EXCEL_COLUMNS) {
                Object[] objects = new Object[3];
                objects[0] = Integer.valueOf(enumType.getEnumAttributesCountIncludeSupertypeCopies(includeLiteralName));
                objects[1] = enumType;
                objects[2] = Short.valueOf(MAX_EXCEL_COLUMNS);
                String text = NLS.bind(Messages.EnumExportPage_msgEnumHasTooManyColumns, objects);
                setErrorMessage(text);
            }

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        if (selectedResource == null) {
            setEnum(null);
            return;
        }
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element == null) {
            setEnum(null);
            return;
        }
        setIpsProject(element.getIpsProject());
        if (element instanceof IIpsSrcFile) {
            IIpsSrcFile src = (IIpsSrcFile)element;
            IpsObjectType ipsObjectType = src.getIpsObjectType();
            setDefaultByEnumValueContainer(src, ipsObjectType);
        }
    }

    private void setDefaultByEnumValueContainer(IIpsSrcFile src, IpsObjectType ipsObjectType) {
        if (ipsObjectType.equals(IpsObjectType.ENUM_TYPE)) {
            IEnumType enumType = (IEnumType)src.getIpsObject();
            if (enumType.isCapableOfContainingValues()) {
                setEnum(enumType);
            }
        } else if (ipsObjectType.equals(IpsObjectType.ENUM_CONTENT)) {
            IEnumContent enumContent = (IEnumContent)src.getIpsObject();
            setEnum(enumContent);
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
        final IEnumValueContainer enumValue = ((EnumRefControl)exportedIpsObjectControl).findEnum(true);
        return enumValue;
    }
}
