/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.EnumsDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.model.enums.IEnumType;

/**
 * The Faktor-IPS editor to edit <code>IEnumType</code> objects with.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumType
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

    /** Returns the <code>IEnumType</code> this editor is currently editing. */
    IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, IpsException {
        addPage(new EnumTypeEditorPage(this));
    }

    @Override
    public IPage createModelDescriptionPage() {
        return new EnumsDescriptionPage(getEnumType());
    }

}
