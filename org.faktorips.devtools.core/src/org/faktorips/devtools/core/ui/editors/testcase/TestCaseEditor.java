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
        	addPage(new TestCaseEditorPage(this, Messages.TestCasePageInput_title, TestCaseContentProvider.TYPE_INPUT, Messages.TestPolicyCmptTypeSection_title_input, Messages.TestPolicyCmptTypeSection_title_inputDetail));
        	addPage(new TestCaseEditorPage(this, Messages.TestCasePageExpectedResult_title, TestCaseContentProvider.TYPE_EXPECTED_RESULT, Messages.TestPolicyCmptTypeSection_title_expectedResult, Messages.TestPolicyCmptTypeSection_title_expectedResultDetail));
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
        return NLS.bind(Messages.TestCaseEditor_title, getTestCase().getName(), getTestCase().getTestCaseType());
    }
}
