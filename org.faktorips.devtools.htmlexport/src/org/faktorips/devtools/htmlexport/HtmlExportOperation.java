package org.faktorips.devtools.htmlexport;

import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
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
     * Instantiates a Documentor. Throws an {@link IllegalArgumentException}, if config is null.
     * 
     * @param config
     * @throws IllegalArgumentException
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
     * 
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

        monitor.beginTask("HTML EXPORT PROGRESS MONITOR", scripts.size());

        for (IDocumentorScript documentorScript : scripts) {
            IProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 1);
            documentorScript.execute(getDocumentorConfiguration(), subProgressMonitor);
        }
        monitor.done();
    }
}
