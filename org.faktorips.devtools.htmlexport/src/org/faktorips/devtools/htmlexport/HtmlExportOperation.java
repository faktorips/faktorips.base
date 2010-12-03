/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

/**
 * The Documentor is the base for the export and should be used as base for the documentation.
 * 
 * @author dicker
 * 
 */
public class HtmlExportOperation implements IWorkspaceRunnable {
    private DocumentorConfiguration config;

    /**
     * Instantiates a Documentor.
     * 
     * @throws IllegalArgumentException thrown if config is null.
     */
    public HtmlExportOperation(DocumentorConfiguration config) {
        setDocumentorConfiguration(config);
    }

    private void setDocumentorConfiguration(DocumentorConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("The DocumentorConfiguration must not be null"); //$NON-NLS-1$
        }
        this.config = config;
    }

    /**
     * @return the {@link DocumentorConfiguration}
     */
    public DocumentorConfiguration getDocumentorConfiguration() {
        return config;
    }

    /**
     * Takes all scripts from the config and and executes them with the configuration
     * 
     * {@inheritDoc}
     */
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        List<IDocumentorScript> scripts = getDocumentorConfiguration().getScripts();

        int monitorScriptFaktor = 9;
        monitor.beginTask("Html Export", scripts.size() * monitorScriptFaktor + 1); //$NON-NLS-1$

        for (IDocumentorScript documentorScript : scripts) {
            IProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, monitorScriptFaktor);
            documentorScript.execute(getDocumentorConfiguration(), subProgressMonitor);
        }

        refreshIfNecessary(new SubProgressMonitor(monitor, 1));

        monitor.done();
    }

    private void refreshIfNecessary(IProgressMonitor monitor) {
        IPath exportPath = new Path(config.getPath());

        IContainer containerExportPath = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(exportPath);
        if (containerExportPath == null || containerExportPath.getType() == IResource.ROOT) {
            monitor.done();
            return;
        }

        try {
            containerExportPath.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        } catch (CoreException e) {
            IpsPlugin.log(new IpsStatus(IStatus.WARNING, "Could not refresh after Html Export", e)); //$NON-NLS-1$
        }
    }
}
