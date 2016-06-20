package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation.AssociationType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.junit.Test;

public class PolicyModelTest {

    private final IPolicyModel policyModel = Models.getPolicyModel(Policy.class);

    private final IPolicyModel superPolicyModel = Models.getPolicyModel(SuperPolicy.class);

    @Test
    public void testGetName() throws Exception {
        assertThat(policyModel.getName(), is("MyPolicy"));
        assertThat(superPolicyModel.getName(), is("MySuperPolicy"));
    }

    @Test
    public void testGetSuperType() throws Exception {
        assertThat(policyModel.getSuperType().getName(), is(superPolicyModel.getName()));
        assertThat(superPolicyModel.getSuperType(), is(nullValue()));
    }

    @Test
    public void testIsConfiguredByPolicyCmptType() throws Exception {
        assertThat(policyModel.isConfiguredByPolicyCmptType(), is(true));
        assertThat(superPolicyModel.isConfiguredByPolicyCmptType(), is(false));
    }

    @Test
    public void testGetProductCmptType() throws Exception {
        assertThat(policyModel.getProductCmptType().getName(), is("MyProduct"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetProductCmptType_NPE_NotConfigured() throws Exception {
        assertThat(superPolicyModel.getProductCmptType().getName(), is(nullValue()));
    }

    @Test
    public void testGetDeclaredAttributes() {
        assertThat(policyModel.getDeclaredAttributes().size(), is(2));
        assertThat(policyModel.getDeclaredAttributes().get(1).getName(), is("const"));
        assertNotNull(policyModel.getDeclaredAttribute("attr"));
    }

    @Test
    public void testGetAttributes() {
        assertThat(policyModel.getAttributes().size(), is(3));
        assertThat(policyModel.getAttributes().get(0).getName(), is("attr"));
        assertNotNull(policyModel.getAttribute("supAttr"));
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(policyModel.getDeclaredAssociations().size(), is(1));
        assertThat(policyModel.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertNotNull(policyModel.getDeclaredAssociation("asso"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(policyModel.getAssociations().size(), is(2));
        assertThat(policyModel.getAssociations().get(0).getName(), is("asso"));
        assertNotNull(policyModel.getAssociation("supAsso"));
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsConfiguredBy(Product.class)
    @IpsAttributes({ "attr", "const" })
    @IpsAssociations({ "asso" })
    private static abstract class Policy extends SuperPolicy {

        @IpsAttribute(name = "const", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public static final int CONST = 2;

        @IpsAttribute(name = "attr", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Range)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso", max = 0, min = 0, targetClass = Policy.class, type = AssociationType.Composition)
        public abstract Policy getPartPolicy();
    }

    @IpsPolicyCmptType(name = "MySuperPolicy")
    @IpsAttributes({ "supAttr" })
    @IpsAssociations({ "supAsso" })
    private static abstract class SuperPolicy extends AbstractModelObject {

        @IpsAttribute(name = "supAttr", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public static final int supAttr = 5;

        @IpsAssociation(name = "supAsso", max = 0, min = 0, targetClass = SuperPolicy.class, type = AssociationType.Composition)
        public abstract SuperPolicy getPartSuperPolicy();
    }

    @IpsProductCmptType(name = "MyProduct")
    private static abstract class Product extends ProductComponent {

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

    }

}
