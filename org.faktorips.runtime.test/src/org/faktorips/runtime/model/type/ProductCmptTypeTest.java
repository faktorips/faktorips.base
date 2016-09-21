package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.junit.Test;

public class ProductCmptTypeTest {

    private final ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

    private final ProductCmptType superProductModel = IpsModel.getProductCmptType(SuperProduct.class);

    @Test
    public void testGetName() throws Exception {
        assertThat(productCmptType.getName(), is("MyProduct"));
        assertThat(superProductModel.getName(), is("MySuperProduct"));
    }

    @Test
    public void testGetSuperType() throws Exception {
        assertThat(productCmptType.getSuperType().getName(), is(superProductModel.getName()));
        assertThat(superProductModel.getSuperType(), is(nullValue()));
    }

    @Test
    public void testIsConfiguredByProductCmptType() throws Exception {
        assertThat(productCmptType.isConfigurationForPolicyCmptType(), is(true));
        assertThat(superProductModel.isConfigurationForPolicyCmptType(), is(false));
    }

    @Test
    public void testGetPolicyCmptType() throws Exception {
        assertThat(productCmptType.getPolicyCmptType().getName(), is("MyPolicy"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetPolicyCmptType_NPE_NotConfigured() throws Exception {
        assertThat(superProductModel.getPolicyCmptType().getName(), is(nullValue()));
    }

    @Test
    public void testIsChangingOverTime() throws Exception {
        assertThat(productCmptType.isChangingOverTime(), is(true));
        assertThat(superProductModel.isChangingOverTime(), is(false));
    }

    @Test
    public void testGetDeclaredAttributes() {
        assertThat(productCmptType.getDeclaredAttributes().size(), is(2));
        assertThat(productCmptType.getDeclaredAttributes().get(0).getName(), is("attr"));
        assertThat(productCmptType.getDeclaredAttributes().get(1).getName(), is("attr_changing"));
    }

    @Test
    public void testGetAttributes() {
        assertThat(productCmptType.getAttributes().size(), is(3));
        assertThat(productCmptType.getAttributes().get(0).getName(), is("attr"));
        assertThat(productCmptType.getAttributes().get(1).getName(), is("attr_changing"));
        assertThat(productCmptType.getAttributes().get(2).getName(), is("supAttr"));
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(productCmptType.getDeclaredAssociations().size(), is(2));
        assertThat(productCmptType.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertThat(productCmptType.getDeclaredAssociations().get(1).getName(), is("asso_changing"));
        assertThat(superProductModel.getDeclaredAssociations().size(), is(1));
        assertThat(superProductModel.getDeclaredAssociations().get(0).getName(), is("supAsso"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(productCmptType.getAssociations().size(), is(3));
        assertThat(productCmptType.getAssociations().get(0).getName(), is("asso"));
        assertThat(productCmptType.getAssociations().get(1).getName(), is("asso_changing"));
        assertThat(productCmptType.getAssociations().get(2).getName(), is("supAsso"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        ProductAssociation association = productCmptType.getDeclaredAssociation("asso");
        ProductAssociation superAsso = productCmptType.getDeclaredAssociation("supAsso");
        ProductAssociation superAssoInSuper = superProductModel.getDeclaredAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso, is(nullValue()));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetDeclaredAssociation_Plural() {
        ProductAssociation association = productCmptType.getDeclaredAssociation("assos");
        ProductAssociation superAsso = productCmptType.getDeclaredAssociation("supAssos");
        ProductAssociation superAssoInSuper = superProductModel.getDeclaredAssociation("supAssos");

        assertThat(association, is(nullValue()));
        assertThat(superAsso, is(nullValue()));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation() {
        ProductAssociation association = productCmptType.getAssociation("asso");
        ProductAssociation superAsso = productCmptType.getAssociation("supAsso");
        ProductAssociation superAssoInSuper = superProductModel.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation_Plural() {
        ProductAssociation superAsso = productCmptType.getAssociation("supAssos");
        ProductAssociation superAssoInSuper = superProductModel.getAssociation("supAssos");

        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsConfigures(Policy.class)
    @IpsChangingOverTime(ProductGen.class)
    @IpsAttributes({ "attr", "attr_changing" })
    @IpsAssociations({ "asso", "asso_changing" })
    private static abstract class Product extends SuperProduct {

        public Product(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

        @IpsAttribute(name = "attr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso", min = 1, max = 2, targetClass = Product.class, kind = AssociationKind.Association)
        public abstract Product getAsso();

    }

    private static abstract class ProductGen extends ProductComponentGeneration {

        public ProductGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @IpsAttribute(name = "attr_changing", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getAttr();

        @IpsAttributeSetter("attr_changing")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso_changing", min = 1, max = 2, targetClass = Product.class, kind = AssociationKind.Association)
        public abstract ProductGen getAsso_changing();

    }

    @IpsProductCmptType(name = "MySuperProduct")
    @IpsAttributes({ "supAttr" })
    @IpsAssociations({ "supAsso" })
    private static abstract class SuperProduct extends ProductComponent {

        @IpsAttribute(name = "supAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getSup() {
            return 1;
        }

        @IpsAssociation(name = "supAsso", pluralName = "supAssos", max = 5, min = 1, targetClass = SuperProduct.class, kind = AssociationKind.Association)
        public abstract SuperProduct getSupAsso();

        public SuperProduct(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

    }

    @IpsPolicyCmptType(name = "MyPolicy")
    private static abstract class Policy extends AbstractModelObject implements IConfigurableModelObject {

    }

}
