/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.m2e.toctree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.embedder.MavenImpl;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.model.testcase.ITocTreeFromDependencyManagerLoader;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsJarBundle;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.QNameUtil;

@SuppressWarnings("restriction")
public class MavenTocTreeLoader implements ITocTreeFromDependencyManagerLoader {

    private static final String POM_FILE_NAME = "pom.xml"; //$NON-NLS-1$
    private static final String INTERNAL_PACKAGE = "internal"; //$NON-NLS-1$

    @Override
    public boolean isResponsibleFor(IIpsProject ipsProject) {
        IMavenProjectFacade mavenProjectFacade = findMavenProjectFacade(ipsProject.getProject());
        return mavenProjectFacade != null && mavenProjectFacade.getMavenProject() != null
                && Arrays.stream(ipsProject.getIpsPackageFragmentRoots())
                        .anyMatch(LibraryIpsPackageFragmentRoot.class::isInstance);
    }

    @Override
    public void loadTocTreeFromDependencyManager(IIpsProject ipsProject, List<String> repositoryPackages)
            {
        try {
            List<IIpsPackageFragmentRoot> ipsRootsList = Arrays.asList(ipsProject.getIpsPackageFragmentRoots());
            Collections.reverse(ipsRootsList);

            IMavenProjectFacade mavenProjectFacade = findMavenProjectFacade(ipsProject.getProject());
            MavenProject mavenProject = mavenProjectFacade.getMavenProject(new NullProgressMonitor());
            DependencyNode mavenDependencies = MavenPlugin.getMavenModelManager().readDependencyTree(mavenProjectFacade,
                    mavenProject, "compile", new NullProgressMonitor());

            Set<IpsJarBundle> jarBundles = ipsRootsList.stream()
                    .filter(LibraryIpsPackageFragmentRoot.class::isInstance)
                    .map(LibraryIpsPackageFragmentRoot.class::cast)
                    .map(LibraryIpsPackageFragmentRoot::getIpsStorage)
                    .filter(IpsJarBundle.class::isInstance)
                    .map(IpsJarBundle.class::cast)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<Artifact, IpsMavenDependency> ipsDependencies = new LinkedHashMap<>();
            findIpsMavenDependenciesAndTocs(mavenDependencies, null, jarBundles, ipsDependencies, mavenProject);

            ipsDependencies.values().stream().map(IpsMavenDependency::toString).forEach(repositoryPackages::add);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    private void findIpsMavenDependenciesAndTocs(DependencyNode rootNode,
            Artifact parent,
            Set<IpsJarBundle> jarBundles,
            Map<Artifact, IpsMavenDependency> ipsDependencies,
            MavenProject mp)
            throws CoreException {

        try {
            for (DependencyNode childNode : rootNode.getChildren()) {

                Artifact child = resolveArtifactFile(childNode, mp);
                File artifactFile = child.getFile().getAbsoluteFile().getCanonicalFile();

                for (IpsJarBundle ipsJarBundle : jarBundles) {
                    File jarBundleFile = getJarBundleFile(ipsJarBundle);
                    if (artifactFile.equals(jarBundleFile)) {

                        String tocFileFromManifest = getTocFileFromManifest(ipsJarBundle);

                        if (parent != null && ipsDependencies.get(parent) != null) {
                            ipsDependencies.get(parent).addDependencies(new IpsMavenDependency(tocFileFromManifest));
                        } else {
                            ipsDependencies.put(child, new IpsMavenDependency(tocFileFromManifest));
                        }
                        break;
                    }
                }
                findIpsMavenDependenciesAndTocs(childNode, child, jarBundles, ipsDependencies, mp);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getJarBundleFile(IpsJarBundle jarBundle) throws IOException {
        Path location = jarBundle.getLocation();
        return location.toFile().getAbsoluteFile().getCanonicalFile();
    }

    private Artifact resolveArtifactFile(DependencyNode child, MavenProject mavenProject) throws CoreException {
        return ((MavenImpl)MavenPlugin.getMaven()).resolve(
                RepositoryUtils.toArtifact(child.getArtifact()),
                mavenProject.getRemoteArtifactRepositories(), new NullProgressMonitor());
    }

    private String getTocFileFromManifest(IpsJarBundle jarBundle) {
        IpsBundleManifest bundleManifest = jarBundle.getBundleManifest();
        for (ManifestElement manifestElement : bundleManifest.getObjectDirElements()) {
            String objectDir = manifestElement.getValue();
            String basePackage = bundleManifest.getBasePackage(objectDir);
            String tocPath = bundleManifest.getTocPath(manifestElement);
            String internalPackage = QNameUtil.concat(basePackage, INTERNAL_PACKAGE);
            Path path = QNameUtil.toPath(internalPackage);
            return path == null ? tocPath : path.resolve(tocPath).toString();
        }
        throw new IpsException("No toc found in the IpsJarBundle " + jarBundle.toString());
    }

    private IMavenProjectFacade findMavenProjectFacade(AProject project) {
        IFile pom = project.getFile(POM_FILE_NAME).unwrap();
        return MavenPlugin.getMavenProjectRegistry().create(pom, true,
                new NullProgressMonitor());
    }

    private static class IpsMavenDependency {

        private final String tocPath;
        private final Set<IpsMavenDependency> dependencies = new LinkedHashSet<>();

        public IpsMavenDependency(String tocPath) {
            this.tocPath = tocPath;
        }

        @Override
        public String toString() {
            return tocPath + dependencies.stream().map(IpsMavenDependency::toString)
                    .collect(Collectors.joining(">,<", "[<", ">]"));
        }

        public void addDependencies(IpsMavenDependency dep) {
            dependencies.add(dep);
        }
    }
}
