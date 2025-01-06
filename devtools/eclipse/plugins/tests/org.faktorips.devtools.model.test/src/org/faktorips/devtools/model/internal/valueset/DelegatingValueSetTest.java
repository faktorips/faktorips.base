/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DelegatingValueSetTest {

    private static final String ANY_VALUE = "anyValue";

    private static final MessageList MESSAGE_LIST = new MessageList(Message.newError(ANY_VALUE, ANY_VALUE));

    @Mock
    private ValueSet delegate;

    @Mock
    private IValueSetOwner parent;

    @Mock
    private IValueSet valuesOf;

    @Mock
    private IValueSet source;

    @Mock
    private EnumDatatype enumDatatype;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private AbstractValueSetValidator<?> validator;

    @InjectMocks
    private DelegatingValueSet delegatingValueSet;

    @Test
    public void testInitPropertiesFromXml() throws Exception {
        Element element = mock(Element.class);

        delegatingValueSet.initPropertiesFromXml(element, ANY_VALUE);

        // nothing expected - no exception just nothing
        verifyNoInteractions(element);
    }

    @Test(expected = IllegalStateException.class)
    public void testSetValuesOf() throws Exception {
        delegatingValueSet.setValuesOf(source);
    }

    @Test(expected = IllegalStateException.class)
    public void testSetAbstract() throws Exception {
        delegatingValueSet.setAbstract(true);
    }

    @Test
    public void testCopy() throws Exception {
        when(delegate.copy(parent, ANY_VALUE)).thenReturn(source);

        assertThat(delegatingValueSet.copy(parent, ANY_VALUE), is(source));
    }

    @Test(expected = IllegalStateException.class)
    public void testSetContainsNull() throws Exception {
        delegatingValueSet.setContainsNull(false);
    }

    @Test(expected = IllegalStateException.class)
    public void testSetLowerBound() throws Exception {
        delegatingValueSet.setLowerBound("123");
    }

    @Test(expected = IllegalStateException.class)
    public void testSetStep() throws Exception {
        delegatingValueSet.setStep("1");
    }

    @Test(expected = IllegalStateException.class)
    public void testSetUpperBound() throws Exception {
        delegatingValueSet.setUpperBound("1234");
    }

    @Test(expected = IllegalStateException.class)
    public void testSetEmpty() throws Exception {
        delegatingValueSet.setEmpty(true);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddValue() throws Exception {
        delegatingValueSet.addValue("asd");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddValues() throws Exception {
        delegatingValueSet.addValues(Arrays.asList("asd"));
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveValueInt() throws Exception {
        delegatingValueSet.removeValue(1);
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveValueString() throws Exception {
        delegatingValueSet.removeValue("1");
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveValues() throws Exception {
        delegatingValueSet.removeValues(Arrays.asList("asd"));
    }

    @Test(expected = IllegalStateException.class)
    public void testSetValue() throws Exception {
        delegatingValueSet.setValue(0, "asd");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddValuesFromDatatype() throws Exception {
        delegatingValueSet.addValuesFromDatatype(enumDatatype);
    }

    @Test(expected = IllegalStateException.class)
    public void testMove() throws Exception {
        delegatingValueSet.move(Arrays.asList(0), false);
    }

    @Test
    public void testGetValueSetOwner() throws Exception {
        assertThat(delegatingValueSet.getValueSetOwner(), is(parent));
    }

    @Test
    public void testGetName() throws Exception {
        when(delegate.getName()).thenReturn(ANY_VALUE);

        assertThat(delegatingValueSet.getName(), is(ANY_VALUE));
    }

    @Test
    public void testGetValueSetType() throws Exception {
        when(delegate.getValueSetType()).thenReturn(ValueSetType.ENUM);

        assertThat(delegatingValueSet.getValueSetType(), is(ValueSetType.ENUM));
    }

    @Test
    public void testCreateElement() throws Exception {
        Document doc = mock(Document.class);
        Element element = mock(Element.class);
        when(delegate.createElement(doc)).thenReturn(element);

        assertThat(delegatingValueSet.createElement(doc), is(element));
    }

    @Test
    public void testPropertiesToXml() throws Exception {
        Element element = mock(Element.class);

        delegatingValueSet.propertiesToXml(element);

        verify(delegate).propertiesToXml(element);
    }

    @Test
    public void testIsAbstract() throws Exception {
        when(delegate.isAbstract()).thenReturn(true);

        assertThat(delegatingValueSet.isAbstract(), is(true));
    }

    @Test
    public void testIsDetailedSpecificationOf() throws Exception {
        when(delegate.isDetailedSpecificationOf(source)).thenReturn(true);

        assertThat(delegatingValueSet.isDetailedSpecificationOf(source), is(true));
    }

    @Test
    public void testIsSameTypeOfValueSet() throws Exception {
        when(delegate.isSameTypeOfValueSet(source)).thenReturn(true);

        assertThat(delegatingValueSet.isSameTypeOfValueSet(source), is(true));
    }

    @Test
    public void testIsUnrestricted() throws Exception {
        when(delegate.isUnrestricted()).thenReturn(true);

        assertThat(delegatingValueSet.isUnrestricted(), is(true));
    }

    @Test
    public void testIsEnum() throws Exception {
        when(delegate.isEnum()).thenReturn(true);

        assertThat(delegatingValueSet.isEnum(), is(true));
    }

    @Test
    public void testCanBeUsedAsSupersetForAnotherEnumValueSet() throws Exception {
        when(delegate.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(true);

        assertThat(delegatingValueSet.canBeUsedAsSupersetForAnotherEnumValueSet(), is(true));
    }

    @Test
    public void testIsRange() throws Exception {
        when(delegate.isRange()).thenReturn(true);

        assertThat(delegatingValueSet.isRange(), is(true));
    }

    @Test
    public void testIsEmpty() throws Exception {
        when(delegate.isEmpty()).thenReturn(true);

        assertThat(delegatingValueSet.isEmpty(), is(true));
    }

    @Test
    public void testIsAbstractAndNotUnrestricted() throws Exception {
        when(delegate.isAbstractAndNotUnrestricted()).thenReturn(true);

        assertThat(delegatingValueSet.isAbstractAndNotUnrestricted(), is(true));
    }

    @Test
    public void testContainsValue() throws Exception {
        when(delegate.containsValue(ANY_VALUE, ipsProject)).thenReturn(true);

        assertThat(delegatingValueSet.containsValue(ANY_VALUE, ipsProject), is(true));
    }

    @Test
    public void testContainsValueSet() throws Exception {
        when(delegate.containsValueSet(source)).thenReturn(true);

        assertThat(delegatingValueSet.containsValueSet(source), is(true));
    }

    @Test
    public void testToShortString() throws Exception {
        when(delegate.toShortString()).thenReturn(ANY_VALUE);

        assertThat(delegatingValueSet.toShortString(), is(ANY_VALUE));

    }

    @Test
    public void testIsContainsNull() throws Exception {
        when(delegate.isContainsNull()).thenReturn(true);

        assertThat(delegatingValueSet.isContainsNull(), is(true));
    }

    @Test
    public void testGetLowerBound() throws Exception {
        RangeValueSet rangeDelegate = mock(RangeValueSet.class);
        delegatingValueSet = new DelegatingValueSet(rangeDelegate, parent);
        when(rangeDelegate.getLowerBound()).thenReturn(ANY_VALUE);

        assertThat(delegatingValueSet.getLowerBound(), is(ANY_VALUE));
    }

    @Test
    public void testGetUpperBound() throws Exception {
        RangeValueSet rangeDelegate = mock(RangeValueSet.class);
        delegatingValueSet = new DelegatingValueSet(rangeDelegate, parent);
        when(rangeDelegate.getUpperBound()).thenReturn(ANY_VALUE);

        assertThat(delegatingValueSet.getUpperBound(), is(ANY_VALUE));
    }

    @Test
    public void testGetStep() throws Exception {
        RangeValueSet rangeDelegate = mock(RangeValueSet.class);
        delegatingValueSet = new DelegatingValueSet(rangeDelegate, parent);
        when(rangeDelegate.getStep()).thenReturn(ANY_VALUE);

        assertThat(delegatingValueSet.getStep(), is(ANY_VALUE));
    }

    @Test
    public void testGetValues() throws Exception {
        EnumValueSet enumDelegate = mock(EnumValueSet.class);
        delegatingValueSet = new DelegatingValueSet(enumDelegate, parent);
        when(enumDelegate.getValues()).thenReturn(new String[] { ANY_VALUE });

        assertThat(delegatingValueSet.getValues(), is(new String[] { ANY_VALUE }));
    }

    @Test
    public void testGetPositions() throws Exception {
        EnumValueSet enumDelegate = mock(EnumValueSet.class);
        delegatingValueSet = new DelegatingValueSet(enumDelegate, parent);
        when(enumDelegate.getPositions(ANY_VALUE)).thenReturn(Arrays.asList(1));

        assertThat(delegatingValueSet.getPositions(ANY_VALUE), is(Arrays.asList(1)));
    }

    @Test
    public void testGetValue() throws Exception {
        EnumValueSet enumDelegate = mock(EnumValueSet.class);
        delegatingValueSet = new DelegatingValueSet(enumDelegate, parent);
        when(enumDelegate.getValue(1)).thenReturn(ANY_VALUE);

        assertThat(delegatingValueSet.getValue(1), is(ANY_VALUE));
    }

    @Test
    public void testSize() throws Exception {
        EnumValueSet enumDelegate = mock(EnumValueSet.class);
        delegatingValueSet = new DelegatingValueSet(enumDelegate, parent);
        when(enumDelegate.size()).thenReturn(123);

        assertThat(delegatingValueSet.size(), is(123));
    }

    @Test
    public void testGetValuesNotContained() throws Exception {
        EnumValueSet enumDelegate = mock(EnumValueSet.class);
        delegatingValueSet = new DelegatingValueSet(enumDelegate, parent);
        when(enumDelegate.getValuesNotContained(enumDelegate)).thenReturn(new String[] { ANY_VALUE });

        assertThat(delegatingValueSet.getValuesNotContained(enumDelegate), is(new String[] { ANY_VALUE }));
    }

    @Test
    public void testGetValuesAsList() throws Exception {
        EnumValueSet enumDelegate = mock(EnumValueSet.class);
        delegatingValueSet = new DelegatingValueSet(enumDelegate, parent);
        when(enumDelegate.getValuesAsList()).thenReturn(Arrays.asList(ANY_VALUE));

        assertThat(delegatingValueSet.getValuesAsList(), is(Arrays.asList(ANY_VALUE)));
    }

    @Test
    public void testGetMaximumLength() throws Exception {
        StringLengthValueSet stringLengthDelegate = mock(StringLengthValueSet.class);
        delegatingValueSet = new DelegatingValueSet(stringLengthDelegate, parent);
        when(stringLengthDelegate.getMaximumLength()).thenReturn("5");

        assertThat(delegatingValueSet.getMaximumLength(), is("5"));
    }

    @Test
    public void testSetMaximumLength() throws Exception {
        StringLengthValueSet stringLengthDelegate = mock(StringLengthValueSet.class);
        delegatingValueSet = new DelegatingValueSet(stringLengthDelegate, parent);
        try {
            delegatingValueSet.setMaximumLength("99");
            fail("Expect IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("DelegatingValueSets cannot be changed"));
        }
    }

    @Test
    public void testGetParsedMaximumLength() throws Exception {
        StringLengthValueSet stringLengthDelegate = mock(StringLengthValueSet.class);
        delegatingValueSet = new DelegatingValueSet(stringLengthDelegate, parent);
        when(stringLengthDelegate.getParsedMaximumLength()).thenReturn(5);

        assertThat(delegatingValueSet.getParsedMaximumLength(), is(5));
    }

    @Test
    public void testValidateThis() throws Exception {
        when(parent.findValueDatatype(ipsProject)).thenReturn(enumDatatype);
        doReturn(validator).when(delegate).createValidator(parent, enumDatatype);
        when(validator.validate()).thenReturn(MESSAGE_LIST);

        MessageList list = new MessageList();
        delegatingValueSet.validateThis(list, ipsProject);

        assertThat(list, is(MESSAGE_LIST));
    }

    @Test
    public void testValidateValue() throws Exception {
        EnumValueSet enumDelegate = mock(EnumValueSet.class);
        EnumValueSetValidator enumValidator = mock(EnumValueSetValidator.class);
        delegatingValueSet = new DelegatingValueSet(enumDelegate, parent);
        when(delegatingValueSet.findValueDatatype(ipsProject)).thenReturn(enumDatatype);
        doReturn(enumValidator).when(enumDelegate).createValidator(parent, enumDatatype);
        when(enumValidator.validateValue(0)).thenReturn(MESSAGE_LIST);

        assertThat(delegatingValueSet.validateValue(0, ipsProject), is(MESSAGE_LIST));
    }

}
