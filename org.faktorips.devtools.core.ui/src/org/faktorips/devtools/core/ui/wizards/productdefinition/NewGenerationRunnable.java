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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

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
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        monitor.beginTask(Messages.NewGenerationRunnable_taskName, timedIpsObjects.size());

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
                try {
                    timedIpsObject.getIpsSrcFile().save(true, monitor);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }

            monitor.worked(1);
        }

        monitor.done();
    }

}