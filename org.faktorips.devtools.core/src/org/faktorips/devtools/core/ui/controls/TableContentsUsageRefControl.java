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
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
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

    protected IIpsObject[] getIpsObjects() throws CoreException {
        if (structureUsage==null) {
            return new IIpsObject[0];
        }
        IIpsObject[] allFound = super.getIpsObjects();
        String[] structures = structureUsage.getTableStructures();
        List result = new ArrayList();
        
        for (int i = 0; i < structures.length; i++) {
            for (int j = 0; j < allFound.length; j++) {
                if (((ITableContents)allFound[j]).getTableStructure().equals(structures[i])) {
                    result.add(allFound[j]);
                }
            }
        }
        
        return (IIpsObject[])result.toArray(new IIpsObject[result.size()]);
    }

    
    
}
