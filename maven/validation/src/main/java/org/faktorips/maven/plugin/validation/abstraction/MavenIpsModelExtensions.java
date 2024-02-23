/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation.abstraction;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Supplier;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plainjava.internal.PlainJavaIpsModelExtensions;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;

public class MavenIpsModelExtensions extends PlainJavaIpsModelExtensions {

    private final MavenSession session;
    private final Supplier<Log> logger;

    public MavenIpsModelExtensions(MavenSession session, Supplier<Log> logger) {
        super();
        this.session = session;
        this.logger = logger;
        super.setInstanceForTest(this);
    }

    @Override
    public IClassLoaderProviderFactory getClassLoaderProviderFactory() {
        return new MavenClassLoaderProviderFactory(session);
    }

    @Override
    public IIpsFeatureVersionManager getIpsFeatureVersionManager(String featureId) {
        IIpsFeatureVersionManager realFeatureVersionManager = super.getIpsFeatureVersionManager(featureId);
        if (realFeatureVersionManager != null) {
            return realFeatureVersionManager;
        }
        logger.get().warn("Feature " + featureId
                + " is not supported by the faktorips-validation-maven-plugin. Its contributions to the validated project will be ignored.");
        return new FakeIpsFeatureVersionManager(featureId);
    }

    private static final class FakeIpsFeatureVersionManager implements IIpsFeatureVersionManager {
        private final String featureId;
        private String predecessorId;
        private String id;

        private FakeIpsFeatureVersionManager(String featureId) {
            this.featureId = featureId;
        }

        @Override
        public void setRequiredForAllProjects(boolean required) {
            // ignore
        }

        @Override
        public void setPredecessorId(String predecessorId) {
            this.predecessorId = predecessorId;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }

        @Override
        public void setFeatureId(String featureId) {
            // ignore
        }

        @Override
        public boolean isRequiredForAllProjects() {
            return false;
        }

        @Override
        public boolean isCurrentVersionCompatibleWith(String otherVersion) {
            return true;
        }

        @Override
        public String getPredecessorId() {
            return predecessorId;
        }

        @Override
        public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
                throws IpsException {
            return new AbstractIpsProjectMigrationOperation[0];
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getFeatureId() {
            return featureId;
        }

        @Override
        public String getCurrentVersion() {
            return "0";
        }

        @Override
        public int compareToCurrentVersion(String otherVersion) {
            return 0;
        }
    }

    private static final class MavenClassLoaderProviderFactory implements IClassLoaderProviderFactory {
        private MavenSession session;

        public MavenClassLoaderProviderFactory(MavenSession session) {
            this.session = session;
        }

        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject, ClassLoader parent) {
            return new MavenClassLoaderProvider(session, ipsProject, parent);
        }

        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
            return new MavenClassLoaderProvider(session, ipsProject);
        }
    }

    private static final class MavenClassLoaderProvider implements IClassLoaderProvider {
        private final IIpsProject ipsProject;
        private final ClassLoader parent;
        private final MavenSession session;

        private MavenClassLoaderProvider(MavenSession session, IIpsProject ipsProject) {
            this(session, ipsProject, null);
        }

        public MavenClassLoaderProvider(MavenSession session, IIpsProject ipsProject, ClassLoader parent) {
            this.session = session;
            this.ipsProject = ipsProject;
            this.parent = parent;
        }

        @Override
        public ClassLoader getClassLoader() {
            try {
                List<MavenProject> projects = session.getProjects();
                MavenProject currentProject = projects.stream()
                        .filter(mavenproject -> ipsProject.getName()
                                .equals(mavenproject.getGroupId() + '.' + mavenproject.getArtifactId()))
                        .findFirst()
                        .orElseGet(session::getCurrentProject);

                URL[] urls = currentProject == null ? new URL[0]
                        : currentProject.getCompileClasspathElements().stream().map(s -> {
                            try {
                                return new File(s).toURI().toURL();
                            } catch (MalformedURLException e) {
                                throw new IpsException(e.getMessage(), e);
                            }
                        }).toArray(URL[]::new);
                return parent == null ? new URLClassLoader(urls) : new URLClassLoader(urls, parent);
            } catch (DependencyResolutionRequiredException e) {
                throw new IpsException(e.getMessage(), e);
            }
        }

        @Override
        public void addClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // ignore
        }

        @Override
        public void removeClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // ignore
        }
    }

}
