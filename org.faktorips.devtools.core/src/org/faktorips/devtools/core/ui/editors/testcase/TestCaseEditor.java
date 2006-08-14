package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * The editor to edit test cases based on test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseEditor extends IpsObjectEditor {

	public TestCaseEditor() {
		super();
	}
	
    /** 
     * Add two pages to edit test case input objects and test case expected result objects.
     * 
     * (@inheritDoc)
     */
    protected void addPages() {
        try {
        	addPage(new TestCaseEditorPage(this, Messages.TestCaseEditor_Input_Title, TestCaseContentProvider.TYPE_INPUT, Messages.TestCaseEditor_Input_SectionTitle, Messages.TestCaseEditor_Input_Description));
        	addPage(new TestCaseEditorPage(this, Messages.TestCaseEditor_ExpectedResult_Title, TestCaseContentProvider.TYPE_EXPECTED_RESULT, Messages.TestCaseEditor_ExpectedResult_SectionTitle, Messages.TestCaseEditor_ExpectedResult_Description));
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
	/**
	 * Returns the test case the editor belongs to.
	 */
	ITestCase getTestCase() {
        try {
            return (ITestCase) getIpsSrcFile().getIpsObject();
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
