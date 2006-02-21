package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ConfigElementTest extends IpsPluginTest {

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

    public void testValidate_NotAValueDatatype() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("valueTest");
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("TestPolicy");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_NOT_A_VALUEDATATYPE));
    	
    	attr.setDatatype("Decimal");
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate();
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_NOT_A_VALUEDATATYPE));
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
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("valueTest");
    	ce.setValueSet(new Range("a", "b"));
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("Decimal");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	
    	// no test for specific message codes because the codes are under controll
    	// of the value set.
    	assertTrue(ml.getNoOfMessages() > 0); 
    	
    	ce.setValueSet(new Range("0", "100"));
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate();
    	assertEquals(0, ml.getNoOfMessages());
    }
    
    public void testValidate_ValueNotInValueset() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPcTypeAttribute("valueTest");
    	ce.setValueSet(new Range("10", "20"));
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
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
    	ce.setValue("12");
    	ce.setPcTypeAttribute("valueTest");
    	ce.setValueSet(new Range("10", "20"));
    	IAttribute attr = policyCmptType.newAttribute();
    	attr.setName("valueTest");
    	attr.setValueSet(new Range("10", "15"));
    	
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("Decimal");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate();
    	// no test for specific message codes because the codes are under controll
    	// of the value set.
    	assertTrue(ml.getNoOfMessages() > 0); 
    	
    	attr.setValueSet(new Range("10", "20"));
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
        cfgElement.setValue("value");
        cfgElement.setValueSet(new Range("22", "33", "4"));
        Element xmlElement = cfgElement.toXml(getTestDocument());

        IConfigElement newCfgElement = generation.newConfigElement();
        newCfgElement.initFromXml(xmlElement);
        assertEquals("value", newCfgElement.getValue());
        assertEquals("22", ((Range)newCfgElement.getValueSet()).getLowerBound());
        assertEquals("33", ((Range)newCfgElement.getValueSet()).getUpperBound());
        assertEquals("4", ((Range)newCfgElement.getValueSet()).getStep());

        EnumValueSet enumValueSet = new EnumValueSet();
        enumValueSet.addValue("one");
        enumValueSet.addValue("two");
        enumValueSet.addValue("three");
        enumValueSet.addValue("four");

        cfgElement.setValueSet(enumValueSet);
        xmlElement = cfgElement.toXml(getTestDocument());
        assertEquals(4, ((EnumValueSet)cfgElement.getValueSet()).getElements().length);
        assertEquals("one", ((EnumValueSet)cfgElement.getValueSet()).getElements()[0]);
        assertEquals("two", ((EnumValueSet)cfgElement.getValueSet()).getElements()[1]);
        assertEquals("three", ((EnumValueSet)cfgElement.getValueSet()).getElements()[2]);
        assertEquals("four", ((EnumValueSet)cfgElement.getValueSet()).getElements()[3]);

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
}
