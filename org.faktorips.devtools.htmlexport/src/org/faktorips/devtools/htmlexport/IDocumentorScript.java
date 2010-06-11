package org.faktorips.devtools.htmlexport;

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
     * should only called by the {@link Documentor}
     * 
     * @param config
     */
    public void execute(DocumentorConfiguration config);
}
