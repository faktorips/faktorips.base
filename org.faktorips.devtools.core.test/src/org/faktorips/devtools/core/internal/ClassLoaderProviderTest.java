/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ClassLoaderProviderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IJavaProject javaProject;
    private ClassLoaderProvider provider;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        javaProject = ipsProject.getJavaProject().unwrap();
        provider = new ClassLoaderProvider(javaProject, ClassLoader.getSystemClassLoader());
    }

    /**
     * Following test covers the following case: A listener is notified and deregisters itself from
     * the provider. This needn't result in a Exception because of a concurrent modification
     * exception in the provider.
     */
    @Test
    public void testRemoveListenerDuringNotification() throws Exception {
        createClassFile();

        Listener l0 = new Listener();
        IClasspathContentsChangeListener l1 = new IClasspathContentsChangeListener() {

            @Override
            public void classpathContentsChanges(AJavaProject project) {
                provider.removeClasspathChangeListener(this);
            }

        };
        Listener l2 = new Listener();
        provider.addClasspathChangeListener(l0);
        provider.addClasspathChangeListener(l1);
        provider.addClasspathChangeListener(l2);
        provider.getClassLoader();

        deleteClassFile();
    }

    @Test
    public void testGetClassLoader() throws Exception {
        createClassFile();
        createJarFileAndAppendToClasspath();
        ClassLoader cl = provider.getClassLoader();
        assertNotNull(cl);

        Class<?> clazz = cl.loadClass("SomeClass");
        assertEquals("SomeClass", clazz.getName());

        clazz = cl.loadClass("org.faktorips.test.ClassInAJar");
        assertEquals("org.faktorips.test.ClassInAJar", clazz.getName());
    }

    @Test
    public void testListenerMechnism_ClassFile() throws Exception {
        createClassFile();
        ClassLoader cl = provider.getClassLoader();
        Class.forName("SomeClass", true, cl);
        Listener listener = new Listener();
        assertNull(listener.project);
        provider.addClasspathChangeListener(listener);
        deleteClassFile();
        assertEquals(javaProject, listener.project);

        cl = provider.getClassLoader();
        try {
            Class.forName("SomeClass", true, cl);
            fail(); // class was deleted, so it shouldn't be found.
        } catch (ClassNotFoundException e) {
        }

        // after re-adding the class file, the class should be loaded again
        listener.project = null;
        createClassFile();
        cl = provider.getClassLoader();
        Class.forName("SomeClass", true, cl);
    }

    /*
     * The following code section has been commented, as an exception is thrown, when the jar-file
     * is deleted. this happens in most cases, not all!
     * 
     * The first test case 'testDemoDeleteProblem' is a test case that just illustrates the problem.
     * 
     * Jan Ortmann, 6.10.2010
     */
    @Ignore
    @Test
    public void test_DemoDeleteProblem() throws Exception {
        createJarFileAndAppendToClasspath();
        deleteJarFile();
    }

    @Ignore
    @Test
    public void testListenerMechnism_JarFile() throws Exception {
        createJarFileAndAppendToClasspath();
        ClassLoader cl = provider.getClassLoader();
        Class.forName("org.faktorips.test.ClassInAJar", true, cl);
        Listener listener = new Listener();
        assertNull(listener.project);
        provider.addClasspathChangeListener(listener);

        deleteJarFile();
        assertEquals(javaProject, listener.project);
        cl = provider.getClassLoader();
        try {
            Class.forName("org.faktorips.test.ClassInAJar", true, cl);
            fail(); // jar was deleted, so the class shouldn't be found.
        } catch (ClassNotFoundException e) {
        }

        // after re-adding the jar, the class should be loaded again
        listener.project = null;
        createJarFile();
        assertEquals(javaProject, listener.project);
        cl = provider.getClassLoader();
        Class.forName("org.faktorips.test.ClassInAJar", true, cl);
    }

    private void createClassFile() throws Exception {
        IPackageFragmentRoot root = javaProject.getPackageFragmentRoots()[0];
        IPackageFragment pack = root.getPackageFragment("");
        pack.createCompilationUnit("SomeClass.java", "public class SomeClass {}", true, null);
        javaProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
    }

    private void deleteClassFile() throws Exception {
        ICoreRunnable runnable = $ -> {
            IFolder outFolder = (IFolder)javaProject.getProject().getWorkspace().getRoot()
                    .findMember(javaProject.getOutputLocation());
            IFile file = outFolder.getFile("SomeClass.class");
            file.delete(true, false, null);
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
    }

    private IFile createJarFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("ClassLoaderProviderTestJar.jar");
        IFile jarFile = javaProject.getProject().getFile("Test.jar");
        jarFile.create(is, true, null);
        is.close();
        return jarFile;
    }

    private void createJarFileAndAppendToClasspath() throws Exception {
        final IFile jarFile = createJarFile();
        ICoreRunnable runnable = $ -> {
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
            System.arraycopy(entries, 0, newEntries, 0, entries.length);
            newEntries[entries.length] = JavaCore.newLibraryEntry(jarFile.getFullPath(), null, null);
            javaProject.setRawClasspath(newEntries, javaProject.getOutputLocation(), false, null);
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
        waitForIndexer(); // Java indexer locks the Jar-File !!!
    }

    private void deleteJarFile() throws Exception {
        waitForIndexer();

        ICoreRunnable runnable = $ -> {
            IFile jarFile = javaProject.getProject().getFile("Test.jar");
            jarFile.delete(true, false, null);
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
    }

    static class Listener implements IClasspathContentsChangeListener {

        private IJavaProject project = null;

        @Override
        public void classpathContentsChanges(AJavaProject project) {
            this.project = project.unwrap();
        }

    }
}
