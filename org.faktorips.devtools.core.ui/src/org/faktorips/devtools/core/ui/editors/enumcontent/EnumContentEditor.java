/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.EnumsDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * The Faktor-IPS editor to edit <code>IEnumContent</code> objects with.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    @Override
    public IPage createModelDescriptionPage() throws CoreRuntimeException {
        IEnumType enumType = getEnumContent().findEnumType(getIpsProject());
        if (enumType != null) {
            return new EnumsDescriptionPage(enumType);
        } else {
            return null;
        }
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        addPage(new EnumContentEditorPage(this));
    }

    @Override
    protected String getUniformPageTitle() {
        return Messages.EnumContentEditor_title + getIpsObject().getName();
    }

    /** Returns the <code>IEnumContent</code> this editor is currently editing. */
    IEnumContent getEnumContent() {
        return (IEnumContent)getIpsObject();
    }
}
