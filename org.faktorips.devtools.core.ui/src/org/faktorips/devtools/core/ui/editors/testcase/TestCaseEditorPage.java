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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * Editor page to edit the test case.
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

    public TestCaseEditorPage(TestCaseEditor editor, String title, TestCaseContentProvider contentProvider,
            String sectionTitle, String sectionDetailTitle) {
        super(editor, PAGE_ID + contentProvider.getContentType(), title);
        this.sectionTitle = sectionTitle;
        this.sectionDetailTitle = sectionDetailTitle;
        this.contentProvider = contentProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        section.dispose();
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        section = new TestCaseSection(formBody, (TestCaseEditor)getEditor(), toolkit, contentProvider, sectionTitle,
                sectionDetailTitle, getManagedForm().getForm(), getEditorSite());
        section.init();
    }

    /**
     * Returns the corresponding test case content provider.
     */
    public TestCaseContentProvider getTestCaseContentProvider() {
        return contentProvider;
    }

    void saveState() {
        section.saveState();
    }

    public void setReadOnly(boolean readOnly) {
        section.setReadOnly(readOnly);
    }

    public void addDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        section.addDetailAreaRedrawListener(listener);
    }

    public void removeDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        section.removeDetailAreaRedrawListener(listener);
    }

    public void refreshInclStructuralChanges() {
        section.refreshTreeAndDetailArea();
    }
}
