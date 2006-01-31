package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class RelationTest extends IpsObjectTestCase {
    
    private PolicyCmptType pcType;
    private IRelation relation;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.POLICY_CMPT_TYPE);
    }
    
    protected void createObjectAndPart() {
        pcType = new PolicyCmptType(pdSrcFile);
        relation = pcType.newRelation();
    }
    
    public void testRemove() {
        relation.delete();
        assertEquals(0, pcType.getAttributes().length);
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetType() {
        relation.setRelationType(RelationType.COMPOSITION);
        relation.setRelationType(RelationType.ASSOZIATION);
        assertEquals(RelationType.ASSOZIATION, relation.getRelationType());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        relation.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, relation.getId());
        assertEquals(RelationType.ASSOZIATION, relation.getRelationType());
        assertTrue(relation.isReadOnlyContainer());
        assertEquals("MotorPart", relation.getTarget());
        assertEquals("blabla", relation.getDescription());
        assertEquals("PolicyPart", relation.getTargetRoleSingular());
        assertEquals("PolicyParts", relation.getTargetRolePlural());
        assertEquals(1, relation.getMinCardinality());
        assertEquals("*", relation.getMaxCardinality());
        assertFalse(relation.isProductRelevant());
        assertEquals("Policy.Parts", relation.getContainerRelation());
        assertEquals("Part.Policy", relation.getReverseRelation());
        assertEquals("PolicyPartType", relation.getTargetRoleSingularProductSide());
        assertEquals("PolicyPartTypes", relation.getTargetRolePluralProductSide());
        assertEquals(2, relation.getMinCardinalityProductSide());
        assertEquals("3", relation.getMaxCardinalityProductSide());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXml() {
        relation = pcType.newRelation(); // id=1 as this is the type's 2 relation
        relation.setRelationType(RelationType.ASSOZIATION);
        relation.setReadOnlyContainer(true);
        relation.setTarget("target");
        relation.setTargetRoleSingular("targetRoleSingular");
        relation.setTargetRolePlural("targetRolePlural");
        relation.setProductRelevant(false);
        relation.setContainerRelation("super");
        relation.setReverseRelation("reverse");
        relation.setMinCardinality(2);
        relation.setMaxCardinality("3");
        relation.setDescription("blabla");
        relation.setTargetRoleSingularProductSide("targetRoleSingularProductSide");
        relation.setTargetRolePluralProductSide("targetRolePluralProductSide");
        relation.setMinCardinalityProductSide(4);
        relation.setMaxCardinalityProductSide("5");
        
        Element element = relation.toXml(this.newDocument());
        
        Relation copy = new Relation();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals(RelationType.ASSOZIATION, copy.getRelationType());
        assertTrue(copy.isReadOnlyContainer());
        assertEquals("target", copy.getTarget());
        assertEquals("targetRoleSingular", copy.getTargetRoleSingular());
        assertEquals("targetRolePlural", copy.getTargetRolePlural());
        assertEquals("super", copy.getContainerRelation());
        assertEquals("reverse", copy.getReverseRelation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals("3", copy.getMaxCardinality());
        assertFalse(copy.isProductRelevant());
        assertEquals("blabla", copy.getDescription());
        
        assertEquals("targetRoleSingularProductSide", copy.getTargetRoleSingularProductSide());
        assertEquals("targetRolePluralProductSide", copy.getTargetRolePluralProductSide());
        assertEquals(4, copy.getMinCardinalityProductSide());
        assertEquals("5", copy.getMaxCardinalityProductSide());        
    }
    
    /**
     * Tests for the correct type of excetion to be thrwon - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			relation.newPart(IAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
}
