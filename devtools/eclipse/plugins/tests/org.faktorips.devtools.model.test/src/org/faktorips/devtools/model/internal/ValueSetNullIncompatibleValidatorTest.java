/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.valueset.IValueSet;
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
public class ValueSetNullIncompatibleValidatorTest {

    @Mock
    private IValueSet valueSetWithNull;
    @Mock
    private IValueSet valueSetWithoutNull;

    @BeforeEach
    public void setUp() {
        when(valueSetWithNull.isContainsNull()).thenReturn(true);
        when(valueSetWithoutNull.isContainsNull()).thenReturn(false);
    }

    @Test
    public void testCanValidate() throws Exception {
        assertFalse(new ValueSetNullIncompatibleValidator(null, null).canValidate());
        assertFalse(new ValueSetNullIncompatibleValidator(null, valueSetWithNull).canValidate());
        assertFalse(new ValueSetNullIncompatibleValidator(valueSetWithNull, null).canValidate());
        assertTrue(new ValueSetNullIncompatibleValidator(valueSetWithNull, valueSetWithoutNull).canValidate());
    }

    @Test
    public void testValidateIfPossible() throws Exception {
        ValueSetNullIncompatibleValidator validator = new ValueSetNullIncompatibleValidator(valueSetWithNull,
                valueSetWithoutNull);
        MessageList messages = validator.validateIfPossible();
        assertTrue(messages.isEmpty());

        validator = new ValueSetNullIncompatibleValidator(valueSetWithoutNull, valueSetWithNull);
        messages = validator.validateIfPossible();
        assertFalse(messages.isEmpty());
        assertNotNull(messages.getMessageByCode(ValueSetNullIncompatibleValidator.MSGCODE_INCOMPATIBLE_VALUESET));
    }

    @Test
    public void testValidateAndAppendMessages_illegalValues() throws Exception {
        MessageList messages = new MessageList();
        ValueSetNullIncompatibleValidator validator = new ValueSetNullIncompatibleValidator(null, null);
        validator.validateAndAppendMessages(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testValidateAndAppendMessages() throws Exception {
        MessageList messages = new MessageList();
        ValueSetNullIncompatibleValidator validator = new ValueSetNullIncompatibleValidator(valueSetWithoutNull,
                valueSetWithNull);

        validator.validateAndAppendMessages(messages);

        assertFalse(messages.isEmpty());
        assertNotNull(messages.getMessageByCode(ValueSetNullIncompatibleValidator.MSGCODE_INCOMPATIBLE_VALUESET));
    }
}
