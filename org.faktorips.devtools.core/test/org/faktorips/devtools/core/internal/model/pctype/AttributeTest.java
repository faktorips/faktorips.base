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

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.internal.model.EnumValueSet;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IRangeValueSet;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 */
public class AttributeTest extends IpsPluginTest {
    
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile ipsSrcFile;
    private PolicyCmptType pcType;
    private IAttribute attribute;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject pdProject = this.newIpsProject("TestProject");
        ipsRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("products.folder", true, null);
        ipsSrcFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)ipsSrcFile.getIpsObject();
        attribute = pcType.newAttribute();
    }
    
    public void testGetConfigElementType() {
        attribute.setProductRelevant(false);
        assertNull(attribute.getConfigElementType());
        
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        assertEquals(ConfigElementType.POLICY_ATTRIBUTE, attribute.getConfigElementType());

        attribute.setAttributeType(AttributeType.CONSTANT);
        assertEquals(ConfigElementType.PRODUCT_ATTRIBUTE, attribute.getConfigElementType());

        attribute.setAttributeType(AttributeType.COMPUTED);
        assertEquals(ConfigElementType.FORMULA, attribute.getConfigElementType());
        
        attribute.setAttributeType(AttributeType.DERIVED);
        assertEquals(ConfigElementType.FORMULA, attribute.getConfigElementType());
    }
    
    public void testRemove() {
        attribute.delete();
        assertEquals(0, pcType.getAttributes().length);
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testSetDatatype() {
        attribute.setDatatype("Money");
        assertEquals("Money", attribute.getDatatype());
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testSetComputed() {
        attribute.setProductRelevant(true);
        assertEquals(true, attribute.isProductRelevant());
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testSetParameters() {
        attribute.setFormulaParameters(new Parameter[]{new Parameter(0, "p", "int")});
        assertEquals(1, attribute.getFormulaParameters().length);
        assertEquals("p", attribute.getFormulaParameters()[0].getName());
        assertEquals("int", attribute.getFormulaParameters()[0].getDatatype());
        assertTrue(ipsSrcFile.isDirty());
    }
    
    public void testGetParameters() {
        Parameter[] params = new Parameter[]{new Parameter(0, "p", "int")};
        attribute.setFormulaParameters(params);
        params[0] = null;
        Parameter[] params2 = attribute.getFormulaParameters();
        assertEquals("p", params2[0].getName());
        assertEquals("int", params2[0].getDatatype());
        params2[0] = null;
        assertNotNull(attribute.getFormulaParameters()[0]);
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        Element root =(Element)doc.getDocumentElement();
        NodeList nl =root.getElementsByTagName("Attribute");
        attribute.initFromXml((Element)nl.item(0));
        assertEquals(42, attribute.getId());
        assertEquals("premium", attribute.getName());
        assertEquals("money", attribute.getDatatype());
        assertFalse(attribute.isProductRelevant());
        assertEquals(AttributeType.COMPUTED, attribute.getAttributeType());
        assertEquals("42EUR", attribute.getDefaultValue());
        Parameter[] params = attribute.getFormulaParameters();
        assertEquals(2, params.length);
        assertEquals("policy", params[0].getName());
        assertEquals("MotorPolicy", params[0].getDatatype());
        assertEquals("vehicle", params[1].getName());
        assertEquals("Vehicle", params[1].getDatatype());
        assertNotNull(attribute.getValueSet());
        
        attribute.initFromXml((Element)nl.item(1));
        assertEquals(2, attribute.getId());
        assertNull(attribute.getDefaultValue());
        assertNotNull(attribute.getValueSet());
        assertEquals(EnumValueSet.class,attribute.getValueSet().getClass());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXml() {
        attribute = pcType.newAttribute();  // => id=1 as this is the type's 2 attribute
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        Parameter[] params = new Parameter[2];
        params[0] = new Parameter(0, "policy", "MotorPolicy");
        params[1] = new Parameter(1, "vehicle", "Vehicle");
        attribute.setFormulaParameters(params);
        attribute.setValueSetType(ValueSetType.RANGE);
        RangeValueSet set = (RangeValueSet)attribute.getValueSet();
        set.setLowerBound("unten");
        set.setUpperBound("oben");
        set.setStep("step");
        Element element = attribute.toXml(this.newDocument());
        
        Attribute copy = new Attribute();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("age", copy.getName());
        assertEquals("decimal", copy.getDatatype());
        assertTrue(copy.isProductRelevant());
        assertEquals(AttributeType.CONSTANT, copy.getAttributeType());
        assertEquals("18", copy.getDefaultValue());
        Parameter[] paramsCopy = copy.getFormulaParameters();
        assertEquals(2, paramsCopy.length);
        assertEquals("policy", paramsCopy[0].getName());
        assertEquals("MotorPolicy", paramsCopy[0].getDatatype());
        assertEquals("vehicle", paramsCopy[1].getName());
        assertEquals("Vehicle", paramsCopy[1].getDatatype());
        assertEquals("unten",((IRangeValueSet)copy.getValueSet()).getLowerBound());
        assertEquals("oben",((IRangeValueSet)copy.getValueSet()).getUpperBound());
        assertEquals("step",((IRangeValueSet)copy.getValueSet()).getStep());

        // Nun ein Attribut mit GenericEnumvalueset testen.
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        params = new Parameter[2];
        params[0] = new Parameter(0, "policy", "MotorPolicy");
        params[1] = new Parameter(1, "vehicle", "Vehicle");
        attribute.setFormulaParameters(params);
        attribute.setValueSetType(ValueSetType.ENUM);
        EnumValueSet set2 = (EnumValueSet)attribute.getValueSet();
        set2.addValue("a");
        set2.addValue("b");
        set2.addValue("x");
        
        element = attribute.toXml(this.newDocument());
        copy = new Attribute();
        copy.initFromXml(element);
        assertEquals("age", attribute.getName());
        assertEquals("decimal", attribute.getDatatype());
        assertTrue(attribute.isProductRelevant());
        assertEquals(AttributeType.CONSTANT, attribute.getAttributeType());
        assertEquals("18", attribute.getDefaultValue());
        paramsCopy = attribute.getFormulaParameters();
        assertEquals(2, paramsCopy.length);
        assertEquals("policy", paramsCopy[0].getName());
        assertEquals("MotorPolicy", paramsCopy[0].getDatatype());
        assertEquals("vehicle", paramsCopy[1].getName());
        assertEquals("Vehicle", paramsCopy[1].getDatatype());
        String [] vekt = ((IEnumValueSet)copy.getValueSet()).getValues();
        assertEquals("a", vekt[0]);
        assertEquals("b", vekt[1]);
        assertEquals("x", vekt[2]);
    }
    
    /**
     * Tests if an calculates attribute that has a paramater that refers to the type
     * the attribute is defined in produces not a stack overflow.
     * @throws CoreException 
     */
    public void testValidate_ParamOfCalculatedAttributeRefersToTheTypeItIsDefinedIn() throws CoreException {
    	attribute.setAttributeType(AttributeType.COMPUTED);
    	attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
    	attribute.setName("premium");
    	Parameter[] params = new Parameter[]{new Parameter(0, "police", pcType.getQualifiedName())};
    	attribute.setFormulaParameters(params);
    	
    	attribute.validate(); // once this produced a stack overflow!
    }

    /**
     * Tests for the correct type of excetion to be thrwon - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			attribute.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testValidate_productRelevant() throws Exception {
    	pcType.setConfigurableByProductCmptType(true);
    	attribute.setProductRelevant(true);
    	
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    	
    	pcType.setConfigurableByProductCmptType(false);
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    }

    public void testValidate_invalidAttributeName() throws Exception {
    	attribute.setName("test");
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));
    	
    	attribute.setName("a.b");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));
    }

    public void testValidate_defaultNotParsableUnknownDatatype() throws Exception {
    	attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
    	attribute.setDefaultValue("1");
    	
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));
    	
    	attribute.setDatatype("a");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));
    }

    public void testValidate_defaultNotParsableInvalidDatatype() throws Exception {
    	attribute.setDatatype(Datatype.INTEGER.getQualifiedName());

    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));
    	
    	attribute.setDatatype("abc");
    	ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));
    }

    public void testValidate_valueNotParsable() throws Exception {
    	attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
    	attribute.setDefaultValue("1");
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));
    	
    	attribute.setDefaultValue("a");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));
    }

    public void testValidate_defaultNotInValueset() throws Exception {
    	attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
    	attribute.setValueSetType(ValueSetType.RANGE);
    	IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
    	range.setLowerBound("0");
    	range.setUpperBound("10");
    	range.setStep("1");
    	attribute.setDefaultValue("1");
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));

    	attribute.setDefaultValue("100");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    public void testValidate_noInputParameters() throws Exception {
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_NO_INPUT_PARAMETERS));
    	
    	attribute.setAttributeType(AttributeType.COMPUTED);
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_NO_INPUT_PARAMETERS));
    }

    public void testValidate_noParametersNeccessary() throws Exception {
    	attribute.setProductRelevant(false);
    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_NO_PARAMETERS_NECCESSARY));
    	
    	Parameter param = new Parameter(0, "test", Datatype.INTEGER.getQualifiedName());
    	attribute.setFormulaParameters(new Parameter[] {param});
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_NO_PARAMETERS_NECCESSARY));
    }

    public void testValidate_emptyParameterName() throws Exception {
    	attribute.setAttributeType(AttributeType.COMPUTED);
    	Parameter param = new Parameter(0, "test", Datatype.INTEGER.getQualifiedName());
    	attribute.setFormulaParameters(new Parameter[] {param});

    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_EMPTY_PARAMETER_NAME));
    	
    	param.setName("");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_EMPTY_PARAMETER_NAME));
    	
    }

    public void testValidate_invalidParameterName() throws Exception {
    	attribute.setAttributeType(AttributeType.COMPUTED);
    	Parameter param = new Parameter(0, "test", Datatype.INTEGER.getQualifiedName());
    	attribute.setFormulaParameters(new Parameter[] {param});

    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_PARAMETER_NAME));
    	
    	param.setName("a.b");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_PARAMETER_NAME));
    }

    public void testValidate_noDatatypeForParameter() throws Exception {
    	attribute.setAttributeType(AttributeType.COMPUTED);
    	Parameter param = new Parameter(0, "test", Datatype.INTEGER.getQualifiedName());
    	attribute.setFormulaParameters(new Parameter[] {param});

    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_NO_DATATYPE_FOR_PARAMETER));
    	
    	param.setDatatype("");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_NO_DATATYPE_FOR_PARAMETER));
    }

    public void testValidate_datatypeNotFound() throws Exception {
    	attribute.setAttributeType(AttributeType.COMPUTED);
    	Parameter param = new Parameter(0, "test", Datatype.INTEGER.getQualifiedName());
    	attribute.setFormulaParameters(new Parameter[] {param});

    	MessageList ml = attribute.validate();
    	assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DATATYPE_NOT_FOUND));

    	param.setDatatype("abc");
    	ml = attribute.validate();
    	assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_DATATYPE_NOT_FOUND));
    }
}

    