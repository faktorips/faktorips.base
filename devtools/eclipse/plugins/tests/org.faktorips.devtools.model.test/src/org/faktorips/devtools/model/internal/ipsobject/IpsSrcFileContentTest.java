/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Test;

public class IpsSrcFileContentTest extends AbstractIpsPluginTest {

    @Test
    public void testGetRootPropertyValue() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestFile");
        IIpsSrcFile ipsSrcFile = policyCmptType.getIpsSrcFile();

        policyCmptType.setSupertype(null);
        String nullPropertyValue = ipsSrcFile.getPropertyValue(PolicyCmptType.PROPERTY_SUPERTYPE);
        assertThat(nullPropertyValue, is(nullValue()));

        policyCmptType.setSupertype("");
        String emptyPropertyValue = ipsSrcFile.getPropertyValue(PolicyCmptType.PROPERTY_SUPERTYPE);
        assertThat(emptyPropertyValue, is(""));

        policyCmptType.setSupertype("supertype");
        String stringPropertyValue = ipsSrcFile.getPropertyValue(PolicyCmptType.PROPERTY_SUPERTYPE);
        assertThat(stringPropertyValue, is("supertype"));

        ipsSrcFile.save(null);
        IpsSrcFileContent ipsSrcFileContent = new IpsSrcFileContent(policyCmptType);
        String propertyValueFromFile = ipsSrcFileContent.getRootPropertyValue(PolicyCmptType.PROPERTY_SUPERTYPE);
        assertThat(propertyValueFromFile, is("supertype"));
    }

    @Test
    public void testGetRootPropertyValue_ValidFrom() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "TestType");
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "TestProduct");
        productCmpt.setValidFrom(null);
        IIpsSrcFile ipsSrcFile = productCmpt.getIpsSrcFile();

        String nullPropertyValue = ipsSrcFile.getPropertyValue(ProductCmpt.XML_ATTRIBUTE_VALID_FROM);
        assertThat(nullPropertyValue, is(nullValue()));

        productCmpt.setValidFrom(new GregorianCalendar(2026, 3, 1));
        String propertyValueFromObject = ipsSrcFile.getPropertyValue(ProductCmpt.XML_ATTRIBUTE_VALID_FROM);
        assertThat(propertyValueFromObject, is("2026-04-01"));

        ipsSrcFile.save(null);
        IpsSrcFileContent ipsSrcFileContent = new IpsSrcFileContent(productCmpt);
        String propertyValueFromFile = ipsSrcFileContent.getRootPropertyValue(ProductCmpt.XML_ATTRIBUTE_VALID_FROM);
        assertThat(propertyValueFromFile, is("2026-04-01"));
    }

    @Test
    public void testUpdateTemplatedValuesOnSave() {
        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "P");
        productCmptType.setChangingOverTime(false);

        IProductCmptTypeAttribute attr = productCmptType.newProductCmptTypeAttribute("a1");
        attr.setChangingOverTime(false);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IAttributeValue templateAttributeValue = productTemplate.getPropertyValue("a1", IAttributeValue.class);
        SingleValueHolder templateValueHolder = new SingleValueHolder(templateAttributeValue, "10");
        templateAttributeValue.setValueHolder(templateValueHolder);
        productTemplate.getIpsSrcFile().save(null);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        AttributeValue attributeValue = (AttributeValue)productCmpt.getPropertyValue("a1", IAttributeValue.class);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "10");
        attributeValue.setValueHolder(valueHolder);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);

        templateValueHolder.setValue(ValueFactory.createStringValue("5"));
        productTemplate.getIpsSrcFile().save(null);

        assertThat(attributeValue.getValueHolderInternal().getStringValue(), is("5"));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));
        assertThat(productCmpt.computeDeltaToModel(ipsProject).getEntries(), is(emptyArray()));
    }

    @Test
    public void testUpdateTemplatedValuesOnSave_DirtyProduct() {
        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "P");
        productCmptType.setChangingOverTime(false);

        IProductCmptTypeAttribute attr = productCmptType.newProductCmptTypeAttribute("a1");
        attr.setChangingOverTime(false);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IAttributeValue templateAttributeValue = productTemplate.getPropertyValue("a1", IAttributeValue.class);
        SingleValueHolder templateValueHolder = new SingleValueHolder(templateAttributeValue, "10");
        templateAttributeValue.setValueHolder(templateValueHolder);
        productTemplate.getIpsSrcFile().save(null);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        AttributeValue attributeValue = (AttributeValue)productCmpt.getPropertyValue("a1", IAttributeValue.class);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "10");
        attributeValue.setValueHolder(valueHolder);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        templateValueHolder.setValue(ValueFactory.createStringValue("5"));
        productTemplate.getIpsSrcFile().save(null);

        assertThat(attributeValue.getValueHolderInternal().getStringValue(), is("5"));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(true));
        assertThat(productCmpt.computeDeltaToModel(ipsProject).getEntries(), is(emptyArray()));
    }

    @Test
    public void testUpdateTemplatedValuesOnSave_DontFixOtherDiffs() {
        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "P");
        productCmptType.setChangingOverTime(false);

        IProductCmptTypeAttribute attr = productCmptType.newProductCmptTypeAttribute("a1");
        attr.setChangingOverTime(false);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmptType.getIpsSrcFile().save(null);

        ProductCmpt productTemplate = newProductTemplate(productCmptType, "ProductTemplate");
        IPropertyValueContainerToTypeDelta delta = productTemplate.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        IAttributeValue templateAttributeValue = productTemplate.getPropertyValue("a1", IAttributeValue.class);
        SingleValueHolder templateValueHolder = new SingleValueHolder(templateAttributeValue, "10");
        templateAttributeValue.setValueHolder(templateValueHolder);
        productTemplate.getIpsSrcFile().save(null);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");

        productCmpt.setTemplate(productTemplate.getQualifiedName());
        delta = productCmpt.computeDeltaToModel(ipsProject);
        delta.fixAllDifferencesToModel();
        AttributeValue attributeValue = (AttributeValue)productCmpt.getPropertyValue("a1", IAttributeValue.class);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "10");
        attributeValue.setValueHolder(valueHolder);
        attributeValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        productCmpt.getIpsSrcFile().save(null);

        productCmptType.newProductCmptTypeAttribute("a2");
        attr.setChangingOverTime(false);
        attr.setValueSetType(ValueSetType.UNRESTRICTED);
        attr.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmptType.getIpsSrcFile().save(null);

        templateValueHolder.setValue(ValueFactory.createStringValue("5"));
        productTemplate.getIpsSrcFile().save(null);

        assertThat(attributeValue.getValueHolderInternal().getStringValue(), is("5"));
        assertThat(productCmpt.getIpsSrcFile().isDirty(), is(false));
        assertThat(productCmpt.computeDeltaToModel(ipsProject).getEntries(), is(not(emptyArray())));
    }

}
