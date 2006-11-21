/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;


/**
 *
 */
public class NewTableContentsWizard extends NewIpsObjectWizard {
    
    private TableContentsPage page;
    
    public NewTableContentsWizard() {
        super(IpsObjectType.TABLE_CONTENTS);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewTableContentsWizard.png"));
    }
    
    /** 
     * {@inheritDoc}
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        page = new TableContentsPage(selection);
        return page;
    }

    /** 
     * {@inheritDoc}
     */
    protected void createAdditionalPages() {
    }

    /** 
     * {@inheritDoc}
     */
    protected void finishIpsObject(IIpsObject pdObject) throws CoreException {
        ITableContents table = (ITableContents)pdObject;
        table.setTableStructure(page.getTableStructure());
        GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        if (date==null) {
            return;
        }
        IIpsObjectGeneration generation = table.newGeneration();
        generation.setValidFrom(date);
        ITableStructure structure = (ITableStructure)table.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, table.getTableStructure());
        if (structure!=null) {
            for (int i=0; i<structure.getNumOfColumns(); i++) {
                table.newColumn(""); //$NON-NLS-1$
            }
        }
    }

}
