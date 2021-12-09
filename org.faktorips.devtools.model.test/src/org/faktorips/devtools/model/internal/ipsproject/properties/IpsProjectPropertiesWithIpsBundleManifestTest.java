/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.abstraction.AFolder;
import org.faktorips.devtools.model.abstraction.AProject;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class IpsProjectPropertiesWithIpsBundleManifestTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IIpsProjectProperties properties;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject realIpsProject = newIpsProject();
        ipsProject = spy(realIpsProject);

        AProject realProject = ipsProject.getProject();
        AProject spiedProject = spy(realProject);
        when(ipsProject.getProject()).thenReturn(spiedProject);

        AFolder folder = mock(AFolder.class);
        doReturn(folder).when(spiedProject).getFolder(anyString());

        AFile file = mock(AFile.class);
        when(file.exists()).thenReturn(true);
        String s = createManifest();
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        when(file.getContents()).thenReturn(inputStream);

        when(spiedProject.getFile(IpsBundleManifest.MANIFEST_NAME)).thenReturn(file);

        properties = new IpsProjectProperties(ipsProject);
        properties.addSupportedLanguage(Locale.ENGLISH);
        properties.addSupportedLanguage(Locale.GERMAN);

    }

    private String createManifest() {
        return "Fips-BasePackage: org.test.basepackage\n" + "Fips-SrcOutput: src\n" + "Fips-ResourceOutput: resource\n"
                + "Fips-ObjectDir: model;toc=\"fips-toc.xml\";messages=\"validation-messages.properties\",\n"
                + " test;toc=\"fips-toc-test.xml\";messages=\"validation-messages-test.properties\"\n\n"
                + "Name: test\n" + "Fips-SrcOutput: test\n" + "Fips-ResourceOutput: testResource\n";
    }

    @Test
    public void testInitFromXml() {
        IpsProjectProperties props = initPropertiesWithDocumentElement();
        assertTrue(props.isModelProject());
        assertTrue(props.isProductDefinitionProject());
        assertFalse(props.isDerivedUnionIsImplementedRuleEnabled());
        assertTrue(props.isReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled());
        assertEquals("myConvention", props.getChangesOverTimeNamingConventionIdForGeneratedCode());
        assertEquals("testPrefix", props.getRuntimeIdPrefix());

        DateBasedProductCmptNamingStrategy namingStrategy = (DateBasedProductCmptNamingStrategy)props
                .getProductCmptNamingStrategy();
        assertEquals(" ", namingStrategy.getVersionIdSeparator());
        assertEquals("yyyy-MM", namingStrategy.getDateFormatPattern());
        assertTrue(namingStrategy.isPostfixAllowed());
        assertEquals(ipsProject, namingStrategy.getIpsProject());

        assertEquals("org.faktorips.devtools.stdbuilder.ipsstdbuilderset", props.getBuilderSetId());

        IIpsObjectPath path = props.getIpsObjectPath();
        assertNotNull(path);
        assertEquals(3, path.getEntries().length);
    }

    protected IpsProjectProperties initPropertiesWithDocumentElement() {
        Element docEl = getTestDocument().getDocumentElement();
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.initFromXml(ipsProject, docEl);
        return props;
    }

}
