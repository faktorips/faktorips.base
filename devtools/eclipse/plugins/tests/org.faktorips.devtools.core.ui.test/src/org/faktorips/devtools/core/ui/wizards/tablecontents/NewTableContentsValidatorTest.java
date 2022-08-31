/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import static org.faktorips.testsupport.IpsMatchers.containsText;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class NewTableContentsValidatorTest {

    @Test
    public void testValidateTableContents_InvalidProject() throws Exception {
        NewTableContentsPMO pmo = mock(NewTableContentsPMO.class);
        NewTableContentsValidator newProdutCmptValidator = new NewTableContentsValidator(pmo);

        MessageList msgList = newProdutCmptValidator.validateTableContents();

        assertNotNull(msgList.getMessageByCode(NewTableContentsValidator.MSG_NO_PROJECT));
    }

    @Test
    public void testValidateTableContents_ValidProject() throws Exception {
        NewTableContentsPMO pmo = mockPmo();
        NewTableContentsValidator newProdutCmptValidator = new NewTableContentsValidator(pmo);
        IIpsProject ipsProject = mockStructureSelection(pmo);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        mockNamingConventions(ipsProject);

        MessageList msgList = newProdutCmptValidator.validateTableContents();

        assertFalse(msgList.containsErrorMsg());
    }

    // FIPS-5387
    @Test
    public void testValidateTableContents_ValidProject_NoProductDefinition() throws Exception {
        NewTableContentsPMO pmo = mockPmo();
        NewTableContentsValidator newProdutCmptValidator = new NewTableContentsValidator(pmo);
        IIpsProject ipsProject = mockStructureSelection(pmo);
        mockNamingConventions(ipsProject);

        when(ipsProject.isProductDefinitionProject()).thenReturn(false);

        MessageList msgList = newProdutCmptValidator.validateTableContents();

        assertFalse(msgList.containsErrorMsg());
    }

    private NewTableContentsPMO mockPmo() {
        NewTableContentsPMO pmo = mock(NewTableContentsPMO.class);
        when(pmo.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);
        when(pmo.getName()).thenReturn("foo");
        return pmo;
    }

    private IIpsProject mockStructureSelection(NewTableContentsPMO pmo) {
        ITableStructure structure = mock(ITableStructure.class);
        when(structure.getNumOfColumns()).thenReturn(42);
        when(pmo.getSelectedStructure()).thenReturn(structure);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(structure.getIpsProject()).thenReturn(ipsProject);
        return ipsProject;
    }

    private void mockNamingConventions(IIpsProject ipsProject) {
        IIpsProjectNamingConventions namingConventions = mock(IIpsProjectNamingConventions.class);
        when(namingConventions.validateUnqualifiedIpsObjectName(any(IpsObjectType.class), anyString())).thenReturn(
                new MessageList());
        when(ipsProject.getNamingConventions()).thenReturn(namingConventions);
    }

    @Test
    public void testValidateTableContents_DeprecatedTableStructure() throws Exception {
        NewTableContentsPMO pmo = mockPmo();
        NewTableContentsValidator newProdutCmptValidator = new NewTableContentsValidator(pmo);
        IIpsProject ipsProject = mockStructureSelection(pmo);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        mockNamingConventions(ipsProject);
        ITableStructure tableStructure = pmo.getSelectedStructure();
        when(tableStructure.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);

        MessageList msgList = newProdutCmptValidator.validateTableContents();

        assertThat(msgList, not(hasMessageCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE)));

        when(tableStructure.isDeprecated()).thenReturn(true);
        IDeprecation deprecation = mock(IDeprecation.class);
        when(tableStructure.getDeprecation()).thenReturn(deprecation);
        when(deprecation.getSinceVersionString()).thenReturn("1.2.3");
        when(deprecation.getDescriptionText(any(Locale.class))).thenReturn("Use Foo instead");

        msgList = newProdutCmptValidator.validateTableContents();

        assertThat(msgList, hasMessageCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE));
        Message message = msgList.getMessageByCode(ITableContents.MSGCODE_DEPRECATED_TABLE_STRUCTURE);
        assertThat(message, containsText("1.2.3"));
        assertThat(message, containsText("Use Foo instead"));
    }

}
