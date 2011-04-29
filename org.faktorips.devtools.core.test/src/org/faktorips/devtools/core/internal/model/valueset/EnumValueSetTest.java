/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EnumValueSetTest extends AbstractIpsPluginTest {

    private DefaultEnumType gender;

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;

    private IPolicyCmptTypeAttribute attr;
    private IConfigElement ce;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        gender = new DefaultEnumType("Gender", DefaultEnumValue.class);
        new DefaultEnumValue(gender, "male");
        new DefaultEnumValue(gender, "female");

        ipsProject = super.newIpsProject("TestProject");
        policyCmptType = newPolicyCmptType(ipsProject, "test.Base");
        attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        policyCmptType.getIpsSrcFile().save(true, null);

        productCmptType = newProductCmptType(ipsProject, "test.Product");
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());

        IProductCmpt cmpt = newProductCmpt(ipsProject, "test.Product");
        cmpt.setProductCmptType(productCmptType.getQualifiedName());
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));

        ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("attr");
    }

    @Test
    public void testCopy() {
        EnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("10");
        set.addValue("5");
        set.addValue("1");

        EnumValueSet copy = (EnumValueSet)set.copy(generation.newConfigElement(), "1");
        assertEquals(3, copy.size());
        assertEquals(0, copy.getPositions("10").get(0).intValue());
        assertEquals(1, copy.getPositions("5").get(0).intValue());
        assertEquals(2, copy.getPositions("1").get(0).intValue());
    }

    @Test
    public void testCopyPropertiesFrom() {
        EnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("10");
        set.addValue("5");
        set.addValue("1");

        EnumValueSet set2 = new EnumValueSet(generation.newConfigElement(), "1");
        set2.copyPropertiesFrom(set);

        assertEquals(3, set2.size());
        assertEquals(0, set2.getPositions("10").get(0).intValue());
        assertEquals(1, set2.getPositions("5").get(0).intValue());
        assertEquals(2, set2.getPositions("1").get(0).intValue());
    }

    @Test
    public void testGetPositions() {
        EnumValueSet set = new EnumValueSet(ce, "1");
        List<Integer> positions = set.getPositions("1");
        assertEquals(0, positions.size());

        set.addValue("1");
        positions = set.getPositions("1");
        assertEquals(1, positions.size());
        assertEquals(0, positions.get(0).intValue());

        set.addValue("2");
        set.addValue("3");
        set.addValue("1");
        positions = set.getPositions("1");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());

        set.addValue("1");
        positions = set.getPositions("1");
        assertEquals(3, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        assertEquals(4, positions.get(2).intValue());

        set.removeValue(0);
        positions = set.getPositions("1");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
    }

    @Test
    public void testContainsValue() {
        EnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("10EUR");
        set.addValue("20EUR");
        set.addValue("30EUR");
        assertTrue(set.containsValue("10EUR"));
        assertTrue(set.containsValue("10 EUR"));
        assertFalse(set.containsValue("15 EUR"));
        assertFalse(set.containsValue("abc"));
        assertFalse(set.containsValue(null));

        set.addValue(null);
        assertTrue(set.containsValue(null));

        MessageList list = new MessageList();
        set.containsValue("15 EUR", list, null, null);
        assertTrue(list.containsErrorMsg());

        list.clear();
        set.containsValue("10 EUR", list, null, null);
        assertFalse(list.containsErrorMsg());
    }

    @Test
    public void testContainsValueInvalidSet() {
        EnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("1");
        MessageList list = new MessageList();
        set.containsValue("10 EUR", list, null, null);
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_VALUE_NOT_CONTAINED));

        list.clear();
        set.addValue("10EUR");
        set.containsValue("10 EUR", list, null, null);
        assertNull(list.getMessageByCode(IValueSet.MSGCODE_VALUE_NOT_CONTAINED));
    }

    @Test
    public void testContainsValueSet() throws Exception {
        EnumValueSet superset = new EnumValueSet(ce, "50");
        superset.addValue("1EUR");
        superset.addValue("2EUR");
        superset.addValue("3EUR");

        EnumValueSet subset = new EnumValueSet(ce, "100");
        assertTrue(superset.containsValueSet(subset));

        subset.addValue("1EUR");
        assertTrue(superset.containsValueSet(subset));

        subset.addValue("2EUR");
        subset.addValue("3EUR");
        assertTrue(superset.containsValueSet(subset));

        MessageList list = new MessageList();
        superset.containsValueSet(subset, list, null, null);
        assertFalse(list.containsErrorMsg());

        subset.addValue("4EUR");
        assertFalse(superset.containsValueSet(subset));

        EnumValueSet abstractSet = new EnumValueSet(ce, "60");
        abstractSet.setAbstract(true);
        assertTrue(abstractSet.containsValueSet(subset));
        assertFalse(subset.containsValueSet(abstractSet));

        list.clear();
        superset.containsValueSet(subset, list, null, null);
        assertTrue(list.containsErrorMsg());

        subset.removeValue("4EUR");
        subset.addValue(null);
        assertFalse(superset.containsValueSet(subset));

        superset.addValue(null);
        assertTrue(superset.containsValueSet(subset));

        IPolicyCmptTypeAttribute attr2 = policyCmptType.newPolicyCmptTypeAttribute();
        attr2.setName("attr2");
        attr2.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptType.getIpsSrcFile().save(true, null);

        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPolicyCmptTypeAttribute("attr2");

        subset = new EnumValueSet(ce2, "50");
        subset.addValue("2EUR");

        list.clear();
        assertFalse(superset.containsValueSet(subset, list, null, null));
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_DATATYPES_NOT_MATCHING));
    }

    @Test
    public void testAddValue() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("one");
        assertEquals(3, set.getValues().length);
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("one", set.getValue(2));
    }

    @Test
    public void testRemoveValue_byValue() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");
        set.addValue("one");
        set.addValue("four");
        set.addValue("one");
        set.addValue("three");
        set.addValue("five");

        // remove something that is not part of the set
        set.removeValue("unknown");
        String[] values = set.getValues();
        assertEquals(8, values.length);

        // "four" has one occurance
        set.removeValue("four");
        values = set.getValues();
        assertEquals(7, values.length);
        assertEquals("one", values[0]);
        assertEquals("two", values[1]);
        assertEquals("three", values[2]);
        assertEquals("one", values[3]);
        assertEquals("three", values[5]);
        assertEquals("one", values[4]);
        assertEquals("five", values[6]);

        // "one" has multiple occurances and is first entry
        set.removeValue("one");
        values = set.getValues();
        assertEquals(4, values.length);
        assertEquals("two", values[0]);
        assertEquals("three", values[1]);
        assertEquals("three", values[2]);
        assertEquals("five", values[3]);

        // "five" has one occurance and is last index
        set.removeValue("five");
        values = set.getValues();
        assertEquals(3, values.length);
        assertEquals("two", values[0]);
        assertEquals("three", values[1]);
        assertEquals("three", values[2]);

        // "three" has multiple occurances and is last entry
        set.removeValue("three");
        values = set.getValues();
        assertEquals(1, values.length);
        assertEquals("two", values[0]);

        // "three" has multiple occurances and is last entry
        set.removeValue("two");
        values = set.getValues();
        assertEquals(0, values.length);

    }

    @Test
    public void testRemoveValue_byIndex() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("one");
        assertEquals(3, set.getValues().length);
        List<Integer> positions = set.getPositions("one");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(2, positions.get(1).intValue());
        positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());

        set.removeValue(0);
        // set is now: two, one
        assertEquals(2, set.getValues().length);
        assertEquals("two", set.getValue(0));
        positions = set.getPositions("one");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
        positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(0, positions.get(0).intValue());

        set.removeValue(0);
        // set is now: one
        assertEquals(1, set.getValues().length);
        assertEquals("one", set.getValue(0));
        positions = set.getPositions("one");
        assertEquals(1, positions.size());
        assertEquals(0, positions.get(0).intValue());
        positions = set.getPositions("two");
        assertEquals(0, positions.size());

        set.removeValue(0);
        // set is now empty
        assertEquals(0, set.getValues().length);
        positions = set.getPositions("one");
        assertEquals(0, positions.size());
        positions = set.getPositions("two");
        assertEquals(0, positions.size());
    }

    @Test
    public void testRemoveValue_byIndex_LastEntry() {
    }

    @Test
    public void testGetValue() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");

        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("two", set.getValue(2));

    }

    @Test
    public void testSetValue_oldValueHasOneOccurenceBeforeTheOperation_NewValueHasNoOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");

        set.setValue(1, "four");
        assertEquals("four", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(0, positions.size());
        positions = set.getPositions("four");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
    }

    @Test
    public void testSetValue_oldValueHasOneOccurenceBeforeTheOperation_NewValueHasOneOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(0, positions.size());
        positions = set.getPositions("three");
        assertEquals(2, positions.size());
        assertEquals(1, positions.get(0).intValue());
        assertEquals(2, positions.get(1).intValue());
    }

    @Test
    public void testSetValue_oldValueHasOneOccurenceBeforeTheOperation_NewValueHasMultipleOccurencesBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("three");
        set.addValue("three");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(0, positions.size());
        positions = set.getPositions("three");
        assertEquals(3, positions.size());
        assertEquals(1, positions.get(0).intValue());
        assertEquals(2, positions.get(1).intValue());
        assertEquals(3, positions.get(2).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndOneAfterTheOperation_NewValueHasNoOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(2, positions.get(0).intValue());
        positions = set.getPositions("three");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndOneAfterTheOperation_NewValueHasOneOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(2, positions.get(0).intValue());
        positions = set.getPositions("one");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndOneAfterTheOperation_NewValueHasMultipleOccurencesBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("one");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(1, positions.size());
        assertEquals(2, positions.get(0).intValue());
        positions = set.getPositions("one");
        assertEquals(3, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
        assertEquals(3, positions.get(2).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndAfterTheOperation_NewValueHasNoOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "three");
        assertEquals("three", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        positions = set.getPositions("three");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(0).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndAfterTheOperation_NewValueHasOneOccurenceBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("two");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        positions = set.getPositions("one");
        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
    }

    @Test
    public void testSetValue_oldValueHasMultipleOccurencesBeforeAndAfterTheOperation_NewValueHasMultipleOccurencesBeforeTheOperation() {
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.addValue("two");
        set.addValue("one");

        set.setValue(1, "one");
        assertEquals("one", set.getValue(1));
        List<Integer> positions = set.getPositions("two");
        assertEquals(2, positions.size());
        assertEquals(2, positions.get(0).intValue());
        assertEquals(3, positions.get(1).intValue());
        positions = set.getPositions("one");
        assertEquals(3, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(1, positions.get(1).intValue());
        assertEquals(4, positions.get(2).intValue());
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        Element element = XmlUtil.getFirstElement(root);
        IEnumValueSet set = new EnumValueSet(ce, "1");
        set.initFromXml(element);
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("three", set.getValue(2));

    }

    @Test
    public void testToXml() {
        EnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        Element element = set.toXml(newDocument());
        IEnumValueSet set2 = new EnumValueSet(ce, "1");
        set2.initFromXml(element);
        assertEquals("one", set2.getValue(0));
        assertEquals("two", set2.getValue(1));
        assertEquals("two", set2.getValue(2));

    }

    @Test
    public void testValidate() throws Exception {
        EnumValueSet set = new EnumValueSet(ce, "1");
        MessageList list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2w");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());

        Datatype[] vds = ipsProject.findDatatypes(true, false);
        ArrayList<Datatype> vdlist = new ArrayList<Datatype>();
        vdlist.addAll(Arrays.asList(vds));
        vdlist.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed(vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);

        IPolicyCmptTypeAttribute attr = ce.findPcTypeAttribute(ipsProject);
        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        attr.getIpsObject().getIpsSrcFile().save(true, null);

        list.clear();
        list = set.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_VALUE_NOT_PARSABLE));

        set.removeValue(0);
        set.removeValue(0);
        set.addValue("1");
        set.addValue(null);
        list.clear();
        list = set.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_NULL_NOT_SUPPORTED));

        // test with unkonwn datatype
        EnumValueSet set2 = new EnumValueSet(ce, "2");
        set2.addValue("1");
        set2.addValue("2");
        list = set2.validate(ipsProject);
        assertEquals(0, list.size());

        ce.getProductCmpt().setProductCmptType("unkown");
        list = set2.validate(ipsProject);
        assertEquals(2, list.size());
        MessageList messages = list.getMessagesFor(set2, IEnumValueSet.PROPERTY_VALUES);
        for (int i = 0; i < messages.size(); i++) {
            assertEquals(Message.WARNING, messages.getMessage(i).getSeverity());
            assertEquals(IValueSet.MSGCODE_UNKNOWN_DATATYPE, messages.getMessage(i).getCode());
        }
    }

    @Test
    public void testValidate_CheckDuplicates() throws CoreException {
        EnumValueSet set = new EnumValueSet(ce, "1");
        MessageList list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("3EUR");
        list = set.validate(ipsProject);
        assertEquals(0, list.size());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());
        Message msg0 = list.getMessage(0);
        assertEquals(IEnumValueSet.MSGCODE_DUPLICATE_VALUE, msg0.getCode());
        ObjectProperty[] ops = msg0.getInvalidObjectProperties();
        assertEquals(2, ops.length);
        assertEquals(0, ops[0].getIndex());
        assertEquals(2, ops[1].getIndex());

        set.addValue("2EUR");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());
        msg0 = list.getMessage(0);
        ops = msg0.getInvalidObjectProperties();
        assertEquals(3, ops.length);

        set.setValue(3, "4EUR");
        list = set.validate(ipsProject);
        assertEquals(1, list.size());
        msg0 = list.getMessage(0);
        ops = msg0.getInvalidObjectProperties();
        assertEquals(2, ops.length);
    }

    @Test
    public void testValidateValue() throws CoreException {
        EnumValueSet set = new EnumValueSet(ce, "1");
        set.addValue("2EUR");
        set.addValue("3EUR");
        set.addValue("2EUR");

        MessageList list = set.validateValue(0, ipsProject);
        assertEquals(1, list.size());
        Message msg0 = list.getMessage(0);
        assertEquals(IEnumValueSet.MSGCODE_DUPLICATE_VALUE, msg0.getCode());
        ObjectProperty[] ops = msg0.getInvalidObjectProperties();
        assertEquals(1, ops.length);
        assertEquals(0, ops[0].getIndex());

        list = set.validateValue(1, ipsProject);
        assertTrue(list.isEmpty());

        list = set.validateValue(2, ipsProject);
        assertEquals(1, list.size());
        msg0 = list.getMessage(0);
        assertEquals(IEnumValueSet.MSGCODE_DUPLICATE_VALUE, msg0.getCode());
        ops = msg0.getInvalidObjectProperties();
        assertEquals(1, ops.length);
        assertEquals(2, ops[0].getIndex());
    }

    @Test
    public void testGetValues() {
        EnumValueSet set = new EnumValueSet(ce, "50");
        String[] values = set.getValues();

        assertEquals(0, values.length);

        set.addValue("1");
        values = set.getValues();
        assertEquals(1, values.length);

        set.addValue(null);
        values = set.getValues();
        assertEquals(2, values.length);
    }

    @Test
    public void testGetContainsNull() {
        EnumValueSet set = new EnumValueSet(ce, "50");

        assertFalse(set.getContainsNull());

        set.setContainsNull(true);
        assertTrue(set.getContainsNull());

        set.setContainsNull(false);
        assertFalse(set.getContainsNull());

        set.addValue(null);
        assertTrue(set.getContainsNull());
    }

    @Test
    public void testSetContainsNull() {
        EnumValueSet set = new EnumValueSet(ce, "50");

        assertFalse(set.getContainsNull());

        set.setContainsNull(true);

        assertTrue(set.getContainsNull());
        assertNull(set.getValue(0));
        assertEquals(1, set.size());

        set.setContainsNull(false);
        assertFalse(set.getContainsNull());
        assertEquals(0, set.size());

    }
}
