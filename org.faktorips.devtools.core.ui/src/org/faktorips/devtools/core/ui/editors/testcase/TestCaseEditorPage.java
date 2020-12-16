/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    private TestCaseContentProvider contentProvider;

    public TestCaseEditorPage(TestCaseEditor editor, String title, TestCaseContentProvider contentProvider,
            String sectionTitle, String sectionDetailTitle) {

        super(editor, PAGE_ID + contentProvider.getContentType(), title);
        this.sectionTitle = sectionTitle;
        this.sectionDetailTitle = sectionDetailTitle;
        this.contentProvider = contentProvider;
    }

    @Override
    public void dispose() {
        section.dispose();
        super.dispose();
    }

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
