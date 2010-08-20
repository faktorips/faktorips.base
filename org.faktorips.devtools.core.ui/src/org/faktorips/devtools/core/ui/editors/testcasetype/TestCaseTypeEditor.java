/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
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
    ITestCaseType getTestCaseType() {
        try {
            return (ITestCaseType)getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getUniformPageTitle() {
        return NLS.bind(Messages.TestCaseTypeEditor_EditorTitle, getTestCaseType().getName());
    }

    @Override
    protected void refreshInclStructuralChanges() {
        editorPage.refreshInclStructuralChanges();
    }
}
