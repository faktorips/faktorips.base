package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
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
import org.faktorips.runtime.modeltype.IPolicyModelAssociation;
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
        assertThat(policyModel.getDeclaredAssociations().size(), is(2));
        assertThat(policyModel.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertNotNull(policyModel.getDeclaredAssociation("asso"));
        assertThat(policyModel.getDeclaredAssociations().get(1).getName(), is("asso2"));
        assertNotNull(policyModel.getDeclaredAssociation("asso2"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        IPolicyModelAssociation association = policyModel.getDeclaredAssociation("asso");
        IPolicyModelAssociation association2 = policyModel.getDeclaredAssociation("asso2");
        IPolicyModelAssociation superAsso = policyModel.getDeclaredAssociation("supAsso");
        IPolicyModelAssociation superAssoInSuper = superPolicyModel.getDeclaredAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(association2.getName(), is("asso2"));
        assertThat(association2.getNamePlural(), is("asso2s"));
        assertThat(superAsso, is(nullValue()));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetDeclaredAssociation_Plural() {
        IPolicyModelAssociation association = policyModel.getDeclaredAssociation("assos");
        IPolicyModelAssociation superAsso = policyModel.getDeclaredAssociation("supAssos");
        IPolicyModelAssociation superAssoInSuper = superPolicyModel.getDeclaredAssociation("supAssos");

        assertThat(association, is(nullValue()));
        assertThat(superAsso, is(nullValue()));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation() {
        IPolicyModelAssociation association = policyModel.getAssociation("asso");
        IPolicyModelAssociation superAsso = policyModel.getAssociation("supAsso");
        IPolicyModelAssociation superAssoInSuper = superPolicyModel.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation_Plural() {
        IPolicyModelAssociation superAsso = policyModel.getAssociation("supAssos");
        IPolicyModelAssociation superAssoInSuper = superPolicyModel.getAssociation("supAssos");

        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(policyModel.getAssociations().size(), is(3));
        assertThat(policyModel.getAssociations().get(0).getName(), is("asso"));
        assertThat(policyModel.getAssociations().get(1).getName(), is("asso2"));
        assertThat(policyModel.getAssociations().get(2).getName(), is("supAsso"));
        assertNotNull(policyModel.getAssociation("supAsso"));
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsConfiguredBy(Product.class)
    @IpsAttributes({ "attr", "const" })
    @IpsAssociations({ "asso", "asso2" })
    private static abstract class Policy extends SuperPolicy {

        @IpsAttribute(name = "const", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public static final int CONST = 2;

        @IpsAttribute(name = "attr", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Range)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso", max = 0, min = 0, targetClass = Policy.class, type = AssociationType.Composition)
        public abstract Policy getPartPolicy();

        @IpsAssociation(name = "asso2", pluralName = "asso2s", max = 5, min = 0, targetClass = Policy.class, type = AssociationType.Composition)
        public abstract List<Policy> getTargets();
    }

    @IpsPolicyCmptType(name = "MySuperPolicy")
    @IpsAttributes({ "supAttr" })
    @IpsAssociations({ "supAsso" })
    private static abstract class SuperPolicy extends AbstractModelObject {

        @IpsAttribute(name = "supAttr", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public static final int supAttr = 5;

        @IpsAssociation(name = "supAsso", pluralName = "supAssos", max = 0, min = 0, targetClass = SuperPolicy.class, type = AssociationType.Composition)
        public abstract SuperPolicy getPartSuperPolicy();
    }

    @IpsProductCmptType(name = "MyProduct")
    private static abstract class Product extends ProductComponent {

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

    }

}
