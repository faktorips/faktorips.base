package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IIpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.testcase.deltapresentation.TestCaseDeltaDialog;

/**
 * The editor to edit test cases based on test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseEditor extends IpsObjectEditor {

    /*
     * Setting key for user's decision not to choose a new test case type, because the old
     * can't be found.
     */
    private final static String SETTING_WORK_WITH_MISSING_TYPE = "workWithMissingType"; //$NON-NLS-1$

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
     * {@inheritDoc}
     */
    protected void disposeInternal() {
        editorPage.saveState();
        super.disposeInternal();
    }

    /**
     * (@inheritDoc)
     */
    protected void addPagesForParsableSrcFile() throws CoreException {
        IIpsObjectEditorSettings settings = getSettings();
        // open the select template dialog if the templ. is missing and the data is changeable
        if (getTestCase().findTestCaseType() == null 
                && couldDateBeChangedIfTestCaseTypeWasntMissing()
                && !IpsPlugin.getDefault().isTestMode()
                && !settings.getBoolean(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE)) {            
            String msg = NLS.bind(Messages.TestCaseEditor_Information_TemplateNotFound, getTestCase().getTestCaseType());
            SetTemplateDialog d = new SetTemplateDialog(getTestCase(), getSite().getShell(), msg); 
            int rc = d.open();
            if (rc==Dialog.CANCEL) {
                getSettings().put(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE, true);
            }
        }
        TestCaseContentProvider contentProviderInput = new TestCaseContentProvider(TestCaseContentProvider.COMBINED,
                getTestCase());

        editorPage = new TestCaseEditorPage(this, Messages.TestCaseEditor_Combined_Title,
                contentProviderInput, Messages.TestCaseEditor_Combined_SectionTitle,
                Messages.TestCaseEditor_Combined_Description);
        
        addPage(editorPage);
        addPage(new DescriptionPage(this));
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

    /**
     * {@inheritDoc}
     */
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException {
        return new TestCaseDeltaDialog(getTestCase().computeDeltaToTestCaseType(), getSite().getShell());
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean computeDataChangeableState() {
        if (!couldDateBeChangedIfTestCaseTypeWasntMissing()) {
            return false;
        }
        try {
            return getTestCase().findTestCaseType() != null;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }  
    
    private boolean couldDateBeChangedIfTestCaseTypeWasntMissing() {
        return super.computeDataChangeableState();
    }
    
}
