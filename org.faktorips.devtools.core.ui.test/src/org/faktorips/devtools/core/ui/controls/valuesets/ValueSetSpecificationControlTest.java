/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controls.valuesets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl.ValueSetPmo;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ValueSetSpecificationControlTest extends AbstractIpsPluginTest {
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private ProductCmptType productCmptType;
    private ProductCmptType superProductCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType = (ProductCmptType)policyCmptType.findProductCmptType(ipsProject);
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        productCmpt.getIpsSrcFile().save(true, null);
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
    }

    @Test
    public void testValidatePMO_configElement() throws CoreException {

        IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
        attr.setName("attr");
        attr.setDatatype(ValueDatatype.INTEGER.getQualifiedName());
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.getValueSet().setContainsNull(false);

        IConfiguredValueSet configValueSet = generation.newPropertyValue(attr, IConfiguredValueSet.class);
        configValueSet.setValueSetType(ValueSetType.UNRESTRICTED);
        configValueSet.getValueSet().setAbstract(false);
        configValueSet.getValueSet().setContainsNull(true);

        assertHasNullNotAllowedMessage(configValueSet);
    }

    @Test
    public void testValidatePMO_overwrittenAttribute() throws CoreException {
        IProductCmptTypeAttribute attr = superProductCmptType.newProductCmptTypeAttribute("attr");
        IProductCmptTypeAttribute overwritingAttr = productCmptType.newProductCmptTypeAttribute("attr");
        attr.setDatatype("Integer");
        overwritingAttr.setDatatype("Integer");
        overwritingAttr.setOverwrite(true);

        List<String> listWithNull = list(null, "1", "2", "3", "4");
        List<String> normalValues = list("1", "9", "99", "999");
        attr.setValueSetCopy(new EnumValueSet(attr, normalValues, "partId"));
        overwritingAttr.setValueSetCopy(new EnumValueSet(overwritingAttr, listWithNull, "partId"));

        assertHasNullNotAllowedMessage(overwritingAttr);
    }

    @Test
    public void testValueSetPmoIsContainsNullEnabled_primitiveAttribute() {
        IProductCmptTypeAttribute primitiveAttribute = productCmptType.newProductCmptTypeAttribute("attr");
        primitiveAttribute.setDatatype(Datatype.PRIMITIVE_INT.getName());
        ValueSetPmo valueSetPmo = new ValueSetSpecificationControl.ValueSetPmo(primitiveAttribute);

        assertThat(valueSetPmo.isContainsNullEnabled(), is(false));
    }

    @Test
    public void testValueSetPmoIsContainsNullEnabled_nonPrimitiveAttribute() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("attr");
        attribute.setDatatype(Datatype.INTEGER.getName());
        ValueSetPmo valueSetPmo = new ValueSetSpecificationControl.ValueSetPmo(attribute);

        assertThat(valueSetPmo.isContainsNullEnabled(), is(true));
    }

    @Test
    public void testValueSetPmoIsContainsNullEnabled_EnumContainsNullAttribute() {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("attr");
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype(Datatype.INTEGER.getName());
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.setValueSetCopy(new EnumValueSet(attribute, Arrays.asList("1", "2", "3", null), "mockId"));
        IConfiguredValueSet configValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        ValueSetPmo valueSetPmo = new ValueSetSpecificationControl.ValueSetPmo(configValueSet);

        assertThat(valueSetPmo.isContainsNullEnabled(), is(true));
    }

    @Test
    public void testValueSetPmoIsContainsNullEnabled_EnumDoesNotContainNullAttribute() {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("attr");
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype(Datatype.INTEGER.getName());
        attribute.changeValueSetType(ValueSetType.ENUM);
        attribute.setValueSetCopy(new EnumValueSet(attribute, Arrays.asList("1", "2", "3"), "mockId"));
        IConfiguredValueSet configValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        ValueSetPmo valueSetPmo = new ValueSetSpecificationControl.ValueSetPmo(configValueSet);

        assertThat(valueSetPmo.isContainsNullEnabled(), is(false));
    }

    private void assertHasNullNotAllowedMessage(IValueSetOwner valueSetOwner) throws CoreException {
        MessageList messageList = new ValueSetSpecificationControl.ValueSetPmo(valueSetOwner).validate(ipsProject);
        assertNotNull(messageList.getMessageByCode(ValueSetPmo.MSG_CODE_NULL_NOT_ALLOWED));
    }

    private List<String> list(String... values) {
        return Arrays.asList(values);
    }
}
