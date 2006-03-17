/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.EnumValueSet;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 */
public class EnumValueSetTest extends IpsPluginTest {
    
    private DefaultEnumType gender;
    private IConfigElement ce;

    protected void setUp() throws Exception {
    	super.setUp();
        gender = new DefaultEnumType("Gender", DefaultEnumValue.class);
        new DefaultEnumValue(gender, "male");
        new DefaultEnumValue(gender, "female");
        
        DefaultTestContent content = new DefaultTestContent();
        IProductCmptGeneration gen = (IProductCmptGeneration)content.getComfortCollisionCoverageA().getGenerations()[0];
        ce = gen.newConfigElement();
    }

//    public void testEnumValueSet() {
//        IEnumValueSet set = new EnumValueSet(ce, gender);
//        String[] elements = set.getValues();
//        assertEquals(2, elements.length);
//        assertEquals("male", elements[0]);
//        assertEquals("female", elements[1]);
//    }

    public void testContainsValue() {
        EnumValueSet set = new EnumValueSet(ce, 1);
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
    	EnumValueSet superset = new EnumValueSet(ce, 1);
    	superset.addValue("1");
    	superset.addValue("2");
    	superset.addValue("3");
    	
    	EnumValueSet subset = new EnumValueSet(ce, 1);
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
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getValues().length);
        assertEquals("one", set.getValue(0));
    }

    public void testRemoveValue() {
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getValues().length);
        set.removeValue(0);
        assertEquals("two", set.getValue(0));
    }

    public void testGetValue() {
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("two", set.getValue(2));

    }

    public void testSetValue() {
        IEnumValueSet set = new EnumValueSet(ce, 1);
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
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.initFromXml(element);
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("three", set.getValue(2));

    }

    public void testToXml() {
        EnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        Element element = set.toXml(this.newDocument());
        IEnumValueSet set2 = new EnumValueSet(ce, 1);
        set2.initFromXml(element);
        assertEquals("one", set2.getValue(0));
        assertEquals("two", set2.getValue(1));
        assertEquals("two", set2.getValue(2));

    }

    public void testValidate() {
        EnumValueSet set = new EnumValueSet(ce, 1);
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
        assertEquals(list.getMessage(0).getCode(), IEnumValueSet.MSGCODE_DUPLICATE_VALUE);
    }

//    public void testCreateFromEnumDatatype() {
//    	IEnumValueSet set = EnumValueSet.createFromEnumDatatype(ce, new EnumDatatypePaymentMode());
//        String[] elements = set.getValues();
//        assertEquals(2, elements.length);
//        assertEquals("annual", elements[0]);
//        assertEquals("monthly", elements[1]);
//    }

    
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
