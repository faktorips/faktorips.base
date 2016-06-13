package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.modeltype.IProductModel;
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

    @IpsProductCmptType(name = "MyProduct")
    @IpsConfigures(Policy.class)
    @IpsChangingOverTime(ProductGen.class)
    private static abstract class Product extends SuperProduct {

        public Product(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

    }

    private static abstract class ProductGen extends ProductComponentGeneration {

        public ProductGen(ProductComponent productCmpt) {
            super(productCmpt);
        }

    }

    @IpsProductCmptType(name = "MySuperProduct")
    private static abstract class SuperProduct extends ProductComponent {

        public SuperProduct(IRuntimeRepository repository, String id, String PolicyKindId, String versionId) {
            super(repository, id, PolicyKindId, versionId);
        }

    }

    @IpsPolicyCmptType(name = "MyPolicy")
    private static abstract class Policy extends AbstractModelObject implements IConfigurableModelObject {

    }

}
