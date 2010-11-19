/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;
import org.faktorips.util.message.Message;

/**
 * Wizard page for configuring table contents ({@link ITableContents}) for export.
 * 
 * @author Thorsten Waertel
 */
public class TableExportPage extends IpsObjectExportPage {

    public static final String PAGE_NAME = "TableExportPage"; //$NON-NLS-1$

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // Override page title
        setTitle(Messages.TableExportPage_title);
    }

    public TableExportPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.TableExportPage_title);
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
        toolkit.createFormLabel(parent, Messages.TableExportPage_labelContents);
        return toolkit.createTableContentsRefControl(null, parent);
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        try {
            if (selectedResource == null) {
                setTableContents(null);
                return;
            }
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile)element;
                if (src.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
                    ITableContents contents = (ITableContents)src.getIpsObject();
                    setTableContents(contents);
                }
            } else if (element != null) {
                setIpsProject(element.getIpsProject());
            } else {
                setTableContents(null);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    public ITableContents getTableContents() throws CoreException {
        if (exportedIpsObjectControl instanceof TableContentsRefControl) {
            return ((TableContentsRefControl)exportedIpsObjectControl).findTableContents();
        }
        return null;
    }

    private void setTableContents(ITableContents contents) {
        if (contents == null) {
            exportedIpsObjectControl.setText(""); //$NON-NLS-1$
            setIpsProject(null);
            return;
        }
        exportedIpsObjectControl.setText(contents.getQualifiedName());
        setIpsProject(contents.getIpsProject());
    }

    @Override
    protected void validateObjectToExport() {
        if (exportedIpsObjectControl.getText().length() == 0) {
            setErrorMessage(Messages.TableExportPage_msgContentsEmpty);
            return;
        }
        try {
            ITableContents contents = getTableContents();
            if (contents == null) {
                setErrorMessage(Messages.TableExportPage_msgNonExisitingContents);
                return;
            }
            if (!contents.exists()) {
                setErrorMessage(Messages.TableExportPage_msgNonExisitingContents);
                return;
            }
            ITableStructure structure = contents.findTableStructure(contents.getIpsProject());
            if (structure.validate(structure.getIpsProject()).getNoOfMessages(Message.ERROR) > 0) {
                setErrorMessage(Messages.TableExportPage_msgStructureNotValid);
                return;
            }
            if (structure.getNumOfColumns() > MAX_EXCEL_COLUMNS) {
                Object[] objects = new Object[3];
                objects[0] = new Integer(structure.getNumOfColumns());
                objects[1] = structure;
                objects[2] = new Short(MAX_EXCEL_COLUMNS);
                String text = NLS
                        .bind(org.faktorips.devtools.core.model.tablecontents.Messages.TableExportOperation_errStructureTooMuchColumns,
                                objects);
                setErrorMessage(text);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
