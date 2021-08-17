/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Test;

public class TemplateValueUiStatusTest extends AbstractIpsPluginTest {

    private static final String ATTRIBUTE = "attribute";

    private IAttributeValue property;

    private IAttributeValue templateProperty;

    private IIpsProject ipsProject;

    private ProductCmptType productCmptType;

    private IAttribute attribute;

    private ProductCmpt template;

    private ProductCmpt product;

    private void createProperty() throws Exception {
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Type");
        attribute = productCmptType.newAttribute();
        attribute.setName(ATTRIBUTE);
        attribute.setDatatype(ValueDatatype.MONEY.getQualifiedName());
        attribute.setChangingOverTime(false);

        template = newProductTemplate(productCmptType, "MyTemplate");
        template.fixAllDifferencesToModel(ipsProject);
        templateProperty = template.getAttributeValue(ATTRIBUTE);
        templateProperty.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        product = newProductCmpt(productCmptType, "MyProduct");
        product.setTemplate(template.getQualifiedName());
        product.fixAllDifferencesToModel(ipsProject);
        property = product.getAttributeValue(ATTRIBUTE);
    }

    @Test
    public void testMapStatus_INHERITED() throws Exception {
        createProperty();
        property.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.INHERITED));
    }

    @Test
    public void testMapStatus_UNDEFINED() throws Exception {
        createProperty();
        templateProperty.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(templateProperty);

        assertThat(status, is(TemplateValueUiStatus.UNDEFINED));
    }

    @Test
    public void testMapStatus_DEFINED__NEWLY_DEFINED() throws Exception {
        createProperty();
        templateProperty.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        property.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.NEWLY_DEFINED));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE_EQUAL__null() throws Exception {
        createProperty();
        property.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE_EQUAL));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE_EQUAL__nonNull() throws Exception {
        createProperty();
        property.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        property.setValueHolder(new SingleValueHolder(property, "asdf"));
        templateProperty.setValueHolder(new SingleValueHolder(templateProperty, "asdf"));

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE_EQUAL));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE_EQUAL__DatatypeCompare() throws Exception {
        createProperty();
        property.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        property.setValueHolder(new SingleValueHolder(property, "7.0 EUR"));
        templateProperty.setValueHolder(new SingleValueHolder(templateProperty, "7 EUR"));

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE_EQUAL));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE() throws Exception {
        createProperty();
        property.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        property.setValueHolder(new SingleValueHolder(property, "7.0 EUR"));
        templateProperty.setValueHolder(new SingleValueHolder(templateProperty, "70 EUR"));

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE));
    }
}
