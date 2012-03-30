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
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;

// TODO AW 30-03-2012: Document this class!
class NewGenerationRunnable extends WorkspaceModifyOperation {

    private final NewGenerationPMO pmo;

    private final List<ITimedIpsObject> timedIpsObjects;

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
            if (monitor.isCanceled()) {
                break;
            }

            boolean generationExists = timedIpsObject.getGenerationByEffectiveDate(pmo.getValidFrom()) != null;
            if (pmo.isSkipExistingGenerations() && generationExists) {
                continue;
            }

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