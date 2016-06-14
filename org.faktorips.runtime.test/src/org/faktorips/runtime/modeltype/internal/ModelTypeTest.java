package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.Models;
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
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAssociation.AssociationType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.junit.Test;

public class ModelTypeTest {

    @Test
    public void testGetAttributes() throws Exception {
        IModelType superSourceModelType = Models.getModelType(SuperSource.class);
        IModelType sourceModelType = Models.getModelType(Source.class);

        List<IModelTypeAttribute> superSourceAttributes = superSourceModelType.getAttributes();
        List<IModelTypeAttribute> sourceAttributes = sourceModelType.getAttributes();

        assertThat(superSourceAttributes.size(), is(2));
        assertThat(sourceAttributes.size(), is(3));
    }

    @Test
    public void testGetAttribute() throws Exception {
        IModelType superSourceModelType = Models.getModelType(SuperSource.class);
        IModelType sourceModelType = Models.getModelType(Source.class);

        IModelTypeAttribute superSuperAttribute = superSourceModelType.getAttribute("SuperAttribute");
        IModelTypeAttribute superOverwrittenAttribute = superSourceModelType.getAttribute("OverwrittenAttribute");
        IModelTypeAttribute overwrittenAttribute = sourceModelType.getAttribute("OverwrittenAttribute");
        IModelTypeAttribute attribute = sourceModelType.getAttribute("Attribute");
        IModelTypeAttribute superAttribute = superSourceModelType.getAttribute("SuperAttribute");

        assertThat(superSuperAttribute.getName(), is("SuperAttribute"));
        assertThat(superAttribute.getName(), is("SuperAttribute"));
        assertThat(superOverwrittenAttribute.getName(), is("OverwrittenAttribute"));
        assertThat(overwrittenAttribute.getName(), is("OverwrittenAttribute"));
        assertThat(superOverwrittenAttribute, not(overwrittenAttribute));
        assertThat(attribute.getName(), is("Attribute"));
    }

    @Test
    public void testGetAssociations() throws Exception {
        IModelType superSourceModelType = Models.getModelType(SuperSource.class);
        IModelType sourceModelType = Models.getModelType(Source.class);
        IModelType superTargetModelType = Models.getModelType(SuperTarget.class);
        IModelType targetModelType = Models.getModelType(Target.class);

        List<IModelTypeAssociation> superSourceAssociations = superSourceModelType.getAssociations();
        List<IModelTypeAssociation> sourceAssociations = sourceModelType.getAssociations();
        List<IModelTypeAssociation> superTargetAssociations = superTargetModelType.getAssociations();
        List<IModelTypeAssociation> targetAssociations = targetModelType.getAssociations();

        assertThat(superSourceAssociations.size(), is(2));
        assertThat(sourceAssociations.size(), is(3));
        assertThat(superTargetAssociations.size(), is(1));
        assertThat(targetAssociations.size(), is(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFakeAnnotation() throws Exception {
        Models.getModelType(Faker.class);
    }

    @IpsPublishedInterface(implementation = SuperSource.class)
    @IpsPolicyCmptType(name = "SuperSource")
    @IpsAssociations({ "SuperTarget", "Target" })
    @IpsAttributes({ "SuperAttribute", "OverwrittenAttribute" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ISuperSource extends IModelObject {

        @IpsAssociation(name = "SuperTarget", pluralName = "SuperTargets", type = AssociationType.Composition, targetClass = SuperTarget.class, min = 0, max = Integer.MAX_VALUE)
        @IpsDerivedUnion
        @IpsInverseAssociation("SuperSource")
        public List<ISuperTarget> getSuperTargets();

        @IpsAssociation(name = "Target", pluralName = "Targets", type = AssociationType.Composition, targetClass = Target.class, min = 0, max = Integer.MAX_VALUE)
        @IpsSubsetOfDerivedUnion("SuperTarget")
        @IpsInverseAssociation("SuperSource")
        public List<? extends ITarget> getTargets();

        @IpsAttribute(name = "SuperAttribute", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
        public int getSuperAttribute();

        @IpsAttribute(name = "OverwrittenAttribute", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
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
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ISource extends ISuperSource {
        @IpsAssociation(name = "AnotherTarget", pluralName = "", type = AssociationType.Composition, targetClass = Target.class, min = 0, max = 1)
        @IpsSubsetOfDerivedUnion("SuperTarget")
        @IpsInverseAssociation("Source")
        public ITarget getAnotherTarget();

        @IpsAttribute(name = "Attribute", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
        public int getAttribute();

        @Override
        @IpsAttribute(name = "OverwrittenAttribute", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
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
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ISuperTarget extends IModelObject {
        @IpsAssociation(name = "SuperSource", pluralName = "", type = AssociationType.CompositionToMaster, targetClass = SuperSource.class, min = 0, max = 1)
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
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test.model-label-and-descriptions", defaultLocale = "de")
    public static interface ITarget extends ISuperTarget {
        @IpsAssociation(name = "Source", pluralName = "", type = AssociationType.CompositionToMaster, targetClass = Source.class, min = 0, max = 1)
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
    @IpsDocumented(bundleName = "org.faktorips.runtime.modeltype.internal.test.model-label-and-descriptions", defaultLocale = "de")
    public static class Faker implements IModelObject {
        @IpsAssociation(name = "Not a real association", pluralName = "", type = AssociationType.Association, targetClass = Faker.class, min = 0, max = 0)
        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

    }

}
