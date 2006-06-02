/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;

/**
 * 
 * @author Thorsten Waertel
 */
public class XlsTableImportIntoExistingOperation extends AbstractXlsTableImportOperation {
    
    private boolean append;
    private String tableContentsName;
    private IIpsProject project;
    
	/**
	 * Constructor for Import
	 */
	public XlsTableImportIntoExistingOperation(String filename, boolean append,
            IIpsProject project, String tableContentsName) {
		super(filename);
        this.append = append;
        this.project = project;
        this.tableContentsName = tableContentsName;
	}

    protected ITableContentsGeneration getImportGeneration(short numberOfCols, IProgressMonitor monitor) throws CoreException {
        ITableContents contents = (ITableContents) project.findIpsObject(IpsObjectType.TABLE_CONTENTS, tableContentsName);
        while (contents.getNumOfColumns() < numberOfCols) {
            contents.newColumn(null);
        }
        ITableContentsGeneration generation = (ITableContentsGeneration) contents.getGenerations()[0];
        if (!append) {
            generation.clear();
        }
        return generation;
    }
}
