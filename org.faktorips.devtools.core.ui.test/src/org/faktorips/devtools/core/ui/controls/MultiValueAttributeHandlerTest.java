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

package org.faktorips.devtools.core.ui.controls;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.controller.fields.PaymentMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MultiValueAttributeHandlerTest {

    private IAttributeValue attrValue;
    private Shell shell;
    private IIpsProject ipsProject;
    private IpsObjectPart valueSetOwner;
    private SingletonMockHelper singletonMockHelper;

    @Before
    public void setUp() throws Exception {
        IpsPlugin ipsPlugin = mock(IpsPlugin.class);
        IpsModel ipsModel = mock(IpsModel.class);
        singletonMockHelper = new SingletonMockHelper();
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);
        when(ipsPlugin.getIpsModel()).thenReturn(ipsModel);
        ipsProject = mock(IIpsProject.class);
        shell = mock(Shell.class);

        attrValue = mock(IAttributeValue.class);
        mockIpsObjectPart(attrValue);
        valueSetOwner = mock(IpsObjectPart.class, withSettings().extraInterfaces(IValueSetOwner.class));
        mockIpsObjectPart(valueSetOwner);
    }

    @After
    public void tearDown() {
        singletonMockHelper.reset();
    }

    private void mockIpsObjectPart(IIpsObjectPart part) {
        when(part.getIpsProject()).thenReturn(ipsProject);
        IIpsObject ipsObject = mock(IIpsObject.class);
        when(part.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.getIpsSrcFile()).thenReturn(null);
    }

    // Mockito does not support generic class arguments
    @SuppressWarnings("unchecked")
    @Test
    public void editEnumValueSetWithSubsetChooser() {
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        IEnumValueSet valueSet = mock(IEnumValueSet.class);
        when(prodAttr.getValueSet()).thenReturn(valueSet);

        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue,
                ValueDatatype.DECIMAL));
        doNothing().when(handler).openMultiValueSubsetDialog(any(List.class));

        handler.editValues();
        verify(handler).openMultiValueSubsetDialog(valueSet);
    }

    @Test
    // ArgumentCaptor does not support generic classes
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void editEnumDatatypeWithSubsetChooser() {
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        EnumDatatype enumDatatype = new PaymentMode();
        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue,
                enumDatatype));

        ArgumentCaptor<List> ValueListCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(handler).openMultiValueSubsetDialog(any(List.class));
        handler.editValues();
        verify(handler).openMultiValueSubsetDialog(ValueListCaptor.capture());

        List<String> allValuesList = ValueListCaptor.getValue();
        assertNotNull(allValuesList);
        allValuesList.contains(PaymentMode.ANNUAL_ID);
        allValuesList.contains(PaymentMode.MONTHLY_ID);
    }

    @Test
    public void editOtherDatatypesWithMultiValueDialog() {
        ValueDatatype datatype = new IntegerDatatype();
        IProductCmptTypeAttribute prodAttr = mock(IProductCmptTypeAttribute.class);
        MultiValueAttributeHandler handler = spy(new MultiValueAttributeHandler(shell, prodAttr, attrValue, datatype));

        doNothing().when(handler).openMultiValueDialog();

        handler.editValues();
        verify(handler).openMultiValueDialog();
    }

}
