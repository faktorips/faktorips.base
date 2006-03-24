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
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
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
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#fillNameComposite(org.eclipse.swt.widgets.Composite, UIToolkit)
     */
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createFormLabel(nameComposite, Messages.TableContentsPage_labelStructure);
        structureControl = toolkit.createTableStructureRefControl(null, nameComposite);
        structureControl.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                if (getIpsObjectName().equals("")) { //$NON-NLS-1$
                    String structureName = structureField.getText();
                    setIpsObjectName(StringUtil.unqualifiedName(structureName));
                }
            }
            
        });
        structureField = new TextButtonField(structureControl);
        structureField.addChangeListener(this);
        addNameLabelField(toolkit);
        structureControl.setFocus();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.IpsObjectPage#sourceFolderChanged()
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
            structureControl.setPdProject(root.getIpsProject());
        } else {
            structureControl.setPdProject(null);
        }
    }
    
    protected void validatePage() throws CoreException {
        super.validatePage();
        if (getErrorMessage()!=null) {
            return;
        }
	    if (structureControl.findTableStructure()==null) {
	        setErrorMessage(NLS.bind(Messages.TableContentsPage_msgMissingStructure, structureControl.getText()));
	    }
        updatePageComplete();
    }
    
}
