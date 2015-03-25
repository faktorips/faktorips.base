/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
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
