package org.faktorips.devtools.core.internal.model.product;

import java.util.List;

import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptTest extends IpsPluginTest {
    
    private ProductCmpt productCmpt;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile srcFile;
    private IIpsProject pdProject;
    
    /*
     * @see PluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        root = pdProject.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);
        srcFile = pack.createIpsFile(IpsObjectType.PRODUCT_CMPT, "TestProduct", true, null);
        productCmpt = (ProductCmpt)srcFile.getIpsObject();
    }
    
    public void testSetPolicyCmptType() {
        productCmpt.setPolicyCmptType("newType");
        assertEquals("newType", productCmpt.getPolicyCmptType());
        assertTrue(srcFile.isDirty());
    }
    
    public void testInitFromXml() {
        productCmpt.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("MotorPolicy", productCmpt.getPolicyCmptType());
        assertEquals(2, productCmpt.getNumOfGenerations());
        IProductCmptGeneration gen = (IProductCmptGeneration)productCmpt.getGenerations()[0];
        assertEquals(1, gen.getNumOfConfigElements());
        IConfigElement ce = gen.getConfigElements()[0];
        assertEquals("1.5", ce.getValue());
    }
    
    public void testToXml() {
        productCmpt.setPolicyCmptType("MotorPolicy");
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        IConfigElement ce1 = gen1.newConfigElement();
        ce1.setValue("0.15");
        productCmpt.newGeneration();
        Element element = productCmpt.toXml(newDocument());
        ProductCmpt copy = new ProductCmpt();
        copy.initFromXml(element);
        assertEquals("MotorPolicy", copy.getPolicyCmptType());
        assertEquals(2, copy.getNumOfGenerations());
        IProductCmptGeneration genCopy = (IProductCmptGeneration)copy.getGenerations()[0];
        assertEquals(1, genCopy.getConfigElements().length);
        assertEquals("0.15", genCopy.getConfigElements()[0].getValue());
    }
    
    public void testContainsFormula() {
        IProductCmptGeneration gen1 = (IProductCmptGeneration)productCmpt.newGeneration();
        IConfigElement ce1 = gen1.newConfigElement();
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        assertFalse(productCmpt.containsFormula());

        IConfigElement ce2 = gen1.newConfigElement();
        ce2.setType(ConfigElementType.FORMULA);
        assertTrue(productCmpt.containsFormula());
    }
    
    public void testDependsOn() throws Exception{
        IPolicyCmptType a = newPolicyCmptType(root, "A");
        IPolicyCmptType b = newPolicyCmptType(root, "B");
        IRelation relation = a.newRelation();
        relation.setTarget(b.getQualifiedName());
        IPolicyCmptType c = newPolicyCmptType(root, "C");
        c.setSupertype(a.getQualifiedName());
        IPolicyCmptType d = newPolicyCmptType(root, "D");
        relation = c.newRelation();
        relation.setTarget(d.getQualifiedName());
        IProductCmpt productCmpt = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "product");
        productCmpt.setPolicyCmptType(c.getQualifiedName());
        QualifiedNameType[] dependsOn = productCmpt.dependsOn();
        List dependsOnAsList = CollectionUtil.toArrayList(dependsOn);
        dependsOnAsList.contains(a.getQualifiedNameType());
        dependsOnAsList.contains(b.getQualifiedNameType());
        dependsOnAsList.contains(d.getQualifiedNameType());
        
        a.getRelations()[0].setProductRelevant(false);
        c.getRelations()[0].setProductRelevant(false);
        dependsOn = productCmpt.dependsOn();
        dependsOnAsList = CollectionUtil.toArrayList(dependsOn);
        dependsOnAsList.contains(a.getQualifiedNameType());
        assertEquals(1, dependsOn.length);
        
        productCmpt = (IProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, "deckung");
        dependsOn = productCmpt.dependsOn();
        assertEquals(0, dependsOn.length);

    }
    
    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
    		productCmpt.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }

}
