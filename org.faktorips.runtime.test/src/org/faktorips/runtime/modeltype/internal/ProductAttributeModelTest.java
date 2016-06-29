package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.faktorips.runtime.modeltype.IProductModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductAttributeModelTest {

    @Mock
    private IRuntimeRepository repository;

    private final Calendar effectiveDate = new GregorianCalendar(1999, 1, 1);

    @Test
    public void testIsProductRelevant() {
        IProductModel productModel = Models.getProductModel(Produkt.class);
        for (IModelTypeAttribute modelTypeAttribute : productModel.getAttributes()) {
            assertThat(modelTypeAttribute.isProductRelevant(), is(true));
        }
        productModel = Models.getProductModel(SubProdukt.class);
        for (IModelTypeAttribute modelTypeAttribute : productModel.getAttributes()) {
            assertThat(modelTypeAttribute.isProductRelevant(), is(true));
        }
    }

    @Test
    public void testGetValue() {
        Produkt productComponent = new Produkt();
        IProductModel productModel = Models.getProductModel(Produkt.class);
        assertThat(productModel.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foo")));
        assertThat(productModel.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"bar")));
        assertThat(productModel.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foobar")));
        productComponent = new SubProdukt();
        productModel = Models.getProductModel(SubProdukt.class);
        assertThat(productModel.getAttribute("attr1").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foo")));
        assertThat(productModel.getAttribute("attr2").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"baz")));
        assertThat(productModel.getAttribute("attr3").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foofoo")));
        assertThat(productModel.getAttribute("attrGen").getValue(productComponent, effectiveDate),
                is(equalTo((Object)"foobaz")));
        assertThat(productModel.getAttribute("attrGen").getValue(productComponent, new GregorianCalendar(1999, 1, 2)),
                is(equalTo((Object)"2ndGen")));
    }

    @Test
    public void testGetValue_noEffectiveDate() {
        Produkt productComponent = new Produkt();
        IProductModel productModel = Models.getProductModel(Produkt.class);
        assertThat(productModel.getAttribute("attr1").getValue(productComponent, null), is(equalTo((Object)"foo")));
        assertThat(productModel.getAttribute("attr2").getValue(productComponent, null), is(equalTo((Object)"bar")));
        assertThat(productModel.getAttribute("attrGen").getValue(productComponent, null), is(equalTo((Object)"foobar")));
        productComponent = new SubProdukt();
        productModel = Models.getProductModel(SubProdukt.class);
        assertThat(productModel.getAttribute("attr1").getValue(productComponent, null), is(equalTo((Object)"foo")));
        assertThat(productModel.getAttribute("attr2").getValue(productComponent, null), is(equalTo((Object)"baz")));
        assertThat(productModel.getAttribute("attr3").getValue(productComponent, null), is(equalTo((Object)"foofoo")));
        assertThat(productModel.getAttribute("attrGen").getValue(productComponent, null), is(equalTo((Object)"foobaz")));
    }

    @Test
    public void testIsChangingOverTime() {
        IProductModel productModel = Models.getProductModel(Produkt.class);
        assertThat(productModel.getAttribute("attr1").isChangingOverTime(), is(false));
        assertThat(productModel.getAttribute("attr2").isChangingOverTime(), is(false));
        assertThat(productModel.getAttribute("attrGen").isChangingOverTime(), is(true));
        productModel = Models.getProductModel(SubProdukt.class);
        assertThat(productModel.getAttribute("attr1").isChangingOverTime(), is(false));
        assertThat(productModel.getAttribute("attr2").isChangingOverTime(), is(false));
        assertThat(productModel.getAttribute("attr3").isChangingOverTime(), is(false));
        assertThat(productModel.getAttribute("attrGen").isChangingOverTime(), is(true));
    }

    @IpsProductCmptType(name = "ProductXYZ")
    @IpsChangingOverTime(ProduktGen.class)
    @IpsAttributes({ "attr1", "attr2", "attrGen" })
    private class Produkt extends ProductComponent {

        private final ProduktGen produktGen = new ProduktGen(this);

        public Produkt() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsAttribute(name = "attr1", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public String getAttr1() {
            return "foo";
        }

        @IpsAttribute(name = "attr2", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public String getAttr2() {
            return "bar";
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

        public ProduktGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

        @IpsAttribute(name = "attrGen", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public String getAttrGen() {
            return "foobar";
        }

    }

    @IpsProductCmptType(name = "SubProductXYZ")
    @IpsChangingOverTime(SubProduktGen.class)
    @IpsAttributes({ "attr2", "attrGen", "attr3" })
    private class SubProdukt extends Produkt {

        private final SubProduktGen subProduktGen = new SubProduktGen(this);

        @Override
        @IpsAttribute(name = "attr2", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public String getAttr2() {
            return "baz";
        }

        @IpsAttribute(name = "attr3", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public String getAttr3() {
            return "foofoo";
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
            if (effectiveDate != ProductAttributeModelTest.this.effectiveDate) {
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
        @IpsAttribute(name = "attrGen", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public String getAttrGen() {
            return "foobaz";
        }

    }

}
