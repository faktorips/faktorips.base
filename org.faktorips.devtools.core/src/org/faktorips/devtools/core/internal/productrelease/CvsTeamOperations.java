/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.productrelease;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.ui.operations.CommitOperation;
import org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation;
import org.eclipse.team.internal.ccvs.ui.operations.TagOperation;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;

/**
 * This implements the {@link ITeamOperations} for the eclipse cvs plugin. Most operations of the
 * plugin are in restricted API. However we neet to use this api to get rich of the CVS operations
 * within eclipse without using an own cvs implementation.
 * 
 * @author dirmeier
 */
@SuppressWarnings("restriction")
// we have to use very much restricted API here
public class CvsTeamOperations implements ITeamOperations {

    private final ObservableProgressMessages observableProgressMessages;

    public CvsTeamOperations(ObservableProgressMessages observableProgressMessages) {
        this.observableProgressMessages = observableProgressMessages;
    }

    @Override
    public void commitFiles(IProject project, IResource[] resources, String comment, IProgressMonitor monitor)
            throws TeamException, InterruptedException {
        monitor.beginTask(null, 2);
        try {
            RepositoryProvider repositoryProvider = RepositoryProvider.getProvider(project);
            if (repositoryProvider == null) {
                observableProgressMessages.warning(Messages.CvsTeamOperations_status_notVersionized);
                return;
            }
            Subscriber subscriber = repositoryProvider.getSubscriber();
            subscriber.refresh(resources, IResource.DEPTH_ZERO, new SubProgressMonitor(monitor, 1));
            List<IResource> syncResources = new ArrayList<IResource>();
            for (IResource aResource : resources) {
                SyncInfo syncInfo = subscriber.getSyncInfo(aResource);
                if (syncInfo == null) {
                    // file seems to be ignored
                    continue;
                }
                if (syncInfo.getKind() != 0 && (syncInfo.getKind() & SyncInfo.OUTGOING) != SyncInfo.OUTGOING) {
                    throw new InterruptedException(Messages.CvsTeamOperations_exception_remoteChanges);
                }
                syncResources.add(aResource);
            }

            CommitOperation commitOperation = new CommitOperation(null,
                    RepositoryProviderOperation.asResourceMappers(syncResources.toArray(new IResource[syncResources
                            .size()])), new Command.LocalOption[0], comment);
            commitOperation.execute(new SubProgressMonitor(monitor, 1));
            observableProgressMessages.info(Messages.ProductReleaseProcessor_status_commit_success);
        } finally {
            monitor.done();
        }
    }

    @Override
    public boolean isProjectSynchronized(IProject project, IProgressMonitor monitor) {
        try {
            RepositoryProvider repositoryProvider = RepositoryProvider.getProvider(project);
            if (repositoryProvider == null) {
                observableProgressMessages.warning(Messages.CvsTeamOperations_status_notVersionized);
                return true;
            }
            SyncInfoSet syncInfoSet = new SyncInfoSet();
            Subscriber subscriber = repositoryProvider.getSubscriber();
            IResource[] resources = new IResource[] { project };
            monitor.beginTask(null, 2);
            if (subscriber != null) {

                subscriber.refresh(resources, IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
                subscriber.collectOutOfSync(resources, IResource.DEPTH_INFINITE, syncInfoSet, new SubProgressMonitor(
                        monitor, 1));
                final boolean empty = syncInfoSet.isEmpty();
                if (empty) {
                    observableProgressMessages.info(Messages.ProductReleaseProcessor_status_synchon);
                } else {
                    for (IResource resource : syncInfoSet.getResources()) {
                        observableProgressMessages.warning(NLS.bind(Messages.CvsTeamOperations_status_notSynchron,
                                resource.getName()));
                    }
                    for (ITeamStatus status : syncInfoSet.getErrors()) {
                        observableProgressMessages.error(status.getMessage());
                    }
                }
                return empty;
            } else {
                // seems to be no cvs project
                observableProgressMessages.warning(Messages.CvsTeamOperations_status_notVersionized);
                return true;
            }
        } catch (RuntimeException e) {
            // runtime exceptions should not be thrown
            throw e;
        } catch (Exception e) {
            // any exception during cvs check would be catched and 'not synchron' is returned
            return false;
        } finally {
            monitor.done();
        }
    }

    @Override
    public String tagProject(IProject project, String version, IProgressMonitor monitor) throws TeamException,
            InterruptedException {
        String tag = version;
        if (tag.matches("[0-9].*")) { //$NON-NLS-1$
            // tag must start with a letter
            tag = "v" + tag; //$NON-NLS-1$
        }
        // replace not allowed characters to '_'
        tag = tag.replaceAll("[\\$,\\.:;@]", "_"); //$NON-NLS-1$ //$NON-NLS-2$

        RepositoryProvider repositoryProvider = RepositoryProvider.getProvider(project);
        if (repositoryProvider == null) {
            observableProgressMessages.warning(Messages.CvsTeamOperations_status_notVersionized);
            return tag;
        }

        IResource[] resources = new IResource[] { project };

        ResourceMapping[] resourceMappers = RepositoryProviderOperation.asResourceMappers(resources);

        TagOperation tagOperation = new TagOperation(null, resourceMappers);
        tagOperation.setTag(new CVSTag(tag, CVSTag.VERSION));
        tagOperation.setInvolvesMultipleResources(true);
        // tagOperation.moveTag();
        IStatus status = tagOperation.tag((CVSTeamProvider)repositoryProvider, resources, true, monitor);
        if (status.getException() != null) {
            throw new InterruptedException("Error while tagging: " + status.getException().getMessage()); //$NON-NLS-1$
        } else if (status.getSeverity() == IStatus.ERROR) {
            throw new InterruptedException(status.getMessage());
        }
        observableProgressMessages.info(NLS.bind(Messages.ProductReleaseProcessor_status_tag_success, tag));
        return tag;
    }

    @Override
    public String getVersionControlSystem() {
        return "CVS"; //$NON-NLS-1$
    }

}
