/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.internal.model.ipsproject.SupportedLanguage;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRuleMessageText;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.IValidationRuleMessageText;
import org.faktorips.devtools.stdbuilder.policycmpttype.AbstractValidationMessagesBuilderTest;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleMessagesGeneratorTest extends AbstractValidationMessagesBuilderTest {

    private static final String RULE_NAME_1 = "rule1";
    private static final String RULE_NAME_2 = "rule2";
    private static final String MY_QNAME = "myQName";
    private static final String QNAME_RULE1 = MY_QNAME + IValidationRule.QNAME_SEPARATOR + RULE_NAME_1;
    private static final String QNAME_RULE2 = MY_QNAME + IValidationRule.QNAME_SEPARATOR + RULE_NAME_2;
    @Mock
    private IPolicyCmptType pcType;

    @Before
    public void setUpPcType() {
        when(pcType.getQualifiedName()).thenReturn(MY_QNAME);
    }

    @Test
    public void testLoadMessagesFromFile() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(propertyFile.getContents()).thenReturn(inputStream);

        new ValidationRuleMessagesGenerator(propertyFile, new SupportedLanguage(Locale.GERMAN), builder);

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);

        when(propertyFile.exists()).thenReturn(true);

        new ValidationRuleMessagesGenerator(propertyFile, new SupportedLanguage(Locale.GERMAN), builder);

        verify(propertyFile).getContents();
        verify(inputStream).close();
    }

    @Test
    public void testGenerate() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);
        ValidationRuleMessageProperties validationMessages = messagesGenerator.getValidationMessages();

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
        assertFalse(validationMessages.isModified());

        List<IValidationRule> vRulesList = new ArrayList<IValidationRule>();
        when(pcType.getValidationRules()).thenReturn(vRulesList);

        messagesGenerator.generate(pcType);

        assertFalse(validationMessages.isModified());

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn(RULE_NAME_1);
        when(validationRule1.getQualifiedRuleName()).thenReturn(QNAME_RULE1);
        when(validationRule1.getMessageText().get(any(Locale.class))).thenReturn(
                new LocalizedString(Locale.GERMAN, "anyMessage"));

        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn(RULE_NAME_2);
        when(validationRule2.getQualifiedRuleName()).thenReturn(QNAME_RULE2);
        when(validationRule2.getMessageText().get(any(Locale.class))).thenReturn(
                new LocalizedString(Locale.GERMAN, "anyMessage"));

        vRulesList.add(validationRule1);
        vRulesList.add(validationRule2);
        when(pcType.getValidationRules()).thenReturn(vRulesList);
        when(pcType.getValidationRule(RULE_NAME_1)).thenReturn(validationRule1);
        when(pcType.getValidationRule(RULE_NAME_2)).thenReturn(validationRule2);

        messagesGenerator.generate(pcType);
        assertTrue(validationMessages.isModified());
        assertEquals(2, validationMessages.size());

        messagesGenerator.saveIfModified("");

        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));

        reset(propertyFile);
        reset(inputStream);

        messagesGenerator.generate(pcType);
        assertFalse(validationMessages.isModified());

        verifyZeroInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
    }

    @Test
    public void testSafeIfModified() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        List<IValidationRule> vRulesList = new ArrayList<IValidationRule>();
        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn(RULE_NAME_1);
        when(validationRule1.getQualifiedRuleName()).thenReturn(QNAME_RULE1);
        when(validationRule1.getMessageText().get(any(Locale.class))).thenReturn(
                new LocalizedString(Locale.GERMAN, "anyMessage"));

        vRulesList.add(validationRule1);
        when(pcType.getValidationRules()).thenReturn(vRulesList);
        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));
    }

    @Test
    public void testSafeIfModified_notModified() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        List<IValidationRule> vRulesList = new ArrayList<IValidationRule>();
        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn(RULE_NAME_1);
        when(validationRule1.getQualifiedRuleName()).thenReturn(QNAME_RULE1);
        when(validationRule1.getMessageText().get(any(Locale.class))).thenReturn(
                new LocalizedString(Locale.GERMAN, "anyMessage"));

        vRulesList.add(validationRule1);
        when(pcType.getValidationRules()).thenReturn(vRulesList);
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
    public void testDeleteMessagesForDeletedRules() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN), builder);

        IValidationRuleMessageText msgTxt1 = mock(IValidationRuleMessageText.class);
        when(msgTxt1.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text1"));
        IValidationRuleMessageText msgTxt2 = mock(IValidationRuleMessageText.class);
        when(msgTxt2.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text2"));
        IValidationRuleMessageText msgTxt3 = mock(IValidationRuleMessageText.class);
        when(msgTxt3.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text3"));

        IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        when(pcType2.getQualifiedName()).thenReturn("pcType2");

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn(RULE_NAME_1);
        when(validationRule1.getQualifiedRuleName()).thenReturn(QNAME_RULE1);
        when(validationRule1.getMessageText()).thenReturn(msgTxt1);
        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn(RULE_NAME_2);
        when(validationRule2.getQualifiedRuleName()).thenReturn(QNAME_RULE2);
        when(validationRule2.getMessageText()).thenReturn(msgTxt2);

        IValidationRule otherRule = mockValidationRule(pcType2);
        when(otherRule.getName()).thenReturn("otherRule");
        when(otherRule.getQualifiedRuleName()).thenReturn("myQName2-otherRule");
        when(otherRule.getMessageText()).thenReturn(msgTxt3);

        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule1);
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule2);
        validationRuleMessagesGenerator.addValidationRuleMessage(otherRule);

        when(pcType.getValidationRule(RULE_NAME_1)).thenReturn(validationRule1);

        validationRuleMessagesGenerator.deleteMessagesForDeletedRules(pcType);

        assertEquals(2, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals(
                "text1",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        validationRule1.getQualifiedRuleName()));
        assertEquals("text3",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(otherRule.getQualifiedRuleName()));
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN), builder);

        IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        when(pcType2.getQualifiedName()).thenReturn("pcType2");

        IValidationRuleMessageText msgTxt1 = mock(IValidationRuleMessageText.class);
        when(msgTxt1.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text1"));
        IValidationRuleMessageText msgTxt2 = mock(IValidationRuleMessageText.class);
        when(msgTxt2.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text2"));
        IValidationRuleMessageText msgTxt3 = mock(IValidationRuleMessageText.class);
        when(msgTxt3.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text3"));

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn(RULE_NAME_1);
        when(validationRule1.getQualifiedRuleName()).thenReturn(QNAME_RULE1);
        when(validationRule1.getMessageText()).thenReturn(msgTxt1);
        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn(RULE_NAME_2);
        when(validationRule2.getQualifiedRuleName()).thenReturn(QNAME_RULE2);
        when(validationRule2.getMessageText()).thenReturn(msgTxt2);

        IValidationRule otherRule = mockValidationRule(pcType2);
        when(otherRule.getName()).thenReturn("otherRule");
        when(otherRule.getQualifiedRuleName()).thenReturn("pcType2-otherRule");
        when(otherRule.getMessageText()).thenReturn(msgTxt3);
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule1);
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule2);
        validationRuleMessagesGenerator.addValidationRuleMessage(otherRule);

        validationRuleMessagesGenerator.deleteAllMessagesFor(MY_QNAME);

        assertEquals(1, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals("text3", validationRuleMessagesGenerator.getValidationMessages().getMessage("pcType2-otherRule"));
    }

    @Test
    public void testAddValidationRuleMessage_emptyMessage() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN), builder);
        IValidationRuleMessageText msgTxt1 = mock(IValidationRuleMessageText.class);
        when(msgTxt1.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, ""));
        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn(RULE_NAME_1);
        when(validationRule1.getQualifiedRuleName()).thenReturn(QNAME_RULE1);
        when(validationRule1.getMessageText()).thenReturn(msgTxt1);

        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule1);

        assertFalse(validationRuleMessagesGenerator.getValidationMessages().isModified());
        assertEquals(0, validationRuleMessagesGenerator.getValidationMessages().size());
    }

    @Test
    public void testAddValidationRuleMessage_emptyMessageDefaultLang() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN, true), builder);
        IValidationRuleMessageText msgTxt1 = mock(IValidationRuleMessageText.class);
        when(msgTxt1.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, ""));
        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn(RULE_NAME_1);
        when(validationRule1.getQualifiedRuleName()).thenReturn(QNAME_RULE1);
        when(validationRule1.getMessageText()).thenReturn(msgTxt1);

        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule1);

        assertTrue(validationRuleMessagesGenerator.getValidationMessages().isModified());
        assertEquals(1, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals("", validationRuleMessagesGenerator.getValidationMessages().getMessage(QNAME_RULE1));
    }

    @Test
    public void testGetMessageText() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        Locale locale = Locale.GERMAN;
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(locale), builder);

        IValidationRule validationRule = mockValidationRule(null);

        IValidationRuleMessageText text = new ValidationRuleMessageText();

        text.add(new LocalizedString(locale, ""));
        when(validationRule.getMessageText()).thenReturn(text);
        String result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("", result);

        text.add(new LocalizedString(locale, "Abc 123 alles klar"));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar", result);

        text.add(new LocalizedString(locale, "Anc {abc123} afs"));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Anc {0} afs", result);

        text.add(new LocalizedString(locale, "Abc 123 alles klar {peter} usw."));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar {0} usw.", result);

        text.add(new LocalizedString(locale, "x{0} Abc 123 alles klar {1} usw."));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("x{0} Abc 123 alles klar {1} usw.", result);

        text.add(new LocalizedString(locale, "{abc} Abc 123 alles klar {xyz} usw."));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw.", result);

        text.add(new LocalizedString(locale, "{abc,number} Abc 123 alles klar {xyz, date, long} usw."));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0,number} Abc 123 alles klar {1, date, long} usw.", result);

        // same parameter multiple times
        text.add(new LocalizedString(locale, "{abc} Abc 123 alles klar {xyz} usw. blabla {xyz} asd {abc} soso"));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw. blabla {1} asd {0} soso", result);

        // parameter with underscore
        text.add(new LocalizedString(locale, "{abc_xyz} asdfsdaf"));
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} asdfsdaf", result);

    }

}
