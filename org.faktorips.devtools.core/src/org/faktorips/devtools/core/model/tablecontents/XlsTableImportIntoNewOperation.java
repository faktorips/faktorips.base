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

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Thorsten Waertel
 */
public class XlsTableImportIntoNewOperation extends AbstractXlsTableImportOperation {
    
    private IIpsPackageFragment pack;
    private String tableStructureName;
    private String tableContentsName;
    
	/**
	 * Constructor for Import
	 */
	public XlsTableImportIntoNewOperation(String filename, IIpsPackageFragment pack, String tableStructureName, String tableContentsName) {
		super(filename);
        this.pack = pack;
        this.tableStructureName = tableStructureName;
        this.tableContentsName = tableContentsName;
	}

    protected ITableContentsGeneration getImportGeneration(short numberOfCols, IProgressMonitor monitor) throws CoreException {
        ITableContents contents = (ITableContents) pack.createIpsFile(IpsObjectType.TABLE_CONTENTS, StringUtil.unqualifiedName(tableContentsName), true, monitor).getIpsObject();
        contents.setTableStructure(tableStructureName);
        while (contents.getNumOfColumns() < numberOfCols) {
            contents.newColumn(null);
        }
        return (ITableContentsGeneration) contents.newGeneration(new GregorianCalendar());
    }
}
