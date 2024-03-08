/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.propertybuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.propertybuilder.AbstractLocalizedPropertiesBuilder;
import org.faktorips.devtools.model.builder.propertybuilder.AbstractPropertiesGenerator;
import org.faktorips.devtools.model.internal.ipsproject.properties.SupportedLanguage;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.junit.Test;

public class AbstractLocalizedPropertiesBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private TestBuilder builder;
    private AFile propertyFile;
    private ISupportedLanguage supportedLanguage;

    private void setUpBuilderAndDependencies() throws Exception {
        supportedLanguage = new SupportedLanguage(Locale.GERMAN);
        ipsProject = newIpsProject();
        root = ipsProject.findIpsPackageFragmentRoot("productdef");
        builder = new TestBuilder(ipsProject.getIpsArtefactBuilderSet());
        propertyFile = builder.getPropertyFile(root, supportedLanguage);
    }

    @Test
    public void testBeforeBuildProcess_VerifyCreateParentFolder() throws Exception {
        setUpBuilderAndDependencies();

        builder.beforeBuildProcess(ipsProject, ABuildKind.INCREMENTAL);

        assertTrue(propertyFile.getParent().exists());
    }

    @Test
    public void testBeforeBuildProcess_LoadFor_FULL_BUILD() throws Exception {
        setUpBuilderAndDependencies();

        builder.beforeBuildProcess(ipsProject, ABuildKind.FULL);

        verify(builder.generator, times(ipsProject.getReadOnlyProperties().getSupportedLanguages().size()))
                .loadMessages();
    }

    @Test
    public void testAfterBuildProcess() throws Exception {
        setUpBuilderAndDependencies();

        builder.afterBuildProcess(ipsProject, ABuildKind.FULL);

        verify(builder.generator, times(ipsProject.getReadOnlyProperties().getSupportedLanguages().size()))
                .saveIfModified();
    }

    @Test
    public void testGetPropertyFile() throws Exception {
        setUpBuilderAndDependencies();

        AFile file = builder.getPropertyFile(root, supportedLanguage);

        assertEquals("property-file_de.properties", file.getName());
    }

    @Test
    public void testDelete() throws Exception {
        setUpBuilderAndDependencies();
        IIpsSrcFile ipsSrcFile = newPolicyCmptType(root, "Any").getIpsSrcFile();

        builder.delete(ipsSrcFile);

        verify(builder.generator, times(ipsProject.getReadOnlyProperties().getSupportedLanguages().size()))
                .deleteAllMessagesFor(ipsSrcFile.getQualifiedNameType());
    }

    private static class TestBuilder extends AbstractLocalizedPropertiesBuilder {

        private AbstractPropertiesGenerator generator = mock(AbstractPropertiesGenerator.class);

        public TestBuilder(IIpsArtefactBuilderSet builderSet) {
            super(builderSet, new LocalizedStringsSet(AbstractLocalizedPropertiesBuilder.class));
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) {
            // nothing to do
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
            return true;
        }

        @Override
        protected String getResourceBundleBaseName(IIpsSrcFolderEntry entry) {
            return "property-file";
        }

        @Override
        protected AbstractPropertiesGenerator createNewMessageGenerator(AFile propertyFile,
                ISupportedLanguage supportedLanguage) {
            return generator;
        }

        /**
         * need to prevent cast to StandardBuilderSet
         */
        @Override
        public String getLocalizedText(String key, Object replacement) {
            return "test text";
        }

    }

}
