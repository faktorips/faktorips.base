/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.search.product.conditions.PolicyAttributeCondition;
import org.junit.Before;
import org.junit.Test;

public class ProductSearchConditionPresentationModelTest {
    private ProductSearchPresentationModel parentSearchPresentationModel;
    private ProductSearchConditionPresentationModel model;
    private PropertyChangeListener listener;
    private IIpsElement ipsElement;

    @Before
    public void setUp() {
        parentSearchPresentationModel = new ProductSearchPresentationModel();

        IProductCmptType productCmptType = mock(IProductCmptType.class);
        parentSearchPresentationModel.setProductCmptType(productCmptType);

        model = new ProductSearchConditionPresentationModel(parentSearchPresentationModel);

        ipsElement = mock(IIpsElement.class);

        listener = mock(PropertyChangeListener.class);
        parentSearchPresentationModel.addPropertyChangeListener(listener);
    }

    @Test
    public void testValidityBeforeInput() {
        assertFalse(model.isValid());
    }

    @Test
    public void testSetConditionWithoutSearchableElements() {
        PolicyAttributeCondition condition = mock(PolicyAttributeCondition.class);

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
        PolicyAttributeCondition condition = createConditionwithSearchableElements();

        model.setCondition(condition);
    }

    @Test
    public void testValidityAfterSetCondition() {
        PolicyAttributeCondition condition = createConditionwithSearchableElements();

        model.setCondition(condition);

        verify(listener).propertyChange(any(PropertyChangeEvent.class));

        assertFalse(model.isValid());
    }

    @Test
    public void testValidityAfterAllSetters() {
        PolicyAttributeCondition condition = createConditionwithSearchableElements();

        model.setCondition(condition);

        verify(listener).propertyChange(any(PropertyChangeEvent.class));
        assertFalse(model.isValid());

        model.setSearchedElement(ipsElement);

        verify(listener, times(2)).propertyChange(any(PropertyChangeEvent.class));
        assertFalse(model.isValid());

        model.setOperatorTypeIndex(0);

        verify(listener, times(3)).propertyChange(any(PropertyChangeEvent.class));
        assertTrue(model.isValid());

        model.setArgument("");

        verify(listener, times(4)).propertyChange(any(PropertyChangeEvent.class));
        assertTrue(model.isValid());
    }

    @Test
    public void testValidityAfterSettingConditionTwice() {
        PolicyAttributeCondition condition = createConditionwithSearchableElements();

        model.setCondition(condition);
        model.setSearchedElement(ipsElement);
        model.setOperatorTypeIndex(0);
        model.setArgument("");

        assertTrue(model.isValid());

        model.setCondition(createConditionwithSearchableElements());
        assertFalse(model.isValid());
    }

    private PolicyAttributeCondition createConditionwithSearchableElements() {
        PolicyAttributeCondition condition = mock(PolicyAttributeCondition.class);

        List<IIpsElement> list = new ArrayList<IIpsElement>();
        list.add(ipsElement);

        when(condition.getSearchableElements(any(IProductCmptType.class))).thenReturn(list);
        return condition;
    }
}
