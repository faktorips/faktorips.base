/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.TestCaseDescriptionPage;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;

/**
 * The editor to edit test case types.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTypeEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    TestCaseTypeEditorPage editorPage;

    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException {
        editorPage = new TestCaseTypeEditorPage(this, Messages.TestCaseTypeEditor_PageName,
                Messages.TestCaseTypeEditor_SectionTitle_Structure, Messages.TestCaseTypeEditor_SectionTitle_Details);

        addPage(editorPage);
    }

    /**
     * Returns the test case type the editor belongs to.
     */
    public ITestCaseType getTestCaseType() {
        try {
            return (ITestCaseType)getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getUniformPageTitle() {
        return NLS.bind(Messages.TestCaseTypeEditor_EditorTitle, getTestCaseType().getName());
    }

    @Override
    protected void refreshIncludingStructuralChanges() {
        editorPage.refreshInclStructuralChanges();
    }

    @Override
    public IPage createModelDescriptionPage() {
        return new TestCaseDescriptionPage(getTestCaseType());
    }
}
