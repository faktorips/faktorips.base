package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
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
        assertThat(policyCmptType.isConfiguredByProductCmptType(), is(true));
        assertThat(superPolicyCmptType.isConfiguredByProductCmptType(), is(false));
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
        assertThat(policyCmptType.getDeclaredAttributes().size(), is(3));
        assertThat(policyCmptType.getDeclaredAttributes().get(1).getName(), is("const"));
    }

    @Test
    public void testGetDeclaredAttribute() {
        assertNotNull(policyCmptType.getDeclaredAttribute("attr"));
        assertEquals("CapitalAttr", policyCmptType.getDeclaredAttribute("CapitalAttr").getName());
        assertEquals("CapitalAttr", policyCmptType.getDeclaredAttribute("capitalAttr").getName());
    }

    @Test
    public void testGetAttributes() {
        assertThat(policyCmptType.getAttributes().size(), is(4));
        assertThat(policyCmptType.getAttributes().get(0).getName(), is("attr"));
    }

    @Test
    public void testGetAttribute() {
        assertNotNull(policyCmptType.getAttribute("supAttr"));
        assertEquals("CapitalAttr", policyCmptType.getAttribute("capitalAttr").getName());
        assertEquals("CapitalAttr", policyCmptType.getAttribute("CapitalAttr").getName());
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(policyCmptType.getDeclaredAssociations().size(), is(2));
        assertThat(policyCmptType.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertNotNull(policyCmptType.getDeclaredAssociation("asso"));
        assertThat(policyCmptType.getDeclaredAssociations().get(1).getName(), is("Asso2"));
        assertNotNull(policyCmptType.getDeclaredAssociation("asso2"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        PolicyAssociation association = policyCmptType.getDeclaredAssociation("asso");
        PolicyAssociation association2 = policyCmptType.getDeclaredAssociation("Asso2");
        PolicyAssociation association2lowerCase = policyCmptType.getDeclaredAssociation("asso2");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getDeclaredAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(association2.getName(), is("Asso2"));
        assertThat(association2.getNamePlural(), is("Asso2s"));
        assertSame(association2, association2lowerCase);
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDeclaredAssociation_thowsException() {
        policyCmptType.getDeclaredAssociation("supAsso");
    }

    @Test
    public void testGetDeclaredAssociation_Plural() {
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getDeclaredAssociation("supAssos");

        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation() {
        PolicyAssociation association = policyCmptType.getAssociation("asso");
        PolicyAssociation association2 = policyCmptType.getAssociation("asso2");
        PolicyAssociation superAsso = policyCmptType.getAssociation("supAsso");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(association2.getName(), is("Asso2"));
        assertThat(association2.getNamePlural(), is("Asso2s"));
        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation_Plural() {
        PolicyAssociation superAsso = policyCmptType.getAssociation("supAssos");
        PolicyAssociation superAssoInSuper = superPolicyCmptType.getAssociation("supAssos");
        PolicyAssociation association2 = policyCmptType.getAssociation("asso2s");

        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
        assertThat(association2.getName(), is("Asso2"));
        assertThat(association2.getNamePlural(), is("Asso2s"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(policyCmptType.getAssociations().size(), is(3));
        assertThat(policyCmptType.getAssociations().get(0).getName(), is("asso"));
        assertThat(policyCmptType.getAssociations().get(1).getName(), is("Asso2"));
        assertThat(policyCmptType.getAssociations().get(2).getName(), is("supAsso"));
        assertNotNull(policyCmptType.getAssociation("supAsso"));
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsConfiguredBy(Product.class)
    @IpsAttributes({ "attr", "const", "CapitalAttr" })
    @IpsAssociations({ "asso", "Asso2" })
    private static abstract class Policy extends SuperPolicy {

        @IpsAttribute(name = "const", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public static final int CONST = 2;

        @IpsAttribute(name = "attr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)
        public abstract String getAttr();

        @IpsAttribute(name = "CapitalAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)
        public abstract String getCapitalAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso", max = 0, min = 0, targetClass = Policy.class, kind = AssociationKind.Composition)
        public abstract Policy getPartPolicy();

        @IpsAssociation(name = "Asso2", pluralName = "Asso2s", max = 5, min = 0, targetClass = Policy.class, kind = AssociationKind.Composition)
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
