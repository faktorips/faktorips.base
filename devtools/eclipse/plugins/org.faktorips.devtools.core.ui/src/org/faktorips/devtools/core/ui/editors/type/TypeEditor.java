/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.abstraction.exception.IpsException;
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
    protected final void addPagesForParsableSrcFile() throws PartInitException, IpsException {
        String sections = IpsPlugin.getDefault().getIpsPreferences().getSectionsInTypeEditors();
        switch (sections) {
            case IpsPreferences.FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE -> addAllInOneSinglePage();
            case IpsPreferences.TWO_SECTIONS_IN_TYPE_EDITOR_PAGE -> addSplittedInMorePages();
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
