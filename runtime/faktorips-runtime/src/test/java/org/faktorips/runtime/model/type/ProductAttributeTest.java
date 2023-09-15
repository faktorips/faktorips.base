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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ProductAttributeTest {

    @Mock
    private IRuntimeRepository repository;

    private final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    @Test
    public void testIsProductRelevant() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        for (ProductAttribute modelTypeAttribute : productCmptType.getAttributes()) {
            assertThat(modelTypeAttribute.isProductRelevant(), is(true));
        }
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        for (ProductAttribute modelTypeAttribute : productCmptType.getAttributes()) {
            assertThat(modelTypeAttribute.isProductRelevant(), is(true));
        }
    }

    @Test
    public void testGetValue() {
        Produkt productComponent = new Produkt();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)42)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foobar")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, effectiveDate),
                is(equalTo((Object)List.of("hello", "world"))));
        productComponent = new SubProdukt();
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, effectiveDate),
                is(equalTo((Object)List.of("hello", "world"))));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)23)));
        assertThat(productCmptType.getAttribute("attr3").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foofoo")));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foobaz")));
        assertThat(
                productCmptType.getAttribute("attrGen").getValue(productComponent, new GregorianCalendar(1999, 1, 2)),
                is(equalTo((Object)"2ndGen")));
    }

    @Test
    public void testGetValue_noEffectiveDate() {
        Produkt productComponent = new Produkt();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, null), is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, null), is(equalTo((Object)42)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, null),
                is(equalTo((Object)"foobar")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, null),
                is(equalTo((Object)List.of("hello", "world"))));

        productComponent = new SubProdukt();
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, null), is(equalTo((Object)"foo")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, null),
                is(equalTo((Object)List.of("hello", "world"))));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, null), is(equalTo((Object)23)));
        assertThat(productCmptType.getAttribute("attr3").getValue(productComponent, null),
                is(equalTo((Object)"foofoo")));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, null),
                is(equalTo((Object)"foobaz")));
    }

    @Test
    public void testSetValue() {
        Produkt productComponent = new Produkt();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        productCmptType.getAttribute("attr1").setValue(productComponent, effectiveDate, "newValue");
        productCmptType.getAttribute("attr2").setValue(productComponent, effectiveDate, 1);
        productCmptType.getAttribute("attrGen").setValue(productComponent, effectiveDate, "newValue");
        productCmptType.getAttribute("multiString").setValue(productComponent, effectiveDate,
                List.of("new", "value"));

        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)1)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, effectiveDate),
                is(equalTo((Object)List.of("new", "value"))));
    }

    @Test
    public void testSetValue_noEffectiveDate() {
        Produkt productComponent = new Produkt();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        productCmptType.getAttribute("attr1").setValue(productComponent, null, "newValue");
        productCmptType.getAttribute("attr2").setValue(productComponent, null, 1);
        productCmptType.getAttribute("attrGen").setValue(productComponent, null, "newValue");
        productCmptType.getAttribute("multiString").setValue(productComponent, null,
                List.of("new", "value"));

        assertThat(productCmptType.getAttribute("attr1").getValue(productComponent, null),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("attr2").getValue(productComponent, null),
                is(equalTo((Object)1)));
        assertThat(productCmptType.getAttribute("attrGen").getValue(productComponent, null),
                is(equalTo((Object)"newValue")));
        assertThat(productCmptType.getAttribute("multiString").getValue(productComponent, null),
                is(equalTo((Object)List.of("new", "value"))));
    }

    @Test
    public void testIsChangingOverTime() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiEnum").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attrGen").isChangingOverTime(), is(true));
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("multiEnum").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attr3").isChangingOverTime(), is(false));
        assertThat(productCmptType.getAttribute("attrGen").isChangingOverTime(), is(true));
    }

    @Test
    public void testIsMultiValue() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("multiEnum").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("attrGen").isMultiValue(), is(false));
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("attr2").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("multiString").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("attr3").isMultiValue(), is(false));
        assertThat(productCmptType.getAttribute("multiEnum").isMultiValue(), is(true));
        assertThat(productCmptType.getAttribute("attrGen").isMultiValue(), is(false));
    }

    @Test
    public void testGetDatatype() {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("attr2").getDatatype(), is(equalTo((Object)Integer.class)));
        assertThat(productCmptType.getAttribute("attrGen").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiString").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiEnum").getDatatype(),
                is(equalTo((Object)AbstractEnumType.class)));
        productCmptType = IpsModel.getProductCmptType(SubProdukt.class);
        assertThat(productCmptType.getAttribute("attr1").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("attr2").getDatatype(), is(equalTo((Object)Integer.class)));
        assertThat(productCmptType.getAttribute("attrGen").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiString").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("attr3").getDatatype(), is(equalTo((Object)String.class)));
        assertThat(productCmptType.getAttribute("multiEnum").getDatatype(),
                is(equalTo((Object)ConcreteEnumType.class)));
    }

    @Test
    public void testIsDeprecated() throws Exception {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").isDeprecated(), is(false));
        assertThat(productCmptType.getAttribute("deprecatedAttribute").isDeprecated(), is(true));
    }

    @Test
    public void testGetDeprecation() throws Exception {
        ProductCmptType productCmptType = IpsModel.getProductCmptType(Produkt.class);
        assertThat(productCmptType.getAttribute("attr1").getDeprecation().isPresent(), is(false));
        Optional<Deprecation> deprecation = productCmptType.getAttribute("deprecatedAttribute").getDeprecation();
        assertThat(deprecation.isPresent(), is(true));
        assertThat(deprecation.get().getSinceVersion().isPresent(), is(false));
        assertThat(deprecation.get().isMarkedForRemoval(), is(false));
    }

    @IpsProductCmptType(name = "ProductXYZ")
    @IpsChangingOverTime(ProduktGen.class)
    @IpsAttributes({ "attr1", "attr2", "multiString", "attrGen", "multiEnum", "deprecatedAttribute" })
    private class Produkt extends ProductComponent {

        private final ProduktGen produktGen = new ProduktGen(this);
        private String attr1 = "foo";
        private Integer attr2 = 42;
        private List<String> multiString = List.of("hello", "world");

        public Produkt() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsAttribute(name = "attr1", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttr1() {
            return attr1;
        }

        @IpsAttributeSetter("attr1")
        public void setAttr1(String newValue) {
            attr1 = newValue;
        }

        @IpsAttribute(name = "attr2", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public Integer getAttr2() {
            return attr2;
        }

        @IpsAttributeSetter("attr2")
        public void setAttr2(Integer newValue) {
            attr2 = newValue;
        }

        @IpsAttribute(name = "multiString", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public List<String> getMultiString() {
            return multiString;
        }

        @IpsAttributeSetter("multiString")
        public void setMultiString(List<String> newValue) {
            multiString = newValue;
        }

        @IpsAttribute(name = "multiEnum", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public List<? extends AbstractEnumType> getMultiEnum() {
            return List.of();
        }

        @IpsAttribute(name = "deprecatedAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        @Deprecated
        public int getDeprecatedAttribute() {
            return -1;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            return produktGen;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return produktGen;
        }

    }

    private class ProduktGen extends ProductComponentGeneration {

        private String attrGen = "foobar";

        public ProduktGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @IpsAttribute(name = "attrGen", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttrGen() {
            return attrGen;
        }

        @IpsAttributeSetter("attrGen")
        public void setAttrGen(String newValue) {
            attrGen = newValue;
        }

    }

    @IpsProductCmptType(name = "SubProductXYZ")
    @IpsChangingOverTime(SubProduktGen.class)
    @IpsAttributes({ "attr2", "attrGen", "attr3", "multiEnum" })
    private class SubProdukt extends Produkt {

        private final SubProduktGen subProduktGen = new SubProduktGen(this);
        private final List<ConcreteEnumType> multiEnum = List.of(new ConcreteEnumType());

        @Override
        @IpsAttribute(name = "attr2", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public Integer getAttr2() {
            return 23;
        }

        @IpsAttribute(name = "attr3", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttr3() {
            return "foofoo";
        }

        @Override
        @IpsAttribute(name = "multiEnum", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public List<ConcreteEnumType> getMultiEnum() {
            return multiEnum;
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

        @Override
        public IProductComponentGeneration getGenerationBase(Calendar effectiveDate) {
            if (effectiveDate != ProductAttributeTest.this.effectiveDate) {
                return new SubProduktGen(this) {

                    @Override
                    public String getAttrGen() {
                        return "2ndGen";
                    }

                };
            }
            return subProduktGen;
        }

        @Override
        public IProductComponentGeneration getLatestProductComponentGeneration() {
            return subProduktGen;
        }

    }

    private class SubProduktGen extends ProduktGen {

        public SubProduktGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @Override
        @IpsAttribute(name = "attrGen", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public String getAttrGen() {
            return "foobaz";
        }

    }

    @IpsEnumType(name = "AbstractEnumType", attributeNames = { "" })
    private interface AbstractEnumType {
        // abstract
    }

    @IpsEnumType(name = "ConcreteEnumType", attributeNames = { "" })
    private static class ConcreteEnumType implements AbstractEnumType {
        // concrete
    }

}
