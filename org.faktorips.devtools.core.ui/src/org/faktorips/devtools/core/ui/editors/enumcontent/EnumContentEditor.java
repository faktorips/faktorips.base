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

import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;

/**
 * The Faktor-IPS editor to edit <code>IEnumContent</code> objects with.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContentEditor extends TypeEditor {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addAllInOneSinglePage() throws PartInitException {
        addPage(new EnumContentPage(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new EnumContentPage(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUniformPageTitle() {
        return Messages.EnumContentEditor_title + getIpsObject().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDescriptionPage() {
        return false;
    }

    /**
     * Returns the enum content this editor is currently editing.
     */
    IEnumContent getEnumContent() {
        return (IEnumContent)getIpsObject();
    }

}
