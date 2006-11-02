package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.forms.editor.FormPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * The editor to edit test cases based on test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseEditor extends IpsObjectEditor {

    TestCaseEditorPage editorPage;
    
    public TestCaseEditor() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
    }

    /**
     * (@inheritDoc)
     */
    protected void addPages() {
        try {
            if (getTestCase().findTestCaseType() == null) {
                String msg = NLS.bind(Messages.TestCaseEditor_Information_TemplateNotFound, getTestCase()
                        .getTestCaseType());
                SetTemplateDialog dialog = new SetTemplateDialog(getTestCase(), getSite().getShell(), msg);
                int button = dialog.open();
                if (button != SetTemplateDialog.OK) {
                    addPage(new FormPage(this, "Empty", "")); //$NON-NLS-1$ //$NON-NLS-2$
                    this.close(false);
                    return;
                }
            }
            TestCaseContentProvider contentProviderInput = new TestCaseContentProvider(TestCaseContentProvider.COMBINED,
                    getTestCase());

            editorPage = new TestCaseEditorPage(this, Messages.TestCaseEditor_Combined_Title,
                    contentProviderInput, Messages.TestCaseEditor_Combined_SectionTitle,
                    Messages.TestCaseEditor_Combined_Description);
            
            addPage(editorPage);
            addPage(new DescriptionPage(this));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns the test case the editor belongs to.
     */
    ITestCase getTestCase() {
        try {
            return (ITestCase)getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
        return NLS.bind(Messages.TestCaseEditor_Title, getTestCase().getName(), getTestCase().getTestCaseType());
    }
}
