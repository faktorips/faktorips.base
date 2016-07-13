package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation.AssociationType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.IProductAssociationModel;
import org.junit.Test;

public class ProductModelTest {

    private final IProductModel productModel = Models.getProductModel(Product.class);

    private final IProductModel superProductModel = Models.getProductModel(SuperProduct.class);

    @Test
    public void testGetName() throws Exception {
        assertThat(productModel.getName(), is("MyProduct"));
        assertThat(superProductModel.getName(), is("MySuperProduct"));
    }

    @Test
    public void testGetSuperType() throws Exception {
        assertThat(productModel.getSuperType().getName(), is(superProductModel.getName()));
        assertThat(superProductModel.getSuperType(), is(nullValue()));
    }

    @Test
    public void testIsConfiguredByProductCmptType() throws Exception {
        assertThat(productModel.isConfigurationForPolicyCmptType(), is(true));
        assertThat(superProductModel.isConfigurationForPolicyCmptType(), is(false));
    }

    @Test
    public void testGetPolicyCmptType() throws Exception {
        assertThat(productModel.getPolicyCmptType().getName(), is("MyPolicy"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetPolicyCmptType_NPE_NotConfigured() throws Exception {
        assertThat(superProductModel.getPolicyCmptType().getName(), is(nullValue()));
    }

    @Test
    public void testIsChangingOverTime() throws Exception {
        assertThat(productModel.isChangingOverTime(), is(true));
        assertThat(superProductModel.isChangingOverTime(), is(false));
    }

    @Test
    public void testGetDeclaredAttributes() {
        assertThat(productModel.getDeclaredAttributes().size(), is(2));
        assertThat(productModel.getDeclaredAttributes().get(0).getName(), is("attr"));
        assertThat(productModel.getDeclaredAttributes().get(1).getName(), is("attr_changing"));
    }

    @Test
    public void testGetAttributes() {
        assertThat(productModel.getAttributes().size(), is(3));
        assertThat(productModel.getAttributes().get(0).getName(), is("attr"));
        assertThat(productModel.getAttributes().get(1).getName(), is("attr_changing"));
        assertThat(productModel.getAttributes().get(2).getName(), is("supAttr"));
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(productModel.getDeclaredAssociations().size(), is(2));
        assertThat(productModel.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertThat(productModel.getDeclaredAssociations().get(1).getName(), is("asso_changing"));
        assertThat(superProductModel.getDeclaredAssociations().size(), is(1));
        assertThat(superProductModel.getDeclaredAssociations().get(0).getName(), is("supAsso"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(productModel.getAssociations().size(), is(3));
        assertThat(productModel.getAssociations().get(0).getName(), is("asso"));
        assertThat(productModel.getAssociations().get(1).getName(), is("asso_changing"));
        assertThat(productModel.getAssociations().get(2).getName(), is("supAsso"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        IProductAssociationModel association = productModel.getDeclaredAssociation("asso");
        IProductAssociationModel superAsso = productModel.getDeclaredAssociation("supAsso");
        IProductAssociationModel superAssoInSuper = superProductModel.getDeclaredAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso, is(nullValue()));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetDeclaredAssociation_Plural() {
        IProductAssociationModel association = productModel.getDeclaredAssociation("assos");
        IProductAssociationModel superAsso = productModel.getDeclaredAssociation("supAssos");
        IProductAssociationModel superAssoInSuper = superProductModel.getDeclaredAssociation("supAssos");

        assertThat(association, is(nullValue()));
        assertThat(superAsso, is(nullValue()));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation() {
        IProductAssociationModel association = productModel.getAssociation("asso");
        IProductAssociationModel superAsso = productModel.getAssociation("supAsso");
        IProductAssociationModel superAssoInSuper = superProductModel.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso.getName(), is("supAsso"));
        assertThat(superAsso.getNamePlural(), is("supAssos"));
        assertThat(superAssoInSuper.getName(), is("supAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("supAssos"));
    }

    @Test
    public void testGetAssociation_Plural() {
        IProductAssociationModel superAsso = productModel.getAssociation("supAssos");
        IProductAssociationModel superAssoInSuper = superProductModel.getAssociation("supAssos");

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

        @IpsAttribute(name = "attr", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso", min = 1, max = 2, targetClass = Product.class, type = AssociationType.Association)
        public abstract Product getAsso();

    }

    private static abstract class ProductGen extends ProductComponentGeneration {

        public ProductGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @IpsAttribute(name = "attr_changing", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
        public abstract String getAttr();

        @IpsAttributeSetter("attr_changing")
        public abstract void setAttr(String attr);

        @IpsAssociation(name = "asso_changing", min = 1, max = 2, targetClass = Product.class, type = AssociationType.Association)
        public abstract ProductGen getAsso_changing();

    }

    @IpsProductCmptType(name = "MySuperProduct")
    @IpsAttributes({ "supAttr" })
    @IpsAssociations({ "supAsso" })
    private static abstract class SuperProduct extends ProductComponent {

        @IpsAttribute(name = "supAttr", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
        public int getSup() {
            return 1;
        }

        @IpsAssociation(name = "supAsso", pluralName = "supAssos", max = 5, min = 1, targetClass = SuperProduct.class, type = AssociationType.Association)
        public abstract SuperProduct getSupAsso();

        public SuperProduct(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

    }

    @IpsPolicyCmptType(name = "MyPolicy")
    private static abstract class Policy extends AbstractModelObject implements IConfigurableModelObject {

    }

}
