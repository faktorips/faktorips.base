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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Test;

public class SetTemplateValueStatusOperationTest extends AbstractIpsPluginTest {

    @Test
    public void testRun() throws CoreRuntimeException {
        IIpsProject ipsProject = newIpsProject();
        ProductCmptType type = newProductCmptType(ipsProject, "Type");
        IProductCmptTypeAttribute attribute = type.newProductCmptTypeAttribute("attribute");

        ProductCmpt t = newProductTemplate(ipsProject, "Template");
        ProductCmpt p1 = newProductCmpt(type, "Prod-1");
        ProductCmpt p2 = newProductCmpt(type, "Prod-2");

        p1.setTemplate(t.getQualifiedName());
        p2.setTemplate(t.getQualifiedName());

        t.newPropertyValue(attribute, IAttributeValue.class);
        IPropertyValue p1Value = p1.newPropertyValue(attribute, IAttributeValue.class);
        IPropertyValue p2Value = p2.newPropertyValue(attribute, IAttributeValue.class);

        List<IPropertyValue> propertyValues = Lists.newArrayList(p1Value, p2Value);
        IProgressMonitor monitor = new NullProgressMonitor();

        new SetTemplateValueStatusOperation(propertyValues, TemplateValueStatus.INHERITED).run(monitor);
        assertThat(p1Value.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(p2Value.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));

        new SetTemplateValueStatusOperation(propertyValues, TemplateValueStatus.DEFINED).run(monitor);
        assertThat(p1Value.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(p2Value.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
    }
}
