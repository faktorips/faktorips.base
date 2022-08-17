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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
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
    public void testFindSuperType() throws Exception {
        assertThat(productCmptType.findSuperType().map(Type::getName).get(), is(superProductModel.getName()));
        assertThat(superProductModel.findSuperType().isPresent(), is(false));
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
        assertThat(productCmptType.getDeclaredAttributes().size(), is(5));
        assertThat(productCmptType.getDeclaredAttributes().get(0).getName(), is("attr"));
        assertThat(productCmptType.getDeclaredAttributes().get(1).getName(), is("overwrittenAttr"));
        assertThat(productCmptType.getDeclaredAttributes().get(2).getName(), is("BigAttr"));
        assertThat(productCmptType.getDeclaredAttributes().get(3).getName(), is("attr_changing"));
        assertThat(productCmptType.getDeclaredAttributes().get(4).getName(), is("abstractSuper"));
    }

    @Test
    public void testGetDeclaredAttribute_FindsCorrectCovariantReturnType() {
        assertThat(superProductModel.getDeclaredAttribute("abstractSuper").getDatatype().getName(),
                is(AbstractEnum.class.getName()));
        assertThat(productCmptType.getDeclaredAttribute("abstractSuper").getDatatype().getName(),
                is(SubOfAbstractEnum.class.getName()));
    }

    @Test
    public void testGetDeclaredAttribute() {
        assertEquals("attr", productCmptType.getDeclaredAttribute("attr").getName());
        assertEquals("attr", productCmptType.getDeclaredAttribute("Attr").getName());
        assertEquals("BigAttr", productCmptType.getDeclaredAttribute("BigAttr").getName());
        assertEquals("BigAttr", productCmptType.getDeclaredAttribute("bigAttr").getName());
    }

    @Test
    public void testGetAttributes() {
        assertThat(productCmptType.getAttributes().size(), is(6));
        assertThat(productCmptType.getAttributes().get(0).getName(), is("attr"));
        assertThat(productCmptType.getAttributes().get(1).getName(), is("overwrittenAttr"));
        assertThat(productCmptType.getAttributes().get(2).getName(), is("BigAttr"));
        assertThat(productCmptType.getAttributes().get(3).getName(), is("attr_changing"));
        assertThat(productCmptType.getAttributes().get(4).getName(), is("abstractSuper"));
        assertThat(productCmptType.getAttributes().get(5).getName(), is("supAttr"));
    }

    @Test
    public void testGetAttribute() {
        assertEquals("attr", productCmptType.getAttribute("attr").getName());
        assertEquals("attr", productCmptType.getAttribute("Attr").getName());
        assertEquals("BigAttr", productCmptType.getAttribute("BigAttr").getName());
        assertEquals("BigAttr", productCmptType.getAttribute("bigAttr").getName());
        assertEquals("supAttr", productCmptType.getAttribute("supAttr").getName());
    }

    @Test
    public void testGetDeclaredAssociations() {
        assertThat(productCmptType.getDeclaredAssociations().size(), is(2));
        assertThat(productCmptType.getDeclaredAssociations().get(0).getName(), is("asso"));
        assertThat(productCmptType.getDeclaredAssociations().get(1).getName(), is("asso_changing"));
        assertThat(superProductModel.getDeclaredAssociations().size(), is(1));
        assertThat(superProductModel.getDeclaredAssociations().get(0).getName(), is("SupAsso"));
    }

    @Test
    public void testGetAssociations() {
        assertThat(productCmptType.getAssociations().size(), is(3));
        assertThat(productCmptType.getAssociations().get(0).getName(), is("asso"));
        assertThat(productCmptType.getAssociations().get(1).getName(), is("asso_changing"));
        assertThat(productCmptType.getAssociations().get(2).getName(), is("SupAsso"));
    }

    @Test
    public void testGetDeclaredAssociation() {
        ProductAssociation association = productCmptType.getDeclaredAssociation("asso");
        ProductAssociation superAssoInSuper = superProductModel.getDeclaredAssociation("SupAsso");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getDeclaredAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertSame(superAssoInSuper, superAssoInSuperLowerCase);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDeclaredAssociation_withexception() {
        productCmptType.getDeclaredAssociation("SupAsso");
    }

    @Test
    public void testGetDeclaredAssociation_Plural() {
        ProductAssociation superAssoInSuper = superProductModel.getDeclaredAssociation("SupAssos");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getDeclaredAssociation("supAssos");

        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertSame(superAssoInSuper, superAssoInSuperLowerCase);
    }

    @Test
    public void testGetAssociation() {
        ProductAssociation association = productCmptType.getAssociation("asso");
        ProductAssociation superAsso = productCmptType.getAssociation("SupAsso");
        ProductAssociation superAssoInSuper = superProductModel.getAssociation("SupAsso");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getAssociation("supAsso");

        assertThat(association.getName(), is("asso"));
        assertThat(association.getNamePlural(), is(IpsStringUtils.EMPTY));
        assertThat(superAsso.getName(), is("SupAsso"));
        assertThat(superAsso.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertSame(superAssoInSuper, superAssoInSuperLowerCase);
    }

    @Test
    public void testGetAssociation_Plural() {
        ProductAssociation superAsso = productCmptType.getAssociation("SupAssos");
        ProductAssociation superAssoInSuper = superProductModel.getAssociation("SupAssos");
        ProductAssociation superAssoInSuperLowerCase = superProductModel.getAssociation("supAssos");

        assertThat(superAsso.getName(), is("SupAsso"));
        assertThat(superAsso.getNamePlural(), is("SupAssos"));
        assertThat(superAssoInSuper.getName(), is("SupAsso"));
        assertThat(superAssoInSuper.getNamePlural(), is("SupAssos"));
        assertSame(superAssoInSuper, superAssoInSuperLowerCase);
    }

    @Test
    public void testIsAttributePresent() {
        assertTrue(productCmptType.isAttributePresent("supAttr"));
        assertFalse(productCmptType.isAttributeDeclared("supAttr"));

        assertTrue(productCmptType.isAttributePresent("overwrittenAttr"));
        assertTrue(productCmptType.isAttributeDeclared("overwrittenAttr"));

        assertTrue(productCmptType.isAttributePresent("BigAttr"));
        assertTrue(productCmptType.isAttributeDeclared("BigAttr"));
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsConfigures(Policy.class)
    @IpsChangingOverTime(ProductGen.class)
    @IpsAttributes({ "attr", "overwrittenAttr", "BigAttr", "attr_changing", "abstractSuper" })
    @IpsAssociations({ "asso", "asso_changing" })
    private abstract static class Product extends SuperProduct {

        public Product(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

        @IpsAttribute(name = "attr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);

        @Override
        @IpsAttribute(name = "overwrittenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getOverwrittenAttr();

        @Override
        @IpsAttribute(name = "abstractSuper", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract SubOfAbstractEnum getAbstractSuper();

        @IpsAttribute(name = "BigAttr", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public abstract String getBigAttr();

        @IpsAssociation(name = "asso", min = 1, max = 2, targetClass = Product.class, kind = AssociationKind.Association)
        public abstract Product getAsso();

    }

    private abstract static class ProductGen extends ProductComponentGeneration {

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
    @IpsAttributes({ "supAttr", "overwrittenAttr", "abstractSuper" })
    @IpsAssociations({ "SupAsso" })
    private abstract static class SuperProduct extends ProductComponent {

        @IpsAttribute(name = "supAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public int getSup() {
            return 1;
        }

        @IpsAttribute(name = "overwrittenAttr", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract String getOverwrittenAttr();

        @IpsAttribute(name = "abstractSuper", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)
        public abstract AbstractEnum getAbstractSuper();

        @IpsAssociation(name = "SupAsso", pluralName = "SupAssos", max = 5, min = 1, targetClass = SuperProduct.class, kind = AssociationKind.Association)
        public abstract SuperProduct getSupAsso();

        public SuperProduct(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

    }

    @IpsPolicyCmptType(name = "MyPolicy")
    private abstract static class Policy extends AbstractModelObject implements IConfigurableModelObject {
        // a policy
    }

    @IpsEnumType(name = "enums.AnnotatedAbstractEnum", attributeNames = { "id", "name" })
    private interface AbstractEnum {

        @IpsEnumAttribute(name = "id", identifier = true, unique = true)
        String getId();

        @IpsEnumAttribute(name = "name", unique = true, displayName = true)
        String getName();
    }

    @IpsEnumType(name = "enums.SubOfAnnotatedAbstractEnum", attributeNames = { "id", "name" })
    private enum SubOfAbstractEnum implements AbstractEnum {

        VALUE("1", "1");

        private final String id;
        private final String name;

        SubOfAbstractEnum(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @IpsEnumAttribute(name = "id", identifier = true, unique = true)
        @Override
        public String getId() {
            return id;
        }

        @IpsEnumAttribute(name = "name", unique = true, displayName = true)
        @Override
        public String getName() {
            return name;
        }

    }

}
