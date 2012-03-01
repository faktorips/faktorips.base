/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;

/**
 * Basic implementation of {@link ITeamOperations} that handles everything that can be done with the
 * core team API and defines methods to be implemented by team provider specific implementations.
 */
public abstract class AbstractTeamOperations implements ITeamOperations {

    private final ObservableProgressMessages observableProgressMessages;

    public AbstractTeamOperations(ObservableProgressMessages observableProgressMessages) {
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

            commitFiles(syncResources, comment, monitor);
            observableProgressMessages.info(Messages.ProductReleaseProcessor_status_commit_success);
        } finally {
            monitor.done();
        }
    }

    /**
     * Commit the files using the commit comment.
     */
    abstract protected void commitFiles(List<IResource> syncResources, String comment, IProgressMonitor monitor)
            throws TeamException, InterruptedException;

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

        IStatus status = tagProject(repositoryProvider, resources, tag, monitor);
        if (status.getException() != null) {
            throw new InterruptedException("Error while tagging: " + status.getException().getMessage()); //$NON-NLS-1$
        } else if (status.getSeverity() == IStatus.ERROR) {
            throw new InterruptedException(status.getMessage());
        }
        observableProgressMessages.info(NLS.bind(Messages.ProductReleaseProcessor_status_tag_success, tag));
        return tag;
    }

    /**
     * Tag the resources with the given tag.
     * 
     * @param repositoryProvider the {@link RepositoryProvider} to use for connecting to the source
     *            control system
     * @param resources the {@link IResource IResources} to be tagged in the repository
     * @param tag the tag
     * @param monitor the {@link IProgressMonitor} to report the operation's progress
     * @return the tag operation's {@link IStatus status}
     */
    abstract protected IStatus tagProject(RepositoryProvider repositoryProvider,
            IResource[] resources,
            String tag,
            IProgressMonitor monitor) throws TeamException;

}