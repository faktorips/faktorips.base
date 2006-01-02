package org.faktorips.devtools.core.internal.model.product;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptRelationTest extends IpsObjectTestCase {

    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptRelation relation;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.PRODUCT_CMPT);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectTestCase#createObjectAndPart()
     */
    protected void createObjectAndPart() {
        productCmpt = new ProductCmpt(pdSrcFile);
        generation = (IProductCmptGeneration)productCmpt.newGeneration();
        relation = generation.newRelation("coverage");
    }

    public void testRemove() {
        relation.delete();
        assertEquals(0, generation.getNumOfRelations());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testSetTarget() {
        relation.setTarget("newTarget");
        assertEquals("newTarget", relation.getTarget());
        assertTrue(pdSrcFile.isDirty());
    }

    public void testToXml() {
        relation = generation.newRelation("coverage");
        relation.setTarget("newTarget");
        relation.setMinCardinality(2);
        relation.setMaxCardinality("3");
        Element element = relation.toXml(newDocument());
        
        IProductCmptRelation copy = new ProductCmptRelation();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("newTarget", copy.getTarget());
        assertEquals("coverage", copy.getPcTypeRelation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals("3", copy.getMaxCardinality());
    }

    public void testInitFromXml() {
        relation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(42, relation.getId());
        assertEquals("FullCoverage", relation.getPcTypeRelation());
        assertEquals("FullCoveragePlus", relation.getTarget());
        assertEquals(2, relation.getMinCardinality());
        assertEquals("3", relation.getMaxCardinality());
    }

}
