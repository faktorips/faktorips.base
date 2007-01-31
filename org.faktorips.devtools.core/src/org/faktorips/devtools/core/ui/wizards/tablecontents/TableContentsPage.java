/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.util.StringUtil;


/**
 *
 */
public class TableContentsPage extends IpsObjectPage {
    
    private TableStructureRefControl structureControl;
    private TextButtonField structureField;
    private Text name;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TableContentsPage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.TableContentsPage_title);
    }
    
    String getTableStructure() {
        return structureControl.getText();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createFormLabel(nameComposite, Messages.TableContentsPage_labelStructure);
        structureControl = toolkit.createTableStructureRefControl(null, nameComposite);

        structureField = new TextButtonField(structureControl);
        structureField.addChangeListener(this);
        name = addNameLabelField(toolkit);
        name.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                setDefaultName();
            }
            public void focusLost(FocusEvent e) {
            }
        });
        
        structureControl.setFocus();
        
        // sets the default table structure if the selection is a table structure or a table content
        String tableStructureInSelect = getTableStructureFromSelection();
        if (tableStructureInSelect != null){
            structureControl.getTextControl().setText(tableStructureInSelect);
            name.setFocus();
        }
    }
    
    private String getTableStructureFromSelection() {
        IIpsObject selectedObject;
        try {
            selectedObject = getSelectedIpsObject();
            if (selectedObject instanceof ITableStructure) {
                return ((ITableStructure)selectedObject).getQualifiedName();
            }
            else if (selectedObject instanceof ITableContents) {
                return ((ITableContents)selectedObject).getTableStructure();
            }
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
            structureControl.setIpsProject(root.getIpsProject());
        } else {
            structureControl.setIpsProject(null);
        }
    }
    
    protected void validatePage() throws CoreException {
        super.validatePage();
        if (getErrorMessage()!=null) {
            return;
        }
        ITableStructure structure = structureControl.findTableStructure();
	    if (structure==null) {
	        setErrorMessage(NLS.bind(Messages.TableContentsPage_msgMissingStructure, structureControl.getText()));
	    } else {
	        if (structure.getNumOfColumns()==0) {
                setErrorMessage(Messages.TableContentsPage_tableStructureHasntGotAnyColumns);
            }
        }
        updatePageComplete();
    }
    
    private void setDefaultName() {
        if (getIpsObjectName().equals("")) { //$NON-NLS-1$
            String structureName = structureField.getText();
            setIpsObjectName(StringUtil.unqualifiedName(structureName));
        }
    }
}
