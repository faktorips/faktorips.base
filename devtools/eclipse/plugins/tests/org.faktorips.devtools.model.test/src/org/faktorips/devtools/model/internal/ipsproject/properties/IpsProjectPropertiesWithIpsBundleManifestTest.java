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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoSession;
import org.w3c.dom.Element;

public class IpsProjectPropertiesWithIpsBundleManifestTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IIpsProjectProperties properties;

    private MockitoSession mockito;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        mockito = createMocks(this);
        super.setUp();

        IIpsProject realIpsProject = newIpsProject();
        ipsProject = spy(realIpsProject);

        AProject realProject = ipsProject.getProject();
        AProject spiedProject = spy(realProject);
        lenient().when(ipsProject.getProject()).thenReturn(spiedProject);

        AFolder folder = mock(AFolder.class);
        lenient().when(folder.getProjectRelativePath()).thenReturn(Path.of("anyfolder"));
        lenient().doReturn(folder).when(spiedProject).getFolder(anyString());

        AFile file = mock(AFile.class);
        lenient().when(file.exists()).thenReturn(true);
        String s = createManifest();
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        lenient().when(file.getContents()).thenReturn(inputStream);

        lenient().when(spiedProject.getFile(IpsBundleManifest.MANIFEST_NAME)).thenReturn(file);

        properties = new IpsProjectProperties(ipsProject);
        properties.addSupportedLanguage(Locale.ENGLISH);
        properties.addSupportedLanguage(Locale.GERMAN);

    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            mockito.finishMocking();
        }
    }

    private String createManifest() {
        return """
                Fips-BasePackage: org.test.basepackage
                Fips-SourcecodeOutput: src
                Fips-ResourceOutput: resource
                Fips-ObjectDir: model;toc="fips-toc.xml";validation-messages="validation-messages.properties",
                 test;toc="fips-toc-test.xml";validation-messages="validation-messages-test.properties"

                Name: test
                Fips-SourcecodeOutput: test
                Fips-ResourceOutput: testResource
                """;
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
        assertEquals(Abstractions.isEclipseRunning() ? 3 : 2, path.getEntries().length);
    }

    protected IpsProjectProperties initPropertiesWithDocumentElement() {
        Element docEl = getTestDocument().getDocumentElement();
        IpsProjectProperties props = new IpsProjectProperties(ipsProject);
        props.initFromXml(ipsProject, docEl);
        return props;
    }

}
