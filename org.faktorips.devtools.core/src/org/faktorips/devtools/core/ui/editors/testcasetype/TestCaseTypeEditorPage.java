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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 *  Editor page to edit the test case type.
 *  
 * @author Joerg Ortmann
 */
public class TestCaseTypeEditorPage extends IpsObjectEditorPage {
    public static final String PAGE_ID = "TestCaseTypeEditorPage"; //$NON-NLS-1$
    
    private String sectionTitle;
    private String sectionDetailTitle;
	
    private TestCaseTypeSection section;
   
    
    public TestCaseTypeEditorPage(TestCaseTypeEditor editor, String title, String sectionTitle, String sectionDetailTitle) {
		super(editor, PAGE_ID, title);
		this.sectionTitle = sectionTitle;
		this.sectionDetailTitle = sectionDetailTitle;
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
        section = new TestCaseTypeSection(formBody, toolkit, ((TestCaseTypeEditor)getEditor()).getTestCaseType(),
                sectionTitle, sectionDetailTitle, getManagedForm().getForm());
    }
}
