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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaImplementation;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plainjava.internal.PlainJavaIpsModelExtensions;
import org.faktorips.maven.plugin.validation.abstraction.MavenIpsModelExtensions;
import org.faktorips.maven.plugin.validation.abstraction.MavenWorkspace;
import org.faktorips.maven.plugin.validation.abstraction.MavenWorkspaceRoot;
import org.faktorips.maven.plugin.validation.mavenversion.MavenVersionProviderFactory;

/**
 * Creates a Faktor-IPS project for the current Maven project and validates it.
 */
@Mojo(name = "faktorips-validate", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class IpsValidationMojo extends AbstractMojo {

    /* private */ static final String BUILD_FAILURE_MESSAGE = "The Faktor-IPS Validation ended with Errors.";

    /**
     * Whether to skip mojo execution.
     */
    @Parameter(property = "faktorips.skipValidation", defaultValue = "false")
    private boolean skip;

    /**
     * Whether to fail the build when validation errors occur or ignore them.
     */
    @Parameter(property = "faktorips.ignoreValidationErrors", defaultValue = "false")
    private boolean ignoreValidationErrors;

    @Parameter(property = "session", readonly = true, required = true)
    private MavenSession session;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        if (skip) {
            log.info("skipping mojo execution");
            return;
        }

        @SuppressWarnings("unchecked")
        boolean alreadyValidated = getPluginContext().put("VALIDATED" + project.getBasedir().getName(),
                Boolean.TRUE) != null;

        if (alreadyValidated) {
            return;
        }

        List<MavenProject> upstreamProjects = session.getProjectDependencyGraph().getUpstreamProjects(project, true);
        initWorkspace(upstreamProjects);
        AProject aProject = Abstractions.getWorkspace().getRoot()
                .getProject(MavenWorkspaceRoot.toProjectName(project));

        if (!aProject.isIpsProject()) {
            return;
        }

        Set<IpsDependency> ipsDependencies = findDependencies(upstreamProjects);
        setIpsObjectPath(ipsDependencies);

        IIpsProject ipsProject = IIpsModel.get().getIpsProject(aProject);

        setVersionProvider(ipsDependencies);

        new IpsProjectValidator(ipsProject, project, log).validate(!ignoreValidationErrors);
    }

    private void initWorkspace(List<MavenProject> upstreamProjects) {
        new MavenIpsModelExtensions(session);
        PlainJavaImplementation.get().setWorkspace(new MavenWorkspace(project, upstreamProjects));
    }

    private void setVersionProvider(Set<IpsDependency> ipsDependencies) {
        var dependenciesInclProject = new LinkedHashSet<>(ipsDependencies);
        dependenciesInclProject.add(IpsDependency.create(project));
        PlainJavaIpsModelExtensions.get().setVersionProviderFactory("org.faktorips.maven.mavenVersionProvider",
                new MavenVersionProviderFactory(dependenciesInclProject));
    }

    private void setIpsObjectPath(Set<IpsDependency> ipsDependencies) {
        Function<IIpsProject, List<IIpsObjectPathEntry>> projectDependencyEntries = ipsDependencies.isEmpty()
                ? i -> new ArrayList<>()
                : i -> createIpsObjectPathEntries(i, ipsDependencies);
        PlainJavaIpsModelExtensions.get().setProjectDependenciesProvider(projectDependencyEntries);
    }

    private Set<IpsDependency> findDependencies(List<MavenProject> upstreamProjects) {
        Set<IpsDependency> ipsDependencies = new LinkedHashSet<>();
        Set<IpsDependency> upstreamIpsProjects = findUpstreamIpsProjects(upstreamProjects);
        ipsDependencies.addAll(upstreamIpsProjects);
        ipsDependencies.addAll(findIpsJars(project, upstreamIpsProjects));
        return ipsDependencies;
    }

    private List<IIpsObjectPathEntry> createIpsObjectPathEntries(IIpsProject ipsProject,
            Set<IpsDependency> ipsDependencies) {
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        List<IIpsObjectPathEntry> entries = new ArrayList<>();
        for (IpsDependency dependency : ipsDependencies) {
            if (dependency.ipsProject() == null) {
                addJarDependency(ipsObjectPath, entries, dependency);
            } else {
                addProjectDependency(ipsObjectPath, entries, dependency);
            }
        }
        return entries;
    }

    private void addProjectDependency(IIpsObjectPath ipsObjectPath,
            List<IIpsObjectPathEntry> entries,
            IpsDependency dependency) {
        entries.add(new IpsProjectRefEntry((IpsObjectPath)ipsObjectPath, dependency.ipsProject()));
    }

    private void addJarDependency(IIpsObjectPath ipsObjectPath,
            List<IIpsObjectPathEntry> entries,
            IpsDependency dependency) {
        IpsBundleEntry ipsBundleEntry = new IpsBundleEntry((IpsObjectPath)ipsObjectPath);
        try {
            ipsBundleEntry.initStorage(dependency.path());
            entries.add(ipsBundleEntry);
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private Set<IpsDependency> findIpsJars(MavenProject project, Set<IpsDependency> upstreamProjects) {
        Set<IpsDependency> dependencies = new HashSet<>();
        ArtifactRepository repository = session.getLocalRepository();

        for (Artifact artifact : project.getArtifacts()) {
            File file = repository.find(artifact).getFile();
            if (upstreamProjects.stream().anyMatch(d -> d.artifactId().equals(artifact.getArtifactId())
                    && d.groupId().equals(artifact.getGroupId())
                    && d.version().equals(artifact.getVersion()))) {
                // already a local dependency
                getLog().info("Using upstream project for " + artifact);
            } else if (file.exists() && isFipsProjectFromManifest(file)) {
                dependencies.add(IpsDependency.create(artifact));
            }
        }
        return dependencies;
    }

    private boolean isFipsProjectFromManifest(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            return jarFile.getManifest().getMainAttributes()
                    .get(new Attributes.Name("Fips-BasePackage")) != null;
        } catch (IOException e) {
            getLog().error(e);
        }
        return false;
    }

    private Set<IpsDependency> findUpstreamIpsProjects(List<MavenProject> upstreamProjects) {
        return upstreamProjects.stream()
                .filter(this::isPackagingJar)
                .filter(this::hasPomFile)
                .filter(this::isFipsProject)
                .map(IpsDependency::create)
                .collect(Collectors.toSet());
    }

    private boolean isPackagingJar(MavenProject p) {
        return p.getPackaging().equalsIgnoreCase("jar");
    }

    private boolean hasPomFile(MavenProject p) {
        return new File(p.getBasedir().getAbsoluteFile(), "pom.xml").exists();
    }

    private boolean isFipsProject(MavenProject p) {
        return new File(p.getBasedir().getAbsoluteFile(), ".ipsproject").exists();
    }
}
