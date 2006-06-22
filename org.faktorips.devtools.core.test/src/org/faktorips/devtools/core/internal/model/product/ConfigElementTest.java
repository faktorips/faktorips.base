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

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.EnumValueSet;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ConfigElementTest extends AbstractIpsPluginTest {

	private IPolicyCmptType policyCmptType;
    private IProductCmpt productCmpt;
    private IIpsSrcFile pdSrcFile;
    private IProductCmptGeneration generation;
    private IConfigElement configElement;
    private IIpsProject project;

    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        policyCmptType = (IPolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy");
        productCmpt = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "TestProduct");
        pdSrcFile = productCmpt.getIpsSrcFile();
        productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
        configElement = generation.newConfigElement();
        
        policyCmptType.getIpsSrcFile().save(true, null);
        productCmpt.getIpsSrcFile().save(true, null);
    }
    
    public void testValidate_UnknownAttribute() throws CoreException {
    	configElement.setPcTypeAttribute("a");
    	MessageList ml = configElement.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    	
    	policyCmptType.newAttribute().setName("a");
        policyCmptType.getIpsSrcFile().save(true, null);
    	
    	ml = configElement.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    }
    
    public void testValidate_UnknownDatatypeFormula() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.FORMULA);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("formulaTest");
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("formulaTest");
    	attr.setAttributeType(AttributeType.DERIVED);
    	
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_FORMULA));
    	
    	attr.setDatatype("Decimal");
    	
    	policyCmptType.getIpsSrcFile().save(true, null);
    	
    	ml = ce.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_FORMULA));
    }
    
    public void testValidate_UnknownDatatypeValue() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("valueTest");
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	attr.setAttributeType(AttributeType.CONSTANT);
    	
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE));
    	
    	attr.setDatatype("Decimal");
    	
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE));
    }

    public void testValidate_WrongFormulaDatatype() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.FORMULA);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("formulaTest");
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("formulaTest");
    	attr.setAttributeType(AttributeType.DERIVED);
    	attr.setDatatype("Money");
    	policyCmptType.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_WRONG_FORMULA_DATATYPE));
    	
    	
    	policyCmptType.getIpsSrcFile().save(true, null);
    	
    	ce.setValue("1EUR");
    	ml = ce.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_WRONG_FORMULA_DATATYPE));
    }
    
    public void testValidate_MissingFormula() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.FORMULA);
    	ce.setPcTypeAttribute("formulaTest");
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("formulaTest");
    	attr.setAttributeType(AttributeType.DERIVED);
    	attr.setDatatype("Money");
    	policyCmptType.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_MISSING_FORMULA));
    	
    	
    	policyCmptType.getIpsSrcFile().save(true, null);
    	
    	ce.setValue("1EUR");
    	ml = ce.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_MISSING_FORMULA));
    }

    public void testValidate_ValueNotParsable() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("valueTest");
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("Money");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));
    	
    	attr.setDatatype("Decimal");
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));
    }
    
    public void testValidate_InvalidValueset() throws CoreException {
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("Decimal");
    	attr.setValueSetType(ValueSetType.RANGE);
    	IRangeValueSet valueSet = (IRangeValueSet)attr.getValueSet();
    	valueSet.setLowerBound("a");
    	valueSet.setUpperBound("b");

    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("valueTest");
    	ce.setValueSetCopy(valueSet);
    	

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	
    	// no test for specific message codes because the codes are under controll
    	// of the value set.
    	assertTrue(ml.getNoOfMessages() > 0); 
    	
    	valueSet = (IRangeValueSet)ce.getValueSet();
    	valueSet.setLowerBound("0");
    	valueSet.setUpperBound("100");
    	
    	valueSet = (IRangeValueSet)attr.getValueSet();
    	valueSet.setLowerBound("0");
    	valueSet.setUpperBound("100");
    	
    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);

    	ml = ce.validate();
    	assertEquals(0, ml.getNoOfMessages());
    }
    
    public void testValidate_InvalidDatatype() throws Exception {
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("test");
    	InvalidDatatype datatype = new InvalidDatatype();
    	attr.setDatatype(datatype.getQualifiedName());

		ValueDatatype[] vds = project.getValueDatatypes(false);
		ArrayList vdlist = new ArrayList();
		vdlist.addAll(Arrays.asList(vds));
		vdlist.add(datatype);

        IIpsProjectProperties properties = project.getProperties();
        properties.setPredefinedDatatypesUsed((ValueDatatype[])vdlist.toArray(new ValueDatatype[vdlist.size()]));
        project.setProperties(properties);
		
		InvalidDatatypeHelper idh = new InvalidDatatypeHelper();
    	idh.setDatatype(datatype);
    	((IpsModel)project.getIpsModel()).addDatatypeHelper(idh);

    	IConfigElement ce = generation.newConfigElement();
    	ce.setPcTypeAttribute("test");
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_INVALID_DATATYPE));
    }
    
    public void testValidate_ValueNotInValueset() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("valueTest");
    	ce.setValueSetType(ValueSetType.RANGE);
    	IRangeValueSet valueSet = (IRangeValueSet)ce.getValueSet();
    	valueSet.setLowerBound("10");
    	valueSet.setUpperBound("20");

    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("Decimal");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET)); 
    	
    	ce.setValue("15");

    	ml = ce.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET)); 
    }
    
    public void testValidate_ValueSetNotASubset() throws CoreException {
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	attr.setValueSetType(ValueSetType.RANGE);
    	IRangeValueSet valueSet = (IRangeValueSet)attr.getValueSet();
    	valueSet.setLowerBound("10");
    	valueSet.setUpperBound("15");
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("Decimal");

    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("12");
    	ce.setPcTypeAttribute("valueTest");
    	ce.setValueSetCopy(valueSet);
    	IRangeValueSet valueSet2 = (IRangeValueSet)ce.getValueSet();
    	valueSet2.setUpperBound("20");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	// no test for specific message codes because the codes are under controll
    	// of the value set.
    	assertTrue(ml.getNoOfMessages() > 0); 
    	
    	valueSet.setUpperBound("20");
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate();
    	assertEquals(0, ml.getNoOfMessages());
    }

 
    
    public void testSetValue() {
        configElement.setValue("newValue");
        assertEquals("newValue", configElement.getValue());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        configElement.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, configElement.getId());
        assertEquals(ConfigElementType.PRODUCT_ATTRIBUTE, configElement.getType());
        assertEquals("rate", configElement.getPcTypeAttribute());
        assertEquals("1.5", configElement.getValue());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        IConfigElement cfgElement = generation.newConfigElement();
        cfgElement.setType(ConfigElementType.POLICY_ATTRIBUTE);
        cfgElement.setValue("value");
        cfgElement.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)cfgElement.getValueSet(); 
        valueSet.setLowerBound("22");
        valueSet.setUpperBound("33");
        valueSet.setStep("4");
        Element xmlElement = cfgElement.toXml(getTestDocument());

        IConfigElement newCfgElement = generation.newConfigElement();
        newCfgElement.initFromXml(xmlElement);
        assertEquals("value", newCfgElement.getValue());
        assertEquals("22", ((IRangeValueSet)newCfgElement.getValueSet()).getLowerBound());
        assertEquals("33", ((IRangeValueSet)newCfgElement.getValueSet()).getUpperBound());
        assertEquals("4", ((IRangeValueSet)newCfgElement.getValueSet()).getStep());

        cfgElement.setValueSetType(ValueSetType.ENUM);
        EnumValueSet enumValueSet = (EnumValueSet)cfgElement.getValueSet();
        enumValueSet.addValue("one");
        enumValueSet.addValue("two");
        enumValueSet.addValue("three");
        enumValueSet.addValue("four");

        xmlElement = cfgElement.toXml(getTestDocument());
        assertEquals(4, ((IEnumValueSet)cfgElement.getValueSet()).getValues().length);
        assertEquals("one", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[0]);
        assertEquals("two", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[1]);
        assertEquals("three", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[2]);
        assertEquals("four", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[3]);
        
        cfgElement.setValue(null);
        xmlElement = cfgElement.toXml(getTestDocument());
        newCfgElement.initFromXml(xmlElement);
        
        assertNull(newCfgElement.getValue());
        

    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			configElement.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    private class InvalidDatatype implements ValueDatatype {

		public Datatype getWrapperType() {
			return null;
		}

		public boolean isParsable(String value) {
			return true;
		}

		public Object getValue(String value) {
			return value;
		}

		public String valueToString(Object value) {
			return value.toString();
		}

		public boolean isNull(Object value) {
			return false;
		}

		public String getName() {
			return getQualifiedName();
		}

		public String getQualifiedName() {
			return "InvalidDatatype";
		}

		public boolean isVoid() {
			return false;
		}

		public boolean isPrimitive() {
			return false;
		}

		public boolean isValueDatatype() {
			return true;
		}

		public String getJavaClassName() {
			return null;
		}

		public MessageList validate() throws Exception {
			MessageList ml = new MessageList();
			
			ml.add(new Message("", "", Message.ERROR));
			
			return ml;
		}

		public int compareTo(Object o) {
			return -1;
		}

        /**
         * {@inheritDoc}
         */
        public boolean hasNullObject() {
            return false;
        }
    	
    }
    
    private class InvalidDatatypeHelper extends AbstractDatatypeHelper {

		protected JavaCodeFragment valueOfExpression(String expression) {
			return null;
		}

		public JavaCodeFragment nullExpression() {
			return null;
		}

		public JavaCodeFragment newInstance(String value) {
			return null;
		}
    	
    }
}
