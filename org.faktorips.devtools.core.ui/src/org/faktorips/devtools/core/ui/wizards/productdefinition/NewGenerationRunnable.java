/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;

/**
 * Allows to create a new {@linkplain IIpsObjectGeneration IPS Object Generation} for a number of
 * provided {@linkplain ITimedIpsObject Timed IPS Objects}.
 * <p>
 * Configuration is done via an instance of {@linkplain NewGenerationPMO}.
 * 
 * @see NewGenerationWizard
 */
class NewGenerationRunnable extends WorkspaceModifyOperation {

    private final NewGenerationPMO pmo;

    private final List<ITimedIpsObject> timedIpsObjects;

    /**
     * @param pmo the {@linkplain PresentationModelObject Presentation Model Object} that configures
     *            this runnable
     * @param timedIpsObjects list of {@linkplain ITimedIpsObject Timed IPS Objects} to create new
     *            {@linkplain IIpsObjectGeneration IPS Object Generations} for
     */
    NewGenerationRunnable(NewGenerationPMO pmo, List<ITimedIpsObject> timedIpsObjects) {
        super();
        this.pmo = pmo;
        this.timedIpsObjects = timedIpsObjects;
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws IpsException, InvocationTargetException,
            InterruptedException {

        String taskName = NLS.bind(Messages.NewGenerationRunnable_taskName, IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular(true));
        monitor.beginTask(taskName, timedIpsObjects.size());
        monitor.setTaskName(taskName);

        for (ITimedIpsObject timedIpsObject : timedIpsObjects) {
            // Check for the user pressing the cancel button, discontinue if so
            if (monitor.isCanceled()) {
                break;
            }

            // Check whether such a generation already exists for this object, skip if desired
            boolean generationExists = timedIpsObject.getGenerationByEffectiveDate(pmo.getValidFrom()) != null;
            if (pmo.isSkipExistingGenerations() && generationExists) {
                monitor.worked(1);
                continue;
            }

            // Create new generation, only save if wasn't dirty before
            boolean wasDirty = timedIpsObject.getIpsSrcFile().isDirty();
            timedIpsObject.newGeneration(pmo.getValidFrom());
            if (!wasDirty) {
                timedIpsObject.getIpsSrcFile().save(monitor);
            }

            monitor.worked(1);
        }

        monitor.done();
    }

}
