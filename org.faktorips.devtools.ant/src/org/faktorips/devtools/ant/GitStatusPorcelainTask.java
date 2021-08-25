/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.GitProvider;
import org.eclipse.egit.core.op.CreatePatchOperation;
import org.eclipse.egit.core.op.CreatePatchOperation.DiffHeaderFormat;
import org.eclipse.egit.core.project.GitProjectData;
import org.eclipse.egit.core.project.RepositoryFinder;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfoSet;

@SuppressWarnings("restriction")
public class GitStatusPorcelainTask extends AbstractIpsTask {

    public enum Verbosity {
        QUIET("quiet"),
        VERBOSE("verbose"),
        DIFF("diff");

        private final String name;

        private Verbosity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        boolean matches(String value) {
            return name.equals(value);
        }
    }

    private static final String TASK_NAME = "GitStatusPorcelainTask";

    private boolean failBuild;
    private String verbosity;

    public GitStatusPorcelainTask() {
        super(TASK_NAME);
    }

    public boolean isFailBuild() {
        return failBuild;
    }

    public void setFailBuild(boolean failBuild) {
        this.failBuild = failBuild;
    }

    public String getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(String verbosity) {
        this.verbosity = verbosity;
    }

    @Override
    protected void executeInternal() throws Exception {

        final Map<IProject, List<IResource>> outOfSync = new HashMap<>();
        final Map<IProject, RepositoryProvider> projectWithRepository = connectWorkspaceProjectsToRepository();

        WorkspaceJob job = new WorkspaceJob("sync") {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                for (Entry<IProject, RepositoryProvider> sourceCtrl : projectWithRepository.entrySet()) {
                    IProject projekt = sourceCtrl.getKey();

                    Subscriber subscriber = Objects.requireNonNull(sourceCtrl.getValue(),
                            "No RepositoryProvider found for " + projekt.getName()).getSubscriber();

                    SyncInfoSet syncInfoSet = new SyncInfoSet();
                    subscriber.collectOutOfSync(new IResource[] { projekt }, IResource.DEPTH_INFINITE,
                            syncInfoSet, monitor);

                    Arrays.stream(syncInfoSet.getResources())
                            .forEach(resource -> outOfSync.computeIfAbsent(projekt, $ -> new ArrayList<>())
                                    .add(resource));
                }
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.BUILD);
        job.schedule();
        job.join();

        if (!outOfSync.isEmpty()) {
            if (Verbosity.VERBOSE.matches(getVerbosity())) {
                for (List<IResource> resources : outOfSync.values()) {
                    for (IResource resource : resources) {
                        log("Changes: " + resource.getLocation(), Project.MSG_INFO);
                    }
                }
            }
            if (Verbosity.DIFF.matches(getVerbosity())) {
                for (IProject gitProject : outOfSync.keySet()) {
                    DiffViewer diff = new DiffViewer(gitProject);
                    log(diff.createDiffView(), Project.MSG_INFO);
                }
            }
            if (failBuild) {
                logNotQuiet("ERROR: There were changes!", Project.MSG_ERR);
                fail("ERROR: There were changes!");
            } else {
                logNotQuiet("WARNING: There were changes!", Project.MSG_WARN);
            }
        } else {
            logNotQuiet("Your branch is up to date.", Project.MSG_INFO);
        }
        errorHandling(job);
    }

    private void logNotQuiet(String msg, int level) {
        if (!Verbosity.QUIET.matches(getVerbosity())) {
            log(msg, level);
        }
    }

    private Map<IProject, RepositoryProvider> connectWorkspaceProjectsToRepository() {
        Map<IProject, RepositoryProvider> projectWithRepository = new HashMap<>();

        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {

            RepositoryProvider repositoryProvider = RepositoryProvider.getProvider(project, GitProvider.ID);

            if (repositoryProvider == null) {
                repositoryProvider = connectProjectToRepository(project);
            }
            projectWithRepository.put(project, repositoryProvider);
        }
        if (projectWithRepository.isEmpty()) {
            throw new BuildException("No Team provider found!");
        }
        return projectWithRepository;
    }

    private RepositoryProvider connectProjectToRepository(IProject project) {
        try {
            RepositoryFinder finder = new RepositoryFinder(project);
            finder.setFindInChildren(false);
            List<RepositoryMapping> repos = finder.find(new NullProgressMonitor());
            RepositoryMapping mapping = findActualRepository(repos);

            GitProjectData projectData = new GitProjectData(project);
            projectData.setRepositoryMappings(List.of(mapping));
            projectData.store();
            GitProjectData.add(project, projectData);

            RepositoryProvider.map(project, GitProvider.ID);

        } catch (CoreException e) {
            throw new BuildException(e);
        }
        return RepositoryProvider.getProvider(project);
    }

    private RepositoryMapping findActualRepository(Collection<RepositoryMapping> repos) {
        if (repos.isEmpty()) {
            throw new BuildException("Error connecting project, no Git repositories found");
        }
        RepositoryMapping mapping = repos.iterator().next();
        IPath absolutePath = mapping.getGitDirAbsolutePath();
        return findActualRepository(repos, absolutePath.toFile());
    }

    private RepositoryMapping findActualRepository(Collection<RepositoryMapping> repos, File suggestedRepo) {
        File path = Path.fromOSString(suggestedRepo.getPath()).toFile();
        for (RepositoryMapping rm : repos) {
            IPath other = rm.getGitDirAbsolutePath();
            if (other == null) {
                continue;
            }
            if (path.equals(other.toFile())) {
                return rm;
            }
        }
        throw new BuildException(MessageFormat.format(
                "Error connecting project, suggested path ''{0}'' does not match found Git repositories ''{1}''.",
                suggestedRepo.toString(), repos.toString()));
    }

    private void errorHandling(WorkspaceJob job) {
        IStatus result = job.getResult();
        if (result.getSeverity() == Status.ERROR) {
            if (result.getException() instanceof RuntimeException) {
                throw (RuntimeException)result.getException();
            }
            throw new RuntimeException("Error while syncing Faktor-IPS: " + result.getMessage(),
                    result.getException());
        }
    }

    private static class DiffViewer {

        private RepositoryMapping mapping;
        private String repoRelativePath;

        public DiffViewer(IProject project) {
            mapping = getGitRepository(project);
            repoRelativePath = mapping.getRepoRelativePath(project.getLocation());
        }

        private RepositoryMapping getGitRepository(IProject project) {
            GitProjectData gpd = GitProjectData.get(project);
            if (gpd != null) {
                return gpd.getRepositoryMapping(project);
            }
            throw new BuildException("Failed finding RepositoryMapping");
        }

        public String createDiffView() throws CoreException {
            return createDiffView(null);
        }

        public String createDiffView(RevCommit rev) throws CoreException {
            CreatePatchOperation op = new CreatePatchOperation(mapping.getRepository(), rev);
            op.setHeaderFormat(DiffHeaderFormat.NONE);
            op.setPathFilter(PathFilterGroup.createFromStrings(repoRelativePath));
            op.execute(new NullProgressMonitor());
            return op.getPatchContent();
        }
    }
}
