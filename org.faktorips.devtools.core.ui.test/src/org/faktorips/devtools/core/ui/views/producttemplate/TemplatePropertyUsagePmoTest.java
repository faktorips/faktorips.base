/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.util.Histogram;
import org.junit.Test;

public class TemplatePropertyUsagePmoTest extends AbstractIpsPluginTest {

    private static final GregorianCalendar EFFECTIVE_DATE = new GregorianCalendar(2016, Calendar.JANUARY, 1);

    @Test
    public void testGetInheritingProductCmpts() throws CoreException {
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
        IPropertyValue t1Value = t1.newPropertyValue(attribute);
        IPropertyValue t2Value = t2.newPropertyValue(attribute);
        IPropertyValue p1Value = p1.newPropertyValue(attribute);
        IPropertyValue p2Value = p2.newPropertyValue(attribute);
        t1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2Value.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        p1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2Value.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        TemplatePropertyUsagePmo valuePmo = new TemplatePropertyUsagePmo(p1Value, EFFECTIVE_DATE);
        assertThat(valuePmo.getInheritingProductCmpts(), hasItems(p2));

        // Property value of generations
        IPropertyValue t1GenValue = t1Gen.newPropertyValue(attribute);
        IPropertyValue t2GenValue = t2Gen.newPropertyValue(attribute);
        IPropertyValue p1GenValue = p1Gen.newPropertyValue(attribute);
        IPropertyValue p2GenValue = p2Gen.newPropertyValue(attribute);
        t1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2GenValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        p1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2GenValue.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        TemplatePropertyUsagePmo genValuePmo = new TemplatePropertyUsagePmo(p1GenValue, EFFECTIVE_DATE);
        assertThat(genValuePmo.getInheritingProductCmpts(), hasItems(p2));
    }

    @Test
    public void testGetDefiningProductCmpts() throws CoreException {
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
        IPropertyValue t1Value = t1.newPropertyValue(attribute);
        IPropertyValue t2Value = t2.newPropertyValue(attribute);
        IPropertyValue p1Value = p1.newPropertyValue(attribute);
        IPropertyValue p2Value = p2.newPropertyValue(attribute);
        t1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2Value.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo valuePmo = new TemplatePropertyUsagePmo(p1Value, EFFECTIVE_DATE);
        assertThat(valuePmo.getDefiningProductCmpts(), hasItems(p1, t2));

        // Property value of generations
        IPropertyValue t1GenValue = t1Gen.newPropertyValue(attribute);
        IPropertyValue t2GenValue = t2Gen.newPropertyValue(attribute);
        IPropertyValue p1GenValue = p1Gen.newPropertyValue(attribute);
        IPropertyValue p2GenValue = p2Gen.newPropertyValue(attribute);
        t1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        t2GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p1GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        p2GenValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo genValuePmo = new TemplatePropertyUsagePmo(p1GenValue, EFFECTIVE_DATE);
        assertThat(genValuePmo.getDefiningProductCmpts(), hasItems(p1, t2));
    }

    @Test
    public void testGetDefinedProductCmptValues() throws CoreException {
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

        TemplatePropertyUsagePmo pmo = new TemplatePropertyUsagePmo(p1Table, EFFECTIVE_DATE);
        Histogram<Object, IProductCmpt> histogram = pmo.getDefinedValuesHistogram();
        assertThat(histogram.getDistribution().size(), is(3));
        assertThat(histogram.getDistribution().get("Table-A"), hasItems(p1));
        assertThat(histogram.getDistribution().get("Table-B"), hasItems(p2, p2));

    }

    @Test
    public void testTemplatePropertyUsagePmo_ShowsTemplateProperty() throws CoreException {

        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();

        IProductCmpt t = newProductTemplate(productCmptType, "Template");
        IProductCmpt p = newProductCmpt(productCmptType, "Product");

        p.setTemplate(t.getQualifiedName());

        IPropertyValue templateValue = t.newPropertyValue(attribute);
        IPropertyValue prodCmptValue = p.newPropertyValue(attribute);

        templateValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        prodCmptValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo templatePmo = new TemplatePropertyUsagePmo(templateValue, EFFECTIVE_DATE);
        assertThat(templatePmo.getIpsObjectPartContainer(), is((IIpsObjectPartContainer)templateValue));
        assertThat(templatePmo.getTemplate(), is(t));

        TemplatePropertyUsagePmo prodCmptPmo = new TemplatePropertyUsagePmo(prodCmptValue, EFFECTIVE_DATE);
        assertThat(prodCmptPmo.getIpsObjectPartContainer(), is((IIpsObjectPartContainer)prodCmptValue));
        assertThat(prodCmptPmo.getTemplate(), is(t));
    }

    @Test
    public void testTemplatePropertyUsagePmo_NonTemplateProperty() throws CoreException {

        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();

        IProductCmpt prodCmpt = newProductCmpt(productCmptType, "Product");
        IPropertyValue propertyValue = prodCmpt.newPropertyValue(attribute);
        propertyValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        TemplatePropertyUsagePmo pmo = new TemplatePropertyUsagePmo(propertyValue, EFFECTIVE_DATE);
        assertThat(pmo.getTemplate(), is(nullValue()));
        assertThat(pmo.getDefiningProductCmpts().isEmpty(), is(true));
        assertThat(pmo.getInheritingProductCmpts().isEmpty(), is(true));
        assertThat(pmo.getDefinedValuesHistogram().getDistribution().isEmpty(), is(true));
    }

}
