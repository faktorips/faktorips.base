/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class NewProdutCmptValidatorTest {

    @Test
    public void testValidateTypeSelection_invalidProject() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);

        MessageList msgList = newProdutCmptValidator.validateTypeSelection();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PROJECT));

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);

        msgList = newProdutCmptValidator.validateTypeSelection();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PROJECT));

        when(ipsProject.isProductDefinitionProject()).thenReturn(true);

        msgList = newProdutCmptValidator.validateTypeSelection();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PROJECT));
    }

    @Test
    public void testValidateTypeSelection_invalidBaseType() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);

        MessageList msgList = newProdutCmptValidator.validateTypeSelection();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_BASE_TYPE));

        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);

        msgList = newProdutCmptValidator.validateTypeSelection();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_BASE_TYPE));
    }

    @Test
    public void testValidateProductCmptPage_selectedType() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_SELECTED_TYPE));

        IProductCmptType type = mock(IProductCmptType.class);
        when(pmo.getSelectedType()).thenReturn(type);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_SELECTED_TYPE));
    }

    IIpsProject mockTypeSelection(NewProductCmptPMO pmo) {
        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        return ipsProject;
    }

    @Test
    public void testValidateProductCmptPage_emptyKindId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
        IIpsProject ipsProject = mockTypeSelection(pmo);

        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_EMPTY_KIND_ID));

        when(pmo.getKindId()).thenReturn("anyKindId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_EMPTY_KIND_ID));
    }

    @Test
    public void testValidateProductCmptPage_emptyVersionId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
        IIpsProject ipsProject = mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertTrue(msgList.containsErrorMsg());
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_EMPTY_VERSION_ID));

        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_EMPTY_VERSION_ID));

        when(pmo.isNeedVersionId()).thenReturn(true);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_EMPTY_VERSION_ID));

        when(pmo.getVersionId()).thenReturn("anyVersionId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_EMPTY_VERSION_ID));
    }

    @Test
    public void testValidateProductCmptPage_invalidKindId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertTrue(msgList.containsErrorMsg());
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_KIND_ID));

        IIpsProject ipsProject = mock(IIpsProject.class);
        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);
        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_KIND_ID));

        when(pmo.getKindId()).thenReturn("");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_KIND_ID));

        when(pmo.getKindId()).thenReturn("anyId");
        when(pmo.getFullName()).thenReturn("anyFullName");
        when(namingStrategy.getKindId("anyFullName")).thenReturn("anyId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_KIND_ID));
    }

    @Test
    public void testValidateProductCmptPage_invalidVersionId() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertTrue(msgList.containsErrorMsg());
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_VERSION_ID));

        IIpsProject ipsProject = mock(IIpsProject.class);
        IProductCmptType baseType = mock(IProductCmptType.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        when(pmo.getSelectedBaseType()).thenReturn(baseType);
        IProductCmptNamingStrategy namingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(namingStrategy);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_VERSION_ID));

        when(pmo.getKindId()).thenReturn("");
        when(pmo.getVersionId()).thenReturn("");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_VERSION_ID));

        when(pmo.isNeedVersionId()).thenReturn(true);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_VERSION_ID));

        when(pmo.getVersionId()).thenReturn("anyId");
        when(pmo.getFullName()).thenReturn("anyFullName");
        when(namingStrategy.getVersionId("anyFullName")).thenReturn("anyId");

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_VERSION_ID));
    }

    @Test
    public void testValidateProductCmptPage_validateName() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
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
        when(pmo.getFullName()).thenReturn("anyFullName");
        when(pmo.getQualifiedName()).thenReturn("qualifiedName");
        when(namingStrategy.getKindId("anyFullName")).thenReturn("anyId");

        IIpsProjectNamingConventions namingConventions = mock(IIpsProjectNamingConventions.class);
        when(ipsProject.getNamingConventions()).thenReturn(namingConventions);

        MessageList msgList = newProdutCmptValidator.validateProductCmptPage();
        assertFalse(msgList.containsErrorMsg());

        Message msg = new Message("invalid name", "invalid name", Message.ERROR);
        when(namingConventions.validateUnqualifiedIpsObjectName(IpsObjectType.PRODUCT_CMPT, "anyFullName")).thenReturn(
                new MessageList(msg));

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertEquals(msg, msgList.getMessageWithHighestSeverity());
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_SRC_FILE_EXISTS));

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsProject.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT, "qualifiedName")).thenReturn(ipsSrcFile);

        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_SRC_FILE_EXISTS));

        when(namingStrategy.getKindId("anyFullName")).thenThrow(new IllegalArgumentException());
        msgList = newProdutCmptValidator.validateProductCmptPage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_FULL_NAME));
    }

    @Test
    public void testValidateFolderAndPackage_invalidPackageRoot() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PACKAGE_ROOT));

        IIpsPackageFragmentRoot ipsPackageRoot = mock(IIpsPackageFragmentRoot.class);
        when(pmo.getPackageRoot()).thenReturn(ipsPackageRoot);

        msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PACKAGE_ROOT));
    }

    @Test
    public void testValidateFolderAndPackage_invalidPackage() throws Exception {
        NewProductCmptPMO pmo = mock(NewProductCmptPMO.class);
        NewProdutCmptValidator newProdutCmptValidator = new NewProdutCmptValidator(pmo);
        mockTypeSelection(pmo);

        MessageList msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PACKAGE));

        IIpsPackageFragment ipsPackage = mock(IIpsPackageFragment.class);
        IIpsPackageFragmentRoot packageRoot = mock(IIpsPackageFragmentRoot.class);
        when(ipsPackage.getRoot()).thenReturn(packageRoot);
        when(pmo.getIpsPackage()).thenReturn(ipsPackage);

        msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNotNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PACKAGE));

        when(pmo.getPackageRoot()).thenReturn(packageRoot);

        msgList = newProdutCmptValidator.validateFolderAndPackage();
        assertNull(msgList.getMessageByCode(NewProdutCmptValidator.MSG_INVALID_PACKAGE));
    }

}
