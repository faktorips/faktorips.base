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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;

/**
 * The <code>EnumTypeValuesPage</code> provides controls to edit the values of an
 * <code>IEnumType</code> and is intended to be used with the <code>EnumTypeEditor</code>.
 * 
 * @see EnumTypeEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeValuesPage extends EnumTypeEditorPage {

    /**
     * Creates a new <code>EnumContentValuesPage</code>.
     * 
     * @param editor The <code>EnumTypeEditor</code> this page belongs to.
     * @param splittedStructure If this flag is set to <code>true</code> the enum values table won't
     *            be part of the page.
     */
    public EnumTypeValuesPage(EnumTypeEditor editor, boolean splittedStructure) {
        super(editor, editor.getEnumType(), splittedStructure, Messages.EnumTypeValuesPage_title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        try {
            createToolbarActions();
            enumValuesSection = new EnumValuesSection(enumType, parentContainer, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        try {
            createToolbarActions();
            enumValuesSection = new EnumValuesSection(enumType, parentContainer, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        // Nothing to do.
    }
}
