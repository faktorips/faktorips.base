/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UniqueIdentifierValidatorTest {

    @Mock
    private EnumValueContainer container;

    @Mock
    private IIpsModel ipsModel;

    private UniqueIdentifierValidator uniqueIdentifierValidator;

    @Before
    public void createUniqueIdentifierValidator() throws Exception {
        when(container.getIpsModel()).thenReturn(ipsModel);
        uniqueIdentifierValidator = new UniqueIdentifierValidator(container);
    }

    @Test
    public void testGetUniqueIdentifierViolations_noViolation() throws Exception {
        createValues();
        IEnumAttributeValue enumAttributeValue = container.findAggregatedEnumValues().get(0).getEnumAttributeValues()
                .get(0);

        List<String> uniqueIdentifierViolations = uniqueIdentifierValidator
                .getUniqueIdentifierViolations(enumAttributeValue);

        assertTrue(uniqueIdentifierViolations.isEmpty());
    }

    @Test
    public void testGetUniqueIdentifierViolations_sameAttributeViolation() throws Exception {
        createValues();
        IEnumAttributeValue firstEnumAttributeValue = container.findAggregatedEnumValues().get(0)
                .getEnumAttributeValues().get(0);
        doReturn(new StringValue("equalValue")).when(firstEnumAttributeValue).getValue();
        IEnumAttributeValue secondEnumAttributeValue = container.findAggregatedEnumValues().get(1)
                .getEnumAttributeValues().get(0);
        doReturn(new StringValue("equalValue")).when(secondEnumAttributeValue).getValue();

        List<String> uniqueIdentifierViolations = uniqueIdentifierValidator
                .getUniqueIdentifierViolations(firstEnumAttributeValue);

        assertThat(uniqueIdentifierViolations, hasItem("equalValue"));
    }

    @Test
    public void testGetUniqueIdentifierViolations_differentAttributes() throws Exception {
        createValues();
        IEnumAttributeValue firstEnumAttributeValue = container.findAggregatedEnumValues().get(0)
                .getEnumAttributeValues().get(0);
        StringValue equalValue = new StringValue("equalValue");
        doReturn(equalValue).when(firstEnumAttributeValue).getValue();
        IEnumAttributeValue secondEnumAttributeValue = container.findAggregatedEnumValues().get(0)
                .getEnumAttributeValues().get(1);
        doReturn(equalValue).when(secondEnumAttributeValue).getValue();

        List<String> uniqueIdentifierViolations = uniqueIdentifierValidator
                .getUniqueIdentifierViolations(firstEnumAttributeValue);

        assertTrue(uniqueIdentifierViolations.isEmpty());
    }

    @Test
    public void testGetUniqueIdentifierViolations_sameAttributeViolationInternational() throws Exception {
        createValues();
        IEnumAttributeValue firstEnumAttributeValue = container.findAggregatedEnumValues().get(0)
                .getEnumAttributeValues().get(0);
        InternationalStringValue value1 = new InternationalStringValue();
        value1.getContent().add(new LocalizedString(Locale.GERMAN, "differen1"));
        value1.getContent().add(new LocalizedString(Locale.ENGLISH, "same"));
        InternationalStringValue value2 = new InternationalStringValue();
        value2.getContent().add(new LocalizedString(Locale.GERMAN, "differen2"));
        value2.getContent().add(new LocalizedString(Locale.ENGLISH, "same"));
        doReturn(value1).when(firstEnumAttributeValue).getValue();
        IEnumAttributeValue secondEnumAttributeValue = container.findAggregatedEnumValues().get(1)
                .getEnumAttributeValues().get(0);
        doReturn(value2).when(secondEnumAttributeValue).getValue();

        List<String> uniqueIdentifierViolations = uniqueIdentifierValidator
                .getUniqueIdentifierViolations(firstEnumAttributeValue);

        assertThat(uniqueIdentifierViolations, hasItem("same"));
    }

    @Test
    public void testGetUniqueIdentifierViolations_sameAttributeViolationDifferentInternational() throws Exception {
        createValues();
        IEnumAttributeValue firstEnumAttributeValue = container.findAggregatedEnumValues().get(0)
                .getEnumAttributeValues().get(0);
        InternationalStringValue value1 = new InternationalStringValue();
        value1.getContent().add(new LocalizedString(Locale.GERMAN, "differen1"));
        value1.getContent().add(new LocalizedString(Locale.ENGLISH, "same"));
        InternationalStringValue value2 = new InternationalStringValue();
        value2.getContent().add(new LocalizedString(Locale.GERMAN, "same"));
        value2.getContent().add(new LocalizedString(Locale.ENGLISH, "different2"));
        doReturn(value1).when(firstEnumAttributeValue).getValue();
        IEnumAttributeValue secondEnumAttributeValue = container.findAggregatedEnumValues().get(1)
                .getEnumAttributeValues().get(0);
        doReturn(value2).when(secondEnumAttributeValue).getValue();

        List<String> uniqueIdentifierViolations = uniqueIdentifierValidator
                .getUniqueIdentifierViolations(firstEnumAttributeValue);

        assertTrue(uniqueIdentifierViolations.isEmpty());
    }

    private void createValues() {
        List<IEnumValue> values = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            IEnumValue enumValue = mock(IEnumValue.class);
            createAttributeValues(enumValue, i);
            values.add(enumValue);
        }
        when(container.findAggregatedEnumValues()).thenReturn(values);
    }

    private void createAttributeValues(IEnumValue enumValue, int i) {
        List<IEnumAttributeValue> enumAttributeValues = new ArrayList<>();
        for (int j = 0; j < 2; j++) {
            IEnumAttributeValue enumAttributeValue = mock(IEnumAttributeValue.class);
            when(enumAttributeValue.getEnumValue()).thenReturn(enumValue);
            doReturn(new StringValue("abc" + i + j)).when(enumAttributeValue).getValue();
            enumAttributeValues.add(enumAttributeValue);
        }
        when(enumValue.getEnumAttributeValues()).thenReturn(enumAttributeValues);
    }

}
