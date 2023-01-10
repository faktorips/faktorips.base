/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
import org.faktorips.values.ListUtil;
import org.faktorips.values.ObjectUtil;
import org.junit.Test;

public class PolicyAssociationTest {

    private final PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(Source.class);
    private final PolicyAssociation association0To1 = policyCmptType.getAssociation("asso");
    private final PolicyAssociation association1To10 = policyCmptType.getAssociation("asso2");
    private final PolicyAssociation association1ToN = policyCmptType.getAssociation("targets1toN");

    @Test
    public void testGetTarget() {
        assertEquals(Target.class, association0To1.getTarget().getJavaClass());
        assertEquals(Target.class, association1To10.getTarget().getJavaClass());
    }

    @Test
    public void testGetNamePlural() {
        assertEquals("testPluralLabel", association0To1.getLabelForPlural(Locale.CANADA));
        assertEquals("assos2", association1To10.getLabelForPlural(Locale.CANADA));
    }

    @Test
    public void testGetAssociationKind() {
        assertEquals(AssociationKind.Association, association0To1.getAssociationKind());
        assertEquals(AssociationKind.Composition, association1To10.getAssociationKind());
    }

    @Test
    public void testGetMinCardinality() {
        assertEquals(0, association0To1.getMinCardinality());
        assertEquals(1, association1To10.getMinCardinality());
    }

    @Test
    public void testGetMaxCardinality() {
        assertEquals(1, association0To1.getMaxCardinality());
        assertEquals(10, association1To10.getMaxCardinality());
    }

    @Test
    public void testGetTargetObjects() {
        Source source = new Source();
        source.target = new Target();

        assertThat(association0To1.getTargetObjects(source).size(), is(1));
        assertThat(association0To1.getTargetObjects(source), hasItem(source.target));
        assertThat(association1To10.getTargetObjects(source).size(), is(1));
        assertThat(association1To10.getTargetObjects(source), hasItem(source.target));
    }

    @Test
    public void testGetUsedName() {
        assertEquals("asso", association0To1.getUsedName());
        assertEquals("assos2", association1To10.getUsedName());
    }

    @Test
    public void testIsDerivedUnion() {
        assertTrue(association0To1.isDerivedUnion());
        assertFalse(association1To10.isDerivedUnion());
    }

    @Test
    public void testIsSubsetOfADerivedUnion() {
        assertTrue(association0To1.isSubsetOfADerivedUnion());
        assertFalse(association1To10.isSubsetOfADerivedUnion());
    }

    @Test
    public void testGetInverseAssociation() {
        assertNull(association0To1.getInverseAssociation());
        assertEquals("Inverse", association1To10.getInverseAssociation());
    }

    @Test
    public void testIsMatchingAssociationPresent() {
        assertFalse(association0To1.isMatchingAssociationPresent());
        assertTrue(association1To10.isMatchingAssociationPresent());
    }

    @Test
    public void testGetMatchingAssociationName() {
        assertNull(association0To1.getMatchingAssociationName());
        assertEquals("Matching", association1To10.getMatchingAssociationName());
    }

    @Test
    public void testGetMatchingAssociationSource() {
        assertNull(association0To1.getMatchingAssociationSource());
        assertEquals("MyProduct", association1To10.getMatchingAssociationSource());
    }

    @Test
    public void testGetMatchingAssociationSourceType() throws Exception {
        assertNull(association0To1.getMatchingAssociationSourceType());
        assertEquals(Product.class, association1To10.getMatchingAssociationSourceType().getJavaClass());
    }

    @Test
    public void testGetMatchingAssociation() {
        ProductAssociation matchingAssociation = association1To10.getMatchingAssociation();
        assertEquals("Matching", matchingAssociation.getName());
        assertEquals("Matching", matchingAssociation.getNamePlural());
        assertEquals(Product.class, matchingAssociation.getType().getJavaClass());
    }

    @Test
    public void testGetMatchingAssociation_NoMatching() {
        assertNull(association0To1.getMatchingAssociation());
    }

    @Test
    public void testFindMatchingAssociation() {
        ProductAssociation matchingAssociation = association1To10.getMatchingAssociation();
        assertEquals("Matching", matchingAssociation.getName());
        assertEquals("Matching", matchingAssociation.getNamePlural());
        assertEquals(Product.class, matchingAssociation.getType().getJavaClass());
    }

    @Test
    public void testFindMatchingAssociation_NoMatching() {
        assertEquals(Optional.empty(), association0To1.findMatchingAssociation());
    }

    @Test
    public void testAddTargetObjects_To1_Association() {
        Source source = new Source();
        Target target = new Target();

        association0To1.addTargetObjects(source, target);
        assertSame(target, source.getTarget());
    }

    @Test
    public void testAddTargetObjects_To1_ReplaceAssociatedObject() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association0To1.addTargetObjects(source, target);
        assertSame(target, source.getTarget());
        association0To1.addTargetObjects(source, target2);
        assertSame(target2, source.getTarget());
    }

    @Test
    public void testAddTargetObjects_ToN_Composition() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association1ToN.addTargetObjects(source, target);
        assertEquals(1, source.getTargets1toN().size());
        association1ToN.addTargetObjects(source, target2);
        assertEquals(2, source.getTargets1toN().size());
    }

    @Test
    public void testAddTargetObjects_VarArg() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association1ToN.addTargetObjects(source, target, target2);
        assertEquals(2, source.getTargets1toN().size());
    }

    @Test
    public void testAddTargetObjects_List() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association1ToN.addTargetObjects(source, Arrays.<IModelObject> asList(target, target2));
        assertEquals(2, source.getTargets1toN().size());
    }

    @Test
    public void testAddTargetObjects_ToN_DoNothingIfEmptyVarArg() {
        Source source = new Source();

        association1ToN.addTargetObjects(source);
        assertEquals(0, source.getTargets1toN().size());
    }

    @Test
    public void testAddTargetObjects_ToN_DoNothingIfEmptyList() {
        Source source = new Source();

        association1ToN.addTargetObjects(source, new ArrayList<IModelObject>());
        assertEquals(0, source.getTargets1toN().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTargetObjects_To1_MultipleObjects() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association0To1.addTargetObjects(source, Arrays.<IModelObject> asList(target, target2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTargetObjects_NoMethod() {
        Source source = new Source();
        Target target = new Target();

        association1To10.addTargetObjects(source, target);
    }

    @Test
    public void testAddTargetObjects_ToN_Overridden() {
        SubSource subSource = new SubSource();
        SubTarget subTarget = new SubTarget();
        assertEquals(0, subSource.getTargets1toN().size());

        PolicyAssociation overridingAssociation = IpsModel.getPolicyCmptType(subSource).getAssociation("targets1toN");
        overridingAssociation.addTargetObjects(subSource, subTarget);

        assertEquals(1, subSource.getTargets1toN().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTargetObjects_ToN_Overridden_WrongType() {
        SubSource subSource = new SubSource();
        Target wrongTarget = new Target();

        PolicyAssociation overridingAssociation = IpsModel.getPolicyCmptType(subSource).getAssociation("targets1toN");
        overridingAssociation.addTargetObjects(subSource, wrongTarget);
    }

    @Test
    public void testAddTargetObjects_ToN_Overridden_WithListenerSupport() {
        SubSource subSource = new SubSource();
        SubTarget subTarget = new SubTarget();
        assertEquals(0, subSource.getMoreTargets1toN().size());

        PolicyAssociation overridingAssociation = IpsModel.getPolicyCmptType(subSource)
                .getAssociation("moreTargets1toN");
        overridingAssociation.addTargetObjects(subSource, subTarget);

        assertEquals(1, subSource.getMoreTargets1toN().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTargetObjects_ToN_Overridden_WrongType_WithListenerSupport() {
        SubSource subSource = new SubSource();
        Target wrongTarget = new Target();

        PolicyAssociation overridingAssociation = IpsModel.getPolicyCmptType(subSource)
                .getAssociation("moreTargets1toN");
        overridingAssociation.addTargetObjects(subSource, wrongTarget);
    }

    @Test
    public void testRemoveTargetObjects_To1_Association() {
        Source source = new Source();
        Target target = new Target();
        source.setTarget(target);

        association0To1.removeTargetObjects(source, target);
        assertNull(source.getTarget());
    }

    @Test
    public void testRemoveTargetObjects_To1_DoNothingIfDifferentObject() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();
        source.setTarget(target);

        association0To1.removeTargetObjects(source, target2);
        assertSame(target, source.getTarget());
    }

    @Test
    public void testRemoveTargetObjects_ToN_Composition() {
        Source source = new Source();
        Target target = new Target();
        source.addTargets1toN(target);

        assertEquals(1, source.getTargets1toN().size());
        association1ToN.removeTargetObjects(source, target);
        assertEquals(0, source.getTargets1toN().size());
        association1ToN.removeTargetObjects(source, target);
        assertEquals(0, source.getTargets1toN().size());
    }

    @Test
    public void testRemoveTargetObjects_ToN_Overridden() {
        SubSource subSource = new SubSource();
        SubTarget subTarget = new SubTarget();
        subSource.addTargets1toN(subTarget);
        assertEquals(1, subSource.getTargets1toN().size());

        PolicyAssociation overridingAssociation = IpsModel.getPolicyCmptType(subSource).getAssociation("targets1toN");
        overridingAssociation.removeTargetObjects(subSource, subTarget);

        assertEquals(0, subSource.getTargets1toN().size());
    }

    @Test
    public void testRemoveTargetObjects_ToN_DoNothingIfNotPresent() {
        Source source = new Source();
        Target target = new Target();
        source.addTargets1toN(target);
        Target target2 = new Target();

        association1ToN.removeTargetObjects(source, target2);
        assertSame(target, source.getTargets1toN().get(0));
    }

    @Test
    public void testRemoveTargetObjects_ToN_DoNothingIfEmptyVarArg() {
        Source source = new Source();
        Target target = new Target();
        source.addTargets1toN(target);

        association1ToN.removeTargetObjects(source);
        assertSame(target, source.getTargets1toN().get(0));
    }

    @Test
    public void testRemoveTargetObjects_ToN_DoNothingIfEmptyList() {
        Source source = new Source();
        Target target = new Target();
        source.addTargets1toN(target);

        association1ToN.removeTargetObjects(source, new ArrayList<IModelObject>());
        assertSame(target, source.getTargets1toN().get(0));
    }

    @Test
    public void testRemoveTargetObjects_VarArg() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();
        association1ToN.addTargetObjects(source, target, target2);

        association1ToN.removeTargetObjects(source, target, target2);
        assertEquals(0, source.getTargets1toN().size());
    }

    @Test
    public void testRemoveTargetObjects_List() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();
        association1ToN.addTargetObjects(source, target, target2);

        association1ToN.removeTargetObjects(source, Arrays.<IModelObject> asList(target, target2));
        assertEquals(0, source.getTargets1toN().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTargetObjects_To1_MultipleObjects() {
        Source source = new Source();
        Target target = new Target();
        Target target2 = new Target();

        association0To1.removeTargetObjects(source, Arrays.<IModelObject> asList(target, target2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTargetObjects_NoMethod() {
        Source source = new Source();
        Target target = new Target();
        source.setTarget(target);

        association1To10.removeTargetObjects(source, target);
    }

    @Test
    public void testIsOverriding() throws Exception {
        PolicyCmptType subSource = IpsModel.getPolicyCmptType(SubSource.class);
        assertFalse(subSource.getAssociation("SubAsso").isOverriding());
        assertTrue(subSource.getAssociation("targets1toN").isOverriding());
    }

    @Test
    public void testGetSuperAssociation() throws Exception {
        PolicyCmptType subSource = IpsModel.getPolicyCmptType(SubSource.class);
        assertThat(subSource.getAssociation("SubAsso").getSuperAssociation(), is(nullValue()));
        assertSame(subSource.getAssociation("targets1toN").getSuperAssociation(),
                policyCmptType.getAssociation("targets1toN"));
    }

    @Test
    public void testGetDocumentation() {
        PolicyAssociation association = policyCmptType.getAssociation("targets1toN");
        PolicyAssociation subAssociation = IpsModel.getPolicyCmptType(SubSource.class).getAssociation("targets1toN");
        assertThat(association.getDescription(Locale.GERMAN), is("Description of targets1toN"));
        assertThat(subAssociation.getDescription(Locale.GERMAN), is("Description of targets1toN"));
    }

    @Test
    public void testIsQualified() {
        PolicyAssociation qualifiedAssociation = IpsModel.getPolicyCmptType(Source.class)
                .getAssociation("moreTargets1toN");
        assertThat(qualifiedAssociation.isQualified(), is(true));
        assertThat(association1ToN.isQualified(), is(false));
    }

    @IpsPolicyCmptType(name = "MySource")
    @IpsAssociations({ "asso", "asso2", "targets1toN", "moreTargets1toN" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class Source implements IModelObject {

        private Target target;
        private final List<Target> targets1toN = new ArrayList<>();
        private final List<Target> moreTargets1toN = new ArrayList<>();

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
        public List<? extends Target> getTargets1toN() {
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

        @IpsAssociation(name = "moreTargets1toN", pluralName = "moreTargets1toN", min = 0, max = Integer.MAX_VALUE, kind = AssociationKind.Composition, targetClass = Target.class, qualified = true)
        public List<? extends Target> getMoreTargets1toN() {
            return moreTargets1toN;
        }

        @IpsAssociationAdder(association = "moreTargets1toN")
        public void addMoreTargets1toN(Target objectToAdd) {
            addMoreTargets1toNInternal(objectToAdd);
        }

        // is generated if change listeners should be generated
        public void addMoreTargets1toNInternal(Target objectToAdd) {
            moreTargets1toN.add(objectToAdd);
        }

        @IpsAssociationRemover(association = "moreTargets1toN")
        public void removeMoreTargets1toN(Target objectToRemove) {
            moreTargets1toN.remove(objectToRemove);
        }
    }

    @IpsPolicyCmptType(name = "MySource")
    @IpsAssociations({ "SubAsso", "targets1toN", "asso", "asso2" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class SubSource extends Source {

        private Target target;

        @IpsAssociation(name = "SubAsso", pluralName = "subAassos", min = 0, max = 1, kind = AssociationKind.Association, targetClass = Target.class)
        public Target getSubAsso() {
            return target;
        }

        @IpsAssociationAdder(association = "SubAsso")
        public void setSubAsso(Target objectToAdd) {
            target = objectToAdd;
        }

        @Override
        @IpsAssociation(name = "targets1toN", pluralName = "targets", min = 0, max = Integer.MAX_VALUE, kind = AssociationKind.Composition, targetClass = Target.class)
        public List<? extends SubTarget> getTargets1toN() {
            return ListUtil.convert(super.getTargets1toN(), SubTarget.class);
        }

        @Override
        @IpsAssociationAdder(association = "targets1toN")
        public void addTargets1toN(Target objectToAdd) {
            ObjectUtil.checkInstanceOf(objectToAdd, SubTarget.class);
            super.addTargets1toN(objectToAdd);
        }

        @Override
        public void addMoreTargets1toNInternal(Target objectToAdd) {
            ObjectUtil.checkInstanceOf(objectToAdd, SubTarget.class);
            super.addMoreTargets1toNInternal(objectToAdd);
        }
    }

    @IpsPolicyCmptType(name = "MyTarget")
    private static class Target implements IModelObject {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MySubTarget")
    private static class SubTarget extends Target {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsAssociations({ "Matching" })
    private abstract static class Product extends ProductComponent {

        private Target target;

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @IpsAssociation(name = "Matching", pluralName = "Matching", min = 1, max = 10, kind = AssociationKind.Composition, targetClass = Target.class)
        public List<Target> getMatchings() {
            return Arrays.asList(target);
        }
    }

}
