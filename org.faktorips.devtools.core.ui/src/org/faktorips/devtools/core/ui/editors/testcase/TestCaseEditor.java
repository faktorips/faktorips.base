/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IIpsObjectEditorSettings;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.testcase.deltapresentation.TestCaseDeltaDialog;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;

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
    private final static String SETTING_WORK_WITH_MISSING_TYPE = "workWithMissingType"; //$NON-NLS-1$

    TestCaseEditorPage editorPage;

    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
    }

    @Override
    protected void disposeInternal() {
        if (editorPage != null) {
            editorPage.saveState();
        }
        super.disposeInternal();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws CoreException {
        IIpsObjectEditorSettings settings = getSettings();
        // open the select template dialog if the templ. is missing and the data is changeable
        if (getTestCase().findTestCaseType(getIpsProject()) == null && couldDataBeChangedIfTestCaseTypeWasntMissing()
                && !IpsPlugin.getDefault().isTestMode()
                && !settings.getBoolean(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE)) {
            String msg = NLS
                    .bind(Messages.TestCaseEditor_Information_TemplateNotFound, getTestCase().getTestCaseType());
            SetTemplateDialog d = new SetTemplateDialog(getTestCase(), getSite().getShell(), msg);
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
        addPage(new DescriptionPage(this));
    }

    /**
     * Returns the test case the editor belongs to.
     */
    ITestCase getTestCase() {
        try {
            return (ITestCase)getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getUniformPageTitle() {
        return NLS.bind(Messages.TestCaseEditor_Title, getTestCase().getName(), getTestCase().getTestCaseType());
    }

    @Override
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException {
        return new TestCaseDeltaDialog(getTestCase().computeDeltaToTestCaseType(), getSite().getShell());
    }

    @Override
    protected boolean computeDataChangeableState() {
        boolean datachangeable = true;
        if (!couldDataBeChangedIfTestCaseTypeWasntMissing()) {
            datachangeable = false;
        } else {
            try {
                datachangeable = getTestCase().findTestCaseType(getIpsProject()) != null;
            } catch (CoreException e) {
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
    public IPage createModelDescriptionPage() throws CoreException {
        return new TestCaseModelDescriptionPage(this);
    }

    public void addDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        editorPage.addDetailAreaRedrawListener(listener);
    }

    public void removeDetailAreaRedrawListener(ITestCaseDetailAreaRedrawListener listener) {
        editorPage.removeDetailAreaRedrawListener(listener);
    }

    @Override
    protected void refreshInclStructuralChanges() {
        editorPage.refreshInclStructuralChanges();
    }
}
