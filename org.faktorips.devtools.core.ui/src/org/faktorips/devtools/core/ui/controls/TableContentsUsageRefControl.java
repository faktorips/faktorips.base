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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Control enter a reference to a table contents. Candidates for the reference are defined by
 * a given table structure usage.
 * 
 * @see ITableStructureUsage
 * 
 * @author Thorsten Guenther
 */
public class TableContentsUsageRefControl extends TableContentsRefControl {

    private ITableStructureUsage structureUsage;
    
    /**
     * @param project
     * @param parent
     * @param toolkit
     */
    public TableContentsUsageRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, ITableStructureUsage structureUsage) {
        super(project, parent, toolkit);
        this.structureUsage = structureUsage;
    }

    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (structureUsage==null) {
            return new IIpsSrcFile[0];
        }
        IIpsSrcFile[] allTableContents = super.getIpsSrcFiles();
        if (allTableContents.length==0) {
            return allTableContents;
        }
        String[] structures = structureUsage.getTableStructures();
        List result = new ArrayList();
        // search in all found table contents if the table structure is
        // a configurated table structures in the table structure usage
        for (int i = 0; i < structures.length; i++) {
            for (int j = 0; j < allTableContents.length; j++) {
                String tableStructure = allTableContents[j].getPropertyValue(TableContents.PROPERTY_TABLE_STRUCTURE);
                if (tableStructure != null && tableStructure.equals(structures[i])) {
                    result.add(allTableContents[j]);
                }
            }
        }
        
        return (IIpsSrcFile[])result.toArray(new IIpsSrcFile[result.size()]);
    }
}
