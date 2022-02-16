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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class NewTableContentsValidatorTest {

    @Test
    public void testValidateTableContents_invalidProject() throws Exception {
        NewTableContentsPMO pmo = mock(NewTableContentsPMO.class);
        NewTableContentsValidator newProdutCmptValidator = new NewTableContentsValidator(pmo);

        MessageList msgList = newProdutCmptValidator.validateTableContents();

        assertNotNull(msgList.getMessageByCode(NewTableContentsValidator.MSG_NO_PROJECT));
    }

    @Test
    public void testValidateTableContents_validProject() throws Exception {
        NewTableContentsPMO pmo = mock(NewTableContentsPMO.class);
        NewTableContentsValidator newProdutCmptValidator = new NewTableContentsValidator(pmo);
        IIpsProject ipsProject = mockStructureSelection(pmo);
        when(ipsProject.isProductDefinitionProject()).thenReturn(true);
        mockNamingConventions(ipsProject);

        MessageList msgList = newProdutCmptValidator.validateTableContents();

        assertFalse(msgList.containsErrorMsg());
    }

    // FIPS-5387
    @Test
    public void testValidateTableContents_validProject_noProductDefinition() throws Exception {
        NewTableContentsPMO pmo = mock(NewTableContentsPMO.class);
        NewTableContentsValidator newProdutCmptValidator = new NewTableContentsValidator(pmo);
        IIpsProject ipsProject = mockStructureSelection(pmo);
        mockNamingConventions(ipsProject);

        when(ipsProject.isProductDefinitionProject()).thenReturn(false);

        MessageList msgList = newProdutCmptValidator.validateTableContents();

        assertFalse(msgList.containsErrorMsg());
    }

    private IIpsProject mockStructureSelection(NewTableContentsPMO pmo) {
        ITableStructure structure = mock(ITableStructure.class);
        when(structure.getNumOfColumns()).thenReturn(42);
        when(pmo.getSelectedStructure()).thenReturn(structure);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        return ipsProject;
    }

    private void mockNamingConventions(IIpsProject ipsProject) {
        IIpsProjectNamingConventions namingConventions = mock(IIpsProjectNamingConventions.class);
        when(namingConventions.validateUnqualifiedIpsObjectName(any(IpsObjectType.class), anyString())).thenReturn(
                new MessageList());
        when(ipsProject.getNamingConventions()).thenReturn(namingConventions);
    }

}
