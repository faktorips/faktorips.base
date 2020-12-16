/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.htmlexport.context.AbstractHtmlExportPluginTest;
import org.faktorips.devtools.htmlexport.helper.AbstractTestLayouter;
import org.faktorips.devtools.htmlexport.helper.ContainsTextTestLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.junit.Test;

public class ExtensionPropertyTest extends AbstractHtmlExportPluginTest {

    private void addExtensionProperty(String id, String name, Class<?> type, String defaultValue) {
        IpsModel ipsModel = (IpsModel)ipsProject.getIpsModel();
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId(id);
        property.setName(name);
        property.setExtendedType(type);
        property.setDefaultValue(defaultValue);

        ipsModel.addIpsObjectExtensionProperty(property);
    }

    @Test
    public void testExtensionPropertyAtIpsObjectWithDefaultValue() throws CoreException {
        Class<PolicyCmptType> type = PolicyCmptType.class;
        String defaultValue = "defaultPolicy";
        final String name = "ExtensionPropertyAtIpsObject";
        String id = "ExtensionPropertyAtIpsObject_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

    @Test
    public void testExtensionPropertyAtIpsObjectWithoutDefaultValue() throws CoreException {
        Class<PolicyCmptType> type = PolicyCmptType.class;
        String defaultValue = null;
        final String name = "ExtensionPropertyAtIpsObject";
        String id = "ExtensionPropertyAtIpsObject_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

    @Test
    public void testExtensionPropertyAtIpsObjectWithSetValue() throws CoreException {
        Class<PolicyCmptType> type = PolicyCmptType.class;
        String defaultValue = null;
        final String name = "ExtensionPropertyAtIpsObject";
        String id = "ExtensionPropertyAtIpsObject_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        String setValue = "SetValueIpsObject";
        policy.setExtPropertyValue(id, setValue);

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);
        objectContentPage.build();

        String[] texts = new String[] { name, setValue };

        assertTextContained(objectContentPage, texts);
    }

    @Test
    public void testExtensionPropertyAtIpsObjectPartWithDefaultValue() throws CoreException {
        Class<PolicyCmptTypeAttribute> type = PolicyCmptTypeAttribute.class;
        String defaultValue = "defaultAttribut";
        final String name = "ExtensionPropertyAtIpsObjectPart";
        String id = "ExtensionPropertyAtIpsObjectPart_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
        policy.newPolicyCmptTypeAttribute();

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

    @Test
    public void testExtensionPropertyAtIpsObjectPartWithoutDefaultValue() throws CoreException {
        Class<PolicyCmptTypeAttribute> type = PolicyCmptTypeAttribute.class;
        String defaultValue = null;
        final String name = "ExtensionPropertyAtIpsObjectPart";
        String id = "ExtensionPropertyAtIpsObjectPart_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
        policy.newPolicyCmptTypeAttribute();

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

    @Test
    public void testExtensionPropertyAtIpsObjectPartWithSetValue() throws CoreException {
        Class<PolicyCmptTypeAttribute> type = PolicyCmptTypeAttribute.class;
        String defaultValue = null;
        final String name = "ExtensionPropertyAtIpsObjectPart";
        String id = "ExtensionPropertyAtIpsObjectPart_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
        IPolicyCmptTypeAttribute attribute = policy.newPolicyCmptTypeAttribute();

        String setValue = "SetValueIpsObjectPart";
        attribute.setExtPropertyValue(id, setValue);

        IPageElement objectContentPage = ContentPageUtil
                .createObjectContentPageElement(policy.getIpsSrcFile(), context);
        objectContentPage.build();

        String[] texts = new String[] { name, setValue };

        assertTextContained(objectContentPage, texts);
    }

    private void assertTextContained(IPageElement objectContentPage, String[] texts) {
        AbstractTestLayouter layouter = new ContainsTextTestLayouter(texts);
        objectContentPage.acceptLayouter(layouter);
        layouter.assertTest();
    }

}
