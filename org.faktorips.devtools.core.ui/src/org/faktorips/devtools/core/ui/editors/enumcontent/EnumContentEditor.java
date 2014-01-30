/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.EnumsDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;

/**
 * The Faktor-IPS editor to edit <tt>IEnumContent</tt> objects with.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        addPage(new EnumContentEditorPage(this));
    }

    @Override
    protected String getUniformPageTitle() {
        return Messages.EnumContentEditor_title + getIpsObject().getName();
    }

    /** Returns the <tt>IEnumContent</tt> this editor is currently editing. */
    IEnumContent getEnumContent() {
        return (IEnumContent)getIpsObject();
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        IEnumType enumType = getEnumContent().findEnumType(getIpsProject());
        if (enumType != null) {
            return new EnumsDescriptionPage(enumType);
        } else {
            return null;
        }
    }

}
