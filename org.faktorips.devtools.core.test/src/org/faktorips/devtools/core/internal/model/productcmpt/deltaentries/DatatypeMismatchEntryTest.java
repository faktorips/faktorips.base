/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.functional.Consumer;
import org.junit.Before;
import org.junit.Test;

public class DatatypeMismatchEntryTest extends AbstractIpsPluginTest {

    private IAttributeValue attrValue;
    private IConfiguredDefault configuredDefault;
    private IIpsProject ipsProject;
    private ProductCmpt productCmpt;
    private final List<String> result = new LinkedList<String>();
    private List<DatatypeMismatchEntry> entries;
    private Consumer<List<String>> valueConsumer = new Consumer<List<String>>() {

        @Override
        public void accept(List<String> list) {
            result.addAll(list);
        }
    };

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        ProductCmptType productCmptType = (ProductCmptType)policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "ProductA");

        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)productCmptType.newAttribute();
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        attribute.setName("attribute");
        attribute.setChangingOverTime(false);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        attrValue = spy(productCmpt.getAttributeValue("attribute"));
        attribute.setDatatype(Datatype.MONEY.getQualifiedName());

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute("polAttr");
        policyCmptTypeAttribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        policyCmptTypeAttribute.setProductRelevant(true);
        policyCmptTypeAttribute.setChangingOverTime(false);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        configuredDefault = productCmpt.getPropertyValue(policyCmptTypeAttribute, IConfiguredDefault.class);

        policyCmptTypeAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
    }

    @Test
    public void testFix() throws Exception {
        DatatypeMismatchEntry datatypeMismatchEntry = new DatatypeMismatchEntry(attrValue,
                Collections.singletonList("10.00"), ValueConverter.TO_MONEY, valueConsumer);

        datatypeMismatchEntry.fix();

        assertEquals(1, result.size());
        assertEquals("10.00 EUR", result.get(0));
    }

    @Test
    public void testFixForListWithUnconvertibleValues() {
        List<String> list = Arrays.asList("10.00", "10", "10.12341234");

        DatatypeMismatchEntry dataTypeMismatchEntry = new DatatypeMismatchEntry(attrValue, list,
                ValueConverter.TO_MONEY, valueConsumer);

        dataTypeMismatchEntry.fix();

        assertEquals(3, result.size());
        assertEquals("10.00 EUR", result.get(0));
        // € always has two digits after the .
        assertEquals("10.00 EUR", result.get(1));
        // can't convert because of precision loss
        assertEquals("10.12341234", result.get(2));
    }

    @Test
    public void testFixForEmptyList() {
        DatatypeMismatchEntry dataTypeMismatchEntry = new DatatypeMismatchEntry(attrValue,
                Collections.<String> emptyList(), ValueConverter.TO_MONEY, valueConsumer);

        dataTypeMismatchEntry.fix();

        assertEquals(true, result.isEmpty());
    }

    @Test
    public void testCreateAttributeValue() {
        assertTrue(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(attrValue)).isEmpty());

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "10.0"));
        assertFalse(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(attrValue)).isEmpty());
    }

    @Test
    public void testCreateIfValidationFailsAttributeValue() throws CoreException {
        doThrow(new CoreException(Status.CANCEL_STATUS)).when(attrValue).validate(ipsProject);

        assertTrue(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(attrValue)).isEmpty());
    }

    @Test
    public void testCreateConfiguredDefault() {
        assertTrue(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(configuredDefault)).isEmpty());

        configuredDefault.setValue("10.0");
        assertFalse(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(configuredDefault)).isEmpty());
    }

    @Test
    public void testFixConfiguredDefault() throws Exception {
        DatatypeMismatchEntry datatypeMismatchEntry = new DatatypeMismatchEntry(configuredDefault,
                Collections.singletonList("10.00"), ValueConverter.TO_MONEY, valueConsumer);

        datatypeMismatchEntry.fix();

        assertEquals(1, result.size());
        assertEquals("10.00 EUR", result.get(0));
    }

    @Test
    public void testForEachMismatch() {
        attrValue.setValueHolder(new SingleValueHolder(attrValue, "10.0"));
        configuredDefault.setValue("11.0");
        entries = DatatypeMismatchEntry
                .forEachMismatch(Arrays.asList(attrValue, configuredDefault, mock(IFormula.class)));
        assertEquals(2, entries.size());
        assertEquals(entries.get(0).getPropertyName(), attrValue.getPropertyName());
        assertEquals(entries.get(1).getPropertyName(), configuredDefault.getPropertyName());
    }

    @Test
    public void testForEachMismatchMultiValue() {
        attrValue.setValueHolder(
                new MultiValueHolder(attrValue, Arrays.asList(new SingleValueHolder(attrValue, "10.99999"),
                        new SingleValueHolder(attrValue, "10"), new SingleValueHolder(attrValue, "10 EUR"))));
        entries = DatatypeMismatchEntry.forEachMismatch(Arrays.asList(attrValue));

        assertEquals(1, entries.size());
        assertEquals(entries.get(0).getPropertyName(), attrValue.getPropertyName());
    }

    @Test
    public void testForEachMismatchNoConversionNeeded() {
        attrValue.setValueHolder(new SingleValueHolder(attrValue, "10.00 EUR"));
        entries = DatatypeMismatchEntry.forEachMismatch(Arrays.asList(attrValue));

        assertEquals(0, entries.size());

    }
}
