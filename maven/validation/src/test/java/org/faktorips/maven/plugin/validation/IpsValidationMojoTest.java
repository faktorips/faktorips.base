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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.internal.impl.DefaultLocalPathComposer;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.ChainedLocalRepositoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

class IpsValidationMojoTest {

    private final MavenProject project = mock(MavenProject.class);

    private final MavenSession session = mock(MavenSession.class);

    private final RepositorySystemSession repositorySession = mock(RepositorySystemSession.class);

    private final LocalRepositoryManager localRepositoryManager = mock(LocalRepositoryManager.class);

    private final DependencyGraphBuilder dependencyGraphBuilder = mock(DependencyGraphBuilder.class);

    @TempDir
    private Path tempDir;

    private IpsValidationMojo mojo;

    @BeforeEach
    void setUp() {
        doReturn(repositorySession).when(session).getRepositorySession();
        doReturn(localRepositoryManager).when(repositorySession).getLocalRepositoryManager();
        mojo = new IpsValidationMojo(project, session, dependencyGraphBuilder);
    }

    @Test
    void testFindIpsJars_FipsJar() throws IOException, DependencyGraphBuilderException {
        File jar = createJar(tempDir.resolve("fips.jar").toFile(), true);
        stubFind(jar);
        Artifact artifact = artifact();
        stubDependencyGraph(artifact);

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of());

        assertThat(dependencies, hasSize(1));
        IpsDependency dependency = dependencies.iterator().next();
        assertThat(dependency.groupId(), is("foo.bar"));
        assertThat(dependency.artifactId(), is("baz"));
        assertThat(dependency.version(), is("1.0"));
        assertThat(dependency.path(), is(jar.toPath()));
    }

    @Test
    void testFindIpsJars_FipsJarOnlyInTailRepository() throws IOException, DependencyGraphBuilderException {
        LocalRepositoryManager head = simpleRepository("head");
        LocalRepositoryManager tail = simpleRepository("tail");
        Artifact artifact = artifact();
        File jarInTail = createJar(new File(tail.getRepository().getBasedir(),
                tail.getPathForLocalArtifact(RepositoryUtils.toArtifact(artifact))), true);
        doReturn(new ChainedLocalRepositoryManager(head, List.of(tail), true)).when(repositorySession)
                .getLocalRepositoryManager();
        stubDependencyGraph(artifact);

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of());

        assertThat(dependencies, hasSize(1));
        assertThat(dependencies.iterator().next().path(), is(jarInTail.toPath()));
    }

    @Test
    void testFindIpsJars_NotInAnyChainedLocalRepository() throws DependencyGraphBuilderException {
        LocalRepositoryManager head = simpleRepository("head");
        LocalRepositoryManager tail = simpleRepository("tail");
        doReturn(new ChainedLocalRepositoryManager(head, List.of(tail), true)).when(repositorySession)
                .getLocalRepositoryManager();
        stubDependencyGraph(artifact());

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of());

        assertThat(dependencies, is(empty()));
    }

    @Test
    void testFindIpsJars_RequestContainsArtifactAndProjectRepositories() throws DependencyGraphBuilderException {
        RemoteRepository remoteRepository = new RemoteRepository.Builder("remote", "default",
                "https://faktorips.org/repository").build();
        doReturn(List.of(remoteRepository)).when(project).getRemoteProjectRepositories();
        stubFind(null);
        stubDependencyGraph(artifact());

        mojo.findIpsJars(project, Set.of());

        ArgumentCaptor<LocalArtifactRequest> requestCaptor = ArgumentCaptor.forClass(LocalArtifactRequest.class);
        verify(localRepositoryManager).find(same(repositorySession), requestCaptor.capture());
        LocalArtifactRequest request = requestCaptor.getValue();
        assertThat(request.getArtifact().getGroupId(), is("foo.bar"));
        assertThat(request.getArtifact().getArtifactId(), is("baz"));
        assertThat(request.getArtifact().getVersion(), is("1.0"));
        assertThat(request.getRepositories(), is(List.of(remoteRepository)));
    }

    @Test
    void testFindIpsJars_ArtifactFileIsResetAfterwards() throws IOException, DependencyGraphBuilderException {
        File originalFile = tempDir.resolve("original.jar").toFile();
        Artifact artifact = artifact();
        artifact.setFile(originalFile);
        stubFind(createJar(tempDir.resolve("fips.jar").toFile(), true));
        stubDependencyGraph(artifact);

        mojo.findIpsJars(project, Set.of());

        assertThat(artifact.getFile(), is(originalFile));
    }

    @Test
    void testFindIpsJars_JarWithoutFipsManifestEntry() throws IOException, DependencyGraphBuilderException {
        stubFind(createJar(tempDir.resolve("other.jar").toFile(), false));
        stubDependencyGraph(artifact());

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of());

        assertThat(dependencies, is(empty()));
    }

    @Test
    void testFindIpsJars_NotFoundInLocalRepositories() throws DependencyGraphBuilderException {
        stubFind(null);
        stubDependencyGraph(artifact());

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of());

        assertThat(dependencies, is(empty()));
    }

    @Test
    void testFindIpsJars_FileDoesNotExist() throws DependencyGraphBuilderException {
        stubFind(tempDir.resolve("missing.jar").toFile());
        stubDependencyGraph(artifact());

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of());

        assertThat(dependencies, is(empty()));
    }

    @Test
    void testFindIpsJars_UpstreamProjectIsNotLookedUpInRepository()
            throws IOException, DependencyGraphBuilderException {
        stubFind(createJar(tempDir.resolve("fips.jar").toFile(), true));
        stubDependencyGraph(artifact());
        IpsDependency upstream = new IpsDependency("baz", "foo.bar", "1.0", tempDir, null, null);

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of(upstream));

        assertThat(dependencies, is(empty()));
        verify(localRepositoryManager, never()).find(any(RepositorySystemSession.class),
                any(LocalArtifactRequest.class));
    }

    @Test
    void testFindIpsJars_ProjectItselfIsIgnored() throws IOException, DependencyGraphBuilderException {
        stubFind(createJar(tempDir.resolve("fips.jar").toFile(), true));
        doReturn("foo.bar").when(project).getGroupId();
        doReturn("baz").when(project).getArtifactId();
        doReturn("1.0").when(project).getVersion();
        stubDependencyGraph(artifact());

        Set<IpsDependency> dependencies = mojo.findIpsJars(project, Set.of());

        assertThat(dependencies, is(empty()));
        verify(localRepositoryManager, never()).find(any(RepositorySystemSession.class),
                any(LocalArtifactRequest.class));
    }

    private LocalRepositoryManager simpleRepository(String name) {
        try {
            return new SimpleLocalRepositoryManagerFactory(new DefaultLocalPathComposer()).newInstance(
                    repositorySession,
                    new LocalRepository(tempDir.resolve(name).toFile()));
        } catch (NoLocalRepositoryManagerException e) {
            throw new IllegalStateException(e);
        }
    }

    // IpsValidationMojo synchronizes on the deprecated session.getLocalRepository()
    @SuppressWarnings("deprecation")
    private void stubDependencyGraph(Artifact... artifacts) throws DependencyGraphBuilderException {
        doReturn(mock(ArtifactRepository.class)).when(session).getLocalRepository();
        doReturn(new DefaultProjectBuildingRequest()).when(session).getProjectBuildingRequest();
        DependencyNode root = mock(DependencyNode.class);
        doAnswer(invocation -> {
            DependencyNodeVisitor visitor = invocation.getArgument(0);
            Arrays.stream(artifacts).forEach(artifact -> {
                DependencyNode node = mock(DependencyNode.class);
                doReturn(artifact).when(node).getArtifact();
                visitor.visit(node);
            });
            return true;
        }).when(root).accept(any(DependencyNodeVisitor.class));
        doReturn(root).when(dependencyGraphBuilder).buildDependencyGraph(any(), any());
    }

    private static File createJar(File jar, boolean fipsProject) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (fipsProject) {
            manifest.getMainAttributes().putValue("Fips-BasePackage", "foo.bar");
        }
        Files.createDirectories(jar.getParentFile().toPath());
        try (OutputStream out = Files.newOutputStream(jar.toPath());
                JarOutputStream jarOut = new JarOutputStream(out, manifest)) {
            // only the manifest is needed
        }
        return jar;
    }

    /**
     * Lets the {@link LocalRepositoryManager} answer every artifact request with the given file (or
     * no file, if <code>null</code>).
     */
    private void stubFind(File file) {
        doAnswer(invocation -> {
            LocalArtifactResult result = new LocalArtifactResult(invocation.getArgument(1));
            result.setFile(file);
            return result;
        }).when(localRepositoryManager).find(any(RepositorySystemSession.class), any(LocalArtifactRequest.class));
    }

    private static Artifact artifact() {
        return new DefaultArtifact("foo.bar", "baz", "1.0", "compile", "jar", null, new DefaultArtifactHandler("jar"));
    }

}
