package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.Signature;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.PluginTest;
import org.faktorips.devtools.core.model.EnumValueSet;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 */
public class AttributeTest extends PluginTest {
    
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IAttribute attribute;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
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
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetDatatype() {
        attribute.setDatatype("Money");
        assertEquals("Money", attribute.getDatatype());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetComputed() {
        attribute.setProductRelevant(true);
        assertEquals(true, attribute.isProductRelevant());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetParameters() {
        attribute.setFormulaParameters(new Parameter[]{new Parameter(0, "p", "int")});
        assertEquals(1, attribute.getFormulaParameters().length);
        assertEquals("p", attribute.getFormulaParameters()[0].getName());
        assertEquals("int", attribute.getFormulaParameters()[0].getDatatype());
        assertTrue(pdSrcFile.isDirty());
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
        assertEquals("0EUR", attribute.getDefaultValue());
        Parameter[] params = attribute.getFormulaParameters();
        assertEquals(2, params.length);
        assertEquals("policy", params[0].getName());
        assertEquals("MotorPolicy", params[0].getDatatype());
        assertEquals("vehicle", params[1].getName());
        assertEquals("Vehicle", params[1].getDatatype());
        assertNotNull(attribute.getValueSet());
        attribute.initFromXml((Element)nl.item(1));
        assertEquals(2, attribute.getId());
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
        ValueSet set = new Range("unten","oben", "step");
        attribute.setValueSet(set);
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
        assertEquals("unten",((Range)copy.getValueSet()).getLowerBound());
        assertEquals("oben",((Range)copy.getValueSet()).getUpperBound());
        assertEquals("step",((Range)copy.getValueSet()).getStep());

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
        EnumValueSet set2 = new EnumValueSet();
        set2.addValue("a");
        set2.addValue("b");
        set2.addValue("x");
        
        attribute.setValueSet(set2);
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
        String [] vekt = ((EnumValueSet)copy.getValueSet()).getElements();
        assertEquals("a", vekt[0]);
        assertEquals("b", vekt[1]);
        assertEquals("x", vekt[2]);
    }

    public void testGetJavaMethod_GetterMethodPolicyImplementation() throws CoreException {
        attribute.setName("premium");
        attribute.setDatatype("Money");
        // getter method implementation
        IMethod method = attribute.getJavaMethod(IAttribute.JAVA_GETTER_METHOD_IMPLEMENATION);
        assertNotNull(method);
        assertEquals("getPremium", method.getElementName());
        assertEquals(0, method.getNumberOfParameters());
    }
    
    public void testGetJavaMethod_GetterMethodPolicyInterface() throws CoreException {
        attribute.setName("premium");
        attribute.setDatatype("Money");
        IMethod method = attribute.getJavaMethod(IAttribute.JAVA_GETTER_METHOD_INTERFACE);
        assertNotNull(method);
        assertEquals("getPremium", method.getElementName());
        assertEquals(0, method.getNumberOfParameters());
    }
    
    public void testGetJavaMethod() throws CoreException {
        attribute.setName("premium");
        attribute.setDatatype("Money");

        // setter method implementation
        IMethod method = attribute.getJavaMethod(IAttribute.JAVA_SETTER_METHOD_IMPLEMENATION);
        assertNotNull(method);
        assertEquals("setPremium", method.getElementName());
        assertEquals(1, method.getNumberOfParameters());
        String paramSign = Signature.createTypeSignature(Datatype.MONEY.getJavaClassName(), false);
        assertEquals(paramSign, method.getParameterTypes()[0]);
        
        // setter method interface
        method = attribute.getJavaMethod(IAttribute.JAVA_SETTER_METHOD_INTERFACE);
        assertNotNull(method);
        assertEquals("setPremium", method.getElementName());
        assertEquals(1, method.getNumberOfParameters());
        paramSign = Signature.createTypeSignature(Datatype.MONEY.getJavaClassName(), false);
        assertEquals(paramSign, method.getParameterTypes()[0]);

        // computation method in product component interface. IAttribute type computed, no parameters
        attribute.setAttributeType(AttributeType.COMPUTED);
        attribute.setFormulaParameters(new Parameter[0]);
        method = attribute.getJavaMethod(IAttribute.JAVA_COMPUTE_ATTRIBUTE_METHOD_PRODUCTCMPT_INTERFACE);
        assertNotNull(method);
        assertEquals("computePremium", method.getElementName());
        assertEquals(0, method.getNumberOfParameters());
        
        // computation method in product component interface. IAttribute type derived, With parameters
        attribute.setAttributeType(AttributeType.DERIVED);
        Parameter[] params = new Parameter[] {
                new Parameter(1, "p0", Datatype.MONEY.getQualifiedName()),
                new Parameter(2, "p1", Datatype.DECIMAL.getQualifiedName()),
        };
        attribute.setFormulaParameters(params);
        method = attribute.getJavaMethod(IAttribute.JAVA_COMPUTE_ATTRIBUTE_METHOD_PRODUCTCMPT_INTERFACE);
        assertNotNull(method);
        assertEquals("computePremium", method.getElementName());
        assertEquals(2, method.getNumberOfParameters());
        
        // computation method in product component interface. IAttribute type constant => should return null
        attribute.setAttributeType(AttributeType.CONSTANT);
        method = attribute.getJavaMethod(IAttribute.JAVA_COMPUTE_ATTRIBUTE_METHOD_PRODUCTCMPT_INTERFACE);
        assertNull(method);
        
        // 
        
        // unkown kind
        try {
            attribute.getJavaMethod(-1);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetJavaField() throws CoreException {
        attribute.setName("premium");
        attribute.setDatatype("Money");
        IField field = attribute.getJavaField(IAttribute.JAVA_FIELD_VALUE_POLICY);
        assertNotNull(field);
        assertEquals("premium", field.getElementName());
    }
}
