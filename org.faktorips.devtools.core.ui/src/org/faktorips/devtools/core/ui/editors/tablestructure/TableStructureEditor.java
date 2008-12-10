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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;


/**
 * Editor to edit table structures.
 */
public class TableStructureEditor extends IpsObjectEditor {

    /**
     * 
     */
    public TableStructureEditor() {
        super();
    }
    
    protected ITableStructure getTableStructure() {
        return (ITableStructure)getIpsObject();
    }

    /** 
     * {@inheritDoc}
     */
    protected void addPagesForParsableSrcFile() throws PartInitException {
        addPage(new StructurePage(this));
        addPage(new DescriptionPage(this));
    }

    /** 
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
        return Messages.TableStructureEditor_title + getIpsObject().getName();
    }

}

