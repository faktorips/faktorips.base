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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.junit.Test;

public class ValidationRuleMessagesPropertiesImporterTest {

    @Test
    public void testImport() throws Exception {
        IFile file = mock(IFile.class);
        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        InputStream stream = mock(InputStream.class);
        when(file.getContents()).thenReturn(stream);

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(file, root,
                Locale.GERMAN);
        importer.importPropertyFile(new NullProgressMonitor());

        verify(file).getContents();
        verify(stream).close();
        verify(root).findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportNothing() throws Exception {

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);

        Properties properties = new Properties();

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(IFile.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());

        assertTrue(result.isOK());

        verify(root).findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusOK() throws Exception {

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.isMutable()).thenReturn(true);

        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IValidationRule rule = mock(IValidationRule.class);

        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        srcFiles.add(ipsSrcFile);
        when(root.findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(srcFiles);

        when(policyCmptType.getQualifiedName()).thenReturn("testPolicy");
        when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);

        when(rule.getIpsObject()).thenReturn(policyCmptType);
        when(rule.getName()).thenReturn("testRule");
        when(rule.getMessageText()).thenReturn(new InternationalString());

        ArrayList<IValidationRule> rules = new ArrayList<IValidationRule>();
        rules.add(rule);
        when(policyCmptType.getValidationRules()).thenReturn(rules);

        Properties properties = new Properties();
        properties.setProperty("testPolicy-testRule", "TestMessage");

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(IFile.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());
        assertTrue(result.toString(), result.isOK());
        assertEquals(new LocalizedString(Locale.GERMAN, "TestMessage"), rule.getMessageText().get(Locale.GERMAN));

        verify(root).findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusIllegalMessage() throws Exception {
        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);

        when(ipsSrcFile.isMutable()).thenReturn(true);
        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        srcFiles.add(ipsSrcFile);
        when(root.findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(srcFiles);

        when(policyCmptType.getQualifiedName()).thenReturn("testPolicy");
        when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);

        ArrayList<IValidationRule> rules = new ArrayList<IValidationRule>();
        when(policyCmptType.getValidationRules()).thenReturn(rules);

        Properties properties = new Properties();
        properties.setProperty("testPolicy-testRule", "TestMessage");

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(IFile.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());
        assertTrue(result.toString(), result.isMultiStatus());
        assertEquals(1, ((MultiStatus)result).getChildren().length);
        IStatus illegalMessageStatus = ((MultiStatus)result).getChildren()[0];
        assertEquals(ValidationRuleMessagesPropertiesImporter.MSG_CODE_ILLEGAL_MESSAGE, illegalMessageStatus.getCode());
        assertTrue(illegalMessageStatus.isMultiStatus());
        assertEquals(1, ((MultiStatus)illegalMessageStatus).getChildren().length);

        verify(root).findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }

    @Test
    public void shouldImportMessagesWithStatusMissingMessage() throws Exception {
        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IValidationRule rule = mock(IValidationRule.class);

        when(ipsSrcFile.isMutable()).thenReturn(true);
        List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
        srcFiles.add(ipsSrcFile);
        when(root.findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(srcFiles);

        when(policyCmptType.getQualifiedName()).thenReturn("testPolicy");
        when(ipsSrcFile.getIpsObject()).thenReturn(policyCmptType);

        when(rule.getIpsObject()).thenReturn(policyCmptType);
        when(rule.getName()).thenReturn("testRule");
        when(rule.getMessageText()).thenReturn(new InternationalString());

        ArrayList<IValidationRule> rules = new ArrayList<IValidationRule>();
        rules.add(rule);
        when(policyCmptType.getValidationRules()).thenReturn(rules);

        Properties properties = new Properties();

        ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                mock(IFile.class), root, Locale.GERMAN);
        IStatus result = importer.importProperties(properties, new NullProgressMonitor());
        assertTrue(result.toString(), result.isMultiStatus());
        assertEquals(1, ((MultiStatus)result).getChildren().length);
        IStatus missingMessageStatus = ((MultiStatus)result).getChildren()[0];
        assertEquals(ValidationRuleMessagesPropertiesImporter.MSG_CODE_MISSING_MESSAGE, missingMessageStatus.getCode());
        assertTrue(missingMessageStatus.isMultiStatus());
        assertEquals(1, ((MultiStatus)missingMessageStatus).getChildren().length);

        verify(root).findAllIpsSrcFiled(IpsObjectType.POLICY_CMPT_TYPE);
        verifyNoMoreInteractions(root);
    }
}
