package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
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
            editorPage = new TestCaseTypeEditorPage(this, Messages.TestCaseTypeEditor_PageName, Messages.TestCaseTypeEditor_SectionTitle_Structure,
                    Messages.TestCaseTypeEditor_SectionTitle_Details);
            
            addPage(editorPage);
            addPage(new DescriptionPage(this));
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
        return NLS.bind(Messages.TestCaseTypeEditor_EditorTitle, getTestCaseType().getName());
    }
}
