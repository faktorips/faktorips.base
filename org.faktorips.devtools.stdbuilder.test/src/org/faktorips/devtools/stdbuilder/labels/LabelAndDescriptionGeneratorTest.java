/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.labels;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.SupportedLanguage;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.labels.LabelAndDescriptionProperties.MessageKey;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedProperties;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedPropertiesBuilder;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.junit.Test;

public class LabelAndDescriptionGeneratorTest extends AbstractIpsPluginTest {

    private static final String MY_QNAME = "myQName";

    private IPolicyCmptType pcType;

    private IIpsProject ipsProject;

    public void setUpPcType() throws Exception {
        ipsProject = newIpsProject("myProject");
        pcType = newPolicyCmptType(ipsProject, MY_QNAME);
    }

    @Test
    public void testLoadMessagesFromFile() throws Exception {
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
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
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        LabelAndDescriptionGenerator messagesGenerator = new LabelAndDescriptionGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);
        AbstractLocalizedProperties labelsAndDescriptions = messagesGenerator.getLocalizedProperties();

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
        assertFalse(labelsAndDescriptions.isModified());

        messagesGenerator.generate(pcType);

        assertFalse(labelsAndDescriptions.isModified());

        setLabel(pcType, "foo");
        setDescription(pcType, "bar");

        messagesGenerator.generate(pcType);
        assertTrue(labelsAndDescriptions.isModified());
        assertEquals(2, labelsAndDescriptions.size());

        messagesGenerator.saveIfModified("");

        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));

        reset(propertyFile);
        reset(inputStream);

        messagesGenerator.generate(pcType);
        assertFalse(labelsAndDescriptions.isModified());

        verifyZeroInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
    }

    @Test
    public void testSafeIfModified() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        LabelAndDescriptionGenerator messagesGenerator = new LabelAndDescriptionGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        setLabel(pcType, "foo");
        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));
    }

    @Test
    public void testSafeIfModified_notModified() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        LabelAndDescriptionGenerator messagesGenerator = new LabelAndDescriptionGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        setLabel(pcType, "foo");
        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));

        messagesGenerator.loadMessages();
        messagesGenerator.generate(pcType);
        messagesGenerator.saveIfModified("");

        verify(propertyFile, never()).setContents(any(InputStream.class), anyBoolean(), anyBoolean(),
                any(NullProgressMonitor.class));
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        LabelAndDescriptionGenerator labelAndDescriptionGenerator = new LabelAndDescriptionGenerator(mock(IFile.class),
                new SupportedLanguage(Locale.GERMAN), builder);

        setLabel(pcType, "foo");
        setDescription(pcType, "bar");

        IPolicyCmptType pcType2 = newPolicyCmptType(ipsProject, "pcType2");
        setLabel(pcType2, "foobar");

        labelAndDescriptionGenerator.addLabelAndDescription(pcType);
        labelAndDescriptionGenerator.addLabelAndDescription(pcType2);

        labelAndDescriptionGenerator.deleteAllMessagesFor(MY_QNAME);

        assertThat(labelAndDescriptionGenerator.getLocalizedProperties().size(), is(equalTo(1)));
        assertThat(
                labelAndDescriptionGenerator.getLocalizedProperties().getMessage(
                        new MessageKey(pcType2, DocumentationType.LABEL).getKey()), is(equalTo("foobar")));
    }

    @Test
    public void testAddLabelsAndDescriptions_emptyMessage() throws Exception {
        setUpPcType();
        AbstractLocalizedPropertiesBuilder builder = mock(LabelAndDescriptionPropertiesBuilder.class);
        LabelAndDescriptionGenerator labelAndDescriptionGenerator = new LabelAndDescriptionGenerator(mock(IFile.class),
                new SupportedLanguage(Locale.GERMAN), builder);

        labelAndDescriptionGenerator.addLabelAndDescription(pcType);

        assertThat(labelAndDescriptionGenerator.getLocalizedProperties().isModified(), is(false));
        assertThat(labelAndDescriptionGenerator.getLocalizedProperties().size(), is(equalTo(0)));
    }

    private void setLabel(IPolicyCmptType pcType, String value) {
        ILabel label = pcType.getLabel(Locale.GERMAN);
        if (label == null) {
            label = pcType.newLabel();
            label.setLocale(Locale.GERMAN);
        }
        label.setValue(value);
    }

    private void setDescription(IPolicyCmptType pcType, String text) {
        IDescription description = pcType.getDescription(Locale.GERMAN);
        if (description == null) {
            description = pcType.newDescription();
            description.setLocale(Locale.GERMAN);
        }
        description.setText(text);
    }

}
