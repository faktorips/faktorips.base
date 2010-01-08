/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * @author Jan Ortmann
 */
public class ClassLoaderProviderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IJavaProject javaProject;
    private ClassLoaderProvider provider;

    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        javaProject = ipsProject.getJavaProject();
        provider = new ClassLoaderProvider(javaProject, true, true);
    }

    /**
     * Following test covers the followng case: A listener is notified and deregisters itself from
     * the provider. This needn't result in a Exception because of a concurrent modification
     * exception in the provider.
     * 
     * @throws Exception
     */
    public void testRemoveListenerDuringNotification() throws Exception {
        createClassFile();

        Listener l0 = new Listener();
        IClasspathContentsChangeListener l1 = new IClasspathContentsChangeListener() {

            public void classpathContentsChanges(IJavaProject project) {
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

    /*
     * Test method for
     * 'org.faktorips.devtools.core.internal.model.ClassLoaderProvider.getClassLoader()'
     */
    public void testGetClassLoader() throws Exception {
        createClassFile();
        createJarFileAndAppendToClasspath();
        ClassLoader cl = provider.getClassLoader();
        assertNotNull(cl);

        Class<?> clazz = Class.forName("SomeClass", true, cl);
        assertEquals("SomeClass", clazz.getName());

        clazz = Class.forName("org.faktorips.test.ClassInAJar", true, cl);
        assertEquals("org.faktorips.test.ClassInAJar", clazz.getName());

    }

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
            fail(); // class was deleted, so ot shouldn't be found.
        } catch (ClassNotFoundException e) {
        }

        // after re-adding the class file, the class should be loaded again
        listener.project = null;
        createClassFile();
        cl = provider.getClassLoader();
        Class.forName("SomeClass", true, cl);
    }

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
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                IFolder outFolder = (IFolder)javaProject.getProject().getWorkspace().getRoot().findMember(
                        javaProject.getOutputLocation());
                IFile file = outFolder.getFile("SomeClass.class");
                file.delete(true, false, null);
            }

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
        IFile jarFile = createJarFile();
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[entries.length] = JavaCore.newLibraryEntry(jarFile.getFullPath(), null, null);
        javaProject.setRawClasspath(newEntries, null);
    }

    private void deleteJarFile() throws Exception {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                IFile jarFile = javaProject.getProject().getFile("Test.jar");
                jarFile.delete(true, false, null);
            }

        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
    }

    class Listener implements IClasspathContentsChangeListener {

        private IJavaProject project = null;

        /**
         * {@inheritDoc}
         */
        public void classpathContentsChanges(IJavaProject project) {
            this.project = project;
        }

    }
}
