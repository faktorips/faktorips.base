/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class NewProductCmptValidatorTest {

    @Test
    public void testValidateAllPagesTypeSelection_invalidProject() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);

        MessageList msgList = newProdutCmptValidator.validateTypeSelection();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PROJECT));

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);

        msgList = newProdutCmptValidator.validateTypeSelection();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PROJECT));

        when(ipsProject.isProductDefinitionProject()).thenReturn(true);

        msgList = newProdutCmptValidator.validateTypeSelection();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PROJECT));
    }

    @Test
    public void testValidateAllPagesTypeSelection_invalidBaseType() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);

        MessageList msgList = newProdutCmptValidator.validateTypeSelection();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_BASE_TYPE));

        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);

        msgList = newProdutCmptValidator.validateTypeSelection();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_BASE_TYPE));
    }

    @Test
    public void testValidateAllPagesProductCmptPage_selectedType() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_SELECTED_TYPE));

        IProductCmptType type = mock(IProductCmptType.class);
        when(pmo.getSelectedType()).thenReturn(type);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_SELECTED_TYPE));
    }

    private IIpsProject mockTypeSelection(NewProductCmptPMO pmo) {
        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        return ipsProject;
    }

    @Test
    public void testValidateAllPagesProductCmptPage_emptyKindId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        IIpsProject ipsProject = mockTypeSelection(pmo);

        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_EMPTY_KIND_ID));

        when(pmo.getKindId()).thenReturn("anyKindId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_EMPTY_KIND_ID));
    }

    @Test
    public void testValidateAllPagesProductCmptPage_emptyVersionId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        IIpsProject ipsProject = mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertTrue(msgList.containsErrorMsg());
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_EMPTY_VERSION_ID));

        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_EMPTY_VERSION_ID));

        when(pmo.isNeedVersionId()).thenReturn(true);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_EMPTY_VERSION_ID));

        when(pmo.getVersionId()).thenReturn("anyVersionId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_EMPTY_VERSION_ID));
    }

    @Test
    public void testValidateAllPagesProductCmptPage_invalidKindId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertTrue(msgList.containsErrorMsg());
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_KIND_ID));

        IIpsProject ipsProject = mock(IIpsProject.class);
        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);
        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_KIND_ID));

        when(pmo.getKindId()).thenReturn("");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_KIND_ID));

        when(pmo.getKindId()).thenReturn("anyId");
        when(pmo.getName()).thenReturn("anyFullName");
        when(namingStrategy.getKindId("anyFullName")).thenReturn("anyId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_KIND_ID));
    }

    @Test
    public void testValidateAllPagesProductCmptPage_invalidVersionId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertTrue(msgList.containsErrorMsg());
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_VERSION_ID));

        IIpsProject ipsProject = mock(IIpsProject.class);
        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);
        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_VERSION_ID));

        when(pmo.getKindId()).thenReturn("");
        when(pmo.getVersionId()).thenReturn("");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_VERSION_ID));

        when(pmo.isNeedVersionId()).thenReturn(true);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_VERSION_ID));

        when(pmo.getVersionId()).thenReturn("anyId");
        when(pmo.getName()).thenReturn("anyFullName");
        when(namingStrategy.getVersionId("anyFullName")).thenReturn("anyId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_VERSION_ID));
    }

    @Test
    public void testValidateAllPagesProductCmptPage_validateName() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        mockTypeSelection(pmo);

        IIpsProject ipsProject = mock(IIpsProject.class);
        IProductCmptType baseType = mock(IProductCmptType.class);
        IProductCmptType type = mock(IProductCmptType.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);
        when(pmo.getSelectedType()).thenReturn(type);
        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        when(pmo.getKindId()).thenReturn("anyId");
        when(pmo.getName()).thenReturn("anyFullName");
        when(pmo.getQualifiedName()).thenReturn("qualifiedName");
        when(namingStrategy.getKindId("anyFullName")).thenReturn("anyId");
        when(pmo.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        IIpsProjectNamingConventions namingConventions = mock(IIpsProjectNamingConventions.class);
        when(ipsProject.getNamingConventions()).thenReturn(namingConventions);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertTrue(msgList.containsErrorMsg());

        when(pmo.getEffectiveDate()).thenReturn(new GregorianCalendar());
        when(namingConventions.validateUnqualifiedIpsObjectName(IpsObjectType.PRODUCT_CMPT, "anyFullName")).thenReturn(
                new MessageList());

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertFalse(msgList.containsErrorMsg());

        Message msg = new Message("invalid name", "invalid name", Message.ERROR);
        when(namingConventions.validateUnqualifiedIpsObjectName(IpsObjectType.PRODUCT_CMPT, "anyFullName")).thenReturn(
                new MessageList(msg));

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertEquals(msg.getCode(), msgList.getMessageWithHighestSeverity().getCode());
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_SRC_FILE_EXISTS));

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsProject.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT, "qualifiedName")).thenReturn(ipsSrcFile);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_SRC_FILE_EXISTS));

        when(namingStrategy.getKindId("anyFullName")).thenThrow(new IllegalArgumentException());
        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_FULL_NAME));
    }

    @Test
    public void testValidateAllPagesProductCmptPage_validateAddToType() throws CoreException {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        IIpsProject ipsProject = mockTypeSelection(pmo);

        IProductCmptType selectedType = mock(IProductCmptType.class);
        when(pmo.getSelectedType()).thenReturn(selectedType);

        MessageList msgList = newProdutCmptValidator.validateAddToType();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_SELECTED_TYPE));

        IProductCmptTypeAssociation addToAssociation = mock(IProductCmptTypeAssociation.class);
        when(pmo.getAddToAssociation()).thenReturn(addToAssociation);

        IProductCmptGeneration addToProductCmptGen = mock(IProductCmptGeneration.class);
        IProductCmpt addToProductCmpt = mock(IProductCmpt.class);
        when(pmo.getAddToProductCmptGeneration()).thenReturn(addToProductCmptGen);
        when(addToProductCmptGen.getProductCmpt()).thenReturn(addToProductCmpt);

        msgList = newProdutCmptValidator.validateAddToType();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_SELECTED_TYPE));

        IProductCmptType targetType = mock(IProductCmptType.class);
        when(addToAssociation.findTargetProductCmptType(ipsProject)).thenReturn(targetType);

        msgList = newProdutCmptValidator.validateAddToType();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_SELECTED_TYPE));

        when(selectedType.isSubtypeOrSameType(targetType, pmo.getIpsProject())).thenReturn(true);

        msgList = newProdutCmptValidator.validateAddToType();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_SELECTED_TYPE));

        when(selectedType.isSubtypeOrSameType(targetType, pmo.getIpsProject())).thenReturn(false);
        msgList = newProdutCmptValidator.validateAddToType();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_SELECTED_TYPE));
    }

    @Test
    public void testValidateAllPagesFolderAndPackage_invalidPackageRoot() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PACKAGE_ROOT));

        IIpsPackageFragmentRoot ipsPackageRoot = mock(IIpsPackageFragmentRoot.class);
        when(pmo.getPackageRoot()).thenReturn(ipsPackageRoot);

        msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PACKAGE_ROOT));
    }

    @Test
    public void testValidateAllPagesFolderAndPackage_invalidPackage() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProductCmptValidator newProdutCmptValidator = new NewProductCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PACKAGE));

        IIpsPackageFragment ipsPackage = mock(IIpsPackageFragment.class);
        IIpsPackageFragmentRoot packageRoot = mock(IIpsPackageFragmentRoot.class);
        when(ipsPackage.getRoot()).thenReturn(packageRoot);
        when(pmo.getIpsPackage()).thenReturn(ipsPackage);

        msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNotNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PACKAGE));

        when(pmo.getPackageRoot()).thenReturn(packageRoot);

        msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNull(msgList.getMessageByCode(NewProductCmptValidator.MSG_INVALID_PACKAGE));
    }

}
