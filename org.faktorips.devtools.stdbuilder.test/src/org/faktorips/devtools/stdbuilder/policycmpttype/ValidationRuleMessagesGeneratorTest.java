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

package org.faktorips.devtools.stdbuilder.policycmpttype;

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
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.junit.Test;

public class ValidationRuleMessagesGeneratorTest extends AbstractValidationMessagesBuilderTest {

    @Test
    public void testLoadMessagesFromFile() throws Exception {
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(propertyFile.getContents()).thenReturn(inputStream);

        new ValidationRuleMessagesGenerator(propertyFile);

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);

        when(propertyFile.exists()).thenReturn(true);

        new ValidationRuleMessagesGenerator(propertyFile);

        verify(propertyFile).getContents();
        verify(inputStream).close();
    }

    @Test
    public void testGenerate() throws Exception {
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile);
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
        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn("rule2");

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
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile);

        messagesGenerator.saveIfModified("", false);

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);

        List<IValidationRule> vRulesList = new ArrayList<IValidationRule>();
        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn("rule1");
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
                mock(IFile.class));

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        when(pcType.getQualifiedName()).thenReturn("abc");

        IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        when(pcType2.getQualifiedName()).thenReturn("pcType2");

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn("rule1");
        when(validationRule1.getMessageText()).thenReturn("abc 123");
        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn("rule2");
        when(validationRule2.getMessageText()).thenReturn("xyz 123");

        IValidationRule otherRule = mockValidationRule(pcType);
        when(otherRule.getName()).thenReturn("otherRule");
        when(otherRule.getMessageText()).thenReturn("xyz otherRule");

        Set<String> ruleNames = new HashSet<String>();
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule1, ruleNames);
        validationRuleMessagesGenerator.addValidationRuleMessage(validationRule2, ruleNames);
        validationRuleMessagesGenerator.addValidationRuleMessage(otherRule, new HashSet<String>());

        List<IValidationRule> validationRules = new ArrayList<IValidationRule>();
        validationRules.add(validationRule1);

        validationRuleMessagesGenerator.deleteMessagesForDeletedRules("abc", validationRules, ruleNames);

        assertEquals(2, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals(
                "abc 123",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        ValidationRuleMessagesGenerator.getMessageKey(validationRule1)));
        assertEquals(
                "xyz otherRule",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        ValidationRuleMessagesGenerator.getMessageKey(otherRule)));
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class));

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        when(pcType.getQualifiedName()).thenReturn("abc");

        IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        when(pcType2.getQualifiedName()).thenReturn("pcType2");

        IValidationRule validationRule1 = mockValidationRule(pcType);
        when(validationRule1.getName()).thenReturn("rule1");
        when(validationRule1.getMessageText()).thenReturn("abc 123");
        IValidationRule validationRule2 = mockValidationRule(pcType);
        when(validationRule2.getName()).thenReturn("rule2");
        when(validationRule2.getMessageText()).thenReturn("xyz 123");

        IValidationRule otherRule = mockValidationRule(pcType);
        when(otherRule.getName()).thenReturn("otherRule");
        when(otherRule.getMessageText()).thenReturn("xyz otherRule");

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
                "xyz otherRule",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        ValidationRuleMessagesGenerator.getMessageKey(otherRule)));

    }

    @Test
    public void testGetMessageText() throws Exception {
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class));

        IValidationRule validationRule = mockValidationRule(null);

        String text = "";
        when(validationRule.getMessageText()).thenReturn(text);
        String result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("", result);

        text = "Abc 123 alles klar";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar", result);

        text = "Anc {abc123} afs";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Anc {0} afs", result);

        text = "Abc 123 alles klar {peter} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar {0} usw.", result);

        text = "x{0} Abc 123 alles klar {1} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("x{0} Abc 123 alles klar {1} usw.", result);

        text = "{abc} Abc 123 alles klar {xyz} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw.", result);

        text = "{abc,number} Abc 123 alles klar {xyz, date, long} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0,number} Abc 123 alles klar {1, date, long} usw.", result);

        // same parameter multiple times
        text = "{abc} Abc 123 alles klar {xyz} usw. blabla {xyz} asd {abc} soso";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw. blabla {1} asd {0} soso", result);
    }

}
