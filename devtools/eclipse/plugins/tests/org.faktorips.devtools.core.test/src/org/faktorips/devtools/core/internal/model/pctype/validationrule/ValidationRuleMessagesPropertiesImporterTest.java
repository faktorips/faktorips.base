/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.model.internal.pctype.ValidationRuleMessageText;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.values.LocalizedString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ValidationRuleMessagesPropertiesImporterTest {

    private static final String MSG_CODE = "MyMsgCode";

    private static final String TEST_POLICY_TEST_RULE = "testPolicy-testRule";

    private static final String TEST_FILE = "org/faktorips/devtools/core/internal/model/pctype/validationrule/validation-test-messages.properties";

    @Mock
    private IIpsPackageFragmentRoot root;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Mock
    private IPolicyCmptType policyCmptType;

    @Mock
    private IValidationRule rule;

    @Mock
    private InputStream inputStream;

    private ValidationRuleMessagesPropertiesImporter importer;

    @BeforeEach
    public void setUp() {
        lenient().when(ipsSrcFile.isMutable()).thenReturn(true);
        List<IIpsSrcFile> srcFiles = new ArrayList<>();
        srcFiles.add(ipsSrcFile);
        lenient().when(root.findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(srcFiles);
        lenient().when(policyCmptType.getQualifiedName()).thenReturn("testPolicy");
        lenient().when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);

        importer = new ValidationRuleMessagesPropertiesImporter(inputStream, root, Locale.GERMAN);

    }

    @Test
    public void testImport() throws Exception {
        inputStream = getClass().getClassLoader().getResourceAsStream(TEST_FILE);

        IStatus status = importer.loadContent();

        assertEquals(IStatus.OK, status.getSeverity());
        verifyNoMoreInteractions(root);
    }

    @Test
    public void testImputStreamClose() throws Exception {

        IStatus status = importer.loadContent();

        assertEquals(IStatus.OK, status.getSeverity());
        verify(inputStream).close();

    }

    @Test
    public void shouldImportNothing() throws Exception {
        Properties properties = new Properties();
        importer.setProperties(properties);
        IStatus result = importer.importContentMap();

        assertTrue(result.isOK());

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusOK() throws Exception {
        mockRule();
        Properties properties = new Properties();
        properties.setProperty(TEST_POLICY_TEST_RULE, "TestMessage");

        importer.setProperties(properties);
        IStatus result = importer.importContentMap();

        assertTrue(result.isOK(), result.toString());
        assertEquals(new LocalizedString(Locale.GERMAN, "TestMessage"), rule.getMessageText().get(Locale.GERMAN));

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusOK_byMsgCode() throws Exception {
        mockRule();
        Properties properties = new Properties();
        properties.setProperty(MSG_CODE, "TestMessage");
        importer.setMethodOfIdentification(ValidationRuleIdentification.MESSAGE_CODE);

        importer.setProperties(properties);
        IStatus result = importer.importContentMap();

        assertTrue(result.isOK(), result.toString());
        assertEquals(new LocalizedString(Locale.GERMAN, "TestMessage"), rule.getMessageText().get(Locale.GERMAN));

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    void mockRule() {
        lenient().when(rule.getQualifiedRuleName()).thenReturn(TEST_POLICY_TEST_RULE);
        lenient().when(rule.getMessageCode()).thenReturn(MSG_CODE);
        when(rule.getMessageText()).thenReturn(new ValidationRuleMessageText());
        ArrayList<IValidationRule> rules = new ArrayList<>();
        rules.add(rule);
        when(policyCmptType.getValidationRules()).thenReturn(rules);
    }

    @Test
    public void shouldImportMessagesWithStatusIllegalMessage() throws Exception {
        ArrayList<IValidationRule> rules = new ArrayList<>();
        when(policyCmptType.getValidationRules()).thenReturn(rules);
        Properties properties = new Properties();
        properties.setProperty(TEST_POLICY_TEST_RULE, "TestMessage");

        importer.setProperties(properties);
        IStatus result = importer.importContentMap();

        assertTrue(result.isMultiStatus(), result.toString());
        assertEquals(1, ((MultiStatus)result).getChildren().length);
        IStatus illegalMessageStatus = ((MultiStatus)result).getChildren()[0];
        assertEquals(ValidationRuleMessagesImportOperation.MSG_CODE_ILLEGAL_MESSAGE, illegalMessageStatus.getCode());
        assertTrue(illegalMessageStatus.isMultiStatus());
        assertEquals(1, ((MultiStatus)illegalMessageStatus).getChildren().length);

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusMissingMessage() throws Exception {
        setUpImportMissingMessage();
        importer.setEnableWarningsForMissingMessages(true);

        IStatus result = importer.importContentMap();

        assertTrue(result.isMultiStatus(), result.toString());
        assertEquals(1, ((MultiStatus)result).getChildren().length);
        IStatus missingMessageStatus = ((MultiStatus)result).getChildren()[0];
        assertEquals(ValidationRuleMessagesImportOperation.MSG_CODE_MISSING_MESSAGE, missingMessageStatus.getCode());
        assertTrue(missingMessageStatus.isMultiStatus());
        assertEquals(1, ((MultiStatus)missingMessageStatus).getChildren().length);

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    private void setUpImportMissingMessage() {
        when(rule.getName()).thenReturn("testRule");
        when(rule.getQualifiedRuleName()).thenReturn(TEST_POLICY_TEST_RULE);
        ArrayList<IValidationRule> rules = new ArrayList<>();
        rules.add(rule);
        when(policyCmptType.getValidationRules()).thenReturn(rules);
        Properties properties = new Properties();
        importer.setProperties(properties);
    }

    @Test
    public void shouldImportMessagesWithStatusMissingMessage_DisabledMissingWarning() throws Exception {
        setUpImportMissingMessage();
        importer.setEnableWarningsForMissingMessages(false);

        IStatus result = importer.importContentMap();

        assertFalse(result.isMultiStatus());
        assertEquals(0, result.getChildren().length);

        verify(root).findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }
}
