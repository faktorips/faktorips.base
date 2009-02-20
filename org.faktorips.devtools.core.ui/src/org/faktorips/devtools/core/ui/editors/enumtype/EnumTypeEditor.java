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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;

/**
 * The Faktor-IPS editor to edit <code>IEnumType</code> objects with.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeEditor extends TypeEditor {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addAllInOneSinglePage() throws PartInitException {
        addPage(new EnumTypeStructurePage(this, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new EnumTypeStructurePage(this, true));
        addPage(new EnumTypeValuesPage(this, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUniformPageTitle() {
        return Messages.EnumTypeEditor_title + getIpsObject().getName();
    }

    /**
     * Returns the enum type this editor is currently editing.
     * 
     * @return A reference to the enum type that is currently being edited by this editor.
     */
    IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

}
