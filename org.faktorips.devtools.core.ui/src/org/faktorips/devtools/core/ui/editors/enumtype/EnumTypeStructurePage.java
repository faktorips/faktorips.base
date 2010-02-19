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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;

/**
 * The <tt>EnumTypeStructurePage</tt> provides controls to edit the properties and the attributes of
 * an <tt>IEnumType</tt> and is intended to be used with the <tt>EnumTypeEditor</tt>.
 * 
 * @see EnumTypeEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeStructurePage extends EnumTypeEditorPage {

    /**
     * Creates a new <tt>EnumTypeStructurePage</tt>.
     * 
     * @param editor The <tt>EnumTypeEditor</tt> this page belongs to.
     * @param splittedStructure If this flag is set to <tt>true</tt> the enumeration values table
     *            won't be part of the page.
     */
    public EnumTypeStructurePage(EnumTypeEditor editor, boolean splittedStructure) {
        super(editor, editor.getEnumType(), splittedStructure, Messages.EnumTypeStructurePage_title);

        if (!splittedStructure) {
            setPartName(Messages.EnumTypeStructurePage_title + ' ' + Messages.EnumTypeStructurePage_andLiteral + ' '
                    + Messages.EnumTypeValuesPage_title);
        }
    }

    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        Composite members = createGridComposite(toolkit, parentContainer, 1, true, GridData.FILL_HORIZONTAL);
        enumAttributesSection = new EnumAttributesSection(enumType, members, toolkit);
        try {
            enumValuesSection = new EnumValuesSection(enumType, parentContainer, toolkit);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        enumAttributesSection = new EnumAttributesSection(enumType, parentContainer, toolkit);
    }

    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        new EnumTypeGeneralInfoSection(enumType, parentContainer, toolkit);
        createToolbarActions();
    }

}
