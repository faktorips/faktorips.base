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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;

/**
 * A Script for Documenting
 * 
 * @author dicker
 * 
 */
public interface IDocumentorScript {
    /**
     * executes the script using the given {@link DocumentationContext} and the ProgressMonitor<br/>
     * 
     * should only called by the {@link HtmlExportOperation}
     * 
     */
    public void execute(DocumentationContext context, IProgressMonitor monitor) throws CoreException;
}
