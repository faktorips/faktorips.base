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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

/**
 * A Script for Documenting
 * 
 * @author dicker
 * 
 */
public interface IDocumentorScript {
    /**
     * executes the script using the given {@link DocumentationContext} and the ProgressMonitor<br>
     * 
     * should only called by the {@link HtmlExportOperation}
     * 
     */
    void execute(DocumentationContext context, IProgressMonitor monitor) throws IpsException;
}
