package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * The editor to edit test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeEditor extends IpsObjectEditor {

    TestCaseTypeEditorPage editorPage;
    
    public TestCaseTypeEditor() {
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
            editorPage = new TestCaseTypeEditorPage(this, "Test Case Type", "Test Case Type Structure",
                    "Test Case Type Details");
            addPage(editorPage);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns the test case type the editor belongs to.
     */
    ITestCaseType getTestCaseType() {
        try {
            return (ITestCaseType)getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
        return NLS.bind("Test Case Type: {0}", getTestCaseType().getName());
    }
}
