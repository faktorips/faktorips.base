/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.MessageList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttributeAbstractDatatypeValidatorTest {
    @Mock
    private IAttribute attribute;

    @Mock
    private ValueDatatype valueDatatype;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IType type;

    private AttributeAbstractDatatypeValidator attributeAbstractDatatypeValidator;

    @BeforeEach
    public void setUp() {
        attributeAbstractDatatypeValidator = new AttributeAbstractDatatypeValidator(attribute, type, ipsProject);
    }

    @Test
    public void testValidateNotAbstractDatatype_datatypeNull() throws Exception {
        MessageList list = new MessageList();

        attributeAbstractDatatypeValidator.validateNotAbstractDatatype(list);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidateNotAbstractDatatype_datatypeNotAbstract() throws Exception {
        when(attribute.findDatatype(ipsProject)).thenReturn(valueDatatype);
        MessageList list = new MessageList();

        attributeAbstractDatatypeValidator.validateNotAbstractDatatype(list);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidateNotAbstractDatatype_datatypeAbstract() throws Exception {
        when(attribute.findDatatype(ipsProject)).thenReturn(valueDatatype);
        when(valueDatatype.isAbstract()).thenReturn(true);
        MessageList list = new MessageList();

        attributeAbstractDatatypeValidator.validateNotAbstractDatatype(list);

        assertFalse(list.isEmpty());
        assertNotNull(list.getMessageByCode(IType.MSGCODE_ABSTRACT_MISSING));
    }

}
