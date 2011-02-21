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

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.fail;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ITestAnswerProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.editors.productcmpt.GenerationSelectionDialog;
import org.junit.Before;
import org.junit.Test;

/**
 * Testcase to test some simple GUI-functions like opening an editor.
 * 
 * @author Thorsten Guenther
 */
public class SimpleDialogTest extends AbstractIpsPluginTest implements ILogListener, ITestAnswerProvider {

    private IpsPlugin plugin;
    private int answer = GenerationSelectionDialog.CHOICE_BROWSE;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        plugin = IpsPlugin.getDefault();
        plugin.getLog().addLogListener(this);
        plugin.setTestMode(true);
        plugin.setTestAnswerProvider(this);
    }

    @Override
    protected void tearDownExtension() throws Exception {
        plugin.getLog().removeLogListener(this);
    }

    @Test
    public void testOpenProductCmptEditor() throws Exception {
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(2003, 7, 1));

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType type = newProductCmptType(ipsProject, "Type");
        IProductCmpt product1 = newProductCmpt(type, "Product1");
        openEditor(product1);

        IProductCmpt product2 = newProductCmpt(type, "Product2");
        openEditor(product1);
        openEditor(product2);
    }

    private void openEditor(IIpsObject file) throws Exception {
        IpsUIPlugin.getDefault().openEditor((IFile)file.getCorrespondingResource());
        plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Override
    public void logging(IStatus status, String plugin) {
        // never ever should a logentry appear...
        fail("Status: " + status.toString() + " (Plugin: " + plugin + ")");
    }

    @Override
    public boolean getBooleanAnswer() {
        return false;
    }

    @Override
    public String getStringAnswer() {
        return null;
    }

    @Override
    public Object getAnswer() {
        return null;
    }

    @Override
    public int getIntAnswer() {
        return answer;
    }
}
