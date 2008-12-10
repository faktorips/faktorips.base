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

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * Provides common behaviour for the subclass type editors for policy and product component type.
 * 
 * @author Peter Erzberger
 */
public abstract class TypeEditor extends IpsObjectEditor {

    /**
     * {@inheritDoc}
     */
    protected final void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        String sections = IpsPlugin.getDefault().getIpsPreferences().getSectionsInTypeEditors();
        if(IpsPreferences.FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE.equals(sections)){
            addAllInOneSinglePage();
        }
        if(IpsPreferences.TWO_SECTIONS_IN_TYPE_EDITOR_PAGE.equals(sections)){
            addSplittedInMorePages();
        }
        addPage(new DescriptionPage(this));
    }
    
    /**
     * This method is called when according to the preference settings all sections are to display on one page 
     */
    protected abstract void addAllInOneSinglePage() throws PartInitException;

    /**
     * This method is called when according to the preference settings the sections are to display on two pages 
     */
    protected abstract void addSplittedInMorePages() throws PartInitException;
}
