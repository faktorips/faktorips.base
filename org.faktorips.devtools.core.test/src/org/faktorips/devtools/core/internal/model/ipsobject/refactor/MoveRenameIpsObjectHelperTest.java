/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class MoveRenameIpsObjectHelperTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private MoveRenameIpsObjectHelper moveRenameHelper;

    private IProductCmptType productCmptType;

    private MessageList messageList;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        messageList = new MessageList();
        ipsProject = newIpsProject("ipsProject");
        productCmptType = newProductCmptType(ipsProject, "prodCmpt");
        newProductCmptType(ipsProject, "otherProductCmp");
        newPolicyCmptType(ipsProject, "policyCmptType");

        moveRenameHelper = new MoveRenameIpsObjectHelper(productCmptType);
    }

    @Test
    public void testValidateIpsModel_ChangeLetterCase() throws CoreException {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "ProdCmpt", messageList);

        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidateIpsModel_NameAlreadyExist() throws CoreException {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "otherProductCmp", messageList);

        assertFalse(messageList.isEmpty());
        assertEquals(1, messageList.size());
    }

    @Test
    public void testValidateIpsModel_NameAlreadyExistUpperCase() throws CoreException {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "OTHERPRODUctCMp", messageList);

        assertFalse(messageList.isEmpty());
        assertEquals(1, messageList.size());
    }

    @Test
    public void testValidateIpsModel_NameAlreadyExistInOtherIpsObjectType() throws CoreException {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "policyCMPTType", messageList);

        assertFalse(messageList.isEmpty());
        assertEquals(1, messageList.size());
    }
}
