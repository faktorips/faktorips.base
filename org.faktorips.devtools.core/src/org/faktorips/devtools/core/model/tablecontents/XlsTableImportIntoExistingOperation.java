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
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

/**
 * 
 * @author Thorsten Waertel
 */
public class XlsTableImportIntoExistingOperation extends AbstractXlsTableImportOperation {
    
    private boolean append;
    private IIpsProject project;
    private ITableContents contents;
    
	/**
	 * Constructor for Import
	 * @throws CoreException 
	 */
	public XlsTableImportIntoExistingOperation(String filename, boolean append,
            IIpsProject project, String tableContentsName) throws CoreException {
		super(filename);
        this.append = append;
        this.project = project;
        contents = (ITableContents) project.findIpsObject(IpsObjectType.TABLE_CONTENTS, tableContentsName);
    }

    protected ITableContentsGeneration getImportGeneration(short numberOfCols, IProgressMonitor monitor) throws CoreException {
        while (contents.getNumOfColumns() < numberOfCols) {
            contents.newColumn(null);
        }
        ITableContentsGeneration generation = (ITableContentsGeneration) contents.getGenerations()[0];
        if (!append) {
            generation.clear();
        }
        return generation;
    }

    protected ITableStructure getStructure() throws CoreException {
		return (ITableStructure) project.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, contents.getTableStructure());
	}
}
