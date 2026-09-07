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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.MessageList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MoveRenameIpsObjectHelperTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private MoveRenameIpsObjectHelper moveRenameHelper;

    private IProductCmptType productCmptType;

    private MessageList messageList;

    @BeforeEach
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
    public void testValidateIpsModel_ChangeLetterCase() {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "ProdCmpt", messageList);

        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidateIpsModel_NameAlreadyExist() {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "otherProductCmp", messageList);

        assertFalse(messageList.isEmpty());
        assertEquals(1, messageList.size());
    }

    @Test
    public void testValidateIpsModel_NameAlreadyExistUpperCase() {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "OTHERPRODUctCMp", messageList);

        assertFalse(messageList.isEmpty());
        assertEquals(1, messageList.size());
    }

    @Test
    public void testValidateIpsModel_NameAlreadyExistInOtherIpsObjectType() {
        IIpsPackageFragmentRoot iIpsPackageFragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = iIpsPackageFragmentRoot.getIpsPackageFragments()[0];
        moveRenameHelper.validateIpsModel(targetIpsPackageFragment, "policyCMPTType", messageList);

        assertFalse(messageList.isEmpty());
        assertEquals(1, messageList.size());
    }

    @Test
    public void testGetAffectedIpsSrcFiles_EnumContentReferencesEnumType() throws Exception {
        IEnumType enumType = newEnumType(ipsProject, "BaseEnumType");
        enumType.setExtensible(true);
        enumType.setEnumContentName("OtherEnumContent");
        enumType.getIpsSrcFile().save(null);

        IEnumContent enumContent = newEnumContent(ipsProject, "OtherEnumContent");
        enumContent.setEnumType(enumType.getQualifiedName());
        enumContent.getIpsSrcFile().save(null);

        MoveRenameIpsObjectHelper enumContentMoveRenameHelper = new MoveRenameIpsObjectHelper(enumContent);

        assertThat(enumContentMoveRenameHelper.getAffectedIpsSrcFiles(), hasItem(enumType.getIpsSrcFile()));
    }
}
