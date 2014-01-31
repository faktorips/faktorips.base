/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.EnumsDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;

/**
 * The Faktor-IPS editor to edit <tt>IEnumType</tt> objects with.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    @Override
    protected String getUniformPageTitle() {
        return Messages.EnumTypeEditor_title + getIpsObject().getName();
    }

    /** Returns the <tt>IEnumType</tt> this editor is currently editing. */
    IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        addPage(new EnumTypeEditorPage(this));
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        return new EnumsDescriptionPage(getEnumType());
    }

}
