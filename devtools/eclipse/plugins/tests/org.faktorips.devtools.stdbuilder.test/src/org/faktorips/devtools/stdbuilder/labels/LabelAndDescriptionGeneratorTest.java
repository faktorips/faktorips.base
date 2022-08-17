/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.labels;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.ipsproject.properties.SupportedLanguage;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.labels.LabelAndDescriptionProperties.MessageKey;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedProperties;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedPropertiesBuilder;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.junit.Test;

public class LabelAndDescriptionGeneratorTest extends AbstractIpsPluginTest {

    private static final String MY_QNAME = "myQName";

    private static final String MY_ENUM_QNAME = "myEnumQName";

    private IPolicyCmptType pcType;

    private IEnumType enumType;

    private IEnumContent enumContent;

    private IIpsProject ipsProject;

    public void setUpPcType() throws Exception {
        ipsProject = newIpsProject("myProject");
        pcType = newPolicyCmptType(ipsProject, MY_QNAME);
        enumType = newEnumType(ipsProject, MY_ENUM_QNAME);
        enumContent = newEnumContent(enumType, MY_ENUM_QNAME);
    }

    @Test
    public void testLoadMessagesFromFile() throws Exception {
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        AFile propertyFile = mock(AFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(propertyFile.getContents()).thenReturn(inputStream);

        new LabelAndDescriptionGenerator(propertyFile, new SupportedLanguage(Locale.GERMAN), builder);

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);

        when(propertyFile.exists()).thenReturn(true);

        new LabelAndDescriptionGenerator(propertyFile, new SupportedLanguage(Locale.GERMAN), builder);

        verify(propertyFile).getContents();
        verify(inputStream).close();
    }

    @Test
    public void testGenerate() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        AFile propertyFile = mock(AFile.class);
        InputStream inputStream = mock(InputStream.class);
        LabelAndDescriptionGenerator messagesGenerator = new LabelAndDescriptionGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);
        AbstractLocalizedProperties labelsAndDescriptions = messagesGenerator.getLocalizedProperties();

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
        assertFalse(labelsAndDescriptions.isModified());

        messagesGenerator.generate(pcType);
        messagesGenerator.generate(enumType);
        messagesGenerator.generate(enumContent);

        assertFalse(labelsAndDescriptions.isModified());

        setLabel(pcType, "foo");
        setDescription(pcType, "bar");
        setDescription(enumType, "et");
        setDescription(enumContent, "ec");

        messagesGenerator.generate(pcType);
        messagesGenerator.generate(enumType);
        messagesGenerator.generate(enumContent);
        assertTrue(labelsAndDescriptions.isModified());
        assertEquals(4, labelsAndDescriptions.size());

        messagesGenerator.saveIfModified();

        verify(propertyFile).create(any(InputStream.class), any(IProgressMonitor.class));

        reset(propertyFile);
        reset(inputStream);

        messagesGenerator.generate(pcType);
        messagesGenerator.generate(enumType);
        messagesGenerator.generate(enumContent);
        assertFalse(labelsAndDescriptions.isModified());

        verifyZeroInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
    }

    @Test
    public void testSafeIfModified() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        AFile propertyFile = mock(AFile.class);
        LabelAndDescriptionGenerator messagesGenerator = new LabelAndDescriptionGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified();

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        setLabel(pcType, "foo");
        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified();

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), any(IProgressMonitor.class));
    }

    @Test
    public void testSafeIfModified_notModified() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        AFile propertyFile = mock(AFile.class);
        LabelAndDescriptionGenerator messagesGenerator = new LabelAndDescriptionGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified();

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        setLabel(pcType, "foo");
        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified();

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), any(IProgressMonitor.class));

        messagesGenerator.loadMessages();
        messagesGenerator.generate(pcType);
        messagesGenerator.saveIfModified();

        verify(propertyFile, never()).setContents(any(InputStream.class), anyBoolean(),
                any(NullProgressMonitor.class));
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        LabelAndDescriptionGenerator labelAndDescriptionGenerator = new LabelAndDescriptionGenerator(mock(AFile.class),
                new SupportedLanguage(Locale.GERMAN), builder);

        setLabel(pcType, "foo");
        setDescription(pcType, "bar");

        IPolicyCmptType pcType2 = newPolicyCmptType(ipsProject, "pcType2");
        setLabel(pcType2, "foobar");

        labelAndDescriptionGenerator.addLabelAndDescription(pcType);
        labelAndDescriptionGenerator.addLabelAndDescription(pcType2);

        labelAndDescriptionGenerator.deleteAllMessagesFor(new QualifiedNameType(MY_QNAME,
                IpsObjectType.POLICY_CMPT_TYPE));

        assertThat(labelAndDescriptionGenerator.getLocalizedProperties().size(), is(equalTo(1)));
        assertThat(
                labelAndDescriptionGenerator.getLocalizedProperties().getMessage(
                        new MessageKey(pcType2, DocumentationKind.LABEL).getKey()),
                is(equalTo("foobar")));
    }

    @Test
    public void testAddLabelsAndDescriptions_emptyMessage() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        LabelAndDescriptionGenerator labelAndDescriptionGenerator = new LabelAndDescriptionGenerator(mock(AFile.class),
                new SupportedLanguage(Locale.GERMAN), builder);

        labelAndDescriptionGenerator.addLabelAndDescription(pcType);

        assertThat(labelAndDescriptionGenerator.getLocalizedProperties().isModified(), is(false));
        assertThat(labelAndDescriptionGenerator.getLocalizedProperties().size(), is(equalTo(0)));
    }

    @Test
    public void testGeneratePluralLabel() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        LabelAndDescriptionGenerator labelAndDescriptionGenerator = new LabelAndDescriptionGenerator(mock(AFile.class),
                new SupportedLanguage(Locale.GERMAN), builder);
        IAssociation association = pcType.newAssociation();
        setLabel(association, "asso", "assos");

        labelAndDescriptionGenerator.addLabelAndDescription(association);

        assertThat(
                labelAndDescriptionGenerator.getLocalizedProperties()
                        .getKeysForIpsObject(pcType.getQualifiedNameType()),
                hasItems(new MessageKey(association, DocumentationKind.LABEL), new MessageKey(association,
                        DocumentationKind.PLURAL_LABEL)));
    }

    private void setLabel(ILabeledElement labeledElement, String value) {
        setLabel(labeledElement, value, null);
    }

    private void setLabel(ILabeledElement labeledElement, String value, String pluralValue) {
        ILabel label = labeledElement.getLabel(Locale.GERMAN);
        if (label == null) {
            label = labeledElement.newLabel();
            label.setLocale(Locale.GERMAN);
        }
        label.setValue(value);
        if (StringUtils.isNotEmpty(pluralValue)) {
            label.setPluralValue(pluralValue);
        }
    }

    private void setDescription(IDescribedElement pcType, String text) {
        IDescription description = pcType.getDescription(Locale.GERMAN);
        if (description == null) {
            description = pcType.newDescription();
            description.setLocale(Locale.GERMAN);
        }
        description.setText(text);
    }

}
