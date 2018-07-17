/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.fail;

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
import org.junit.Before;
import org.junit.Test;

/**
 * Testcase to test some simple GUI-functions like opening an editor.
 * 
 * @author Thorsten Guenther
 */
public class SimpleDialogTest extends AbstractIpsPluginTest implements ILogListener, ITestAnswerProvider {

    private IpsPlugin plugin;

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
    public void tearDown() throws Exception {
        plugin.setTestMode(false);
        plugin.setTestAnswerProvider(null);
        super.tearDown();
    }

    @Override
    protected void tearDownExtension() throws Exception {
        plugin.getLog().removeLogListener(this);
    }

    @Test
    public void testOpenProductCmptEditor() throws Exception {
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
        return 0;
    }

}
