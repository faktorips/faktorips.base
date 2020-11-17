/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.ipsobject;

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.SimpleCustomValidationForProductCmptAttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.junit.Test;

public class SimpleCustomValidationForProductCmptAttributeValueTest extends AbstractIpsPluginTest {

    @Test
    public void testValidate() throws CoreException {

        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "ProdType");
        IProductCmptTypeAttribute productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute("foo");
        SimpleCustomValidationForProductCmptAttributeValue validation = new SimpleCustomValidationForProductCmptAttributeValue(
                productCmptType.getName(), productCmptTypeAttribute.getName()) {

            @Override
            public ValidationResult validate(String value, IIpsProject ipsProject) throws CoreException {
                return newError("4711", "bar");
            }
        };
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "Prod");
        IAttributeValue attributeValue = productCmpt.newPropertyValue(productCmptTypeAttribute, IAttributeValue.class);

        MessageList messageList = validation.validate(attributeValue, ipsProject);

        assertThat(messageList, hasMessageCode("4711"));
        Message message = messageList.getMessageByCode("4711");
        assertThat(message.getText(), is("bar"));
        assertThat(message.getSeverity(), is(Message.ERROR));
        ObjectProperty[] invalidObjectProperties = message.getInvalidObjectProperties();
        assertThat(invalidObjectProperties.length, is(1));
        assertThat(invalidObjectProperties[0].getObject(), is((Object)attributeValue));
        assertThat(invalidObjectProperties[0].getProperty(), is(IAttributeValue.PROPERTY_VALUE_HOLDER));
    }

}
