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
	
	/* Test case content providers */
	private TestCaseContentProvider contentProviderInput;
	private TestCaseContentProvider contentProviderExpResult;
	
    /** 
     * Add two pages to edit test case input objects and test case expected result objects.
     * 
     * (@inheritDoc)
     */
    protected void addPages() {
        try {
        	contentProviderInput = new TestCaseContentProvider(TestCaseContentProvider.TYPE_INPUT, getTestCase());
        	contentProviderExpResult = new TestCaseContentProvider(TestCaseContentProvider.TYPE_EXPECTED_RESULT, getTestCase());
        	
        	if (false){
        		// dummy to simulate combined view for input and expected result
        		contentProviderInput.setCombinedContent(true);
            	TestCaseEditorPage pageCombined = new TestCaseEditorPage(this, Messages.TestCaseEditor_Combined_Title, 
            			contentProviderInput, Messages.TestCaseEditor_Combined_SectionTitle, Messages.TestCaseEditor_Combined_Description);
            	addPage(pageCombined);
        	} else {
        		TestCaseEditorPage pageInput = new TestCaseEditorPage(this, Messages.TestCaseEditor_Input_Title, 
	        			contentProviderInput, Messages.TestCaseEditor_Input_SectionTitle, Messages.TestCaseEditor_Input_Description);
	        	addPage(pageInput);
	        	
	        	TestCaseEditorPage pageExpectedResult = new TestCaseEditorPage(this, Messages.TestCaseEditor_ExpectedResult_Title, 
	        			contentProviderExpResult, Messages.TestCaseEditor_ExpectedResult_SectionTitle, Messages.TestCaseEditor_ExpectedResult_Description);
	        	addPage(pageExpectedResult);
	
	        	// display all pages first, to pregenerate the content, therefore the color could be changed 
	        	//   if the test run listener notifies failures
	        	setActivePage(TestCaseEditorPage.PAGE_ID + TestCaseContentProvider.TYPE_EXPECTED_RESULT);
	        	setActivePage(TestCaseEditorPage.PAGE_ID + TestCaseContentProvider.TYPE_INPUT);
        	}
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
	 * Returns the content provider for the expected result data of the test case.
	 */
	public TestCaseContentProvider getContentProviderExpectedResult() {
		return contentProviderExpResult;
	}

	/**
	 * Returns the content provider for the input data of the test case.
	 */
	public TestCaseContentProvider getContentProviderInput() {
		return contentProviderInput;
	}

	/**
	 * {@inheritDoc}
	 */
    protected String getUniformPageTitle() {
        return NLS.bind(Messages.TestCaseEditor_Title, getTestCase().getName(), getTestCase().getTestCaseType());
    }
}
