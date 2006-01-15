package org.faktorips.devtools.core.model;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.XmlTestCase;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Andy Rösch
 */
public class EnumValueSetTest extends XmlTestCase {
    
    private DefaultEnumType gender;
    private DefaultEnumValue male;
    private DefaultEnumValue female;

    protected void setUp() {
        gender = new DefaultEnumType("Gender", DefaultEnumValue.class);
        male = new DefaultEnumValue(gender, "male");
        female = new DefaultEnumValue(gender, "female");
    }

    public void testEnumValueSet() {
        EnumValueSet set = new EnumValueSet(gender);
        String[] elements = set.getElements();
        assertEquals(2, elements.length);
        assertEquals("male", elements[0]);
        assertEquals("female", elements[1]);
    }

    public void testContains() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("10EUR");
        set.addValue("20EUR");
        set.addValue("30EUR");
        assertTrue(set.contains("10EUR", Datatype.MONEY));
        assertTrue(set.contains("10 EUR", Datatype.MONEY));
        assertFalse(set.contains("15 EUR", Datatype.MONEY));
        assertFalse(set.contains("abc", Datatype.MONEY));
    }

    public void testAddValue() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getElements().length);
        assertEquals("one", set.getValue(0));
    }

    public void testRemoveValue() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getElements().length);
        set.removeValue(0);
        assertEquals("two", set.getValue(0));
    }

    public void testGetValue() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("two", set.getValue(2));

    }

    public void testSetValue() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.setValue(1, "anderstwo");
        assertEquals("anderstwo", set.getValue(1));
    }

    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        Element element = XmlUtil.getFirstElement(root);
        EnumValueSet set = (EnumValueSet)EnumValueSet.createFromXml(element);
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("three", set.getValue(2));

    }

    public void testToXml() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        Element element = set.toXml(this.newDocument());
        EnumValueSet set2 = (EnumValueSet)EnumValueSet.createFromXml(element);
        assertEquals("one", set2.getValue(0));
        assertEquals("two", set2.getValue(1));
        assertEquals("two", set2.getValue(2));

    }

    public void testValidate() {
        EnumValueSet set = new EnumValueSet();
        MessageList list = new MessageList();
        set.validate(Datatype.DECIMAL, list);
        assertEquals(0, list.getNoOfMessages());

        set.addValue("2");
        set.validate(Datatype.DECIMAL, list);
        assertEquals(0, list.getNoOfMessages());

        set.addValue("2w");
        set.validate(Datatype.DECIMAL, list);
        assertEquals(1, list.getNoOfMessages());

        assertFalse(list.getMessagesFor("2w").isEmpty());

        set.addValue("2w");
        list.clear();
        set.validate(Datatype.STRING, list);
        assertEquals(2, list.getNoOfMessages());
        String msg = list.getMessage(0).getText();
        assertEquals("The value 2w is more than once in the value set!", msg);
    }

    public void testSetElements() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        String[] newValues = { "1", "2" };
        set.setElements(newValues);
        assertEquals("1", set.getValue(0));
        assertEquals("2", set.getValue(1));
        assertEquals(2, set.getElements().length);
    }

}
