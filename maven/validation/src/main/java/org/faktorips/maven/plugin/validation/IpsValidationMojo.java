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
import java.util.Arrays;
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
import org.codehaus.plexus.logging.Logger;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaImplementation;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaWorkspace;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plainjava.internal.PlainJavaIpsModelExtensions;
import org.faktorips.maven.plugin.validation.mavenversion.MavenVersionProviderFactory;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Validates a Faktor-IPS project.
 */
@Mojo(name = "faktorips-validate", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class IpsValidationMojo extends AbstractMojo {

    /**
     * Whether to skip mojo execution.
     */
    @Parameter(property = "faktorips.skipValidation", defaultValue = "false")
    private boolean skip;

    @Parameter(property = "session", readonly = true, required = true)
    private MavenSession session;

    @Component
    private Logger logger;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("skipping mojo execution");
            return;
        }
        initWorkspace();
        AProject aProject = Abstractions.getWorkspace().getRoot().getProject(project.getBasedir().getName());

        if (!aProject.isIpsProject()) {
            return;
        }

        Set<IpsDependency> ipsDependencies = findDependencies();
        setIpsObjectPath(ipsDependencies);

        IIpsProject ipsProject = IIpsModel.get().getIpsProject(aProject);

        setVersionProvider(ipsDependencies);

        MessageList validationResults = validate(ipsProject);

        logMessages(validationResults, getLog());
    }

    private MessageList validate(IIpsProject ipsProject) {
        MessageList validationResults = ipsProject.validate();

        // TODO FIPS-9513 -> parallelStream()
        Arrays.stream(ipsProject.getIpsPackageFragmentRoots(false))
                .filter(IIpsPackageFragmentRoot::isBasedOnSourceFolder)
                .map(IIpsPackageFragmentRoot::getIpsPackageFragments)
                .flatMap(Arrays::stream)
                .map(IIpsPackageFragment::getChildren)
                .flatMap(Arrays::stream)
                .filter(IIpsSrcFile.class::isInstance)
                .map(IIpsSrcFile.class::cast)
                .map(IIpsSrcFile::getIpsObject)
                .map(o -> o.validate(ipsProject))
                .forEach(validationResults::add);
        return validationResults;
    }

    private void initWorkspace() {
        PlainJavaWorkspace plainJavaWorkspace = new PlainJavaWorkspace(project.getBasedir().getParentFile());
        PlainJavaImplementation.get().setWorkspace(plainJavaWorkspace);
    }

    private void setVersionProvider(Set<IpsDependency> ipsDependencies) {
        var dependenciesInclProject = new LinkedHashSet<>(ipsDependencies);
        dependenciesInclProject.add(IpsDependency.create(project));
        PlainJavaIpsModelExtensions.get().setVersionProviderFactory("org.faktorips.maven.mavenVersionProvider",
                new MavenVersionProviderFactory(dependenciesInclProject));
    }

    static void logMessages(MessageList messageList, Log log) {
        for (Message message : messageList) {
            StringBuilder sb = new StringBuilder();
            sb.append(message.getText());
            sb.append(" (");
            sb.append(message.getCode());
            sb.append(")");
            message.appendInvalidObjectProperties(sb);
            String messageWithoutSeverity = sb.toString();
            switch (message.getSeverity()) {
                case ERROR:
                    log.error(messageWithoutSeverity);
                    break;
                case WARNING:
                    log.warn(messageWithoutSeverity);
                    break;
                default:
                    log.info(messageWithoutSeverity);
            }
        }
    }

    private void setIpsObjectPath(Set<IpsDependency> ipsDependencies) {
        Function<IIpsProject, List<IIpsObjectPathEntry>> projectDependencyEntries = ipsDependencies.isEmpty()
                ? i -> new ArrayList<>()
                : i -> createIpsObjectPathEntries(i,
                        ipsDependencies);
        PlainJavaIpsModelExtensions.get().setProjectDependenciesProvider(projectDependencyEntries);
    }

    private Set<IpsDependency> findDependencies() {
        Set<IpsDependency> ipsDependencies = new LinkedHashSet<>();
        ipsDependencies.addAll(findUpstreamProjects());
        ipsDependencies.addAll(findIpsJars(project));
        return ipsDependencies;
    }

    private List<IIpsObjectPathEntry> createIpsObjectPathEntries(IIpsProject ipsProject,
            Set<IpsDependency> ipsDependencies) {
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        List<IIpsObjectPathEntry> entries = new ArrayList<>();
        for (IpsDependency dependency : ipsDependencies) {
            if (!dependency.isProject()) {
                IpsBundleEntry ipsBundleEntry = new IpsBundleEntry((IpsObjectPath)ipsObjectPath);
                try {
                    ipsBundleEntry.initStorage(dependency.getPath());
                    entries.add(ipsBundleEntry);
                } catch (IOException e) {
                    getLog().error(e);
                }
            } else {
                entries.add(new IpsProjectRefEntry((IpsObjectPath)ipsObjectPath, dependency.getIpsProject()));
            }
        }
        return entries;
    }

    private Set<IpsDependency> findIpsJars(MavenProject project) {
        Set<IpsDependency> dependencies = new HashSet<>();
        ArtifactRepository localRepository = session.getLocalRepository();
        Set<Artifact> dependencyArtifacts = project.getArtifacts();
        for (Artifact artifact : dependencyArtifacts) {
            File file = localRepository.find(artifact).getFile();
            try (JarFile jarFile = new JarFile(file)) {
                Object attribute = jarFile.getManifest().getMainAttributes()
                        .get(new Attributes.Name("Fips-BasePackage"));
                if (attribute != null) {
                    dependencies.add(IpsDependency.create(artifact));
                }
            } catch (IOException e) {
                getLog().error(e);
            }
        }
        return dependencies;
    }

    private Set<IpsDependency> findUpstreamProjects() {
        List<MavenProject> upstreamProjects = session.getProjectDependencyGraph()
                .getUpstreamProjects(project, true);
        return upstreamProjects.stream()
                .filter(p -> p.getPackaging().equalsIgnoreCase("jar"))
                .filter(p -> new File(p.getBasedir().getAbsoluteFile(), "pom.xml").exists())
                .filter(p -> new File(p.getBasedir().getAbsoluteFile(), ".ipsproject").exists())
                .map(IpsDependency::create)
                .collect(Collectors.toSet());
    }
}
