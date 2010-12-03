/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractHtmlExportTest;
import org.faktorips.devtools.htmlexport.test.helper.AbstractTestLayouter;
import org.faktorips.devtools.htmlexport.test.helper.ContainsTextTestLayouter;

public class ExtensionPropertyTest extends AbstractHtmlExportTest {

    private void addExtensionProperty(String id, String name, Class<?> type, String defaultValue) {
        IpsModel ipsModel = (IpsModel)ipsProject.getIpsModel();
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId(id);
        property.setName(name);
        property.setExtendedType(type);
        property.setDefaultValue(defaultValue);

        ipsModel.addIpsObjectExtensionProperty(property);
    }

    public void testExtensionPropertyAtIpsObjectWithDefaultValue() throws CoreException {
        Class<PolicyCmptType> type = PolicyCmptType.class;
        String defaultValue = "defaultPolicy";
        final String name = "ExtensionPropertyAtIpsObject";
        String id = "ExtensionPropertyAtIpsObject_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

    public void testExtensionPropertyAtIpsObjectWithoutDefaultValue() throws CoreException {
        Class<PolicyCmptType> type = PolicyCmptType.class;
        String defaultValue = null;
        final String name = "ExtensionPropertyAtIpsObject";
        String id = "ExtensionPropertyAtIpsObject_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

    public void testExtensionPropertyAtIpsObjectWithSetValue() throws CoreException {
        Class<PolicyCmptType> type = PolicyCmptType.class;
        String defaultValue = null;
        final String name = "ExtensionPropertyAtIpsObject";
        String id = "ExtensionPropertyAtIpsObject_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        String setValue = "SetValueIpsObject";
        policy.setExtPropertyValue(id, setValue);

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                context);
        objectContentPage.build();

        String[] texts = new String[] { name, setValue };

        assertTextContained(objectContentPage, texts);
    }

    public void testExtensionPropertyAtIpsObjectPartWithDefaultValue() throws CoreException {
        Class<PolicyCmptTypeAttribute> type = PolicyCmptTypeAttribute.class;
        String defaultValue = "defaultAttribut";
        final String name = "ExtensionPropertyAtIpsObjectPart";
        String id = "ExtensionPropertyAtIpsObjectPart_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
        policy.newPolicyCmptTypeAttribute();

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

    public void testExtensionPropertyAtIpsObjectPartWithoutDefaultValue() throws CoreException {
        Class<PolicyCmptTypeAttribute> type = PolicyCmptTypeAttribute.class;
        String defaultValue = null;
        final String name = "ExtensionPropertyAtIpsObjectPart";
        String id = "ExtensionPropertyAtIpsObjectPart_ID";

        addExtensionProperty(id, name, type, defaultValue);

        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");
        policy.newPolicyCmptTypeAttribute();

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                context);
        objectContentPage.build();

        String[] texts = new String[] { name, defaultValue };

        assertTextContained(objectContentPage, texts);
    }

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

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                context);
        objectContentPage.build();

        String[] texts = new String[] { name, setValue };

        assertTextContained(objectContentPage, texts);
    }

    private void assertTextContained(AbstractPageElement objectContentPage, String[] texts) {
        AbstractTestLayouter layouter = new ContainsTextTestLayouter(texts);
        objectContentPage.acceptLayouter(layouter);
        layouter.assertTest();
    }

}
