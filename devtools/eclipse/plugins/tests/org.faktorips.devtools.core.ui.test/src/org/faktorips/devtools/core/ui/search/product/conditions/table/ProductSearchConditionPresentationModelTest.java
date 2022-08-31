/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.ui.search.product.ProductSearchPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.PolicyAttributeConditionType;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class ProductSearchConditionPresentationModelTest {
    private ProductSearchPresentationModel parentSearchPresentationModel;
    private ProductSearchConditionPresentationModel model;
    private PropertyChangeListener listener;
    private IIpsElement ipsElement;
    private ISearchOperatorType operatorType;

    @Before
    public void setUp() {
        parentSearchPresentationModel = new ProductSearchPresentationModel();

        IProductCmptType productCmptType = mock(IProductCmptType.class);
        parentSearchPresentationModel.setProductCmptType(productCmptType);

        model = new ProductSearchConditionPresentationModel(parentSearchPresentationModel);

        ipsElement = mock(IIpsElement.class);
        when(ipsElement.getName()).thenReturn("productAttribute");
        operatorType = mock(ISearchOperatorType.class);

        listener = mock(PropertyChangeListener.class);
        parentSearchPresentationModel.addPropertyChangeListener(listener);
    }

    @Test
    public void testValidityBeforeInput() {
        assertFalse(model.isValid());
    }

    @Test
    public void testSetConditionWithoutSearchableElements() {
        PolicyAttributeConditionType condition = mock(PolicyAttributeConditionType.class);

        List<IIpsElement> emptyList = Collections.emptyList();
        when(condition.getSearchableElements(any(IProductCmptType.class))).thenReturn(emptyList);

        try {
            model.setCondition(condition);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected Exception
        }
    }

    @Test
    public void testSetConditionWithSearchableElements() {
        PolicyAttributeConditionType condition = createConditionwithSearchableElements();

        model.setCondition(condition);
    }

    @Test
    public void testValidityAfterSetCondition() {
        PolicyAttributeConditionType condition = createConditionwithSearchableElements();

        model.setCondition(condition);

        verify(listener).propertyChange(any(PropertyChangeEvent.class));

        assertFalse(model.isValid());
    }

    @Test
    public void testValidityAfterAllSetters() {
        PolicyAttributeConditionType condition = createConditionwithSearchableElements();

        model.setCondition(condition);

        verify(listener).propertyChange(any(PropertyChangeEvent.class));
        assertFalse(model.isValid());

        model.setSearchedElement(ipsElement);

        verify(listener, times(2)).propertyChange(any(PropertyChangeEvent.class));
        assertFalse(model.isValid());

        model.setOperatorType(null);

        verify(listener, times(3)).propertyChange(any(PropertyChangeEvent.class));
        assertFalse(model.isValid());

        model.setOperatorType(operatorType);

        verify(listener, times(4)).propertyChange(any(PropertyChangeEvent.class));
        assertTrue(model.isValid());

        model.setArgument("");

        verify(listener, times(5)).propertyChange(any(PropertyChangeEvent.class));
        assertTrue(model.isValid());
    }

    @Test
    public void testValidityAfterSettingConditionTwice() {
        PolicyAttributeConditionType condition = createConditionwithSearchableElements();

        model.setCondition(condition);
        model.setSearchedElement(ipsElement);
        model.setOperatorType(operatorType);
        model.setArgument("");

        assertTrue(model.isValid());

        model.setCondition(createConditionwithSearchableElements());
        assertFalse(model.isValid());
    }

    private PolicyAttributeConditionType createConditionwithSearchableElements() {
        PolicyAttributeConditionType condition = mock(PolicyAttributeConditionType.class);

        List<IIpsElement> list = new ArrayList<>();
        list.add(ipsElement);

        when(condition.getSearchableElements(any(IProductCmptType.class))).thenReturn(list);
        return condition;
    }

    @Test
    public void testSetSearchableElementByName() {
        PolicyAttributeConditionType condition = createConditionwithSearchableElements();
        model.setCondition(condition);
        model.setSearchedElement(null);

        model.setSearchedElementByName("doesNotExist");
        assertNull(model.getSearchedElement());
        model.setSearchedElementByName("productAttribute");
        assertNotNull(model.getSearchedElement());
        model.setSearchedElementByName("doesNotExist");
        assertNotNull(model.getSearchedElement());
    }
}
