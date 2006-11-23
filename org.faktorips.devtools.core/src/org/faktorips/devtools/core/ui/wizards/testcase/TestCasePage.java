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

package org.faktorips.devtools.core.ui.wizards.testcase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;


/**
 * @author Joerg Ortmann
 */
public class TestCasePage extends IpsObjectPage implements ValueChangeListener {
    
    private TestCaseTypeRefControl superTypeControl;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public TestCasePage(IStructuredSelection selection) throws JavaModelException {
        super(selection, Messages.TestCasePage_title);
    }
    
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        toolkit.createFormLabel(nameComposite, Messages.TestCasePage_labelSuperclass);
        superTypeControl = toolkit.createTestCaseTypeRefControl(null, nameComposite);
        
        TextButtonField supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);
        super.fillNameComposite(nameComposite, toolkit);

        superTypeControl.setFocus();
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getPdPackageFragmentRoot();
        if (root!=null) {
            superTypeControl.setPdProject(root.getIpsProject());
        } else {
            superTypeControl.setPdProject(null);
        }
    }
    
    String getSuperType() {
        return superTypeControl.getText();
    }

	/**
	 * {@inheritDoc}
	 */
	protected void validateName() {
		super.validateName();
	}
	
    protected void validatePage() throws CoreException {
        super.validatePage();
        if (getErrorMessage()!=null) {
            return;
        }
	    if (superTypeControl.findTestCaseType()==null) {
	        setErrorMessage(NLS.bind(Messages.TestCasePage_msgTestCaseTypeDoesNotExist, superTypeControl.getText()));
	    }
        updatePageComplete();
    }
}
