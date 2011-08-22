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

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.policycmpttype.AbstractValidationMessagesBuilderTest;
import org.faktorips.devtools.stdbuilder.policycmpttype.MessagesProperties;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessagesGenerator;
import org.junit.Test;

public class ValidationRuleMessagesGeneratorTest extends AbstractValidationMessagesBuilderTest {

    @Test
    public void testLoadMessagesFromFile() throws Exception {
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(propertyFile.getContents()).thenReturn(inputStream);

        new ValidationRuleMessagesGenerator(propertyFile, Locale.GERMAN);

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);

        when(propertyFile.exists()).thenReturn(true);

        new ValidationRuleMessagesGenerator(propertyFile, Locale.GERMAN);

        verify(propertyFile).getContents();
        verify(inputStream).close();
    }

    @Test
    public void testGenerate() throws Exception {
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                Locale.GERMAN);
        MessagesProperties validationMessages = messagesGenerator.getValidationMessages();

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
        assertFalse(validationMessages.isModified());

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);

        List<IValidationRule> vRulesList = new ArrayList<IValidationRule>();
        when(pcType.getValidationRules()).thenReturn(vRulesList);

        messagesGenerator.generate(pcType);

        assertFalse(validationMessages.isModified());

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn("rule1");
        when(validationRule1.getMessageText().get(any(Locale.class))).thenReturn(
                new LocalizedString(Locale.GERMAN, "anyMessage"));

        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn("rule2");
        when(validationRule2.getMessageText().get(any(Locale.class))).thenReturn(
                new LocalizedString(Locale.GERMAN, "anyMessage"));

        vRulesList.add(validationRule1);
        vRulesList.add(validationRule2);
        when(pcType.getValidationRules()).thenReturn(vRulesList);

        messagesGenerator.generate(pcType);
        assertTrue(validationMessages.isModified());
        assertEquals(2, validationMessages.size());

        messagesGenerator.saveIfModified("", false);

        verify(propertyFile).setContents(any(InputStream.class), anyBoolean(), anyBoolean(),
                any(IProgressMonitor.class));

        reset(propertyFile);
        reset(inputStream);

        messagesGenerator.generate(pcType);
        assertFalse(validationMessages.isModified());

        verifyZeroInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
    }

    @Test
    public void testSafeIfModified() throws Exception {
        IFile propertyFile = mock(IFile.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                Locale.GERMAN);

        messagesGenerator.saveIfModified("", false);

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);

        List<IValidationRule> vRulesList = new ArrayList<IValidationRule>();
        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn("rule1");
        when(validationRule1.getMessageText().get(any(Locale.class))).thenReturn(
                new LocalizedString(Locale.GERMAN, "anyMessage"));

        vRulesList.add(validationRule1);
        when(pcType.getValidationRules()).thenReturn(vRulesList);
        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified("", false);

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));

    }

    @Test
    public void testDeleteMessagesForDeletedRules() throws Exception {
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), Locale.GERMAN);

        IInternationalString msgTxt1 = mock(IInternationalString.class);
        when(msgTxt1.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text1"));
        IInternationalString msgTxt2 = mock(IInternationalString.class);
        when(msgTxt2.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text2"));
        IInternationalString msgTxt3 = mock(IInternationalString.class);
        when(msgTxt3.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text3"));

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        when(pcType.getQualifiedName()).thenReturn("abc");

        IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        when(pcType2.getQualifiedName()).thenReturn("pcType2");

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn("rule1");
        when(validationRule1.getMessageText()).thenReturn(msgTxt1);
        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn("rule2");
        when(validationRule2.getMessageText()).thenReturn(msgTxt2);

        IValidationRule otherRule = mockValidationRule(pcType);
        when(otherRule.getName()).thenReturn("otherRule");
        when(otherRule.getMessageText()).thenReturn(msgTxt3);

        Set<String> ruleNames = new HashSet<String>();
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule1, ruleNames);
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule2, ruleNames);
        validationRuleMessagesGenerator.addValidationRuleMessage(otherRule, new HashSet<String>());

        List<IValidationRule> validationRules = new ArrayList<IValidationRule>();
        validationRules.add(validationRule1);

        validationRuleMessagesGenerator.deleteMessagesForDeletedRules("abc", validationRules, ruleNames);

        assertEquals(2, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals(
                "text1",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        ValidationRuleMessagesGenerator.getMessageKey(validationRule1)));
        assertEquals(
                "text3",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        ValidationRuleMessagesGenerator.getMessageKey(otherRule)));
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), Locale.GERMAN);

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        when(pcType.getQualifiedName()).thenReturn("abc");

        IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        when(pcType2.getQualifiedName()).thenReturn("pcType2");

        IInternationalString msgTxt1 = mock(IInternationalString.class);
        when(msgTxt1.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text1"));
        IInternationalString msgTxt2 = mock(IInternationalString.class);
        when(msgTxt2.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text2"));
        IInternationalString msgTxt3 = mock(IInternationalString.class);
        when(msgTxt3.get(any(Locale.class))).thenReturn(new LocalizedString(Locale.GERMAN, "text3"));

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn("rule1");
        when(validationRule1.getMessageText()).thenReturn(msgTxt1);
        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn("rule2");
        when(validationRule2.getMessageText()).thenReturn(msgTxt2);

        IValidationRule otherRule = mockValidationRule(pcType);
        when(otherRule.getName()).thenReturn("otherRule");
        when(otherRule.getMessageText()).thenReturn(msgTxt3);

        Set<String> ruleNames = validationRuleMessagesGenerator.getRuleNames("abc");
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule1, ruleNames);
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule2, ruleNames);

        Set<String> ruleNames2 = validationRuleMessagesGenerator.getRuleNames("pcType2");
        validationRuleMessagesGenerator.addValidationRuleMessage(otherRule, ruleNames2);

        List<IValidationRule> validationRules = new ArrayList<IValidationRule>();
        validationRules.add(validationRule1);

        validationRuleMessagesGenerator.deleteAllMessagesFor("abc");

        assertEquals(1, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals(
                "text3",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        ValidationRuleMessagesGenerator.getMessageKey(otherRule)));

    }

    @Test
    public void testGetMessageText() throws Exception {
        Locale locale = Locale.GERMAN;
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), locale);

        IValidationRule validationRule = mockValidationRule(null);

        IInternationalString text = new InternationalString();

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
    }

    @Test
    public void testGetReplacementParameters() throws Exception {
        IInternationalString internationalString = new InternationalString();

        internationalString.add(new LocalizedString(Locale.GERMAN, ""));
        Set<String> replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(
                internationalString, true);
        assertEquals(0, replacementParameters.size());

        internationalString.add(new LocalizedString(Locale.GERMAN, "anc {abc123} afs"));
        replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(internationalString, true);
        assertEquals(1, replacementParameters.size());
        Iterator<String> iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "{abc123} xyq {0}"));
        replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(internationalString, true);
        assertEquals(2, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());
        assertEquals("p0", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "{abc123} xyq {0}"));
        replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(internationalString, false);
        assertEquals(2, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());
        assertEquals("0", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "abc {abc123} xyq {0} {abc123}"));
        replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(internationalString, true);
        assertEquals(2, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());
        assertEquals("p0", iterator.next());

        internationalString.add(new LocalizedString(Locale.ENGLISH, "abc {abc123} xyq {0} {abc123}"));
        replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(internationalString, true);
        assertEquals(2, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());
        assertEquals("p0", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "abc {0} xyq {1} {2}"));
        internationalString.add(new LocalizedString(Locale.ENGLISH, "abc {1} xyq {2} {3}"));
        replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(internationalString, true);
        assertEquals(4, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("p0", iterator.next());
        assertEquals("p1", iterator.next());
        assertEquals("p2", iterator.next());
        assertEquals("p3", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "abc {0} xyq {1} {2}"));
        internationalString.add(new LocalizedString(Locale.ENGLISH, "abc {1} xyq {2} {3}"));
        replacementParameters = ValidationRuleMessagesGenerator.getReplacementParameters(internationalString, false);
        assertEquals(4, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("0", iterator.next());
        assertEquals("1", iterator.next());
        assertEquals("2", iterator.next());
        assertEquals("3", iterator.next());
    }

}
