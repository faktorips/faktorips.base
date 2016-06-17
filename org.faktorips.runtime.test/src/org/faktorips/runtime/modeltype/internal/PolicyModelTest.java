package org.faktorips.runtime.modeltype.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
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
        assertThat(policyModel.getAttributes().size(), is(2));
        assertThat(policyModel.getAttributes().get(1).getName(), is("const"));
        assertNotNull(policyModel.getAttribute("attr"));
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsConfiguredBy(Product.class)
    @IpsAttributes({ "attr", "const" })
    private static abstract class Policy extends SuperPolicy {

        @IpsAttribute(name = "const", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)
        public static final int CONST = 2;

        @IpsAttribute(name = "attr", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Range)
        public abstract String getAttr();

        @IpsAttributeSetter("attr")
        public abstract void setAttr(String attr);
    }

    @IpsPolicyCmptType(name = "MySuperPolicy")
    private static abstract class SuperPolicy extends AbstractModelObject {

    }

    @IpsProductCmptType(name = "MyProduct")
    private static abstract class Product extends ProductComponent {

        public Product(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

    }

}
