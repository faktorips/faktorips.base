package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.runtime.modeltype.IModelTypeAssociation.AssociationType;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.IProductModelAssociation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductModelAssociationTest {

    @Mock
    private IRuntimeRepository repository;

    private final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    private final IProductModel modelType = Models.getProductModel(Source.class);
    private final IProductModelAssociation association = modelType.getAssociation("asso");
    private final IProductModelAssociation association2 = modelType.getAssociation("asso2");

    @Test
    public void testGetTarget() {
        assertEquals(Target.class, association.getTarget().getJavaClass());
        assertEquals(Target.class, association2.getTarget().getJavaClass());
    }

    @Test
    public void testGetNamePlural() {
        assertEquals("testPluralLabelProduct", association.getLabelForPlural(Locale.CANADA));
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
        ProductGen productGen = source.productGen;
        ProductGen productGen2 = source.productGen2;
        productGen.target = new Target();
        productGen2.target = new Target();
        productGen2.target2 = new Target();

        when(repository.getLatestProductComponentGeneration(source)).thenReturn(productGen);

        assertThat(association.getTargetObjects(source, null).size(), is(1));
        assertThat(association.getTargetObjects(source, null), hasItem(source.target));

        assertThat(association2.getTargetObjects(source, null).size(), is(1));
        assertThat(association2.getTargetObjects(source, null), hasItem(productGen.target));

        assertThat(association2.getTargetObjects(source, effectiveDate).size(), is(1));
        assertThat(association2.getTargetObjects(source, effectiveDate), hasItem(productGen.target));

        assertThat(association2.getTargetObjects(source, new GregorianCalendar(1999, 2, 2)).size(), is(2));
        assertThat(association2.getTargetObjects(source, new GregorianCalendar(1999, 2, 2)),
                hasItem(productGen2.target));
        assertThat(association2.getTargetObjects(source, new GregorianCalendar(1999, 2, 2)),
                hasItem(productGen2.target2));
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
        assertEquals("MyPolicy", association2.getMatchingAssociationSource());
    }

    @Test
    public void testGetMatchingAssociationSourceType() throws Exception {
        assertNull(association.getMatchingAssociationSourceType());
        assertEquals(Policy.class, association2.getMatchingAssociationSourceType().getJavaClass());
    }

    @IpsProductCmptType(name = "MySource")
    @IpsAssociations({ "asso", "asso2" })
    @IpsChangingOverTime(ProductGen.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test", defaultLocale = "de")
    private class Source extends ProductComponent {

        public Source() {
            super(repository, "id", "productKindId", "versionId");
        }

        private Target target;
        private final ProductGen productGen = new ProductGen(this);
        private final ProductGen productGen2 = new ProductGen(this);

        @IpsAssociation(name = "asso", pluralName = "assos", min = 0, max = 1, type = AssociationType.Association, targetClass = Target.class)
        @IpsDerivedUnion
        @IpsSubsetOfDerivedUnion("derivedUnion")
        public Target getTarget() {
            return target;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            if (effectiveDate != ProductModelAssociationTest.this.effectiveDate) {
                return productGen2;
            }
            return productGen;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return productGen;
        }

    }

    private class ProductGen extends ProductComponentGeneration {

        private Target target;
        private Target target2;

        public ProductGen(Source product) {
            super(product);
        }

        @IpsAssociation(name = "asso2", pluralName = "assos2", min = 1, max = 10, type = AssociationType.Composition, targetClass = Target.class)
        @IpsMatchingAssociation(source = Policy.class, name = "Matching")
        @IpsInverseAssociation("Inverse")
        public List<Target> getTargets() {
            ArrayList<Target> targets = new ArrayList<Target>(2);
            if (target != null) {
                targets.add(target);
            }
            if (target2 != null) {
                targets.add(target2);
            }
            return targets;
        }
    }

    @IpsProductCmptType(name = "MyTarget")
    private class Target extends ProductComponent {

        public Target() {
            super(repository, "id2", "productKindId2", "versionId2");
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsConfiguredBy(Source.class)
    private class Policy implements IModelObject {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }
}
