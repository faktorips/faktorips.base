package org.faktorips.runtime.modeltype.internal;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.runtime.modeltype.IModelTypeAssociation.AssociationType;
import org.junit.Test;

public class ModelTypeTest {

    @Test
    public void testGetAssociations() throws Exception {
    }

    @IpsPolicyCmptType(name = "Source")
    @IpsAssociations({ "asso1", "asso2" })
    private static class Source implements IModelObject {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

        @IpsAssociation(name = "asso", pluralName = "assos", min = 1, max = 10, type = AssociationType.Association, targetClass = Target.class)
        @IpsDerivedUnion
        @IpsSubsetOfDerivedUnion("derivedUnion")
        public Target getTarget() {
            return null;
        }

    }

    private static class Target implements IModelObject {

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }

    }

}
