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
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;

/**
 * The Faktor-IPS editor to edit <code>IEnumType</code> objects with.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
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
        addPage(new EnumTypeStructurePage(this, false));
        // TODO AW: out commented 2nd page for release 2.3.0rfinal, see FS#1379
        // addPage(new EnumTypeValuesPage(this, true));
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
     */
    IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

}
