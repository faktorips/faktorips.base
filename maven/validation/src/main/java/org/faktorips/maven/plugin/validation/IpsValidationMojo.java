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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;
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
@Mojo(name = "faktorips-validate", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST, requiresDependencyCollection = ResolutionScope.TEST)
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

    private final MavenProject project;

    private final MavenSession session;

    private final DependencyGraphBuilder dependencyGraphBuilder;

    @Inject
    public IpsValidationMojo(MavenProject project,
            MavenSession session,
            DependencyGraphBuilder dependencyGraphBuilder) {
        this.project = project;
        this.session = session;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
    }

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

        initWorkspace();
        AProject aProject = Abstractions.getWorkspace().getRoot()
                .getProject(MavenWorkspaceRoot.toProjectName(project));

        if (!aProject.isIpsProject()) {
            return;
        }

        IIpsProject ipsProject = IIpsModel.get().getIpsProject(aProject);

        new IpsProjectValidator(ipsProject, project, log).validate(!ignoreValidationErrors);
    }

    private void initWorkspace() {
        synchronized (session) {
            if (!(PlainJavaIpsModelExtensions.get() instanceof MavenIpsModelExtensions)) {
                new MavenIpsModelExtensions(session, this::getLog);
                PlainJavaImplementation.get()
                        .setWorkspace(new MavenWorkspace(session.getTopLevelProject(), session.getAllProjects()));
                Set<IpsDependency> ipsProjectsInBuild = asIpsDependencies(session.getAllProjects());
                setVersionProvider(ipsProjectsInBuild);
                PlainJavaIpsModelExtensions.get()
                        .setProjectDependenciesProvider(i -> createProjectDependencies(ipsProjectsInBuild, i));
            }
        }
    }

    private List<IIpsObjectPathEntry> createProjectDependencies(Set<IpsDependency> ipsProjectsInBuild,
            IIpsProject ipsProject) {
        MavenProject mavenProject = findByIpsProject(ipsProjectsInBuild, ipsProject).get();
        Set<IpsDependency> projectDependencies = session.getProjectDependencyGraph()
                .getUpstreamProjects(mavenProject, true)
                .stream()
                .flatMap(p -> findByMavenProject(ipsProjectsInBuild, p))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<IpsDependency> ipsDependencies = new LinkedHashSet<>(projectDependencies);
        ipsDependencies.addAll(findIpsJars(mavenProject, projectDependencies));
        return createIpsObjectPathEntries(ipsProject, ipsDependencies);
    }

    private Stream<IpsDependency> findByMavenProject(Set<IpsDependency> ipsDependencies, MavenProject mavenProject) {
        return ipsDependencies.stream()
                .filter(d -> d.getMavenProject().filter(Predicate.isEqual(mavenProject)).isPresent())
                .findFirst().stream();
    }

    private Optional<MavenProject> findByIpsProject(Set<IpsDependency> ipsDependencies, IIpsProject ipsProject) {
        return ipsDependencies.stream()
                .filter(d -> d.ipsProject().equals(ipsProject))
                .flatMap(d -> d.getMavenProject().stream())
                .findFirst();
    }

    private void setVersionProvider(Set<IpsDependency> ipsDependencies) {
        PlainJavaIpsModelExtensions.get().setVersionProviderFactory("org.faktorips.maven.mavenVersionProvider",
                new MavenVersionProviderFactory(ipsDependencies));
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
        // don't let multiple runs of the validation plugin do this concurrently
        synchronized (repository) {
            List<DependencyNode> children = getChildrenFromDependencyGraphBuilder(project);
            for (DependencyNode node : children) {
                Artifact artifact = node.getArtifact();
                if (isItself(artifact, project)) {
                    continue;
                }
                File originalFile = artifact.getFile();
                try {
                    // find sets the artifact's file to the one expected in the repository,
                    // even if it does not exist, so we have to reset this afterwards
                    File fileFromRepository = repository.find(artifact).getFile();
                    if (upstreamProjects.stream().anyMatch(ipsDependency -> isItself(artifact, ipsDependency))) {
                        // already a local dependency
                        getLog().info("Using upstream project for " + artifact);
                    } else if (fileFromRepository.exists() && isFipsProjectFromManifest(fileFromRepository)) {
                        dependencies.add(IpsDependency.create(artifact));
                    }
                } finally {
                    artifact.setFile(originalFile);
                }
            }
        }

        return dependencies;
    }

    private List<DependencyNode> getChildrenFromDependencyGraphBuilder(MavenProject project) {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setProject(project);
        DependencyNode depenGraphRootNode;
        try {
            depenGraphRootNode = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, null);

            CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
            depenGraphRootNode.accept(visitor);
            return visitor.getNodes();
        } catch (DependencyGraphBuilderException e) {
            throw new RuntimeException("Failed to collect dependencies in Maven project" + project.getName(), e);
        }
    }

    private boolean isItself(Artifact artifact, IpsDependency ipsDependency) {
        return Objects.equals(ipsDependency.artifactId(), artifact.getArtifactId())
                && Objects.equals(ipsDependency.groupId(), artifact.getGroupId())
                && Objects.equals(ipsDependency.version(), artifact.getVersion());
    }

    private boolean isItself(Artifact artifact, MavenProject mproject) {
        return Objects.equals(mproject.getArtifactId(), artifact.getArtifactId())
                && Objects.equals(mproject.getGroupId(), artifact.getGroupId())
                && Objects.equals(mproject.getVersion(), artifact.getVersion());
    }

    private boolean isFipsProjectFromManifest(File file) {
        if (file.getName().matches(".*\\.(JAR|jar)")) {
            try (JarFile jarFile = new JarFile(file)) {
                return jarFile.getManifest().getMainAttributes()
                        .get(new Attributes.Name("Fips-BasePackage")) != null;
            } catch (IOException e) {
                getLog().error("Can't read JAR file " + file, e);
            }
        }
        return false;
    }

    private Set<IpsDependency> asIpsDependencies(List<MavenProject> mavenProjects) {
        return mavenProjects.stream()
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
