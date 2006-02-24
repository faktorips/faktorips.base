package org.faktorips.devtools.core.model;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.util.XmlAbstractTestCase;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 */
public class EnumValueSetTest extends XmlAbstractTestCase {
    
    private DefaultEnumType gender;

    protected void setUp() {
        gender = new DefaultEnumType("Gender", DefaultEnumValue.class);
        new DefaultEnumValue(gender, "male");
        new DefaultEnumValue(gender, "female");
    }

    public void testEnumValueSet() {
        EnumValueSet set = new EnumValueSet(gender);
        String[] elements = set.getValues();
        assertEquals(2, elements.length);
        assertEquals("male", elements[0]);
        assertEquals("female", elements[1]);
    }

    public void testContainsValue() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("10EUR");
        set.addValue("20EUR");
        set.addValue("30EUR");
        assertTrue(set.containsValue("10EUR", Datatype.MONEY));
        assertTrue(set.containsValue("10 EUR", Datatype.MONEY));
        assertFalse(set.containsValue("15 EUR", Datatype.MONEY));
        assertFalse(set.containsValue("abc", Datatype.MONEY));
        
        try {
        	set.containsValue("10EUR", null);
        	fail();
        }
        catch (NullPointerException e) {
			// nothing to do
		}
        
        MessageList list = new MessageList();
        set.containsValue("15 EUR", Datatype.MONEY, list, null, null);
        assertTrue(list.containsErrorMsg());
        
        list.clear();
        set.containsValue("10 EUR", Datatype.MONEY, list, null, null);
        assertFalse(list.containsErrorMsg());
    }
    
    public void testContainsValueSet() {
    	EnumValueSet superset = new EnumValueSet();
    	superset.addValue("1");
    	superset.addValue("2");
    	superset.addValue("3");
    	
    	EnumValueSet subset = new EnumValueSet();
    	assertTrue(superset.containsValueSet(subset, Datatype.INTEGER));
    	
    	subset.addValue("1");
    	assertTrue(superset.containsValueSet(subset, Datatype.INTEGER));

    	subset.addValue("2");
    	subset.addValue("3");
    	assertTrue(superset.containsValueSet(subset, Datatype.INTEGER));

    	MessageList list = new MessageList();
    	superset.containsValueSet(subset, Datatype.INTEGER, list, null, null);
    	assertFalse(list.containsErrorMsg());

    	subset.addValue("4");
    	assertFalse(superset.containsValueSet(subset, Datatype.INTEGER));

    	list.clear();
    	superset.containsValueSet(subset, Datatype.INTEGER, list, null, null);
    	assertTrue(list.containsErrorMsg());
    	
        try {
        	superset.containsValueSet(subset, null, list, null, null);
        	fail();
        }
        catch (NullPointerException e) {
			// nothing to do
		}

    }

    public void testAddValue() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getValues().length);
        assertEquals("one", set.getValue(0));
    }

    public void testRemoveValue() {
        EnumValueSet set = new EnumValueSet();
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getValues().length);
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
        assertEquals(list.getMessage(0).getCode(), EnumValueSet.MSGCODE_DUPLICATE_VALUE);
    }

    public void testCreateFromEnumDatatype() {
    	EnumValueSet set = EnumValueSet.createFromEnumDatatype(new EnumDatatypePaymentMode());
        String[] elements = set.getValues();
        assertEquals(2, elements.length);
        assertEquals("annual", elements[0]);
        assertEquals("monthly", elements[1]);
    }

    
    class EnumDatatypePaymentMode extends AbstractDatatype implements EnumDatatype {

		/**
		 * {@inheritDoc}
		 */
		public String[] getAllValueIds() {
			return new String[]{"annual", "monthly"};
		}

		
		/**
		 * {@inheritDoc}
		 */
		public Datatype getWrapperType() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isParsable(String value) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getValue(String value) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String valueToString(Object value) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isNull(Object value) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getName() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getQualifiedName() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isPrimitive() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isValueDatatype() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getJavaClassName() {
			return null;
		}


		public boolean isSupportingNames() {
			return false;
		}


		public String getValueName(String id) {
			throw new RuntimeException("Not supported");
		}
    	
    }
}
