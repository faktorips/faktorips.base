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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.TestEnumType;
import org.faktorips.devtools.core.internal.model.EnumValueSet;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.IpsProject;
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
import org.faktorips.devtools.core.model.pctype.ITableStructureUsage;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
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

    private IPolicyCmptType pcTypeInput;
    
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
        
        pcTypeInput = newPolicyCmptType(project, "policyCmptTypeInput");
        
        newDefinedEnumDatatype((IpsProject)project, new Class[]{TestEnumType.class});
    }
    
    public void testGetEnumDatatypesAllowedInFormula() throws CoreException {
        EnumDatatype testType = project.findEnumDatatype("TestEnumType");
        assertNotNull(testType);
        
        // config element is not a formula
        configElement.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
        EnumDatatype[] enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);

        // missing policy component type attribute
        configElement.setType(ConfigElementType.FORMULA);
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);
        
        // enum type is the return value of the formula 
        IAttribute attr = policyCmptType.newAttribute();
        attr.setName("attr");
        attr.setDatatype(testType.getQualifiedName());
        attr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        configElement.setPcTypeAttribute("attr");
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);
        
        // enum type defined as "direct" parameter 
        attr.setDatatype("Integer");
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);
        
        Parameter[] params = new Parameter[1];
        params[0] = new Parameter(0, "param1", testType.getQualifiedName());
        attr.setFormulaParameters(params);
        
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);

        // enum type used as datatype of policy component type attribute
        IPolicyCmptType policyType = newPolicyCmptType(project, "Coverage");
        params[0] = new Parameter(0, "coverage", policyType.getQualifiedName());
        attr.setFormulaParameters(params);
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length); // 0 because policeType hasn't got an attribute so far

        IAttribute policyAttr = policyType.newAttribute();
        policyAttr.setDatatype("Integer");
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length); // 0 because policeType hasn't got an attribute with an enum type so far
        
        IAttribute policyAttr2 = policyType.newAttribute();
        policyAttr2.setDatatype(testType.getQualifiedName());

        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);
        
        // enums accesible via tables (because they are used as column datatypes)
        policyAttr2.setDatatype("String");
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);
        
        ITableStructure tableStructure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Table");
        IColumn col0 = tableStructure.newColumn();
        col0.setDatatype("Integer");
        
        ITableStructureUsage structureUsage = policyCmptType.newTableStructureUsage();
        structureUsage.addTableStructure(tableStructure.getQualifiedName());
        productCmpt.fixAllDifferencesToModel();
        
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length); // 0 because table structure hasn't got a column with an enum type so far
        
        IColumn col1 = tableStructure.newColumn();
        col1.setDatatype(testType.getQualifiedName());
        enumtypes = configElement.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);
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
    	attr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
    	
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
    	attr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
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
    	attr.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
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
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Decimal");
        IRangeValueSet valueSetAttr = (IRangeValueSet)attr.getValueSet();
        valueSetAttr.setLowerBound(null);
        valueSetAttr.setUpperBound(null);
        
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
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
    	
    	valueSet.setUpperBound("20");
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate();
    	assertEquals(0, ml.getNoOfMessages());
        
        // check lower unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate();
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
        
        valueSet.setLowerBound("10");
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate();
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound("10");
        valueSet2.setUpperBound(null);
        ml = ce.validate();
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
        
        // check upper unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound("10");
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate();
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound("10");
        ml = ce.validate();
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
    }

    public void testValidate_WrongTypeForFormulaTests() throws Exception{
        configElement.setType(ConfigElementType.FORMULA);
        configElement.newFormulaTestCase().setName("formulaTest1");
        MessageList ml = configElement.validate();
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_WRONG_TYPE_FOR_FORMULA_TESTS)); 
        
        configElement.setType(ConfigElementType.POLICY_ATTRIBUTE);
        ml = configElement.validate();
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_WRONG_TYPE_FOR_FORMULA_TESTS)); 
        
        configElement.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
        ml = configElement.validate();
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_WRONG_TYPE_FOR_FORMULA_TESTS));         
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
        
        // check formula test
        assertEquals(1, configElement.getFormulaTestCases().length);
        assertNotNull(configElement.getFormulaTestCase("formulaTest1"));
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
        
        // formula test
        cfgElement = generation.newConfigElement();
        cfgElement.setType(ConfigElementType.FORMULA);
        cfgElement.newFormulaTestCase().setName("formulaTest1");
        xmlElement = cfgElement.toXml(getTestDocument());

        newCfgElement = generation.newConfigElement();
        newCfgElement.initFromXml(xmlElement);
        assertEquals("formulaTest1", newCfgElement.getFormulaTestCases()[0].getName());
    }

    /**
     * Tests for the correct type of exception to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			configElement.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testFormulaNewDeleteTest() throws Exception {
        // tests for creating and deleting of the formula test case
        assertEquals(0, configElement.getFormulaTestCases().length);
        
        configElement.setType(ConfigElementType.FORMULA);
        IFormulaTestCase formulaTest1 = configElement.newFormulaTestCase();
        IFormulaTestCase formulaTest2 = configElement.newFormulaTestCase();
        formulaTest1.setName("formulaTest1");
        formulaTest2.setName("formulaTest2");
        
        assertEquals(formulaTest1, configElement.getFormulaTestCase(formulaTest1.getName()));
        assertEquals(formulaTest2, configElement.getFormulaTestCase(formulaTest2.getName()));
        assertEquals(2, configElement.getFormulaTestCases().length);
        
        formulaTest1.delete();
        formulaTest2.delete();
        assertEquals(0, configElement.getFormulaTestCases().length);
        assertNull(configElement.getFormulaTestCase(formulaTest1.getName()));
    }
    
    public void testContainsFormulaTest() throws Exception {
        configElement.setType(ConfigElementType.FORMULA);
        assertFalse(configElement.getProductCmpt().containsFormulaTest());
        
        IFormulaTestCase formulaTest1 = configElement.newFormulaTestCase();
        formulaTest1.setName("formulaTest1");
        assertTrue(configElement.getProductCmpt().containsFormulaTest());
        
        formulaTest1.delete();
        assertFalse(configElement.getProductCmpt().containsFormulaTest());
    }
    
    public void testGetParameterIdentifiersUsedInFormula() throws CoreException {
        IAttribute attributeInput = pcTypeInput.newAttribute();
        attributeInput.setName("attributeInput1");
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());
        attributeInput = pcTypeInput.newAttribute();
        attributeInput.setName("attributeInput2");
        attributeInput.setAttributeType(AttributeType.CHANGEABLE);
        attributeInput.setDatatype(Datatype.STRING.getQualifiedName());
        IPolicyCmptType pcType = newPolicyCmptType(project, "policyCmptType1");
        IAttribute attribute = pcType.newAttribute();
        attribute.setName("attribute1");
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(0, "param1", Datatype.INTEGER.getQualifiedName());
        params[1] = new Parameter(1, "param2", Datatype.INTEGER.getQualifiedName());
        params[2] = new Parameter(2, "policyInputX", pcTypeInput.getQualifiedName());

        attribute.setFormulaParameters(params);
        
        ((IProductCmptGeneration)configElement.getParent()).getProductCmpt().setPolicyCmptType(pcType.getQualifiedName());
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attribute.getName());
        configElement.setValue("param1 + param2 + policyInputX.attributeInput1");
        
        List identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(3, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("param1"));
        assertTrue(identifierInFormula.contains("param2"));
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput1"));
        
        configElement.setValue("param1+param2*2+policyInputX.attributeInput1");
        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(3, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("param1"));
        assertTrue(identifierInFormula.contains("param2"));
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput1"));        
        
        configElement.setValue("param1x+Xparam2*2+policyInputX.attributeInput1");
        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        // check wrong number of identifiers
        assertFalse(identifierInFormula.size()==3);
        
        // check with empty formula
        configElement.setValue(null);
        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(0, identifierInFormula.size());
        
        // check with WENN formula (implicit cast e.g. Integer)
        configElement.setValue("WENN(policyInputX.attributeInput2 = \"1\"; 1; 10)");
        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(1, identifierInFormula.size());   
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput2"));
        
        // check with WENN formula (binary operation exact match?)
        configElement.setValue("WENN(policyInputX.attributeInput1 = 1;1;10)");
        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(1, identifierInFormula.size());   
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput1")); 
        
        params = new Parameter[1];
        params[0] = new Parameter(0, "testParam", "TestEnumType");
        attribute.setFormulaParameters(params);
        
        // check with WENN formula and operation with implicit casting 
        // (e.g. the first argument of a formula is an enum type)
        configElement.setValue("WENN(testParam = TestEnumType.1;1;10)");
        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(1, identifierInFormula.size());   
        assertTrue(identifierInFormula.contains("testParam"));
        
        // check table access formula that matches implicit conversions 
        // e.g. TableUsage.value1(key1, 1), key1 is Integer
        
        ITableStructure tableStructure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Table");
        IColumn col = tableStructure.newColumn();
        col.setDatatype("Integer");
        col.setName("column1");
        col = tableStructure.newColumn();
        col.setDatatype("Integer");
        col.setName("column2");
        col = tableStructure.newColumn();
        col.setDatatype("Integer");
        col.setName("column3");        
        IUniqueKey key = tableStructure.newUniqueKey();
        key.addKeyItem("column1");
        key.addKeyItem("column2");
        
        ITableContents tableContents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TableContents");
        tableContents.setTableStructure(tableStructure.getQualifiedName());
        
        ITableStructureUsage structureUsage = policyCmptType.newTableStructureUsage();
        structureUsage.setRoleName("Table");
        structureUsage.addTableStructure(tableStructure.getQualifiedName());
        productCmpt.fixAllDifferencesToModel();
        
        ITableContentUsage tableContentUsage = ((IProductCmptGeneration)configElement.getParent()).newTableContentUsage();
        tableContentUsage.setStructureUsage("Table");
        tableContentUsage.setTableContentName("TableContents");
        
        params = new Parameter[1];
        params[0] = new Parameter(0, "testParam", Datatype.INTEGER.getQualifiedName());
        attribute.setFormulaParameters(params);

        configElement.setValue("Table.column3(testParam;1)");

        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(1, identifierInFormula.size());   
        assertTrue(identifierInFormula.contains("testParam"));
    }
    
    public void testGetParameterIdentifiersUsedInFormulaWithEnum() throws CoreException{
        IAttribute attributeInput = pcTypeInput.newAttribute();
        attributeInput.setName("attributeInput1");
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());
        attributeInput = pcTypeInput.newAttribute();
        attributeInput.setName("attributeInput2");
        attributeInput.setAttributeType(AttributeType.CHANGEABLE);
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());
        IPolicyCmptType pcType = newPolicyCmptType(project, "policyCmptType1");
        IAttribute attribute = pcType.newAttribute();
        attribute.setName("attribute1");
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(0, "param1", Datatype.INTEGER.getQualifiedName());
        params[1] = new Parameter(1, "param2", Datatype.INTEGER.getQualifiedName());
        params[2] = new Parameter(2, "policyInputX", pcTypeInput.getQualifiedName());
        
        attribute.setFormulaParameters(params);
        
        ((IProductCmptGeneration)configElement.getParent()).getProductCmpt().setPolicyCmptType(pcType.getQualifiedName());
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attribute.getName());
        
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        newDefinedEnumDatatype((IpsProject)project, new Class[]{TestEnumType.class});

        // check with non enum datatype (unknow identifier)
        configElement.setValue("TestEnumType.test");
        List identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(0, identifierInFormula.size());
        
        // check with enum datatype
        configElement.setValue("TestEnumType.1");
        identifierInFormula = Arrays.asList(configElement.getParameterIdentifiersUsedInFormula());
        assertEquals(0, identifierInFormula.size());
    }
    
    public void testMoveFormulaTestCases(){
        configElement.setType(ConfigElementType.FORMULA);
        IFormulaTestCase ftc0 = configElement.newFormulaTestCase();
        IFormulaTestCase ftc1 = configElement.newFormulaTestCase();
        IFormulaTestCase ftc2 = configElement.newFormulaTestCase();
        IFormulaTestCase ftc3 = configElement.newFormulaTestCase();
        configElement.moveFormulaTestCases(new int[]{1, 3}, true);
        IFormulaTestCase[] ftcs = configElement.getFormulaTestCases();
        assertEquals(ftc1, ftcs[0]);
        assertEquals(ftc0, ftcs[1]);
        assertEquals(ftc3, ftcs[2]);
        assertEquals(ftc2, ftcs[3]);
        
        configElement.moveFormulaTestCases(new int[]{0, 2}, false);
        ftcs = configElement.getFormulaTestCases();
        assertEquals(ftc0, ftcs[0]);
        assertEquals(ftc1, ftcs[1]);
        assertEquals(ftc2, ftcs[2]);
        assertEquals(ftc3, ftcs[3]);        
    }
    
    private class InvalidDatatype implements ValueDatatype {

		public ValueDatatype getWrapperType() {
			return null;
		}

		public boolean isParsable(String value) {
			return true;
		}

		public String getName() {
			return getQualifiedName();
		}

		public String getQualifiedName() {
			return "InvalidDatatype";
		}

		public String getDefaultValue() {
            return null;
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

        public boolean isNull(String value) {
            return false;
        }

        public boolean supportsCompare() {
            return false;
        }

        public int compare(String valueA, String valueB) throws UnsupportedOperationException {
            return 0;
        }

        public boolean areValuesEqual(String valueA, String valueB) {
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
