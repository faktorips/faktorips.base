/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Control enter a reference to a table contents. Candidates for the reference are defined by a
 * given table structure usage.
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
    public TableContentsUsageRefControl(IIpsProject project, Composite parent, UIToolkit toolkit,
            ITableStructureUsage structureUsage) {
        super(project, parent, toolkit);
        this.structureUsage = structureUsage;
        setDialogFilterEnabled(false);
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (structureUsage == null) {
            return new IIpsSrcFile[0];
        }
        IIpsSrcFile[] allTableContents = super.getIpsSrcFiles();
        if (allTableContents.length == 0) {
            return allTableContents;
        }
        String[] structures = structureUsage.getTableStructures();
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        // search in all found table contents if the table structure is
        // a configurated table structures in the table structure usage
        for (String structure : structures) {
            for (IIpsSrcFile allTableContent : allTableContents) {
                String tableStructure = allTableContent.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
                if (tableStructure != null && tableStructure.equals(structure)) {
                    result.add(allTableContent);
                }
            }
        }

        return result.toArray(new IIpsSrcFile[result.size()]);
    }
}
