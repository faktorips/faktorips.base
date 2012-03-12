/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * Provides common behavior for all type editors.
 * 
 * @author Peter Erzberger
 */
public abstract class TypeEditor extends IpsObjectEditor {

    @Override
    protected final void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        String sections = IpsPlugin.getDefault().getIpsPreferences().getSectionsInTypeEditors();
        if (IpsPreferences.FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE.equals(sections)) {
            addAllInOneSinglePage();
        } else if (IpsPreferences.TWO_SECTIONS_IN_TYPE_EDITOR_PAGE.equals(sections)) {
            addSplittedInMorePages();
        }
    }

    /**
     * This method is called when according to the preference settings all sections are to display
     * on one page.
     */
    // The method name is wrong: only structure and behaviour page are meant
    // Please fix if you add another page to the type editor
    protected abstract void addAllInOneSinglePage() throws PartInitException;

    /**
     * This method is called when according to the preference settings the sections are to display
     * on two pages.
     */
    // The method name is wrong: only structure and behaviour page are meant
    // Please fix if you add another page to the type editor
    protected abstract void addSplittedInMorePages() throws PartInitException;

}
