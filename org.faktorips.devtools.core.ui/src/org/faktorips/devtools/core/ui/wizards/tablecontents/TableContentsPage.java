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

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.ipsimport.IpsObjectImportWizard;

public class TableContentsPage extends IpsObjectPage {

    private TableStructureRefControl structureControl;
    private TextButtonField structureField;
    private Text name;

    private ITableContents createdTableContents;

    public TableContentsPage(IStructuredSelection selection) {
        super(IpsObjectType.TABLE_CONTENTS, selection, Messages.TableContentsPage_title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/NewTableContentsWizard.png")); //$NON-NLS-1$
    }

    String getTableStructureName() {
        return structureControl.getText();
    }

    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createFormLabel(nameComposite, Messages.TableContentsPage_labelStructure);
        structureControl = toolkit.createTableStructureRefControl(null, nameComposite);

        structureField = new TextButtonField(structureControl);
        structureField.addChangeListener(this);
        name = addNameLabelField(toolkit, nameComposite);

        structureControl.setFocus();

        // sets the default table structure if the selection is a table structure or a table content
        String tableStructureInSelect = getTableStructureFromSelection();
        if (tableStructureInSelect != null) {
            structureControl.getTextControl().setText(tableStructureInSelect);
            name.setFocus();
        }
    }

    /**
     * Calls the supertype method and checks sets the focus to the structure field if it is empty
     * and the source folder field isn't empty.
     */
    @Override
    protected void setDefaultFocus() {
        super.setDefaultFocus();
        if (StringUtils.isEmpty(getSourceFolder())) {
            return;
        }
        if (StringUtils.isEmpty(structureField.getText())) {
            structureField.getControl().setFocus();
        }
    }

    private String getTableStructureFromSelection() {
        IIpsObject selectedObject;
        try {
            selectedObject = getSelectedIpsObject();
            if (selectedObject instanceof ITableStructure) {
                return ((ITableStructure)selectedObject).getQualifiedName();
            } else if (selectedObject instanceof ITableContents) {
                return ((ITableContents)selectedObject).getTableStructure();
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            structureControl.setIpsProject(root.getIpsProject());
        } else {
            structureControl.setIpsProject(null);
        }
    }

    public ITableStructure getTableStructure() {
        try {
            return structureControl.findTableStructure();
        } catch (CoreException e) {
            return null;
        }
    }

    @Override
    protected void validatePageExtension() throws CoreException {
        if (getErrorMessage() != null) {
            return;
        }
        if (structureControl.getText().length() == 0) {
            setErrorMessage(Messages.TableContentsPage_msgStructureEmpty);
            return;
        }
        ITableStructure structure = structureControl.findTableStructure();
        if (structure == null) {
            setErrorMessage(NLS.bind(Messages.TableContentsPage_msgMissingStructure, structureControl.getText()));
        } else {
            if (structure.getNumOfColumns() == 0) {
                setErrorMessage(Messages.TableContentsPage_tableStructureHasntGotAnyColumns);
            }
        }
    }

    @Override
    public IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        IIpsSrcFile createdIpsSrcFile = super.createIpsSrcFile(monitor);
        createdTableContents = (ITableContents)createdIpsSrcFile.getIpsObject();
        return createdIpsSrcFile;
    }

    @Override
    public void finishIpsObjects(IIpsObject pdObject, List<IIpsObject> modifiedIpsObjects) throws CoreException {
        ITableContents table = (ITableContents)pdObject;
        table.setTableStructure(getTableStructureName());
        GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        if (date == null) {
            return;
        }
        IIpsObjectGeneration generation = table.newGeneration();
        generation.setValidFrom(date);
        ITableStructure structure = (ITableStructure)table.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE,
                table.getTableStructure());
        if (structure != null) {
            for (int i = 0; i < structure.getNumOfColumns(); i++) {
                table.newColumn(""); //$NON-NLS-1$
            }
        }
    }

    /**
     * Returns the <tt>ITableContents</tt> that has been created by this page or <tt>null</tt> if it
     * has not been created yet.
     */
    public ITableContents getCreatedTableContents() {
        return createdTableContents;
    }

    @Override
    public boolean canFlipToNextPage() {
        IWizard wizard = getWizard();
        if (wizard instanceof IpsObjectImportWizard) {
            if (((IpsObjectImportWizard)wizard).isExcelTableFormatSelected()) {
                // do not show the configuration/preview page for excel
                return false;
            }
        }

        return super.canFlipToNextPage();
    }

}
