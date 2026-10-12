/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.runtime.MessageList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NewProductDefinitionValidatorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private NewProductDefinitionPMO pmo;

    private NewProductDefinitionValidator validator;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        pmo = mock(NewProductDefinitionPMO.class);
        when(pmo.getIpsProject()).thenReturn(ipsProject);
        when(pmo.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        validator = new NewProductDefinitionValidator(pmo) {

            @Override
            public MessageList validateAllPages() {
                return new MessageList();
            }

            @Override
            protected MessageList validateBeforeFolderAndPacket() {
                return new MessageList();
            }
        };
    }

    @Test
    public void testValidateIpsObjectName_NameWithAsterisk() {
        when(pmo.getName()).thenReturn("*");
        when(pmo.getQualifiedName()).thenReturn("*");

        MessageList messages = validator.validateIpsObjectName("name");

        assertThat(messages, hasMessageCode(IIpsProjectNamingConventions.INVALID_NAME));
        assertThat(messages, lacksMessageCode(NewProductDefinitionValidator.MSG_SRC_FILE_EXISTS));
    }

    @Test
    public void testValidateIpsObjectName_QualifiedNameWithAsterisk() {
        when(pmo.getName()).thenReturn("*");
        when(pmo.getQualifiedName()).thenReturn("a.b.*");

        MessageList messages = validator.validateIpsObjectName("name");

        assertThat(messages, hasMessageCode(IIpsProjectNamingConventions.INVALID_NAME));
        assertThat(messages, lacksMessageCode(NewProductDefinitionValidator.MSG_SRC_FILE_EXISTS));
    }

}
