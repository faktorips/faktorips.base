/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class TypeEditorStructurePage extends IpsObjectEditorPage {

    final static String PAGEID = "Structure"; //$NON-NLS-1$

    /**
     * @param editor
     * @param id
     * @param tabPageName
     */
    public TypeEditorStructurePage(TypeEditor editor, String title) {
        super(editor, PAGEID, title);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        createGeneralPageInfoSection(formBody, toolkit);
        String sections = IpsPlugin.getDefault().getIpsPreferences().getSectionsInTypeEditors();
        if(IpsPreferences.FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE.equals(sections)){
            createContentForSingleStructurePage(formBody, toolkit);
        }
        if(IpsPreferences.TWO_SECTIONS_IN_TYPE_EDITOR_PAGE.equals(sections)){
            createContentForSplittedStructurePage(formBody, toolkit);
        }
    }

    /**
     * The creation of the general page information section which is displayed at the top right below
     * the title of the page has to be implemented here.
     */
    protected abstract void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit);

    /**
     * The creation of the sections of this page should go here, assuming that all sections that are 
     * to display in the editor are on one page.
     */
    protected abstract void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit);

    /**
     * The creation of the sections of this page should go here, assuming that all sections that are 
     * to display in the editor are distributed over two pages.
     */
    protected abstract void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit);
}
