/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
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
        uniqueIdentifierValidator = new UniqueIdentifierValidator(container);
    }

    @Before
    public void initContainer() {
        when(container.getIpsModel()).thenReturn(ipsModel);
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
        List<IEnumValue> values = new ArrayList<IEnumValue>();
        for (int i = 0; i < 5; i++) {
            IEnumValue enumValue = mock(IEnumValue.class);
            createAttributeValues(enumValue, i);
            values.add(enumValue);
        }
        when(container.findAggregatedEnumValues()).thenReturn(values);
    }

    private void createAttributeValues(IEnumValue enumValue, int i) {
        List<IEnumAttributeValue> enumAttributeValues = new ArrayList<IEnumAttributeValue>();
        for (int j = 0; j < 2; j++) {
            IEnumAttributeValue enumAttributeValue = mock(IEnumAttributeValue.class);
            when(enumAttributeValue.getEnumValue()).thenReturn(enumValue);
            doReturn(new StringValue("abc" + i + j)).when(enumAttributeValue).getValue();
            enumAttributeValues.add(enumAttributeValue);
        }
        when(enumValue.getEnumAttributeValues()).thenReturn(enumAttributeValues);
    }

}
