/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class Migration_3_4_0Test extends AbstractIpsPluginTest {

    private IValidationRule rule;
    private PolicyCmptType policyCmptType;
    private IIpsProject ipsProject;
    private Element docEl;
    private IProductCmptType prodType;
    private IProductCmptTypeAttribute attribute;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.addSupportedLanguage(Locale.FRENCH);
        properties.addSupportedLanguage(Locale.ENGLISH);
        ipsProject.setProperties(properties);
        policyCmptType = newPolicyCmptType(ipsProject, "TestPCTYpe");
        rule = policyCmptType.newRule();
        rule.setName("Rule1");

        prodType = newProductCmptType(ipsProject, "TestProdType");
        attribute = (IProductCmptTypeAttribute)prodType.newAttribute();

        docEl = getTestDocument().getDocumentElement();
    }

    @Test
    public void migratePolicyCmptTypes() throws CoreException {
        // default values returned by the new meta model
        assertFalse(rule.isConfigurableByProductComponent());
        assertTrue(rule.isActivatedByDefault());

        rule.initFromXml(XmlUtil.getElement(docEl, 0));

        assertFalse(rule.isConfigurableByProductComponent());
        assertTrue(rule.isActivatedByDefault());

        policyCmptType.getIpsSrcFile().save(true, null);
        new Migration_3_4_0(ipsProject, "test").migrate(policyCmptType.getIpsSrcFile());
        policyCmptType.getIpsSrcFile().save(true, null);

        policyCmptType = (PolicyCmptType)policyCmptType.getIpsSrcFile().getIpsObject();
        rule = policyCmptType.getValidationRule("Rule1");
        assertFalse(rule.isConfigurableByProductComponent());
        assertTrue(rule.isActivatedByDefault());
    }

    @Test
    public void migratePreviouslyMigratedPolicyCmptTypes() throws CoreException {
        migratePolicyCmptTypes();

        policyCmptType = (PolicyCmptType)policyCmptType.getIpsSrcFile().getIpsObject();
        rule = policyCmptType.getValidationRule("Rule1");
        rule.setConfigurableByProductComponent(true);
        rule.setActivatedByDefault(false);

        policyCmptType.getIpsSrcFile().save(true, null);
        new Migration_3_4_0(ipsProject, "test").migrate(policyCmptType.getIpsSrcFile());
        policyCmptType.getIpsSrcFile().save(true, null);

        policyCmptType = (PolicyCmptType)policyCmptType.getIpsSrcFile().getIpsObject();
        rule = policyCmptType.getValidationRule("Rule1");
        assertTrue(rule.isConfigurableByProductComponent());
        assertFalse(rule.isActivatedByDefault());
    }

    @Test
    public void migrateProductCmptTypes() throws CoreException {
        // default values returned by the new meta model
        assertTrue(attribute.isChangingOverTime());

        attribute.initFromXml(XmlUtil.getElement(docEl, 1));

        assertTrue(attribute.isChangingOverTime());

        prodType.getIpsSrcFile().save(true, null);
        new Migration_3_4_0(ipsProject, "test").migrate(prodType.getIpsSrcFile());
        prodType.getIpsSrcFile().save(true, null);

        prodType = (ProductCmptType)prodType.getIpsSrcFile().getIpsObject();
        attribute = (IProductCmptTypeAttribute)prodType.getAttribute("StringTest");
        assertTrue(attribute.isChangingOverTime());
    }

    @Test
    public void migratePreviouslyMigratedProductCmptTypes() throws CoreException {
        migrateProductCmptTypes();

        prodType = (ProductCmptType)prodType.getIpsSrcFile().getIpsObject();
        attribute = (IProductCmptTypeAttribute)prodType.getAttribute("StringTest");
        attribute.setChangingOverTime(false);

        prodType.getIpsSrcFile().save(true, null);
        new Migration_3_4_0(ipsProject, "test").migrate(prodType.getIpsSrcFile());
        prodType.getIpsSrcFile().save(true, null);

        prodType = (ProductCmptType)prodType.getIpsSrcFile().getIpsObject();
        attribute = (IProductCmptTypeAttribute)prodType.getAttribute("StringTest");
        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void createsDefaultLabel() throws CoreException {
        List<ILabel> labels = rule.getLabels();
        for (ILabel label : labels) {
            label.delete();
        }
        assertEquals(0, rule.getLabels().size());

        policyCmptType.getIpsSrcFile().save(true, null);
        new Migration_3_4_0(ipsProject, "test").migrate(policyCmptType.getIpsSrcFile());
        policyCmptType.getIpsSrcFile().save(true, null);

        policyCmptType = (PolicyCmptType)policyCmptType.getIpsSrcFile().getIpsObject();
        rule = policyCmptType.getValidationRule("Rule1");
        assertNotNull(rule);
        assertEquals(3, rule.getLabels().size());
    }

}
