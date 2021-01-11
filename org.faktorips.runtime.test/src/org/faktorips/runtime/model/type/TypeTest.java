package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.junit.Test;

public class TypeTest {

    @Test
    public void testGetAttributes() throws Exception {
        Type superSourceModelType = IpsModel.getType(SuperSource.class);
        Type sourceModelType = IpsModel.getType(Source.class);

        List<? extends Attribute> superSourceAttributes = superSourceModelType.getAttributes();
        List<? extends Attribute> sourceAttributes = sourceModelType.getAttributes();

        assertThat(superSourceAttributes.size(), is(2));
        assertThat(sourceAttributes.size(), is(3));
    }

    @Test
    public void testGetAttribute() throws Exception {
        Type superSourceModelType = IpsModel.getType(SuperSource.class);
        Type sourceModelType = IpsModel.getType(Source.class);

        Attribute superSuperAttribute = superSourceModelType.getAttribute("SuperAttribute");
        Attribute superOverwrittenAttribute = superSourceModelType.getAttribute("OverwrittenAttribute");
        Attribute overwrittenAttribute = sourceModelType.getAttribute("OverwrittenAttribute");
        Attribute attribute = sourceModelType.getAttribute("Attribute");
        Attribute superAttribute = superSourceModelType.getAttribute("SuperAttribute");

        assertThat(superSuperAttribute.getName(), is("SuperAttribute"));
        assertThat(superAttribute.getName(), is("SuperAttribute"));
        assertThat(superOverwrittenAttribute.getName(), is("OverwrittenAttribute"));
        assertThat(overwrittenAttribute.getName(), is("OverwrittenAttribute"));
        assertThat(superOverwrittenAttribute, not(overwrittenAttribute));
        assertThat(attribute.getName(), is("Attribute"));
    }

    @Test
    public void testGetAssociations() throws Exception {
        Type superSourceModelType = IpsModel.getType(SuperSource.class);
        Type sourceModelType = IpsModel.getType(Source.class);
        Type superTargetModelType = IpsModel.getType(SuperTarget.class);
        Type targetModelType = IpsModel.getType(Target.class);

        List<? extends Association> superSourceAssociations = superSourceModelType.getAssociations();
        List<? extends Association> sourceAssociations = sourceModelType.getAssociations();
        List<? extends Association> superTargetAssociations = superTargetModelType.getAssociations();
        List<? extends Association> targetAssociations = targetModelType.getAssociations();

        assertThat(superSourceAssociations.size(), is(2));
        assertThat(sourceAssociations.size(), is(3));
        assertThat(superTargetAssociations.size(), is(1));
        assertThat(targetAssociations.size(), is(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFakeAnnotation() throws Exception {
        IpsModel.getType(Faker.class);
    }

    @Test
    public void testSearchDeclaredMethod_exists() {
        Type type = IpsModel.getType(ISource.class);

        assertNotNull(type.searchDeclaredMethod(IpsAttribute.class, a -> a.name().equals("Attribute")));
    }

    @Test
    public void testSearchDeclaredMethod_missing() {
        Type type = IpsModel.getType(ISource.class);

        assertNull(type.searchDeclaredMethod(IpsAttribute.class,
                a -> a.name().equals("Attribute") && AttributeKind.CONSTANT == a.kind()));
    }

    @IpsPublishedInterface(implementation = SuperSource.class)
    @IpsPolicyCmptType(name = "SuperSource")
    @IpsAssociations({ "SuperTarget", "Target" })
    @IpsAttributes({ "SuperAttribute", "OverwrittenAttribute" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ISuperSource extends IModelObject {

        @IpsAssociation(name = "SuperTarget", pluralName = "SuperTargets", kind = AssociationKind.Composition, targetClass = SuperTarget.class, min = 0, max = Integer.MAX_VALUE)
        @IpsDerivedUnion
        @IpsInverseAssociation("SuperSource")
        public List<ISuperTarget> getSuperTargets();

        @IpsAssociation(name = "Target", pluralName = "Targets", kind = AssociationKind.Composition, targetClass = Target.class, min = 0, max = Integer.MAX_VALUE)
        @IpsSubsetOfDerivedUnion("SuperTarget")
        @IpsInverseAssociation("SuperSource")
        public List<? extends ITarget> getTargets();

        @IpsAttribute(name = "SuperAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getSuperAttribute();

        @IpsAttribute(name = "OverwrittenAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getOverwrittenAttribute();

    }

    public static abstract class SuperSource implements ISuperSource {

        private Target target;

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @Override
        public List<ISuperTarget> getSuperTargets() {
            return Arrays.asList((ISuperTarget)target);
        }

        @Override
        public List<? extends ITarget> getTargets() {
            return Arrays.asList(target);
        }

    }

    @IpsPublishedInterface(implementation = Source.class)
    @IpsPolicyCmptType(name = "Source")
    @IpsAssociations({ "AnotherTarget" })
    @IpsAttributes({ "OverwrittenAttribute", "Attribute" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ISource extends ISuperSource {
        @IpsAssociation(name = "AnotherTarget", pluralName = "", kind = AssociationKind.Composition, targetClass = Target.class, min = 0, max = 1)
        @IpsSubsetOfDerivedUnion("SuperTarget")
        @IpsInverseAssociation("Source")
        public ITarget getAnotherTarget();

        @IpsAttribute(name = "Attribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getAttribute();

        @Override
        @IpsAttribute(name = "OverwrittenAttribute", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getOverwrittenAttribute();

    }

    public static abstract class Source extends SuperSource implements ISource {

        private Target target;

        @Override
        public ITarget getAnotherTarget() {
            return target;
        }

    }

    @IpsPublishedInterface(implementation = SuperTarget.class)
    @IpsPolicyCmptType(name = "SuperTarget")
    @IpsAssociations({ "SuperSource" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ISuperTarget extends IModelObject {
        @IpsAssociation(name = "SuperSource", pluralName = "", kind = AssociationKind.CompositionToMaster, targetClass = SuperSource.class, min = 0, max = 1)
        @IpsInverseAssociation("SuperTarget")
        public ISuperSource getSuperSource();

    }

    public static abstract class SuperTarget implements ISuperTarget {

        private SuperSource superSource;

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @Override
        public ISuperSource getSuperSource() {
            return superSource;
        }

    }

    @IpsPublishedInterface(implementation = Target.class)
    @IpsPolicyCmptType(name = "Target")
    @IpsAssociations({ "SuperSource", "Source" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ITarget extends ISuperTarget {
        @IpsAssociation(name = "Source", pluralName = "", kind = AssociationKind.CompositionToMaster, targetClass = Source.class, min = 0, max = 1)
        @IpsInverseAssociation("AnotherTarget")
        public ISource getSource();

    }

    public static abstract class Target extends SuperTarget implements ITarget {

        private Source source;
        private SuperSource superSource;

        @Override
        public ISource getSource() {
            return source;
        }

        @Override
        public ISuperSource getSuperSource() {
            if (superSource != null) {
                return superSource;
            }
            if (source != null) {
                return source;
            }
            return null;
        }
    }

    @IpsPolicyCmptType(name = "Faker")
    @IpsAssociations({})
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test.model-label-and-descriptions", defaultLocale = "de")
    public static class Faker implements IModelObject {
        @IpsAssociation(name = "Not a real association", pluralName = "", kind = AssociationKind.Association, targetClass = Faker.class, min = 0, max = 0)
        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

    }
}
