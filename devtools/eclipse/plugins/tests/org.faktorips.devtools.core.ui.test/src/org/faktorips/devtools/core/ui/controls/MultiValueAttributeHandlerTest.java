/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.ui.controller.fields.PaymentMode;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MultiValueAttributeHandlerTest {

    private IAttributeValue attrValue;
    private Shell shell;
    private IIpsProject ipsProject;
    private IpsObjectPart valueSetOwner;

    @Before
    public void setUp() throws Exception {
        ipsProject = mock(IIpsProject.class);
        shell = mock(Shell.class);

        attrValue = mock(IAttributeValue.class);
        mockIpsObjectPart(attrValue);
        valueSetOwner = mock(IpsObjectPart.class, withSettings().extraInterfaces(IValueSetOwner.class));
        mockIpsObjectPart(valueSetOwner);
    }

    private void mockIpsObjectPart(IIpsObjectPart part) {
        when(part.getIpsProject()).thenReturn(ipsProject);
        IIpsObject ipsObject = mock(IIpsObject.class);
        when(part.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.getIpsSrcFile()).thenReturn(null);
    }

    @Test
    public void editEnumValueSetWithSubsetChooser() {
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        IEnumValueSet valueSet = mock(IEnumValueSet.class);
        when(valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(true);
        when(prodAttr.getValueSet()).thenReturn(valueSet);

        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue,
                ValueDatatype.DECIMAL));
        doNothing().when(handler).openMultiValueSubsetDialog(anyList());

        handler.editValues();
        verify(handler).openMultiValueSubsetDialog(valueSet);
    }

    @Test
    public void editAbstractEnumValueSet_withEditTable() {
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        IEnumValueSet valueSet = mock(IEnumValueSet.class);
        when(valueSet.isEnum()).thenReturn(true);
        when(valueSet.isAbstract()).thenReturn(true);
        when(prodAttr.getValueSet()).thenReturn(valueSet);

        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue,
                ValueDatatype.DECIMAL));
        doNothing().when(handler).openMultiValueDialog();

        handler.editValues();
        verify(handler).openMultiValueDialog();
    }

    @Test
    public void editAbstractEnumValueSetWithEnumDatatype_UsingSubsetChooser() {
        MultiValueAttributeHandler handler = setUpEnumDatatypeAttributeWithEnumValueSet(true);

        List<String> allValuesList = verifyOpenAndCaptureValues(handler);
        assertEquals(3, allValuesList.size());
        assertTrue(allValuesList.contains(null));
        allValuesList.contains(PaymentMode.ANNUAL_ID);
        allValuesList.contains(PaymentMode.MONTHLY_ID);
    }

    @Test
    public void editEnumDatatypeWithSubsetChooser() {
        MultiValueAttributeHandler handler = setUpEnumDatatypeAttributeWithEnumValueSet(false);

        List<String> allValuesList = verifyOpenAndCaptureValues(handler);
        assertEquals(1, allValuesList.size());
        assertTrue(allValuesList.contains(PaymentMode.ANNUAL_ID));
    }

    // ArgumentCaptor does not support generic classes
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<String> verifyOpenAndCaptureValues(MultiValueAttributeHandler handler) {
        ArgumentCaptor<List> valueListCaptor = ArgumentCaptor.forClass(List.class);
        handler.editValues();
        verify(handler).openMultiValueSubsetDialog(valueListCaptor.capture());

        List<String> allValuesList = valueListCaptor.getValue();
        assertNotNull(allValuesList);
        return allValuesList;
    }

    protected MultiValueAttributeHandler setUpEnumDatatypeAttributeWithEnumValueSet(boolean abstractValueSet) {
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        EnumDatatype enumDatatype = new PaymentMode();
        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue,
                enumDatatype));

        List<String> valueSetValues = new ArrayList<>();
        valueSetValues.add(PaymentMode.ANNUAL_ID);

        IEnumValueSet enumValueset = mock(IEnumValueSet.class);
        when(enumValueset.isEnum()).thenReturn(true);
        when(enumValueset.isAbstract()).thenReturn(abstractValueSet);
        when(enumValueset.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(!abstractValueSet);
        when(prodAttr.getValueSet()).thenReturn(enumValueset);
        when(enumValueset.getValuesAsList()).thenReturn(valueSetValues);
        when(enumValueset.isContainsNull()).thenReturn(true);
        doNothing().when(handler).openMultiValueSubsetDialog(anyList());
        return handler;
    }

    @Test
    public void editOtherDatatypesWithMultiValueDialog() {
        ValueDatatype datatype = new IntegerDatatype();
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue, datatype));

        IValueSet valueset = mock(UnrestrictedValueSet.class);
        when(valueset.isEnum()).thenReturn(false);
        when(prodAttr.getValueSet()).thenReturn(valueset);
        doNothing().when(handler).openMultiValueDialog();

        handler.editValues();
        verify(handler).openMultiValueDialog();
    }

    @Test
    public void aquireValueSetDynamically() {
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue,
                ValueDatatype.DECIMAL));
        doNothing().when(handler).openMultiValueDialog();

        IValueSet unrestrictedValueset = mock(UnrestrictedValueSet.class);
        when(prodAttr.getValueSet()).thenReturn(unrestrictedValueset);
        handler.editValues();
        verify(handler).openMultiValueDialog();
        reset(handler);

        doNothing().when(handler).openMultiValueSubsetDialog(any(IEnumValueSet.class));
        IValueSet enumValueset = mock(IEnumValueSet.class);
        when(enumValueset.canBeUsedAsSupersetForAnotherEnumValueSet()).thenReturn(true);
        when(prodAttr.getValueSet()).thenReturn(enumValueset);
        handler.editValues();
        verify(handler).openMultiValueSubsetDialog(any(IEnumValueSet.class));
    }

}
