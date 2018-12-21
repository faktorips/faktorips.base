package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.junit.Test;

public class PolicyAssociationTest {

    private final PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(Source.class);
    private final PolicyAssociation association = policyCmptType.getAssociation("asso");
    private final PolicyAssociation association2 = policyCmptType.getAssociation("asso2");
    private final PolicyAssociation association1ToN = policyCmptType.getAssociation("targets1toN");

    @Test
    public void testGetTarget() {
        assertEquals(Target.class, association.getTarget().getJavaClass());
        assertEquals(Target.class, association2.getTarget().getJavaClass());
    }

    @Test
    public void testGetNamePlural() {
        assertEquals("testPluralLabel", association.getLabelForPlural(Locale.CANADA));
        assertEquals("assos2", association2.getLabelForPlural(Locale.CANADA));
    }

    @Test
    public void testGetAssociationKind() {
        assertEquals(AssociationKind.Association, association.getAssociationKind());
        assertEquals(AssociationKind.Composition, association2.getAssociationKind());
    }

    @Test
    public void testGetMinCardinality() {
        assertEquals(0, association.getMinCardinality());
        assertEquals(1, association2.getMinCardinality());
    }

    @Test
    public void testGetMaxCardinality() {
        assertEquals(1, association.getMaxCardinality());
        assertEquals(10, association2.getMaxCardinality());
    }

    @Test
    public void testGetTargetObjects() {
        Source source = new Source();
        source.target = new Target();

        assertThat(association.getTargetObjects(source).size(), is(1));
        assertThat(association.getTargetObjects(source), hasItem(source.target));
        assertThat(association2.getTargetObjects(source).size(), is(1));
        assertThat(association2.getTargetObjects(source), hasItem(source.target));
    }

    @Test
    public void testGetUsedName() {
        assertEquals("asso", association.getUsedName());
        assertEquals("assos2", association2.getUsedName());
    }

    @Test
    public void testIsDerivedUnion() {
        assertTrue(association.isDerivedUnion());
        assertFalse(association2.isDerivedUnion());
    }

    @Test
    public void testIsSubsetOfADerivedUnion() {
        assertTrue(association.isSubsetOfADerivedUnion());
        assertFalse(association2.isSubsetOfADerivedUnion());
    }

    @Test
    public void testGetInverseAssociation() {
        assertNull(association.getInverseAssociation());
        assertEquals("Inverse", association2.getInverseAssociation());
    }

    @Test
    public void testIsMatchingAssociationPresent() {
        assertFalse(association.isMatchingAssociationPresent());
        assertTrue(association2.isMatchingAssociationPresent());
    }

    @Test
    public void testGetMatchingAssociationName() {
        assertNull(association.getMatchingAssociationName());
        assertEquals("Matching", association2.getMatchingAssociationName());
    }

    @Test
    public void testGetMatchingAssociationSource() {
        assertNull(association.getMatchingAssociationSource());
        assertEquals("MyProduct", association2.getMatchingAssociationSource());
    }

    @Test
    public void testGetMatchingAssociationSourceType() throws Exception {
        assertNull(association.getMatchingAssociationSourceType());
        assertEquals(Product.class, association2.getMatchingAssociationSourceType().getJavaClass());
    }

    @Test
    public void testAddTargetObject_to1_association() {
        Source source = new Source();
        Target target = new Target();

        association.addTargetObject(source, target);
        assertSame(target, source.getTarget());
    }

    @Test
    public void testAddTargetObject_to1_replaceAssociatedObject() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association.addTargetObject(source, target);
        assertSame(target, source.getTarget());
        association.addTargetObject(source, target2);
        assertSame(target2, source.getTarget());
    }

    @Test
    public void testAddTargetObject_toN_Composition() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association1ToN.addTargetObject(source, target);
        assertEquals(1, source.getTargets1toN().size());
        association1ToN.addTargetObject(source, target2);
        assertEquals(2, source.getTargets1toN().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTargetObject_NoMethod() {
        Source source = new Source();
        Target target = new Target();

        association2.addTargetObject(source, target);
    }

    @Test
    public void testRemoveTargetObject_to1_association() {
        Source source = new Source();
        Target target = new Target();
        source.setTarget(target);

        association.removeTargetObject(source, target);
        assertNull(source.getTarget());
    }

    @Test
    public void testRemoveTargetObject_to1_doNothingIfDifferentObject() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();
        source.setTarget(target);

        association.removeTargetObject(source, target2);
        assertSame(target, source.getTarget());
    }

    @Test
    public void testRemoveTargetObject_toN_composition() {
        Source source = new Source();
        Target target = new Target();
        source.addTargets1toN(target);

        assertEquals(1, source.getTargets1toN().size());
        association1ToN.removeTargetObject(source, target);
        assertEquals(0, source.getTargets1toN().size());
        association1ToN.removeTargetObject(source, target);
        assertEquals(0, source.getTargets1toN().size());
    }

    @Test
    public void testRemoveTargetObject_toN_doNothingIfNotPresent() {
        Source source = new Source();
        Target target = new Target();
        source.addTargets1toN(target);
        Target target2 = new Target();

        association1ToN.removeTargetObject(source, target2);
        assertSame(target, source.getTargets1toN().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTargetObject_NoMethod() {
        Source source = new Source();
        Target target = new Target();
        source.setTarget(target);

        association2.removeTargetObject(source, target);
    }

    @IpsPolicyCmptType(name = "MySource")
    @IpsAssociations({ "asso", "asso2", "targets1toN" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class Source implements IModelObject {

        private Target target;
        private final List<Target> targets1toN = new ArrayList<PolicyAssociationTest.Target>();

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @IpsAssociation(name = "asso", pluralName = "assos", min = 0, max = 1, kind = AssociationKind.Association, targetClass = Target.class)
        @IpsDerivedUnion
        @IpsSubsetOfDerivedUnion("derivedUnion")
        public Target getTarget() {
            return target;
        }

        @IpsAssociationAdder(association = "asso")
        public void setTarget(Target objectToAdd) {
            target = objectToAdd;
        }

        @IpsAssociation(name = "asso2", pluralName = "assos2", min = 1, max = 10, kind = AssociationKind.Composition, targetClass = Target.class)
        @IpsMatchingAssociation(source = Product.class, name = "Matching")
        @IpsInverseAssociation("Inverse")
        public List<Target> getTargets() {
            return Arrays.asList(target);
        }

        @IpsAssociation(name = "targets1toN", pluralName = "targets", min = 0, max = Integer.MAX_VALUE, kind = AssociationKind.Composition, targetClass = Target.class)
        public List<Target> getTargets1toN() {
            return targets1toN;
        }

        @IpsAssociationAdder(association = "targets1toN")
        public void addTargets1toN(Target objectToAdd) {
            targets1toN.add(objectToAdd);
        }

        @IpsAssociationRemover(association = "targets1toN")
        public void removeTargets1toN(Target objectToRemove) {
            targets1toN.remove(objectToRemove);
        }

    }

    @IpsPolicyCmptType(name = "MyTarget")
    private static class Target implements IModelObject {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsProductCmptType(name = "MyProduct")
    private static abstract class Product extends ProductComponent {

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

    }

}
