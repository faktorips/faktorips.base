/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation;

import static org.faktorips.maven.plugin.validation.IpsValidationMessageMapper.MOJO_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.jupiter.api.Test;

class IpsProjectValidatorTest {

    @Test
    void testValidate_ValidatesAllPackages() throws MojoFailureException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.validate())
                .thenReturn(MessageList.of(Message.newWarning("1", "A warning on the project level")));

        IIpsSrcFile ipsSrcFile1 = mock(IIpsSrcFile.class);
        IIpsObject ipsObject1 = mock(IIpsObject.class);
        when(ipsObject1.toString()).thenReturn("Obj1");
        when(ipsObject1.validate(ipsProject)).thenReturn(
                MessageList.of(
                        Message.newError("1A", "This is an error on the first object", ipsObject1, "prop"),
                        Message.newError("1B", "This is another error on the first object", ipsObject1, "prop")));
        when(ipsSrcFile1.getIpsObject()).thenReturn(ipsObject1);

        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        IIpsObject ipsObject2 = mock(IIpsObject.class);
        when(ipsObject2.toString()).thenReturn("Obj2");
        when(ipsObject2.validate(ipsProject)).thenReturn(
                MessageList.of(Message.newWarning("2", "This is a warning on the second object", ipsObject2, "prop2")));
        when(ipsSrcFile2.getIpsObject()).thenReturn(ipsObject2);

        IIpsPackageFragment baseFragment = mock(IIpsPackageFragment.class);
        when(baseFragment.getChildren()).thenReturn(new IIpsElement[] { ipsSrcFile1, ipsSrcFile2 });

        IIpsSrcFile ipsSrcFile3 = mock(IIpsSrcFile.class);
        IIpsObject ipsObject3 = mock(IIpsObject.class);
        when(ipsObject3.validate(ipsProject)).thenReturn(
                MessageList.of(Message.newInfo("3", "This is an info on an object from another package")));
        when(ipsSrcFile3.getIpsObject()).thenReturn(ipsObject3);

        IIpsPackageFragment otherFragment = mock(IIpsPackageFragment.class);
        when(otherFragment.getChildren()).thenReturn(new IIpsElement[] { ipsSrcFile3 });

        IIpsPackageFragmentRoot sourceFolderRoot = mock(IIpsPackageFragmentRoot.class);
        when(sourceFolderRoot.isBasedOnSourceFolder()).thenReturn(true);
        when(sourceFolderRoot.getIpsPackageFragments())
                .thenReturn(new IIpsPackageFragment[] { baseFragment, otherFragment });

        when(ipsProject.getIpsPackageFragmentRoots(false))
                .thenReturn(new IIpsPackageFragmentRoot[] { sourceFolderRoot });
        MavenProject mavenProject = mock(MavenProject.class);
        Log log = mock(Log.class);
        IpsProjectValidator ipsProjectValidator = new IpsProjectValidator(ipsProject, mavenProject, log);

        ipsProjectValidator.validate(false);

        verify(log).warn(MOJO_NAME + " A warning on the project level (1)[]");
        verify(log).error(MOJO_NAME + " This is an error on the first object (1A)[Obj1.prop]");
        verify(log).error(MOJO_NAME + " This is another error on the first object (1B)[Obj1.prop]");
        verify(log).warn(MOJO_NAME + " This is a warning on the second object (2)[Obj2.prop2]");
        verify(log).info(MOJO_NAME + " This is an info on an object from another package (3)[]");
    }

    @Test
    void testValidate_ValidatesOnlySourceFragments() throws MojoFailureException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.validate())
                .thenReturn(MessageList.of(Message.newWarning("1", "A warning on the project level")));

        IIpsSrcFile ipsSrcFile = mockIpsObjectSrcFile(ipsProject, "O",
                Message.newError("2", "An error on the object level"));

        IIpsPackageFragment baseFragment = mock(IIpsPackageFragment.class);
        when(baseFragment.getChildren()).thenReturn(new IIpsElement[] { ipsSrcFile });

        IIpsPackageFragmentRoot sourceFolderRoot = mock(IIpsPackageFragmentRoot.class);
        when(sourceFolderRoot.isBasedOnSourceFolder()).thenReturn(true);
        when(sourceFolderRoot.getIpsPackageFragments()).thenReturn(new IIpsPackageFragment[] { baseFragment });

        IIpsPackageFragmentRoot otherRoot = mock(IIpsPackageFragmentRoot.class);
        when(otherRoot.isBasedOnSourceFolder()).thenReturn(false);

        when(ipsProject.getIpsPackageFragmentRoots(false))
                .thenReturn(new IIpsPackageFragmentRoot[] { sourceFolderRoot, otherRoot });
        MavenProject mavenProject = mock(MavenProject.class);
        Log log = mock(Log.class);
        IpsProjectValidator ipsProjectValidator = new IpsProjectValidator(ipsProject, mavenProject, log);

        ipsProjectValidator.validate(false);

        verify(otherRoot, never()).getIpsPackageFragments();
        verify(log).warn(MOJO_NAME + " A warning on the project level (1)[]");
        verify(log).error(MOJO_NAME + " An error on the object level (2)[]");
    }

    @Test
    void testValidate_FailsOnError() {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsProject.validate()).thenReturn(MessageList.of(Message.newError("E", "An error")));

        when(ipsProject.getIpsPackageFragmentRoots(false)).thenReturn(new IIpsPackageFragmentRoot[] {});
        MavenProject mavenProject = mock(MavenProject.class);
        Log log = mock(Log.class);
        IpsProjectValidator ipsProjectValidator = new IpsProjectValidator(ipsProject, mavenProject, log);

        assertThrows(MojoFailureException.class, () -> ipsProjectValidator.validate(true));

        verify(log).error(MOJO_NAME + " An error (E)[]");
    }

    @Test
    void testValidate_FailsOnException() {
        IIpsProject ipsProject = mock(IIpsProject.class);
        doThrow(new IpsException("Ex")).when(ipsProject).validate();
        MavenProject mavenProject = mock(MavenProject.class);
        Log log = mock(Log.class);
        IpsProjectValidator ipsProjectValidator = new IpsProjectValidator(ipsProject, mavenProject, log);

        assertThrows(MojoFailureException.class, () -> ipsProjectValidator.validate(true));

        verify(log).error(MOJO_NAME + ' ' + IpsException.class.getName() + ": Ex");
    }

    @Test
    void testValidate_DoesNotFailOnExceptionIfFailBuildOnValidationErrorsIsFalse() throws MojoFailureException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        doThrow(new IpsException("Ex")).when(ipsProject).validate();
        MavenProject mavenProject = mock(MavenProject.class);
        Log log = mock(Log.class);
        IpsProjectValidator ipsProjectValidator = new IpsProjectValidator(ipsProject, mavenProject, log);

        ipsProjectValidator.validate(false);

        verify(log).error(MOJO_NAME + ' ' + IpsException.class.getName() + ": Ex");
    }

    private static IIpsSrcFile mockIpsObjectSrcFile(IIpsProject ipsProject, String objectName, Message... messages) {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        IIpsObject ipsObject = mock(IIpsObject.class);
        when(ipsObject.toString()).thenReturn(objectName);
        when(ipsObject.validate(ipsProject))
                .thenReturn(MessageList.of(messages));
        when(ipsSrcFile.getIpsObject()).thenReturn(ipsObject);
        return ipsSrcFile;
    }

}
