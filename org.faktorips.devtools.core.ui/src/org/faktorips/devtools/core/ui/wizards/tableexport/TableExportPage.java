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

package org.faktorips.devtools.core.ui.wizards.tableexport;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;

/**
 * Wizard page for configuring tablecontents ({@link ITableContents}) for export.
 * 
 * @author Thorsten Waertel
 */
public class TableExportPage extends IpsObjectExportPage {

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // Override page title
        setTitle(Messages.TableExportPage_title);
    }

    public static final String PAGE_NAME = "TableExportPage";    
    
    /**
	 * @param pageName
     * @param selection
     * @throws JavaModelException
	 */
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

	/**
	 * {@inheritDoc}
	 */
    public IpsObjectRefControl createExportedIpsObjectRefControlWithLabel(UIToolkit toolkit, Composite parent) {
        toolkit.createFormLabel(parent, "Table contents:");
        return toolkit.createTableContentsRefControl(null, parent);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void setDefaults(IResource selectedResource) {
        try {
	    	if (selectedResource==null) {
	            setTableContents(null);
	            return;
	        }
	        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
            if (element instanceof IIpsSrcFile) {
                IIpsSrcFile src = (IIpsSrcFile) element;
                if (src.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS) {
                    ITableContents contents = (ITableContents) src.getIpsObject();
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
            return ((TableContentsRefControl)exportedIpsObjectControl)
                .findTableContents();
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
    
    
    protected void validateObjectToExport() {
        try {
            ITableContents contents = getTableContents();
            if (contents == null) {
                setErrorMessage(Messages.TableExportPage_msgInvalidContents);
                return;
            }
            if (!contents.exists()) {
                setErrorMessage(Messages.TableExportPage_msgNonExisitingContents);
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            setErrorMessage(Messages.TableExportPage_msgValidateContentsError + e);
            return;
        }
    }
    
}
