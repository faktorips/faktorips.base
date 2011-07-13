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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Test;

public class ValidationRuleMessagesPropertiesBuilderTest extends AbstractValidationMessagesBuilderTest {

    @Test
    public void testIsBuilderFor() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder builder = new ValidationRuleMessagesPropertiesBuilder(builderSet);
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

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class));

        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsPackageFragment pack = mockPackageFragment();

        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        when(ipsSrcFile.getIpsObject()).thenReturn(pcType);

        builderSpy.build(ipsSrcFile);

        verify(generatorMock).generate(pcType);

        // String pcTypeName = "PcTypeTestName";
        // String vrule1Name = "rule1Name";
        // String testMessage1 = "testMessage1";
        //
        // IValidationRule vRule1 = mockValidationRule(pcType);
        // when(vRule1.getName()).thenReturn(vrule1Name);
        // when(vRule1.getMessageText()).thenReturn(testMessage1);
        //
        // String vrule2Name = "rule2Name";
        // String testMessage2 = "testMessage2";
        //
        // IValidationRule vRule2 = mockValidationRule(pcType);
        // when(vRule2.getName()).thenReturn(vrule2Name);
        // when(vRule2.getMessageText()).thenReturn(testMessage2);
        //
        // List<IValidationRule> listOfValidationRules = new ArrayList<IValidationRule>();
        // listOfValidationRules.add(vRule1);
        //
        // when(pcType.getQualifiedName()).thenReturn(pcTypeName);
        // when(pcType.getValidationRules()).thenReturn(listOfValidationRules);
        //
        // assertEquals(1, messages.size());
        // assertEquals(messages.getMessage(pcTypeName + "_" + vrule1Name), testMessage1);
        //
        // listOfValidationRules.add(vRule2);
        //
        // builder.build(ipsSrcFile);
        //
        // assertEquals(2, messages.size());
        // assertEquals(messages.getMessage(pcTypeName + "_" + vrule1Name), testMessage1);
        // assertEquals(messages.getMessage(pcTypeName + "_" + vrule2Name), testMessage2);
        //
        // // when calling the build method with an other object, the older messages should not be
        // // removed
        // String pcTypeName2 = "PcTypeTestName2";
        // IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        // when(ipsSrcFile2.getIpsPackageFragment()).thenReturn(pack);
        // IPolicyCmptType pcType2 = mock(IPolicyCmptType.class);
        // when(ipsSrcFile2.getIpsObject()).thenReturn(pcType2);
        // when(pcType2.getQualifiedName()).thenReturn(pcTypeName2);
        //
        // IValidationRule vRule2_1 = mockValidationRule(pcType2);
        // when(vRule2_1.getName()).thenReturn(vrule1Name);
        // when(vRule2_1.getMessageText()).thenReturn(testMessage1);
        // IValidationRule vRule2_2 = mockValidationRule(pcType2);
        // when(vRule2_2.getName()).thenReturn(vrule2Name);
        // when(vRule2_2.getMessageText()).thenReturn(testMessage2);
        //
        // List<IValidationRule> listOfValidationRules2 = new ArrayList<IValidationRule>();
        // listOfValidationRules2.add(vRule2_1);
        // listOfValidationRules2.add(vRule2_2);
        //
        // when(pcType2.getValidationRules()).thenReturn(listOfValidationRules2);
        //
        // builder.build(ipsSrcFile2);
        //
        // assertEquals(4, messages.size());
        // assertEquals(messages.getMessage(pcTypeName + "_" + vrule1Name), testMessage1);
        // assertEquals(messages.getMessage(pcTypeName + "_" + vrule2Name), testMessage2);
        // assertEquals(messages.getMessage(pcTypeName2 + "_" + vrule1Name), testMessage1);
        // assertEquals(messages.getMessage(pcTypeName2 + "_" + vrule2Name), testMessage2);
    }

    @Test
    public void shouldCreateDifferentMessageFilesForDifferentRoots() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder builder = new ValidationRuleMessagesPropertiesBuilder(builderSet);

        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        ValidationRuleMessagesGenerator messageGenerator = builder.getMessagesGenerator(ipsSrcFile);
        assertSame(messageGenerator, builder.getMessagesGenerator(ipsSrcFile));

        IIpsSrcFile otherIpsSrcFile = mock(IIpsSrcFile.class);
        when(otherIpsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        assertSame(messageGenerator, builder.getMessagesGenerator(otherIpsSrcFile));

        IIpsPackageFragment pack2 = mockPackageFragment();

        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        when(ipsSrcFile2.getIpsPackageFragment()).thenReturn(pack2);

        assertNotSame(messageGenerator, builder.getMessagesGenerator(ipsSrcFile2));

        // overwrite the root of pack2
        doReturn(pack.getRoot()).when(pack2).getRoot();
        assertSame(messageGenerator, builder.getMessagesGenerator(ipsSrcFile2));
    }

    @Test
    public void testBuildIgnoresOtherTypes() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class));

        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        // should not throw any exception
        builderSpy.build(ipsSrcFile);
        verifyZeroInteractions(generatorMock);

        IIpsObject anyIpsObject = mock(IIpsObject.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(anyIpsObject);
        // should not throw any exception
        builderSpy.build(ipsSrcFile);
        verifyZeroInteractions(generatorMock);
    }

    @Test
    public void testDelete() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment pack = mockPackageFragment();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class));

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IPolicyCmptType pcType = mock(IPolicyCmptType.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(pcType);
        String pcTypeName = "PcTypeTestName";
        when(ipsSrcFile.getQualifiedNameType()).thenReturn(
                new QualifiedNameType(pcTypeName, IpsObjectType.POLICY_CMPT_TYPE));
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        builderSpy.delete(ipsSrcFile);

        verify(generatorMock).deleteAllMessagesFor(pcTypeName);

    }

    @Test
    public void testDeleteIgnoresOtherTypes() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class));

        IIpsPackageFragment pack = mockPackageFragment();

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IpsObjectType ipsObjectType = mock(IpsObjectType.class);
        when(ipsSrcFile.getQualifiedNameType()).thenReturn(new QualifiedNameType("pcType", ipsObjectType));
        when(ipsSrcFile.getIpsPackageFragment()).thenReturn(pack);

        // should not throw any exception
        builderSpy.delete(ipsSrcFile);
        verifyZeroInteractions(generatorMock);

        IIpsObject anyIpsObject = mock(IIpsObject.class);
        when(ipsSrcFile.getIpsObject()).thenReturn(anyIpsObject);

        // should not throw any exception
        builderSpy.delete(ipsSrcFile);
        verifyZeroInteractions(generatorMock);
    }

    @Test
    public void shouldBuildDerivedArtifacts() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder builder = new ValidationRuleMessagesPropertiesBuilder(builderSet);
        assertTrue(builder.buildsDerivedArtefacts());
    }

    @Test
    public void testGetPropertyFile() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        ValidationRuleMessagesPropertiesBuilder validationMessagesBuilder = new ValidationRuleMessagesPropertiesBuilder(
                builderSet);

        IFile file = mock(IFile.class);

        IPath path = new Path(ROOT_FOLDER + "/" + ValidationRuleMessagesPropertiesBuilder.MESSAGES_BASENAME
                + ValidationRuleMessagesPropertiesBuilder.MESSAGES_PREFIX);

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
    public void testBeforeBuildProcess() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment fragment = mockPackageFragment();
        IIpsPackageFragmentRoot root = fragment.getRoot();

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });

        ValidationRuleMessagesPropertiesBuilder validationMessagesBuilder = new ValidationRuleMessagesPropertiesBuilder(
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

        // MessagesProperties validationMessages = mock(MessagesProperties.class);
        // validationMessagesBuilder.loadMessagesFromFile(propertyFile, validationMessages);
        // verify(validationMessages).load(any(InputStream.class));
        // reset(validationMessages);
        //
        // validationMessagesBuilder.beforeBuildProcess(ipsProject,
        // IncrementalProjectBuilder.INCREMENTAL_BUILD);
        // verifyZeroInteractions(validationMessages);
        //
        // validationMessagesBuilder.beforeBuildProcess(ipsProject,
        // IncrementalProjectBuilder.FULL_BUILD);
        // verify(validationMessages).clear();
        // verifyNoMoreInteractions(validationMessages);
    }

    @Test
    public void testAfterBuildProcess() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment pack = mockPackageFragment();
        IIpsPackageFragmentRoot root = pack.getRoot();

        // we use a spy object to insert the generatorMock
        ValidationRuleMessagesGenerator generatorMock = mock(ValidationRuleMessagesGenerator.class);
        ValidationRuleMessagesPropertiesBuilder builderSpy = spy(new ValidationRuleMessagesPropertiesBuilder(builderSet));
        doReturn(generatorMock).when(builderSpy).getMessagesGenerator(any(IIpsPackageFragmentRoot.class));

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] {});

        builderSpy.afterBuildProcess(ipsProject, 0);
        verifyZeroInteractions(generatorMock);

        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });
        builderSpy.afterBuildProcess(ipsProject, 0);

        verify(generatorMock).saveIfModified(anyString(), anyBoolean());
    }

    @Test
    public void shouldCreateFolderIfNotExists() throws Exception {
        IIpsArtefactBuilderSet builderSet = mockBuilderSet();
        IIpsPackageFragment fragment = mockPackageFragment();
        IIpsPackageFragmentRoot root = fragment.getRoot();

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(new IIpsPackageFragmentRoot[] { root });

        ValidationRuleMessagesPropertiesBuilder validationMessagesBuilder = new ValidationRuleMessagesPropertiesBuilder(
                builderSet);

        IFolder folder = mock(IFolder.class);
        IFile propertyFile = validationMessagesBuilder.getPropertyFile(root);
        when(propertyFile.getParent()).thenReturn(folder);

        validationMessagesBuilder.beforeBuildProcess(ipsProject, IncrementalProjectBuilder.INCREMENTAL_BUILD);
        verify(folder).create(anyBoolean(), anyBoolean(), any(IProgressMonitor.class));
    }

}
