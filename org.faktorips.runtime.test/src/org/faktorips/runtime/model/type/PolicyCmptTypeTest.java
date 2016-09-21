package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.junit.Test;

public class PolicyCmptTypeTest {

    private final PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(Policy.class);

    private final PolicyCmptType superPolicyCmptType = IpsModel.getPolicyCmptType(SuperPolicy.class);

    @Test
    public void testGetName() throws Exception {
        assertThat(policyCmptType.getName(), is("MyPolicy"));
        assertThat(superPolicyCmptType.getName(), is("MySuperPolicy"));
    }

    @Test
    public void testGetSuperType() throws Exception {
        assertThat(policyCmptType.getSuperType().getName(), is(superPolicyCmptType.getName()));
        assertThat(superPolicyCmptType.getSuperType(), is(nullValue()));
    }

    @Test
    public void testIsConfiguredByPolicyCmptType() throws Exception {
        assertThat(policyCmptType.isConfiguredByPolicyCmptType(), is(true));
        assertThat(superPolicyCmptType.isConfiguredByPolicyCmptType(), is(false));
    }

    @Test
    public void testGetProductCmptType() throws Exception {
        assertThat(policyCmptType.getProductCmptType().getName(), is("MyProduct"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetProductCmptType_NPE_NotConfigured() throws Exception {
        assertThat(superPolicyCmptType.getProductCmptType().getName(), is(nullValue()));
    }

    @Test
    public void testGetDeclaredAttributes() {
        assertThat(policyCmptType.getDeclaredAttributes().size(), is(2));
        assertThat(policyCmptType.getDeclaredAttributes().get(1).getName(), is("const"));
        assertNotNull(policyCmptType.getDeclaredAttribute("attr"));
    }

    @Test
    public void testGetAttributes() {
        assertThat(policyCmptType.getAttributes().size(), is(3));
        assertThat(policyCmptType.getAttributes().get(0).getName(), is("attr"));
        assertNotNull(policyCmptType.getAttribute("supAttr"));
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(policyCmptType.getDeclaredAssociations().size(), is(2));
        assertThat(policyCmptType.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertNotNull(policyCmptType.getDeclaredAssociation("asso"));
        assertThat(policyCmptType.getDeclaredAssociations().get(1).getName(), is("asso2"));
        assertNotNull(policyCmptType.getDeclaredAssociation("asso2"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        PolicyAssociation association = policyCmptType.getDeclaredAssociation("asso");
        PolicyAssociation association2 = policyCmptType.getDeclaredAssociation("asso2");
        PolicyAssociation superAsso = policyCmptType.getDeclaredAssociation("supAsso");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getDeclaredAssociation("supAsso");

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
        PolicyAssociation association = policyCmptType.getDeclaredAssociation("assos");
        PolicyAssociation superAsso = policyCmptType.getDeclaredAssociation("supAssos");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getDeclaredAssociation("supAssos");

        assertThat(association, is(nullValue()));
        assertThat(superAsso, is(nullValue()));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation() {
        PolicyAssociation association = policyCmptType.getAssociation("asso");
        PolicyAssociation superAsso = policyCmptType.getAssociation("supAsso");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation_Plural() {
        PolicyAssociation superAsso = policyCmptType.getAssociation("supAssos");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getAssociation("supAssos");

        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(policyCmptType.getAssociations().size(), is(3));
        assertThat(policyCmptType.getAssociations().get(0).getName(), is("asso"));
        assertThat(policyCmptType.getAssociations().get(1).getName(), is("asso2"));
        assertThat(policyCmptType.getAssociations().get(2).getName(), is("supAsso"));
        assertNotNull(policyCmptType.getAssociation("supAsso"));
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsConfiguredBy(Product.class)
    @IpsAttributes({ "attr", "const" })
    @IpsAssociations({ "asso", "asso2" })
    private static abstract class Policy extends SuperPolicy {

        @IpsAttribute(name = "const", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final int CONST = 2;

        @IpsAttribute(name = "attr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso", max = 0, min = 0, targetClass = Policy.class, kind = AssociationKind.Composition)
        public abstract Policy getPartPolicy();

        @IpsAssociation(name = "asso2", pluralName = "asso2s", max = 5, min = 0, targetClass = Policy.class, kind = AssociationKind.Composition)
        public abstract List<Policy> getTargets();
    }

    @IpsPolicyCmptType(name = "MySuperPolicy")
    @IpsAttributes({ "supAttr" })
    @IpsAssociations({ "supAsso" })
    private static abstract class SuperPolicy extends AbstractModelObject {

        @IpsAttribute(name = "supAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final int supAttr = 5;

        @IpsAssociation(name = "supAsso", pluralName = "supAssos", max = 0, min = 0, targetClass = SuperPolicy.class, kind = AssociationKind.Composition)
        public abstract SuperPolicy getPartSuperPolicy();
    }

    @IpsProductCmptType(name = "MyProduct")
    private static abstract class Product extends ProductComponent {

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

    }

}
