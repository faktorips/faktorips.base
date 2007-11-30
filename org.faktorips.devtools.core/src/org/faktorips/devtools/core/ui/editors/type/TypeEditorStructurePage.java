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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class TypeEditorStructurePage extends IpsObjectEditorPage {

    final static String PAGEID = "Structure"; //$NON-NLS-1$
    private boolean twoSectionsWhenTrueOtherwiseFour;

    /**
     * @param editor
     * @param id
     * @param tabPageName
     */
    public TypeEditorStructurePage(TypeEditor editor, boolean twoSectionsWhenTrueOtherwiseFour, String title) {
        super(editor, PAGEID, title);
        this.twoSectionsWhenTrueOtherwiseFour = twoSectionsWhenTrueOtherwiseFour;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        createGeneralPageInfoSection(formBody, toolkit);
        if(twoSectionsWhenTrueOtherwiseFour){
            createContentForSplittedStructurePage(formBody, toolkit);
        }
        if(!twoSectionsWhenTrueOtherwiseFour){
            createContentForSingleStructurePage(formBody, toolkit);
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
