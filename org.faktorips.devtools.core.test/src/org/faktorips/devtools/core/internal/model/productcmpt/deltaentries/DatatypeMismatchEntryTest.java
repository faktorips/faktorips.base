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
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.functional.Consumer;
import org.junit.Before;
import org.junit.Test;

public class DatatypeMismatchEntryTest extends AbstractIpsPluginTest {

    private IAttributeValue attrValue;
    private IIpsProject ipsProject;
    private ProductCmpt productCmpt;
    private Consumer<List<String>> valueConsumer = new Consumer<List<String>>() {

        @Override
        public void accept(List<String> list) {
            result.addAll(list);
        }
    };
    private final List<String> result = new LinkedList<String>();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Product");
        productCmpt = newProductCmpt(productCmptType, "ProductA");

        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)productCmptType.newAttribute();
        attribute.setName("attribute");
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);
        attrValue = spy(productCmpt.getLatestProductCmptGeneration().getAttributeValue("attribute"));
        attribute.setDatatype(Datatype.MONEY.getQualifiedName());
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
    public void testCreate() {
        assertFalse(DatatypeMismatchEntry.create(attrValue).isPresent());

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "10.0"));
        assertTrue(DatatypeMismatchEntry.create(attrValue).isPresent());
    }

    @Test
    public void testCreateIfValidationFails() throws CoreException {
        doThrow(new CoreException(Status.CANCEL_STATUS)).when(attrValue).validate(ipsProject);

        assertFalse(DatatypeMismatchEntry.create(attrValue).isPresent());
    }

}
