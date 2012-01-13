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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.search.product.conditions.types.AllowanceSearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IOperandProvider;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.PolicyAttributeConditionType;
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
    public void testGetSearchableElements() throws CoreException {

        IPolicyCmptTypeAttribute zahlweiseAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        zahlweiseAttribute.setProductRelevant(true);
        zahlweiseAttribute.setName("zahlweise");

        IPolicyCmptTypeAttribute personAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        personAttribute.setProductRelevant(true);
        personAttribute.setName("person");

        IPolicyCmptTypeAttribute vertragsnummernAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        vertragsnummernAttribute.setProductRelevant(false);
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
        when(valueSet.copy(any(IIpsObjectPart.class), any(String.class))).thenReturn(valueSet);

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

        AllowanceSearchOperatorType[] values = AllowanceSearchOperatorType.values();

        List<? extends ISearchOperatorType> searchOperatorTypes = condition.getSearchOperatorTypes(attributComparable);

        assertEquals(values.length, searchOperatorTypes.size());
        for (AllowanceSearchOperatorType allowanceSearchOperatorType : values) {
            assertTrue(searchOperatorTypes.contains(allowanceSearchOperatorType));
        }
    }

    @Test
    public void testOperandProvider() throws CoreException {
        IPolicyCmptTypeAttribute attribut = policyCmptType.newPolicyCmptTypeAttribute();
        attribut.setName("zahlweise");

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");
        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.newGeneration();

        IConfigElement configElement = generation.newConfigElement(attribut);

        String lower = "100";
        String upper = "200";
        String step = "10";
        IValueSet expectedValueSet = new RangeValueSet(attribut, attribut.getId(), lower, upper, step);
        configElement.setValueSetCopy(expectedValueSet);

        IOperandProvider operandProvider = condition.createOperandProvider(attribut);

        RangeValueSet foundRangeSet = (RangeValueSet)operandProvider.getSearchOperand(generation);

        assertTrue(foundRangeSet != null);

        assertEquals(lower, foundRangeSet.getLowerBound());
        assertEquals(upper, foundRangeSet.getUpperBound());
        assertEquals(step, foundRangeSet.getStep());

    }
}
