/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.junit.Test;

public class TemplatePropertyUsagePmoTest extends AbstractIpsPluginTest {

    private static final GregorianCalendar EFFECTIVE_DATE = new GregorianCalendar(2016, Calendar.JANUARY, 1);

    @Test
    public void testGetInheritingTemplatedValues() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();

        IProductCmpt t1 = newProductTemplate(productCmptType, "Template-1");
        IProductCmpt t2 = newProductTemplate(productCmptType, "Template-2");
        IProductCmpt p1 = newProductCmpt(productCmptType, "Product-1");
        IProductCmpt p2 = newProductCmpt(productCmptType, "Product-2");

        t2.setTemplate(t1.getQualifiedName());
        p1.setTemplate(t1.getQualifiedName());
        p2.setTemplate(t2.getQualifiedName());

        IProductCmptGeneration t1Gen = (IProductCmptGeneration)t1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration t2Gen = (IProductCmptGeneration)t2.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p1Gen = (IProductCmptGeneration)p1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p2Gen = (IProductCmptGeneration)p2.newGeneration(EFFECTIVE_DATE);

        // Property value of product components
        ITemplatedValue t1Value = t1.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue t2Value = t2.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p1Value = p1.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p2Value = p2.newPropertyValue(attribute, IAttributeValue.class);
        t1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2Value.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        p1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2Value.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        TemplatePropertyUsagePmo valuePmo = new TemplatePropertyUsagePmo();
        valuePmo.setTemplatedValue(t1Value);
        assertThat(valuePmo.getInheritingTemplatedValues(), hasItems(p2Value));

        // Property value of generations
        ITemplatedValue t1GenValue = t1Gen.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue t2GenValue = t2Gen.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p1GenValue = p1Gen.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p2GenValue = p2Gen.newPropertyValue(attribute, IAttributeValue.class);
        t1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2GenValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        p1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2GenValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        TemplatePropertyUsagePmo genValuePmo = new TemplatePropertyUsagePmo();
        genValuePmo.setTemplatedValue(t1GenValue);
        assertThat(genValuePmo.getInheritingTemplatedValues(), hasItems(p2Value));
    }

    @Test
    public void testGetInheritingTemplatedValues_UndefinedTemplateValue() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute genAttribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("attribute");
        attribute.setName("genAttribute");

        IProductCmpt t1 = newProductTemplate(productCmptType, "Template-1");
        IProductCmpt t2 = newProductTemplate(productCmptType, "Template-2");
        IProductCmpt p1 = newProductCmpt(productCmptType, "Product-1");
        IProductCmpt p2 = newProductCmpt(productCmptType, "Product-2");

        t2.setTemplate(t1.getQualifiedName());
        p1.setTemplate(t1.getQualifiedName());
        p2.setTemplate(t2.getQualifiedName());

        IProductCmptGeneration t1Gen = (IProductCmptGeneration)t1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration t2Gen = (IProductCmptGeneration)t2.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p1Gen = (IProductCmptGeneration)p1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p2Gen = (IProductCmptGeneration)p2.newGeneration(EFFECTIVE_DATE);

        // Property value of product components
        IPropertyValue t1Value = t1.newPropertyValue(attribute, IAttributeValue.class);
        IPropertyValue t2Value = t2.newPropertyValue(attribute, IAttributeValue.class);
        IPropertyValue p1Value = p1.newPropertyValue(attribute, IAttributeValue.class);
        IPropertyValue p2Value = p2.newPropertyValue(attribute, IAttributeValue.class);
        t1Value.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        t2Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2Value.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(new TemplatePropertyUsagePmo(t1Value).getInheritingTemplatedValues().isEmpty(), is(true));
        assertThat(new TemplatePropertyUsagePmo(t2Value).getInheritingTemplatedValues(),
                hasItems((ITemplatedValue)p2Value));

        // Property value of generations
        ITemplatedValue t1GenValue = t1Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        ITemplatedValue t2GenValue = t2Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        ITemplatedValue p1GenValue = p1Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        ITemplatedValue p2GenValue = p2Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        t1GenValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        t2GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2GenValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(new TemplatePropertyUsagePmo(t1GenValue).getInheritingTemplatedValues().isEmpty(), is(true));
        assertThat(new TemplatePropertyUsagePmo(t2GenValue).getInheritingTemplatedValues(), hasItems(p2GenValue));
    }

    @Test
    public void testGetDefiningTemplatedValues() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();

        IProductCmpt t1 = newProductTemplate(productCmptType, "Template-1");
        IProductCmpt t2 = newProductTemplate(productCmptType, "Template-2");
        IProductCmpt p1 = newProductCmpt(productCmptType, "Product-1");
        IProductCmpt p2 = newProductCmpt(productCmptType, "Product-2");

        t2.setTemplate(t1.getQualifiedName());
        p1.setTemplate(t1.getQualifiedName());
        p2.setTemplate(t2.getQualifiedName());

        IProductCmptGeneration t1Gen = (IProductCmptGeneration)t1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration t2Gen = (IProductCmptGeneration)t2.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p1Gen = (IProductCmptGeneration)p1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p2Gen = (IProductCmptGeneration)p2.newGeneration(EFFECTIVE_DATE);

        // Property value of product components
        ITemplatedValue t1Value = t1.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue t2Value = t2.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p1Value = p1.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p2Value = p2.newPropertyValue(attribute, IAttributeValue.class);
        t1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo valuePmo = new TemplatePropertyUsagePmo();
        valuePmo.setTemplatedValue(t1Value);
        assertThat(valuePmo.getDefiningTemplatedValues(), hasItems(p1Value, t2Value));

        // Property value of generations
        ITemplatedValue t1GenValue = t1Gen.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue t2GenValue = t2Gen.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p1GenValue = p1Gen.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p2GenValue = p2Gen.newPropertyValue(attribute, IAttributeValue.class);
        t1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo genValuePmo = new TemplatePropertyUsagePmo();
        genValuePmo.setTemplatedValue(t1GenValue);
        assertThat(genValuePmo.getDefiningTemplatedValues(), hasItems(p1Value, t2Value));
    }

    @Test
    public void testGetDefiningTemplatedValues_UndefinedTemplateValue() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        IProductCmptTypeAttribute genAttribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("attribute");
        attribute.setName("genAttribute");

        IProductCmpt t1 = newProductTemplate(productCmptType, "Template-1");
        IProductCmpt t2 = newProductTemplate(productCmptType, "Template-2");
        IProductCmpt p1 = newProductCmpt(productCmptType, "Product-1");
        IProductCmpt p2 = newProductCmpt(productCmptType, "Product-2");

        t2.setTemplate(t1.getQualifiedName());
        p1.setTemplate(t1.getQualifiedName());
        p2.setTemplate(t2.getQualifiedName());

        IProductCmptGeneration t1Gen = (IProductCmptGeneration)t1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration t2Gen = (IProductCmptGeneration)t2.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p1Gen = (IProductCmptGeneration)p1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p2Gen = (IProductCmptGeneration)p2.newGeneration(EFFECTIVE_DATE);

        // Property value of product components
        ITemplatedValue t1Value = t1.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue t2Value = t2.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p1Value = p1.newPropertyValue(attribute, IAttributeValue.class);
        ITemplatedValue p2Value = p2.newPropertyValue(attribute, IAttributeValue.class);
        t1Value.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        t2Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2Value.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(new TemplatePropertyUsagePmo(t1Value).getDefiningTemplatedValues(), hasItems(p1Value, t2Value));
        assertThat(new TemplatePropertyUsagePmo(t2Value).getDefiningTemplatedValues().isEmpty(), is(true));

        // Property value of generations
        ITemplatedValue t1GenValue = t1Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        ITemplatedValue t2GenValue = t2Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        ITemplatedValue p1GenValue = p1Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        ITemplatedValue p2GenValue = p2Gen.newPropertyValue(genAttribute, IAttributeValue.class);
        t1GenValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        t2GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2GenValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        assertThat(new TemplatePropertyUsagePmo(t1GenValue).getDefiningTemplatedValues(),
                hasItems(p1GenValue, t2GenValue));
        assertThat(new TemplatePropertyUsagePmo(t2GenValue).getDefiningTemplatedValues().isEmpty(), is(true));
    }

    @Test
    public void testGetDefinedProductCmptValues() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        ITableStructureUsage tableStructurUsage = productCmptType.newTableStructureUsage();
        tableStructurUsage.setRoleName("RoleName");

        IProductCmpt t = newProductTemplate(productCmptType, "Template");
        IProductCmpt p1 = newProductCmpt(productCmptType, "Product-1");
        IProductCmpt p2 = newProductCmpt(productCmptType, "Product-2");
        IProductCmpt p3 = newProductCmpt(productCmptType, "Product-3");

        p1.setTemplate(t.getQualifiedName());
        p2.setTemplate(t.getQualifiedName());
        p3.setTemplate(t.getQualifiedName());

        IProductCmptGeneration tGen = (IProductCmptGeneration)t.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p1Gen = (IProductCmptGeneration)p1.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p2Gen = (IProductCmptGeneration)p2.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration p3Gen = (IProductCmptGeneration)p3.newGeneration(EFFECTIVE_DATE);

        ITableContentUsage tTable = tGen.newTableContentUsage(tableStructurUsage);
        ITableContentUsage p1Table = p1Gen.newTableContentUsage(tableStructurUsage);
        ITableContentUsage p2Table = p2Gen.newTableContentUsage(tableStructurUsage);
        ITableContentUsage p3Table = p3Gen.newTableContentUsage(tableStructurUsage);

        tTable.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1Table.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2Table.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p3Table.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        p1Table.setTableContentName("Table-A");
        p2Table.setTableContentName("Table-B");
        p3Table.setTableContentName("Table-B");

        TemplatePropertyUsagePmo pmo = new TemplatePropertyUsagePmo();
        pmo.setTemplatedValue(tTable);
        Histogram<Object, ITemplatedValue> histogram = pmo.getDefinedValuesHistogram();
        assertThat(histogram.countElements(), is(3));
        assertThat(histogram.getDistribution().get("Table-A"), hasItems((ITemplatedValue)p1Table));
        assertThat(histogram.getDistribution().get("Table-B"), hasItems((ITemplatedValue)p2Table, p2Table));

    }

    @Test
    public void testTemplatePropertyUsagePmo_ShowsTemplateProperty() {

        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();

        IProductCmpt t = newProductTemplate(productCmptType, "Template");
        IProductCmpt p = newProductCmpt(productCmptType, "Product");

        p.setTemplate(t.getQualifiedName());

        IPropertyValue templateValue = t.newPropertyValue(attribute, IAttributeValue.class);
        IPropertyValue prodCmptValue = p.newPropertyValue(attribute, IAttributeValue.class);

        templateValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        prodCmptValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo templatePmo = new TemplatePropertyUsagePmo();
        templatePmo.setTemplatedValue(templateValue);
        assertThat(templatePmo.getIpsObjectPartContainer(), is((IIpsObjectPartContainer)templateValue));
        assertThat(templatePmo.getTemplate(), is((ITemplatedValueContainer)t));
    }

    @Test
    public void testTemplatePropertyUsagePmo_NonTemplateProperty() {

        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();

        IProductCmpt prodCmpt = newProductCmpt(productCmptType, "Product");
        IPropertyValue propertyValue = prodCmpt.newPropertyValue(attribute, IAttributeValue.class);
        propertyValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo pmo = new TemplatePropertyUsagePmo();
        pmo.setTemplatedValue(propertyValue);
        assertThat(pmo.getDefiningTemplatedValues().isEmpty(), is(true));
        assertThat(pmo.getInheritingTemplatedValues().isEmpty(), is(true));
        assertThat(pmo.getDefinedValuesHistogram().getDistribution().isEmpty(), is(true));
    }

    @Test
    public void testGetActualTemplateValue() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        ITableStructureUsage tableStructurUsage = productCmptType.newTableStructureUsage();
        tableStructurUsage.setRoleName("RoleName");

        IProductCmpt t = newProductTemplate(productCmptType, "Template");
        IProductCmpt p = newProductCmpt(productCmptType, "Product");

        p.setTemplate(t.getQualifiedName());

        IProductCmptGeneration tGen = (IProductCmptGeneration)t.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration pGen = (IProductCmptGeneration)p.newGeneration(EFFECTIVE_DATE);

        ITableContentUsage tTable = tGen.newTableContentUsage(tableStructurUsage);
        ITableContentUsage pTable = pGen.newTableContentUsage(tableStructurUsage);

        tTable.setTableContentName("templateTable");
        pTable.setTableContentName("productTable");

        TemplatePropertyUsagePmo pmo = new TemplatePropertyUsagePmo(pTable);
        assertThat(pmo.getActualTemplateValue(), is((Object)"templateTable"));

    }

    @Test
    public void testGetActualTemplateValue_null() {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        ITableStructureUsage tableStructurUsage = productCmptType.newTableStructureUsage();
        tableStructurUsage.setRoleName("RoleName");

        IProductCmpt t = newProductTemplate(productCmptType, "Template");
        IProductCmpt p = newProductCmpt(productCmptType, "Product");

        p.setTemplate(t.getQualifiedName());

        IProductCmptGeneration tGen = (IProductCmptGeneration)t.newGeneration(EFFECTIVE_DATE);
        IProductCmptGeneration pGen = (IProductCmptGeneration)p.newGeneration(EFFECTIVE_DATE);

        ITableContentUsage tTable = tGen.newTableContentUsage(tableStructurUsage);
        ITableContentUsage pTable = pGen.newTableContentUsage(tableStructurUsage);

        tTable.setTableContentName(null);
        pTable.setTableContentName(null);

        TemplatePropertyUsagePmo pmo = new TemplatePropertyUsagePmo(pTable);
        assertThat(pmo.getActualTemplateValue(), is(nullValue()));

    }

}
