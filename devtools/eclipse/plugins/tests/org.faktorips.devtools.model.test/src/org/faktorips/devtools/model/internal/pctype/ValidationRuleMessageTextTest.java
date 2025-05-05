/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.IValidationRuleMessageText;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;
import org.junit.Test;

public class ValidationRuleMessageTextTest {

    @Test
    public void testGetReplacementParameters() throws Exception {
        IValidationRuleMessageText internationalString = new ValidationRuleMessageText();

        internationalString.add(new LocalizedString(Locale.GERMAN, ""));
        Set<String> replacementParameters = internationalString.getReplacementParameters();
        assertEquals(0, replacementParameters.size());

        internationalString.add(new LocalizedString(Locale.GERMAN, "anc {abc123} afs"));
        replacementParameters = internationalString.getReplacementParameters();
        assertEquals(1, replacementParameters.size());
        Iterator<String> iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "{abc123} xyq {0}"));
        replacementParameters = internationalString.getReplacementParameters();
        assertEquals(2, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());
        assertEquals("0", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "abc {abc123} xyq {0} {abc123}"));
        replacementParameters = internationalString.getReplacementParameters();
        assertEquals(2, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());
        assertEquals("0", iterator.next());

        internationalString.add(new LocalizedString(Locale.ENGLISH, "abc {abc123} xyq {0} {abc123}"));
        replacementParameters = internationalString.getReplacementParameters();
        assertEquals(2, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("abc123", iterator.next());
        assertEquals("0", iterator.next());

        internationalString.add(new LocalizedString(Locale.GERMAN, "abc {0} xyq {1} {2}"));
        internationalString.add(new LocalizedString(Locale.ENGLISH, "abc {1} xyq {2} {3}"));
        replacementParameters = internationalString.getReplacementParameters();
        assertEquals(4, replacementParameters.size());
        iterator = replacementParameters.iterator();
        assertEquals("0", iterator.next());
        assertEquals("1", iterator.next());
        assertEquals("2", iterator.next());
        assertEquals("3", iterator.next());
    }

    @Test
    public void testValidateReplacementParameters() throws Exception {
        ValidationRuleMessageText validationRuleMessageText = new ValidationRuleMessageText();
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsProjectProperties projectProperties = mock(IIpsProjectProperties.class);
        ISupportedLanguage defaultLanguage = mock(ISupportedLanguage.class);
        Locale defaultLocale = Locale.of("testDefaultLocale");

        when(ipsProject.getReadOnlyProperties()).thenReturn(projectProperties);
        when(projectProperties.getDefaultLanguage()).thenReturn(defaultLanguage);
        when(defaultLanguage.getLocale()).thenReturn(defaultLocale);

        MessageList list = new MessageList();
        validationRuleMessageText.validateReplacementParameters(ipsProject, list);
        assertTrue(list.isEmpty());

        validationRuleMessageText.add(new LocalizedString(defaultLocale, "abc 123 {123}"));
        list = new MessageList();
        validationRuleMessageText.validateReplacementParameters(ipsProject, list);
        assertTrue(list.isEmpty());

        validationRuleMessageText.add(new LocalizedString(Locale.of("otherLocale"), "abc 123 {123}"));
        list = new MessageList();
        validationRuleMessageText.validateReplacementParameters(ipsProject, list);
        assertTrue(list.isEmpty());

        validationRuleMessageText.add(new LocalizedString(Locale.of("otherLocale2"), "abc 123 {xy123}"));
        list = new MessageList();
        validationRuleMessageText.validateReplacementParameters(ipsProject, list);
        assertFalse(list.isEmpty());
        assertTrue(list.getMessageByCode(IValidationRule.MSGCODE_MESSAGE_TEXT_PARAMETER_INVALID) != null);
    }
}
