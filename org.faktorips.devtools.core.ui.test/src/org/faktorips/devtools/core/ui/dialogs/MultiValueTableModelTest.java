/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.junit.Before;
import org.junit.Test;

public class MultiValueTableModelTest extends AbstractIpsPluginTest {

    private IAttributeValue attributeValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        IProductCmpt component = newProductCmpt(ipsProject, "qName");
        attributeValue = new AttributeValue(component, "");
        List<ISingleValueHolder> list = new ArrayList<>();
        list.add(new SingleValueHolder(attributeValue, "A"));
        list.add(new SingleValueHolder(attributeValue, "B"));
        list.add(new SingleValueHolder(attributeValue, "C"));

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, list);
        attributeValue.setValueHolder(multiValueHolder);
    }

    @Test
    public void applyValueList() {
        MultiValueTableModel model = new MultiValueTableModel(attributeValue);
        model.addElement();
        assertEquals(4, getAttributeValueList().size());
        assertEquals(4, model.getElements().size());
        model.addElement();
        assertEquals(5, getAttributeValueList().size());
        assertEquals(5, model.getElements().size());

        assertEquals(5, getAttributeValueList().size());
        assertEquals(5, model.getElements().size());
        assertSame(getAttributeValueList().get(4), model.getElements().get(4).getSingleValueHolder());
    }

    protected List<ISingleValueHolder> getAttributeValueList() {
        return ((MultiValueHolder)attributeValue.getValueHolder()).getValue();
    }

    @Test
    public void swapElements() {
        MultiValueTableModel model = new MultiValueTableModel(attributeValue);
        model.swapElements(1, 2);
        assertEquals(3, model.getElements().size());
        assertEquals("C", model.getElements().get(1).getSingleValueHolder().getValue().getContentAsString());
        assertEquals("B", model.getElements().get(2).getSingleValueHolder().getValue().getContentAsString());
        model.swapElements(1, 2);
        assertEquals(3, model.getElements().size());
        assertEquals("B", model.getElements().get(1).getSingleValueHolder().getValue().getContentAsString());
        assertEquals("C", model.getElements().get(2).getSingleValueHolder().getValue().getContentAsString());
    }

    @Test
    public void addElement() {
        MultiValueTableModel model = new MultiValueTableModel(attributeValue);
        model.addElement();
        assertEquals(4, model.getElements().size());
        assertNull(model.getElements().get(3).getSingleValueHolder().getValue().getContentAsString());
        model.addElement();
        assertEquals(5, model.getElements().size());
        assertNull(model.getElements().get(4).getSingleValueHolder().getValue().getContentAsString());
    }

    @Test
    public void removeElement() {
        MultiValueTableModel model = new MultiValueTableModel(attributeValue);
        model.removeElement(1);
        assertEquals(2, model.getElements().size());
        assertEquals("C", model.getElements().get(1).getSingleValueHolder().getValue().getContentAsString());
        model.removeElement(0);
        assertEquals(1, model.getElements().size());
        assertEquals("C", model.getElements().get(0).getSingleValueHolder().getValue().getContentAsString());
    }

    @Test
    public void treatViewItemsWithEqualValueAsNotEqual() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue, "value");
        SingleValueViewItem item1 = new SingleValueViewItem(holder, 0);
        SingleValueViewItem item2 = new SingleValueViewItem(holder, 1);
        assertFalse(item1.equals(item2));
    }

    @Test
    public void treatViewItemsAsNotEqual() {
        SingleValueHolder holder = new SingleValueHolder(attributeValue, "value");
        SingleValueViewItem item1 = new SingleValueViewItem(holder, 0);
        SingleValueViewItem item2 = new SingleValueViewItem(holder, 1);
        List<SingleValueViewItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        assertEquals(0, items.indexOf(item1));
        assertEquals(1, items.indexOf(item2));
    }
}
