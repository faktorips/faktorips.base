/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.eclipse.core.runtime.IStatus;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.ALogListener;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

/**
 * Testcase to test some simple GUI-functions like opening an editor.
 * 
 * @author Thorsten Guenther
 */
public class SimpleDialogTest extends AbstractIpsPluginTest implements ALogListener {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IpsLog.get().addLogListener(this);
    }

    @Override
    protected void tearDownExtension() throws Exception {
        IpsLog.get().removeLogListener(this);
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
        IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Override
    public void logging(IStatus status, String plugin) {
        // never ever should a logentry appear...
        fail("Status: " + status.toString() + " (Plugin: " + plugin + ")");
    }

}
