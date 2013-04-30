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

package org.faktorips.devtools.formulalibrary.ui.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;

/**
 * The Faktor-IPS editor to edit <tt>IFormulaLibrary</tt> objects with.
 * 
 */
public class FormulaLibraryContentEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    @Override
    protected void addPagesForParsableSrcFile() throws CoreException {
        addPage(new FormulaLibraryEditorPage(this));
    }

    @Override
    protected String getUniformPageTitle() {
        return Messages.FormulaLibraryEditor_title + getIpsObject().getName();
    }

    /** Returns the <tt>IEnumContent</tt> this editor is currently editing. */
    protected IFormulaLibrary getFormulaLibrary() {
        return (IFormulaLibrary)getIpsObject();
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        return null;
    }

}
