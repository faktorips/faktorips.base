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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 *  Editor page to edit the input or the expected result of the test case.
 *  
 * @author Joerg Ortmann
 */
public class TestCaseEditorPage extends IpsObjectEditorPage {
    public static final String PAGE_ID = "TestCaseEditorPage"; //$NON-NLS-1$
    
    private String sectionTitle;
    private String sectionDetailTitle;
	
    private TestCaseSection section;
    
    // Content provider
    private TestCaseContentProvider contentProvider;
    
    public TestCaseEditorPage(TestCaseEditor editor, String title, TestCaseContentProvider contentProvider, String sectionTitle, String sectionDetailTitle) {
		super(editor, PAGE_ID + contentProvider.getContentType(), title);
		this.sectionTitle = sectionTitle;
		this.sectionDetailTitle = sectionDetailTitle;
		this.contentProvider = contentProvider;
	}
	
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        section.dispose();
        super.dispose();
    }

    /**
	 * {@inheritDoc}
	 */
	protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(1, false));
        section = new TestCaseSection(formBody, (TestCaseEditor) getEditor(), toolkit, contentProvider, sectionTitle, sectionDetailTitle, getManagedForm().getForm());
	}
	
	/**
	 * Returns the corresponding test case content provider.
	 */
    public TestCaseContentProvider getTestCaseContentProvider(){
    	return contentProvider;
    }
    
    /**
     * Refreshs the status of the controls.<br>
     * The test run status will be reseted and the tree viewer will be refreshed.
     */
    public void refreshControlStatus(){
        section.resetTestRunStatus();
        section.refreshTree();
    }
}
