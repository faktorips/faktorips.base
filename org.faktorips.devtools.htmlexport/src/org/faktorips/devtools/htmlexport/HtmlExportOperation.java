/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * Operation to run the HtmlExport
 * 
 * @author dicker
 * 
 */
public class HtmlExportOperation implements ICoreRunnable {

    private static final Set<Class<? extends NLS>> MESSAGE_CLAZZES = new HashSet<>();
    private DocumentationContext context;

    /**
     * Instantiates a HtmlExportOperation.
     * 
     * @throws IllegalArgumentException thrown if context is null.
     */
    public HtmlExportOperation(DocumentationContext context) {
        setDocumentationContext(context);
    }

    private void setDocumentationContext(DocumentationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("The DocumentationContext must not be null"); //$NON-NLS-1$
        }
        this.context = context;
    }

    /**
     * @return the {@link DocumentationContext}
     */
    public DocumentationContext getDocumentationContext() {
        return context;
    }

    /**
     * Takes all scripts from the context and and executes them with the context
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    public void run(IProgressMonitor monitor) {
        List<IDocumentorScript> scripts = getDocumentationContext().getScripts();

        int monitorScriptFaktor = 9;
        monitor.beginTask("Html Export", scripts.size() * monitorScriptFaktor + 1); //$NON-NLS-1$

        for (IDocumentorScript documentorScript : scripts) {
            IProgressMonitor subProgressMonitor = new org.eclipse.core.runtime.SubProgressMonitor(monitor,
                    monitorScriptFaktor);
            documentorScript.execute(getDocumentationContext(), subProgressMonitor);
        }

        refreshIfNecessary(new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));

        monitor.done();
    }

    private void refreshIfNecessary(IProgressMonitor monitor) {
        if (context.getPath() == null) {
            monitor.done();
            return;
        }

        IPath exportPath = new Path(context.getPath());

        IContainer containerExportPath = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(exportPath);
        if (containerExportPath == null || containerExportPath.getType() == IResource.ROOT) {
            monitor.done();
            return;
        }

        try {
            containerExportPath.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        } catch (CoreException e) {
            context.addStatus(new IpsStatus(IStatus.WARNING, "Could not refresh after Html Export", e)); //$NON-NLS-1$
        }
    }

    public static void registerMessageClazz(Class<? extends NLS> clazz) {
        MESSAGE_CLAZZES.add(clazz);
    }
}
