/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;

public abstract class AbstractAbstractionTestSetup {

    @After
    public void tearDown() {
        Abstractions.getWorkspace().getRoot().getProjects()
                .stream()
                .forEach(p -> p.delete(null));
    }

    /**
     * Returns a path of the source folder relativ to its project e.g. {@code src} will get
     * {@code Project/src}.
     * 
     * @return the path of the source folder
     */
    public abstract Path srcFolder();

    /**
     * Returns a list of folders to create, relativ to their project.
     * 
     * @return a list of folders.
     */
    public abstract List<Path> additionalFolders();

    /**
     * Returns a list of files to create, relativ to their project. The files are created without
     * content.
     * 
     * @return a list of files to create
     */
    public abstract List<Path> files();

    /**
     * Adds other projects the {@code project} depends on.
     * 
     * @param project the dependent project
     * @param dependencies the dependencies for the {@code project}
     */
    public abstract void addDependencies(AProject project, AProject... dependencies);

    /**
     * Creates an java project. For example in Eclipse it will add the java nature to the project.
     * 
     * @param project the project
     * @return the java project
     */
    public abstract AJavaProject toJavaProject(AProject project);

    /**
     * Creates an IPS project. For example in Eclipse it will add the IPS nature to the project.
     * 
     * @param project the project
     */
    protected abstract void toIpsProjectImpl(AProject project);

    /**
     * Creates an empty Project.
     * 
     * @param name the name of the project.
     * @return the project
     */
    protected abstract AProject newProjectImpl(String name);

    /**
     * Specifically creates a new simple IPS project, depending on the execution environment. This
     * simple IPS project has the expected files and folders, but no content e.g. the .ipsproject
     * file is empty. In an Eclipse environment it also adds the IpsNature to the project.
     * 
     * @param name the name of the project to create.
     * @param dependencies other projects this project depends on
     * @return the created IPS project
     * @throws IllegalArgumentException if the dependencies are null or empty
     */
    public AProject newSimpleIpsProjectWithDependencies(String name, AProject... dependencies) {
        AProject ipsProject = newAbstractionProjectWithDependencies(name, dependencies);
        toIpsProject(ipsProject);
        return ipsProject;
    }

    /**
     * Specifically creates a new IPS project, depending on the execution environment. This simple
     * IPS project has the expected files and folders, but no content e.g. the .ipsproject file is
     * empty. In an Eclipse environment it also adds the IpsNature to the project.
     * 
     * @param name the name of the project to create.
     * @return the created IPS project
     */
    public AProject newSimpleIpsProject(String name) {
        AProject ipsProject = newAbstractionProject(name);
        toIpsProject(ipsProject);
        return ipsProject;
    }

    /**
     * Creates a new project, depending on the execution environment. See {@link Abstractions} for
     * details.
     * 
     * @param name the name of the project to create.
     * @param dependencies other projects this project depends on
     * @return the created project
     * @throws IllegalArgumentException if the dependencies are null or empty
     */
    public AProject newAbstractionProjectWithDependencies(String name, AProject... dependencies) {
        if (dependencies == null || dependencies.length < 1) {
            throw new IllegalArgumentException("The dependencies can not be null or empty."); //$NON-NLS-1$
        }
        AProject project = newAbstractionProject(name);
        addDependencies(project, dependencies);
        return project;
    }

    /**
     * Creates a new project, depending on the execution environment. See {@link Abstractions} for
     * details. Also all configured files and folders are created.
     * 
     * @param name the name of the project to create.
     * @return the created project
     */
    public AProject newAbstractionProject(String name) {
        AProject project = newProjectImpl(name);
        createFolder(project, srcFolder());
        for (Path folder : additionalFolders()) {
            createFolder(project, folder);
        }
        for (Path file : files()) {
            createFile(project, file);
        }
        project.refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);
        return project;
    }

    /**
     * Adds IPS to a project and creates expected empty files and folders. In Eclipse environment
     * also the IpsNature.
     * 
     * @param project the project
     */
    public void toIpsProject(AProject project) {
        try {
            toIpsProjectImpl(project);
            project.getLocation().resolve("productdef").toFile().mkdirs(); //$NON-NLS-1$
            project.getLocation().resolve(".ipsproject").toFile().createNewFile(); //$NON-NLS-1$

            project.refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A convenience method to read an {@link String} from an {@link InputStream}. Uses the default
     * encoding {@link Charset#defaultCharset()}
     * 
     * @param is the input stream to read from
     * @return the content of the input stream
     * @throws RuntimeException if an IO error occurs.
     */
    public String readFrom(InputStream is) {
        try {
            return IOUtils.toString(is, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A convenience method to write an {@link String} to an {@link InputStream}. Uses the default
     * encoding {@link Charset#defaultCharset()}
     * 
     * @param content the string to write
     * @return an input stream with the {@code content}
     */
    public InputStream writeTo(String content) {
        return IOUtils.toInputStream(content, Charset.defaultCharset());
    }

    private void createFile(AProject project, Path file) {
        try {
            project.getLocation().resolve(file).toFile().createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createFolder(AProject project, Path folder) {
        project.getLocation().resolve(folder).toFile().mkdirs();
    }

    protected static <A extends AAbstraction> Matcher<A> wrapperOf(Object object) {
        return new WrapperMatcher<>(object);
    }

    protected static final class WrapperMatcher<A extends AAbstraction> extends TypeSafeMatcher<A> {
        private final Matcher<Object> equals;

        private WrapperMatcher(Object object) {
            equals = equalTo(object);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a wrapper of ");
            equals.describeTo(description);
        }

        @Override
        protected void describeMismatchSafely(A item, Description mismatchDescription) {
            mismatchDescription.appendText("is a wrapper of ");
            mismatchDescription.appendValue(item.unwrap());
        }

        @Override
        protected boolean matchesSafely(A abstraction) {
            return equals.matches(abstraction.unwrap());
        }
    }

}
