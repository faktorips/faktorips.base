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

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * The <code>TypeEditorStructurePage</code> provides an abstract base class for editor pages that
 * want to make it possible to edit ips object types.
 * 
 * @author Jan Ortmann
 */
public abstract class TypeEditorStructurePage extends IpsObjectEditorPage {

    final static String PAGEID = "Structure"; //$NON-NLS-1$

    /*
     * TODO AW: needs to be renamed because the number of sections will be defined by subclasses.
     * The enum type editor for example has (apart from the general info section) two sections: the
     * attributes section and the values section. If in the ips preferences two sections is set,
     * then the values section will be shown on a second tab. So the enum type editor would have
     * oneSectionWhenTrueOtherwiseTwo. This is a thing that needs also be changed in the ips
     * preferences to be a more general setting. FS#1379
     */
    private boolean twoSectionsWhenTrueOtherwiseFour;

    /**
     * Creates a new <code>TypeEditorStructurePage</code>.
     * 
     * @param editor The <code>TypeEditor</code> this page belongs to.
     * @param twoSectionsWhenTrueOtherwiseFour
     * @param title The title shown at the top of the page when the page is selected.
     */
    public TypeEditorStructurePage(TypeEditor editor, boolean twoSectionsWhenTrueOtherwiseFour, String title) {
        super(editor, PAGEID, title);
        this.twoSectionsWhenTrueOtherwiseFour = twoSectionsWhenTrueOtherwiseFour;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));

        createGeneralPageInfoSection(formBody, toolkit);

        if (twoSectionsWhenTrueOtherwiseFour) {
            createContentForSplittedStructurePage(formBody, toolkit);
        } else {
            createContentForSingleStructurePage(formBody, toolkit);
        }
    }

    /**
     * The creation of the general page information section which is displayed at the top below the
     * title of the page has to be implemented here.
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
