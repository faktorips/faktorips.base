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

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
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

        if (hasDescriptionPage()) {
            addPage(new DescriptionPage(this));
        }
    }

    /**
     * This method is called when according to the preference settings all sections are to display
     * on one page.
     */
    protected abstract void addAllInOneSinglePage() throws PartInitException;

    /**
     * This method is called when according to the preference settings the sections are to display
     * on two pages.
     */
    protected abstract void addSplittedInMorePages() throws PartInitException;

    /**
     * Returns whether this editor should have a description page. May be overridden by subclasses.
     * <p>
     * Returns <tt>true</tt> by default.
     */
    protected boolean hasDescriptionPage() {
        return true;
    }

}
