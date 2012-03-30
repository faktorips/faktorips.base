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

package org.faktorips.devtools.core.ui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.dialogs.MultiValueLableModel;
import org.junit.Before;
import org.junit.Test;

public class MultiValueLableModelTest extends AbstractIpsPluginTest {

    private IAttributeValue attributeValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        IProductCmpt component = newProductCmpt(ipsProject, "qName");
        attributeValue = new AttributeValue(component, "");
        List<SingleValueHolder> list = new ArrayList<SingleValueHolder>();
        list.add(new SingleValueHolder(attributeValue, "A"));
        list.add(new SingleValueHolder(attributeValue, "B"));
        list.add(new SingleValueHolder(attributeValue, "C"));

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, list);
        attributeValue.setValueHolder(multiValueHolder);
    }

    @Test
    public void applyValueList() {
        MultiValueLableModel model = new MultiValueLableModel(attributeValue);
        model.addElement();
        assertEquals(3, getAttributeValueList().size());
        assertEquals(4, model.getList().size());
        model.addElement();
        assertEquals(3, getAttributeValueList().size());
        assertEquals(5, model.getList().size());

        model.applyValueList();
        assertEquals(5, getAttributeValueList().size());
        assertEquals(5, model.getList().size());
        assertSame(getAttributeValueList().get(4), model.getList().get(4));
    }

    protected List<SingleValueHolder> getAttributeValueList() {
        return ((MultiValueHolder)attributeValue.getValueHolder()).getValue();
    }

    @Test
    public void swapElements() {
        MultiValueLableModel model = new MultiValueLableModel(attributeValue);
        model.swapElements(1, 2);
        assertEquals(3, model.getList().size());
        assertEquals("C", model.getList().get(1).getValue());
        assertEquals("B", model.getList().get(2).getValue());
        model.swapElements(1, 2);
        assertEquals(3, model.getList().size());
        assertEquals("B", model.getList().get(1).getValue());
        assertEquals("C", model.getList().get(2).getValue());
    }

    @Test
    public void addElement() {
        MultiValueLableModel model = new MultiValueLableModel(attributeValue);
        model.addElement();
        assertEquals(4, model.getList().size());
        assertEquals(null, model.getList().get(3).getValue());
        model.addElement();
        assertEquals(5, model.getList().size());
        assertEquals(null, model.getList().get(4).getValue());
    }

    @Test
    public void removeElement() {
        MultiValueLableModel model = new MultiValueLableModel(attributeValue);
        model.removeElement(1);
        assertEquals(2, model.getList().size());
        assertEquals("C", model.getList().get(1).getValue());
        model.removeElement(0);
        assertEquals(1, model.getList().size());
        assertEquals("C", model.getList().get(0).getValue());
    }
}
