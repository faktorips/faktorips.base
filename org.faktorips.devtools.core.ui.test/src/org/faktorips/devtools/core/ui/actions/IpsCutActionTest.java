/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.commands.IpsObjectPartStateListTransfer;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for IpsCutAction.
 * 
 * @author Thorsten Guenther
 */
public class IpsCutActionTest extends AbstractIpsPluginTest {

    IpsCutAction cutAction;
    IPolicyCmptTypeAttribute attribute;
    IPolicyCmptType pcType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject project = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("products.folder", true, null);

        IIpsSrcFile pdSrcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newPolicyCmptTypeAttribute();

        cutAction = new IpsCutAction(new TestSelectionProvider(), IpsPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getShell());

    }

    @Test
    public void testRun() {
        String current = new IpsObjectPartState(attribute).toString();

        assertEquals(1, pcType.getNumOfAttributes());
        cutAction.run();
        Clipboard clipboard = new Clipboard(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell()
                .getDisplay());
        IpsObjectPartState[] states = (IpsObjectPartState[])clipboard.getContents(new IpsObjectPartStateListTransfer(
                pcType.getClass().getClassLoader()));

        assertEquals(current, states[0].toString());

        assertEquals(0, pcType.getNumOfAttributes());
    }

    private class TestSelectionProvider implements ISelectionProvider {

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public ISelection getSelection() {
            return new StructuredSelection(attribute);
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public void setSelection(ISelection selection) {
        }
    }
}
