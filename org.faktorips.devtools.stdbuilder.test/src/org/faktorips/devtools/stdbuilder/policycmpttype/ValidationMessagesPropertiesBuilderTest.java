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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.junit.Ignore;
import org.junit.Test;

public class ValidationMessagesPropertiesBuilderTest extends AbstractValidationMessagesBuilderTest {

    @Test
    public void testIsBuilderFor() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder builder = new ValidationMessagesPropertiesBuilder(builderSet);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(builder.isBuilderFor(ipsSrcFile));

        IpsObjectType mockIpsObjectType = mock(IpsObjectType.class);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(mockIpsObjectType);
        assertFalse(builder.isBuilderFor(ipsSrcFile));
    }

    @Test
    public void testBuild() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder builder = new ValidationMessagesPropertiesBuilder(builderSet);

        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);

        String pcTypeName = "PcTypeTestName";
        String vrule1Name = "rule1Name";
        String testMessage1 = "testMessage1";

        IValidationRule vRule1 = mockValidationRule(pcType);
        when(vRule1.getName()).thenReturn(vrule1Name);
        when(vRule1.getMessageText()).thenReturn(testMessage1);

        String vrule2Name = "rule2Name";
        String testMessage2 = "testMessage2";

        IValidationRule vRule2 = mockValidationRule(pcType);
        when(vRule2.getName()).thenReturn(vrule2Name);
        when(vRule2.getMessageText()).thenReturn(testMessage2);

        List<IValidationRule> listOfValidationRules = new ArrayList<IValidationRule>();
        listOfValidationRules.add(vRule1);

        when(ipsSrcFile.getIpsObject()).thenReturn(pcType);
        when(pcType.getQualifiedName()).thenReturn(pcTypeName);
        when(pcType.getValidationRules()).thenReturn(listOfValidationRules);

        builder.build(ipsSrcFile);

        ValidationMessages messages = builder.getMessages(ipsSrcFile);
        assertEquals(1, messages.size());
        assertEquals(messages.getMessage(pcTypeName + "_" + vrule1Name), testMessage1);

        listOfValidationRules.add(vRule2);

        builder.build(ipsSrcFile);

        assertEquals(2, messages.size());
        assertEquals(messages.getMessage(pcTypeName + "_" + vrule1Name), testMessage1);
        assertEquals(messages.getMessage(pcTypeName + "_" + vrule2Name), testMessage2);

        // when calling the build method with an other object, the older messages should not be
        // removed
        String pcTypeName2 = "PcTypeTestName2";
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        when(ipsSrcFile2.getIpsPackageFragment()).thenReturn(pack);
        IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        when(ipsSrcFile2.getIpsObject()).thenReturn(pcType2);
        when(pcType2.getQualifiedName()).thenReturn(pcTypeName2);

        IValidationRule vRule2_1 = mockValidationRule(pcType2);
        when(vRule2_1.getName()).thenReturn(vrule1Name);
        when(vRule2_1.getMessageText()).thenReturn(testMessage1);
        IValidationRule vRule2_2 = mockValidationRule(pcType2);
        when(vRule2_2.getName()).thenReturn(vrule2Name);
        when(vRule2_2.getMessageText()).thenReturn(testMessage2);

        List<IValidationRule> listOfValidationRules2 = new ArrayList<IValidationRule>();
        listOfValidationRules2.add(vRule2_1);
        listOfValidationRules2.add(vRule2_2);

        when(pcType2.getValidationRules()).thenReturn(listOfValidationRules2);

        builder.build(ipsSrcFile2);

        assertEquals(4, messages.size());
        assertEquals(messages.getMessage(pcTypeName + "_" + vrule1Name), testMessage1);
        assertEquals(messages.getMessage(pcTypeName + "_" + vrule2Name), testMessage2);
        assertEquals(messages.getMessage(pcTypeName2 + "_" + vrule1Name), testMessage1);
        assertEquals(messages.getMessage(pcTypeName2 + "_" + vrule2Name), testMessage2);
    }

    @Test
    public void shouldCreateDifferentMessageFilesForDifferentRoots() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder builder = new ValidationMessagesPropertiesBuilder(builderSet);

        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        ValidationMessages messages1 = builder.getMessages(ipsSrcFile);
        assertSame(messages1, builder.getMessages(ipsSrcFile));

        IIpsSrcFile otherIpsSrcFile = mock(IIpsSrcFile.class);
        when(otherIpsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        assertSame(messages1, builder.getMessages(otherIpsSrcFile));

        IIpsPackageFragment pack2 = mockPackageFragment();

        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        when(ipsSrcFile2.getIpsPackageFragment()).thenReturn(pack2);

        assertNotSame(messages1, builder.getMessages(ipsSrcFile2));

        // overwrite the root of pack2
        doReturn(pack.getRoot()).when(pack2).getRoot();
        assertSame(messages1, builder.getMessages(ipsSrcFile2));
    }

    @Test
    public void testBuildIgnoresOtherTypes() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder builder = new ValidationMessagesPropertiesBuilder(builderSet);
        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        // should not throw any exception
        builder.build(ipsSrcFile);
        assertEquals(0, builder.getMessages(ipsSrcFile).size());

        IIpsObject anyIpsObject = mock(IIpsObject.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(anyIpsObject);
        // should not throw any exception
        builder.build(ipsSrcFile);
        assertEquals(0, builder.getMessages(ipsSrcFile).size());
    }

    @Test
    @Ignore
    public void testDelete() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder builder = new ValidationMessagesPropertiesBuilder(builderSet);
        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);

        String pcTypeName = "PcTypeTestName";
        String vrule1Name = "rule1Name";
        String testMessage1 = "testMessage1";

        IValidationRule vRule1 = mockValidationRule(pcType);
        when(vRule1.getName()).thenReturn(vrule1Name);
        when(vRule1.getMessageText()).thenReturn(testMessage1);

        String vrule2Name = "rule2Name";
        String testMessage2 = "testMessage2";

        IValidationRule vRule2 = mockValidationRule(pcType);
        when(vRule2.getName()).thenReturn(vrule2Name);
        when(vRule2.getMessageText()).thenReturn(testMessage2);

        List<IValidationRule> listOfValidationRules = new ArrayList<IValidationRule>();
        listOfValidationRules.add(vRule1);

        when(ipsSrcFile.getIpsObject()).thenReturn(pcType);
        when(pcType.getQualifiedName()).thenReturn(pcTypeName);
        when(pcType.getValidationRules()).thenReturn(listOfValidationRules);

        ValidationMessages messages = builder.getMessages(ipsSrcFile);
        messages.put(pcTypeName + "_" + vrule1Name, testMessage1);
        messages.put(pcTypeName + "_" + vrule2Name, testMessage2);
        messages.put(pcTypeName + "_" + vrule1Name, testMessage1);
        messages.put("anyOther1", testMessage1);
        messages.put("anyOther2", testMessage1);

        builder.delete(ipsSrcFile);

        assertEquals(3, messages.size());
        assertEquals(messages.getMessage(pcTypeName + "_" + vrule2Name), testMessage2);

        listOfValidationRules.add(vRule2);

        builder.delete(ipsSrcFile);
        assertEquals(2, messages.size());

    }

    @Test
    public void testDeleteIgnoresOtherTypes() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder builder = new ValidationMessagesPropertiesBuilder(builderSet);
        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        // should not throw any exception
        builder.delete(ipsSrcFile);
        assertEquals(0, builder.getMessages(ipsSrcFile).size());

        IIpsObject anyIpsObject = mock(IIpsObject.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(anyIpsObject);
        // should not throw any exception
        builder.delete(ipsSrcFile);
        assertEquals(0, builder.getMessages(ipsSrcFile).size());
    }

    @Test
    public void shouldBuildDerivedArtifacts() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder builder = new ValidationMessagesPropertiesBuilder(builderSet);
        assertTrue(builder.buildsDerivedArtefacts());
    }

    @Test
    public void testGetPropertyFile() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder validationMessagesBuilder = new ValidationMessagesPropertiesBuilder(
                builderSet);

        IFile file = mock(IFile.class);

        IPath path = new Path(ROOT_FOLDER + "/" + ValidationMessagesPropertiesBuilder.MESSAGES_BASENAME
                + ValidationMessagesPropertiesBuilder.MESSAGES_PREFIX);

        IFolder derivedFolder = mock(IFolder.class);
        when(derivedFolder.getFile(path)).thenReturn(file);

        IIpsSrcFolderEntry ipsSrcFolderEntry = mock(IIpsSrcFolderEntry.class);
        when(ipsSrcFolderEntry.getOutputFolderForDerivedJavaFiles()).thenReturn(derivedFolder);
        when(ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses()).thenReturn(TEST_PACK);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        when(root.getIpsObjectPathEntry()).thenReturn(ipsSrcFolderEntry);

        IFile propertyFile = validationMessagesBuilder.getPropertyFile(root);
        assertEquals(file, propertyFile);
    }

    @Test
    public void shouldLoadMessagesFromFile() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder validationMessagesBuilder = new ValidationMessagesPropertiesBuilder(
                builderSet);

        IIpsPackageFragment packageFragment = mockPackageFragment();

        ValidationMessagesPropertiesBuilder validationMessagesBuilderSpy = spy(validationMessagesBuilder);
        validationMessagesBuilderSpy.getMessages(packageFragment.getRoot());
        verify(validationMessagesBuilderSpy).loadMessagesFromFile(any(IFile.class), any(ValidationMessages.class));
    }

    @Test
    public void testLoadMessagesFromFile() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder validationMessagesBuilder = new ValidationMessagesPropertiesBuilder(
                builderSet);

        IIpsPackageFragment packageFragment = mockPackageFragment();

        // this should be the mock created by mockPackageFragment()
        IFile propertyFile = validationMessagesBuilder.getPropertyFile(packageFragment.getRoot());
        InputStream inputStream = mock(InputStream.class);
        when(propertyFile.getContents()).thenReturn(inputStream);

        ValidationMessages validationMessages = mock(ValidationMessages.class);

        validationMessagesBuilder.loadMessagesFromFile(propertyFile, validationMessages);
        verifyZeroInteractions(validationMessages);

        when(propertyFile.exists()).thenReturn(true);
        validationMessagesBuilder.loadMessagesFromFile(propertyFile, validationMessages);
        verify(validationMessages).load(inputStream);
    }

    @Test
    public void testBeforeBuildProcess() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment fragment = mockPackageFragment();
        IIpsPackageFragmentRoot root = fragment.getRoot();

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });

        ValidationMessagesPropertiesBuilder validationMessagesBuilder = new ValidationMessagesPropertiesBuilder(
                builderSet);

        IFile propertyFile = validationMessagesBuilder.getPropertyFile(root);
        when(propertyFile.exists()).thenReturn(true);
        IFolder folder = mock(IFolder.class);
        when(propertyFile.getParent()).thenReturn(folder);

        validationMessagesBuilder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        verify(folder).exists();
        verify(folder).create(anyBoolean(), anyBoolean(), any(IProgressMonitor.class));

        reset(folder);
        when(folder.exists()).thenReturn(true);
        validationMessagesBuilder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        verify(folder).exists();
        verifyNoMoreInteractions(folder);

        ValidationMessages validationMessages = mock(ValidationMessages.class);
        validationMessagesBuilder.loadMessagesFromFile(propertyFile, validationMessages);
        verify(validationMessages).load(any(InputStream.class));
        reset(validationMessages);

        validationMessagesBuilder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        verifyZeroInteractions(validationMessages);

        validationMessagesBuilder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.FULL_BUILD);
        verify(validationMessages).clear();
        verifyNoMoreInteractions(validationMessages);
    }

    @Test
    public void shouldCreateFolderIfNotExists() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment fragment = mockPackageFragment();
        IIpsPackageFragmentRoot root = fragment.getRoot();

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });

        ValidationMessagesPropertiesBuilder validationMessagesBuilder = new ValidationMessagesPropertiesBuilder(
                builderSet);

        IFolder folder = mock(IFolder.class);
        IFile propertyFile = validationMessagesBuilder.getPropertyFile(root);
        when(propertyFile.getParent()).thenReturn(folder);

        validationMessagesBuilder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        verify(folder).create(anyBoolean(), anyBoolean(), any(IProgressMonitor.class));
    }

    @Test
    public void shouldSaveMessagesWhenModified() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment fragment = mockPackageFragment();
        IIpsPackageFragmentRoot root = fragment.getRoot();
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });

        ValidationMessagesPropertiesBuilder validationMessagesBuilder = new ValidationMessagesPropertiesBuilder(
                builderSet);

        IFile propertyFileMock = validationMessagesBuilder.getPropertyFile(root);
        ValidationMessages validationMessages = mock(ValidationMessages.class);
        validationMessagesBuilder.loadMessagesFromFile(propertyFileMock, validationMessages);

        verify(propertyFileMock).exists();
        verifyNoMoreInteractions(propertyFileMock);
        verifyZeroInteractions(validationMessages);

        when(validationMessages.isModified()).thenReturn(true);

        validationMessagesBuilder.afterBuildProcess(ipsProject, IncrementalProjectBuilder.AUTO_BUILD);

        verify(validationMessages).store(any(OutputStream.class), anyString());
        verify(propertyFileMock).setContents(any(InputStream.class), eq(true), eq(true), any(IProgressMonitor.class));
    }

    @Test
    public void testGetMessageText() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationMessagesPropertiesBuilder validationMessagesBuilder = new ValidationMessagesPropertiesBuilder(
                builderSet);

        IValidationRule validationRule = mockValidationRule(null);

        String text = "";
        when(validationRule.getMessageText()).thenReturn(text);
        String result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("", result);

        text = "Abc 123 alles klar";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar", result);

        text = "Anc {abc123} afs";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("Anc {0} afs", result);

        text = "Abc 123 alles klar {peter} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar {0} usw.", result);

        text = "x{0} Abc 123 alles klar {1} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("x{0} Abc 123 alles klar {1} usw.", result);

        text = "{abc} Abc 123 alles klar {xyz} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw.", result);

        text = "{abc,number} Abc 123 alles klar {xyz, date, long} usw.";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("{0,number} Abc 123 alles klar {1, date, long} usw.", result);

        // same parameter multiple times
        text = "{abc} Abc 123 alles klar {xyz} usw. blabla {xyz} asd {abc} soso";
        when(validationRule.getMessageText()).thenReturn(text);
        result = validationMessagesBuilder.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw. blabla {1} asd {0} soso", result);
    }

}
