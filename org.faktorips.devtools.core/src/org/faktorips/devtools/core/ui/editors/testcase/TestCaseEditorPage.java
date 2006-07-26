/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 *  Editor page to edit the input or the expected result of the test case.
 *  
 * @author Joerg Ortmann
 */
public class TestCaseEditorPage extends IpsObjectEditorPage {
    private static final String PAGE_ID = "TestCaseInput"; //$NON-NLS-1$
    
    /** Type of the to be edit objects: input or expected result */
    private int objectType;
    
    private String sectionTitle;
    private String sectionDetailTitle;
	
    public TestCaseEditorPage(TestCaseEditor editor, String title, int objectType, String sectionTitle, String sectionDetailTitle) {
		super(editor, PAGE_ID, title);
		this.objectType = objectType;
		this.sectionTitle = sectionTitle;
		this.sectionDetailTitle = sectionDetailTitle;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(1, false));
		TestCaseContentProvider contentProviderInput =  new TestCaseContentProvider(objectType, getTestCase());

		new TestCaseSection(formBody, toolkit, contentProviderInput, sectionTitle, sectionDetailTitle, getManagedForm().getForm());
	}
	
	/**
	 * Returns the corresponding editor.
	 */
    private TestCaseEditor getTestCaseEditor() {
        return (TestCaseEditor)getEditor();
    }
    
    /**
     * Returns the test case which wil be edit by this editor page.
     */
    private ITestCase getTestCase() {
        return getTestCaseEditor().getTestCase(); 
    } 
}
