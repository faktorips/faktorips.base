/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.MessageList;

/**
 * Wizard page for configuring table contents ({@link ITableContents}) for export.
 * 
 * @author Thorsten Waertel
 */
public class TableExportPage extends IpsObjectExportPage {

    @SuppressWarnings("hiding")
    public static final String PAGE_NAME = "TableExportPage"; //$NON-NLS-1$

    public TableExportPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.TableExportPage_title, selection);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // Override page title
        setTitle(Messages.TableExportPage_title);
    }

    @Override
    public IpsObjectRefControl createExportedIpsObjectRefControlWithLabel(UIToolkit toolkit, Composite parent) {
        toolkit.createFormLabel(parent, Messages.TableExportPage_labelContents);
        return toolkit.createTableContentsRefControl(null, parent);
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        IIpsSrcFile srcFile = getIpsSrcFile(selectedResource);
        if (srcFile == null
                || IIpsModel.get().findIpsElement(srcFile.getIpsProject().getCorrespondingResource()) == null) {
            setTableContents(null);
            return;
        }
        setDefaultByTableContents(srcFile);
    }

    /**
     * Extracts the selected table content from the provided {@link IIpsSrcFile}.
     * 
     * @param src The {@link IIpsSrcFile} matching with the currently selected view
     */
    private void setDefaultByTableContents(IIpsSrcFile src) {
        IpsObjectType ipsObjectType = src.getIpsObjectType();
        if (ipsObjectType.equals(IpsObjectType.TABLE_CONTENTS)) {
            ITableContents contents = (ITableContents)src.getIpsObject();
            setTableContents(contents);
        } else {
            setTableContents(null);
        }
    }

    /**
     * Provides the currently selected table content by getting it from the UI control.
     * 
     * @return The currently selected {@link ITableContents}
     */
    public ITableContents getTableContents() throws CoreRuntimeException {
        if (exportedIpsObjectControl instanceof TableContentsRefControl) {
            return ((TableContentsRefControl)exportedIpsObjectControl).findTableContents();
        }
        return null;
    }

    /**
     * Sets the selected table content for the UI control.
     * 
     * @param contents The selected {@link ITableContents}
     */
    private void setTableContents(ITableContents contents) {
        if (contents == null) {
            exportedIpsObjectControl.updateSelection(null);
            setIpsProject(null);
            return;
        }
        exportedIpsObjectControl.updateSelection(contents.getQualifiedNameType());
        setIpsProject(contents.getIpsProject());
    }

    @Override
    protected void validateObjectToExport() {
        if (exportedIpsObjectControl.getText().length() == 0) {
            setErrorMessage(Messages.TableExportPage_msgContentsEmpty);
            return;
        }
        ITableContents contents = getTableContents();
        if (contents == null || !contents.exists()) {
            setErrorMessage(Messages.TableExportPage_msgNonExisitingContents);
            return;
        }
        ITableStructure structure = contents.findTableStructure(contents.getIpsProject());
        if (structure == null || !structure.exists()) {
            setErrorMessage(Messages.TableExportPage_msgNonExisitingStructure);
            return;
        }
        MessageList structureValidationMessages = structure.validate(structure.getIpsProject());
        removeVersionFormatValidation(structureValidationMessages);
        if (structureValidationMessages.containsErrorMsg()) {
            setWarningMessage(Messages.TableExportPage_msgStructureNotValid);
        } else {
            clearWarningMessage();
        }
        if (structure.getNumOfColumns() > MAX_EXCEL_COLUMNS) {
            Object[] objects = new Object[3];
            objects[0] = Integer.valueOf(structure.getNumOfColumns());
            objects[1] = structure;
            objects[2] = Short.valueOf(MAX_EXCEL_COLUMNS);
            String text = NLS
                    .bind(org.faktorips.devtools.model.tablecontents.Messages.TableExportOperation_errStructureTooMuchColumns,
                            objects);
            setErrorMessage(text);
        }
    }
}
