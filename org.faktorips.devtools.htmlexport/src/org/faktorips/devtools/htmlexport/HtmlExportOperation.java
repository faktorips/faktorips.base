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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

/**
 * The Documentor is the base for the export and should be used as base for the documentation.
 * 
 * @author dicker
 * 
 */
public class HtmlExportOperation implements IWorkspaceRunnable {
    private DocumentationContext context;
    private final static Set<Class<? extends NLS>> MESSAGE_CLAZZES = new HashSet<Class<? extends NLS>>();

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
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        List<IDocumentorScript> scripts = getDocumentationContext().getScripts();

        int monitorScriptFaktor = 9;
        monitor.beginTask("Html Export", scripts.size() * monitorScriptFaktor + 1); //$NON-NLS-1$

        for (IDocumentorScript documentorScript : scripts) {
            IProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, monitorScriptFaktor);
            documentorScript.execute(getDocumentationContext(), subProgressMonitor);
        }

        refreshIfNecessary(new SubProgressMonitor(monitor, 1));

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
