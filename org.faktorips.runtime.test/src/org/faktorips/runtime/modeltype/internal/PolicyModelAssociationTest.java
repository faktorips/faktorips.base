package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.runtime.modeltype.IModelTypeAssociation.AssociationType;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IPolicyModelAssociation;
import org.junit.Test;

public class PolicyModelAssociationTest {

    private final IPolicyModel modelType = Models.getPolicyModel(Source.class);
    private final IPolicyModelAssociation association = modelType.getAssociation("asso");
    private final IPolicyModelAssociation association2 = modelType.getAssociation("asso2");

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
    public void testGetAssociationType() {
        assertEquals(AssociationType.Association, association.getAssociationType());
        assertEquals(AssociationType.Composition, association2.getAssociationType());
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

    @IpsPolicyCmptType(name = "MySource")
    @IpsAssociations({ "asso", "asso2" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test", defaultLocale = "de")
    private static class Source implements IModelObject {

        private Target target;

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @IpsAssociation(name = "asso", pluralName = "assos", min = 0, max = 1, type = AssociationType.Association, targetClass = Target.class)
        @IpsDerivedUnion
        @IpsSubsetOfDerivedUnion("derivedUnion")
        public Target getTarget() {
            return target;
        }

        @IpsAssociation(name = "asso2", pluralName = "assos2", min = 1, max = 10, type = AssociationType.Composition, targetClass = Target.class)
        @IpsMatchingAssociation(source = Product.class, name = "Matching")
        @IpsInverseAssociation("Inverse")
        public List<Target> getTargets() {
            return Arrays.asList(target);
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
