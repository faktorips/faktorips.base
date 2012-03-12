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

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.ui.operations.CommitOperation;
import org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation;
import org.eclipse.team.internal.ccvs.ui.operations.TagOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;

/**
 * This implements the {@link ITeamOperations} for the Eclipse CVS plugin. Most operations of the
 * plugin are in restricted API. However we need to use this API to use the CVS operations within
 * Eclipse without using our own CVS implementation.
 * 
 * @author dirmeier
 */
@SuppressWarnings("restriction")
// we have to use a lot of restricted API here
public class CvsTeamOperations extends AbstractTeamOperations {

    public CvsTeamOperations(ObservableProgressMessages observableProgressMessages) {
        super(observableProgressMessages);
    }

    static boolean isCvsProject(IIpsProject ipsProject) {
        return CVSTeamProvider.isSharedWithCVS(ipsProject.getProject());
    }

    @Override
    protected void commitFiles(List<IResource> syncResources, String comment, IProgressMonitor monitor)
            throws CVSException, InterruptedException {
        CommitOperation commitOperation = new CommitOperation(
                null,
                RepositoryProviderOperation.asResourceMappers(syncResources.toArray(new IResource[syncResources.size()])),
                new Command.LocalOption[0], comment);
        commitOperation.execute(new SubProgressMonitor(monitor, 1));
    }

    @Override
    protected IStatus tagProject(RepositoryProvider repositoryProvider,
            IResource[] resources,
            String tag,
            IProgressMonitor monitor) throws CVSException {
        ResourceMapping[] resourceMappers = RepositoryProviderOperation.asResourceMappers(resources);

        TagOperation tagOperation = new TagOperation(null, resourceMappers);
        tagOperation.setTag(new CVSTag(tag, CVSTag.VERSION));
        tagOperation.setInvolvesMultipleResources(true);
        // tagOperation.moveTag();
        IStatus status = tagOperation.tag((CVSTeamProvider)repositoryProvider, resources, true, monitor);
        return status;
    }

    @Override
    public String getVersionControlSystem() {
        return "CVS"; //$NON-NLS-1$
    }

}
