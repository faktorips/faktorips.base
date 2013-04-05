/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
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

        IProject realProject = ipsProject.getProject();
        IProject spiedProject = spy(realProject);
        when(ipsProject.getProject()).thenReturn(spiedProject);

        IFolder folder = mock(IFolder.class);
        doReturn(folder).when(spiedProject).getFolder(anyString());

        IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);
        String s = createManifest();
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        when(file.getContents()).thenReturn(inputStream);

        when(spiedProject.getFile(IpsBundleManifest.MANIFEST_NAME)).thenReturn(file);

        properties = new IpsProjectProperties();
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
        assertTrue(props.isJavaProjectContainsClassesForDynamicDatatypes());
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
        IpsProjectProperties props = new IpsProjectProperties();
        props.initFromXml(ipsProject, docEl);
        return props;
    }

}
