/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.junit.Before;
import org.junit.Test;

public class PolicyAttributeConditionTest extends AbstractIpsPluginTest {

    private final PolicyAttributeConditionType condition = new PolicyAttributeConditionType();
    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private PolicyCmptType policyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        policyCmptType = newPolicyAndProductCmptType(ipsProject, "aaa.PolicyType", "aaa.ProductType");
        productCmptType = ipsProject.findProductCmptType(policyCmptType.getProductCmptType());
    }

    @Test
    public void testGetSearchableElements() {

        IPolicyCmptTypeAttribute zahlweiseAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        zahlweiseAttribute.setValueSetConfiguredByProduct(true);
        zahlweiseAttribute.setName("zahlweise");

        IPolicyCmptTypeAttribute personAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        personAttribute.setValueSetConfiguredByProduct(true);
        personAttribute.setName("person");

        IPolicyCmptTypeAttribute vertragsnummernAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        vertragsnummernAttribute.setValueSetConfiguredByProduct(false);
        vertragsnummernAttribute.setName("vertragsnummern");

        List<? extends IIpsElement> searchableElements = condition.getSearchableElements(productCmptType);

        assertEquals(2, searchableElements.size());
        assertTrue(searchableElements.contains(zahlweiseAttribute));
        assertTrue(searchableElements.contains(personAttribute));

    }

    @Test
    public void testGetValueDatatypeAndValueSet() {
        IPolicyCmptTypeAttribute attribut = policyCmptType.newPolicyCmptTypeAttribute();

        IntegerDatatype integerDatatype = new IntegerDatatype();
        attribut.setDatatype(integerDatatype.getName());

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.copy(any(IValueSetOwner.class), any(String.class))).thenReturn(valueSet);

        attribut.setValueSetCopy(valueSet);

        assertTrue(condition.hasValueSet());

        try {
            condition.getAllowedValues(attribut);
            fail();
        } catch (Exception e) {
            // nix
        }

        assertEquals(integerDatatype, condition.getValueDatatype(attribut));
        assertEquals(valueSet, condition.getValueSet(attribut));

    }

    @Test
    public void testGetSearchOperatorTypes() {
        IPolicyCmptTypeAttribute attributComparable = policyCmptType.newPolicyCmptTypeAttribute();

        ValueSetSearchOperatorType[] values = ValueSetSearchOperatorType.values();

        List<? extends ISearchOperatorType> searchOperatorTypes = condition.getSearchOperatorTypes(attributComparable);

        assertEquals(values.length, searchOperatorTypes.size());
        for (ValueSetSearchOperatorType valueSetSearchOperatorType : values) {
            assertTrue(searchOperatorTypes.contains(valueSetSearchOperatorType));
        }
    }

    @Test
    public void testOperandProvider() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("zahlweise");

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");
        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.newGeneration();

        IConfiguredValueSet configValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);

        String lower = "100";
        String upper = "200";
        String step = "10";
        IValueSet expectedValueSet = new RangeValueSet(attribute, attribute.getId(), lower, upper, step);
        configValueSet.setValueSetCopy(expectedValueSet);

        IOperandProvider operandProvider = condition.createOperandProvider(attribute);

        IRangeValueSet foundRangeSet = (IRangeValueSet)operandProvider.getSearchOperand(generation);

        assertNotNull(foundRangeSet);

        assertEquals(lower, foundRangeSet.getLowerBound());
        assertEquals(upper, foundRangeSet.getUpperBound());
        assertEquals(step, foundRangeSet.getStep());

    }
}
