/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

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
