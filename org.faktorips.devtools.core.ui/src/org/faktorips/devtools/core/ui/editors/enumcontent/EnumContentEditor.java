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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
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

    public IPage createModelDescriptionPage() throws CoreException {
        return new EnumContentModelDescriptionPage(this);
    }

}
