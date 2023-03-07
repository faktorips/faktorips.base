/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.runtime.MessageList;

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
        super(Messages.EnumExportPage_title, selection);
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
        IEnumValueContainer enumValueContainer = getEnum();
        if ((enumValueContainer == null) || !enumValueContainer.exists()) {
            setErrorMessage(Messages.EnumExportPage_msgNonExistingEnum);
            return;
        }

        if (enumValueContainer.validate(enumValueContainer.getIpsProject()).containsErrorMsg()) {
            setErrorMessage(Messages.EnumExportPage_msgEnumNotValid);
            return;
        }

        if (enumValueContainer instanceof IEnumType enumType) {
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
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        IIpsSrcFile srcFile = getIpsSrcFile(selectedResource);
        if (srcFile == null
                || IIpsModel.get().findIpsElement(srcFile.getIpsProject().getCorrespondingResource()) == null) {
            setEnum(null);
            return;
        }
        setDefaultByEnumValueContainer(srcFile);
    }

    /**
     * Extracts the selected enum from the provided {@link IIpsSrcFile}.
     * 
     * @param src The {@link IIpsSrcFile} matching with the currently selected view
     */
    private void setDefaultByEnumValueContainer(IIpsSrcFile src) {
        IpsObjectType ipsObjectType = src.getIpsObjectType();
        if (ipsObjectType.equals(IpsObjectType.ENUM_TYPE)) {
            IEnumType enumType = (IEnumType)src.getIpsObject();
            if (enumType.isCapableOfContainingValues()) {
                setEnum(enumType);
                return;
            }
        } else if (ipsObjectType.equals(IpsObjectType.ENUM_CONTENT)) {
            IEnumContent enumContent = (IEnumContent)src.getIpsObject();
            setEnum(enumContent);
            return;
        }
        setEnum(null);
    }

    /**
     * Sets the selected enum for the UI control.
     * 
     * @param enumContainer The selected {@link IEnumValueContainer}
     */
    private void setEnum(IEnumValueContainer enumContainer) {
        if (enumContainer == null) {
            setIpsProject(null);
            exportedIpsObjectControl.updateSelection(null);
            return;
        }
        setIpsProject(enumContainer.getIpsProject());
        exportedIpsObjectControl.updateSelection(enumContainer.getQualifiedNameType());
    }

    /**
     * Provides the currently selected enum by getting it from the UI control.
     * 
     * @return The currently selected {@link IEnumValueContainer}
     */
    public IEnumValueContainer getEnum() {
        return ((EnumRefControl)exportedIpsObjectControl).findEnum();
    }
}
