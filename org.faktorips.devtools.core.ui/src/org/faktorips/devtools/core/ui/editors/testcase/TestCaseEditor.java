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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.IIpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.testcase.deltapresentation.TestCaseDeltaDialog;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.TestCaseDescriptionPage;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;

/**
 * The editor to edit test cases based on test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    /**
     * Setting key for user's decision not to choose a new test case type, because the old can't be
     * found.
     */
    private static final String SETTING_WORK_WITH_MISSING_TYPE = "workWithMissingType"; //$NON-NLS-1$

    TestCaseEditorPage editorPage;

    @Override
    protected void disposeInternal() {
        if (editorPage != null) {
            editorPage.saveState();
        }
        super.disposeInternal();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws IpsException, PartInitException {
        IIpsObjectEditorSettings settings = getSettings();
        // open the select template dialog if the templ. is missing and the data is changeable
        if (getTestCase().findTestCaseType(getIpsProject()) == null && couldDataBeChangedIfTestCaseTypeWasntMissing()
                && !settings.getBoolean(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE)) {
            String msg = NLS
                    .bind(Messages.TestCaseEditor_Information_TemplateNotFound, getTestCase().getTestCaseType());
            SetTestCaseTypeDialog d = new SetTestCaseTypeDialog(getTestCase(), getSite().getShell(), msg);
            int rc = d.open();
            if (rc == Dialog.CANCEL) {
                getSettings().put(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE, true);
            }
        }
        TestCaseContentProvider contentProviderInput = new TestCaseContentProvider(TestCaseContentProvider.COMBINED,
                getTestCase());

        editorPage = new TestCaseEditorPage(this, Messages.TestCaseEditor_Combined_Title, contentProviderInput,
                Messages.TestCaseEditor_Combined_SectionTitle, Messages.TestCaseEditor_Combined_Description);

        addPage(editorPage);
    }

    /**
     * Returns the test case the editor belongs to.
     */
    public ITestCase getTestCase() {
        try {
            return (ITestCase)getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getUniformPageTitle() {
        return NLS.bind(Messages.TestCaseEditor_Title, getTestCase().getName(), getTestCase().getTestCaseType());
    }

    @Override
    protected Dialog createDialogToFixDifferencesToModel() {
        return new TestCaseDeltaDialog(getTestCase().computeDeltaToModel(getIpsProject()), getSite().getShell());
    }

    @Override
    protected boolean computeDataChangeableState() {
        boolean datachangeable = true;
        if (!couldDataBeChangedIfTestCaseTypeWasntMissing()) {
            datachangeable = false;
        } else {
            try {
                datachangeable = getTestCase().findTestCaseType(getIpsProject()) != null;
            } catch (IpsException e) {
                IpsPlugin.log(e);
                datachangeable = false;
            }
        }
        if (editorPage != null) {
            editorPage.setReadOnly(!datachangeable);
        }

        return datachangeable;
    }

    private boolean couldDataBeChangedIfTestCaseTypeWasntMissing() {
        return super.computeDataChangeableState();
    }

    @Override
    public IPage createModelDescriptionPage() {
        ITestCaseType testCaseType = getTestCase().findTestCaseType(getIpsProject());
        if (testCaseType != null) {
            return new TestCaseDescriptionPage(testCaseType);
        } else {
            return null;
        }
    }

    public void addDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        editorPage.addDetailAreaRedrawListener(listener);
    }

    public void removeDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        editorPage.removeDetailAreaRedrawListener(listener);
    }

    @Override
    protected void refreshIncludingStructuralChanges() {
        editorPage.refreshInclStructuralChanges();
    }
}
