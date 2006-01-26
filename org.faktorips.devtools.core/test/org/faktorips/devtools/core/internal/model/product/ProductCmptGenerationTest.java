package org.faktorips.devtools.core.internal.model.product;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptGenerationTest extends IpsPluginTest {

    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsPackageFragmentRoot root;
    
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project =  newIpsProject("TestProject");
        
        root = project.getIpsPackageFragmentRoots()[0];
        productCmpt = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "testProduct");
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
    }
    
    public void testGetChildren() throws CoreException  {
        IConfigElement cf0 = generation.newConfigElement();
        IProductCmptRelation r0 = generation.newRelation("targetRole");
        IIpsElement[] children = generation.getChildren();
        assertEquals(2, children.length);
        assertSame(cf0, children[0]);
        assertSame(r0, children[1]);
    }
    
    public void testGetConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());
        
        IConfigElement ce1 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);

        IConfigElement ce2 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);
        assertEquals(ce2, generation.getConfigElements()[1]);
    }

    public void testGetConfigElements_Type() {
        IConfigElement ce1 = generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setType(ConfigElementType.FORMULA);
        IConfigElement ce3 = generation.newConfigElement();
        
        IConfigElement[] elements = generation.getConfigElements(ConfigElementType.PRODUCT_ATTRIBUTE);
        assertEquals(2, elements.length);
        assertEquals(ce1, elements[0]);
        assertEquals(ce3, elements[1]);
        
        elements = generation.getConfigElements(ConfigElementType.POLICY_ATTRIBUTE);
        assertEquals(0, elements.length);
    }
    
    public void testGetConfigElement_AttributeName() {
        IConfigElement ce1 = generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPcTypeAttribute("a2");
        
        assertEquals(ce2, generation.getConfigElement("a2"));
        assertNull(generation.getConfigElement("unkown"));
        
    }

    public void testGetNumOfConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());
        
        IConfigElement ce1 = generation.newConfigElement();
        assertEquals(1, generation.getNumOfConfigElements());

        IConfigElement ce2 = generation.newConfigElement();
        assertEquals(2, generation.getNumOfConfigElements());
    }

    public void testNewConfigElement() {
        IConfigElement ce = generation.newConfigElement();
        assertEquals(generation, ce.getParent());
        assertEquals(1, generation.getNumOfConfigElements());
    }

    /*
     * Class under test for ProductCmptRelation[] getRelations()
     */
    public void testGetRelations() {
        IProductCmptRelation r1 = generation.newRelation("coverage");
        assertEquals(r1, generation.getRelations()[0]);

        IProductCmptRelation r2 = generation.newRelation("risk");
        assertEquals(r1, generation.getRelations()[0]);
        assertEquals(r2, generation.getRelations()[1]);
    }

    /*
     * Class under test for ProductCmptRelation[] getRelations(String)
     */
    public void testGetRelationsString() {
        IProductCmptRelation r1 = generation.newRelation("coverage");
        IProductCmptRelation r2 = generation.newRelation("risk");
        IProductCmptRelation r3 = generation.newRelation("coverage");
        
        IProductCmptRelation[] relations = generation.getRelations("coverage");
        assertEquals(2, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r1, relations[1]);

        relations = generation.getRelations("unknown");
        assertEquals(0, relations.length);
    }

    public void testGetNumOfRelations() {
        assertEquals(0, generation.getNumOfRelations());
        
        IProductCmptRelation r1 = generation.newRelation("coverage");
        assertEquals(1, generation.getNumOfRelations());

        IProductCmptRelation r2 = generation.newRelation("risk");
        assertEquals(2, generation.getNumOfRelations());
    }

    public void testNewRelation() {
        IProductCmptRelation relation = generation.newRelation("coverage");
        assertEquals(generation, relation.getParent());
        assertEquals(1, generation.getNumOfRelations());
        assertEquals(relation, generation.getRelations()[0]);
    }

    /*
     * Class under test for void toXml(Element)
     */
    public void testToXmlElement() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        generation.newConfigElement();
        generation.newConfigElement();
        generation.newRelation("coverage");
        generation.newRelation("coverage");
        generation.newRelation("coverage");
        Element element = generation.toXml(newDocument());
        
        IProductCmptGeneration copy = new ProductCmptGeneration();
        copy.initFromXml(element);
        assertEquals(2, copy.getNumOfConfigElements());
        assertEquals(3, copy.getNumOfRelations());
    }

    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
        
        IConfigElement[] configElements = generation.getConfigElements();
        assertEquals(1, configElements.length);
        
        IProductCmptRelation[] relations = generation.getRelations();
        assertEquals(1, relations.length);
    }

    
    public void testValidate() throws Exception{
        
        IPolicyCmptType a = newPolicyCmptType(root, "A");
        IPolicyCmptType b = newPolicyCmptType(root, "B");
        IAttribute bAttribute = b.newAttribute();
        bAttribute.setAttributeType(AttributeType.CHANGEABLE);
        bAttribute.setName("bAttribute");
        bAttribute.setDatatype("String");
        IAttribute anAttribute = a.newAttribute();
        anAttribute.setName("anAttribute");
        anAttribute.setAttributeType(AttributeType.COMPUTED);
        anAttribute.setDatatype("String");
        Parameter p = new Parameter(0, "b", b.getQualifiedName());
        anAttribute.setFormulaParameters(new Parameter[]{p});
        
        IProductCmpt aProduct = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "aProduct");
        aProduct.setPolicyCmptType(a.getQualifiedName());
        IProductCmptGeneration aProductGen = (IProductCmptGeneration)aProduct.newGeneration();
        IConfigElement configElement = aProductGen.newConfigElement();
        configElement.setPcTypeAttribute("anAttribute");
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setValue("b.bAttribute");
        MessageList msgList = aProductGen.validate();
        assertTrue(msgList.isEmpty());
        
        //change the name of bAttribute. A validation message from the formula validation is expected
        bAttribute.setName("cAttribute");
        msgList = aProductGen.validate();
        assertNotNull(msgList.getMessageByCode(ExprCompiler.UNDEFINED_IDENTIFIER));
        
    }
}