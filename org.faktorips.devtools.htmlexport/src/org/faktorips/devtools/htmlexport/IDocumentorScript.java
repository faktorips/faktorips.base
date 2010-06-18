package org.faktorips.devtools.htmlexport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

/**
 * A Script for Documenting
 * 
 * @author dicker
 * 
 */
public interface IDocumentorScript {
    /**
     * executes the script using the {@link DocumentorConfiguration} <br/>
     * 
     * should only called by the {@link HtmlExportOperation}
     * 
     * @param config
     * @param monitor ProgressMonitor
     */
    public void execute(DocumentorConfiguration config, IProgressMonitor monitor);
}
