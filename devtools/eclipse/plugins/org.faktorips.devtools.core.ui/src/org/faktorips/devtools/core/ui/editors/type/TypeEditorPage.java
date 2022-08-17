/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * The <code>TypeEditorPage</code> provides an abstract base class for editor pages that want to
 * make it possible to edit IPS object types.
 * 
 * @author Jan Ortmann
 */
public abstract class TypeEditorPage extends IpsObjectEditorPage {

    /*
     * TODO AW: Needs to be renamed because the number of sections will be defined by subclasses.
     * The enumeration type editor for example has (apart from the general info section) two
     * sections: the attributes section and the values section. If in the IPS preferences two
     * sections is set, then the values section will be shown on a second tab. So the enumeration
     * type editor would have oneSectionWhenTrueOtherwiseTwo. This is a thing that needs also be
     * changed in the IPS preferences to be a more general setting. FS#1379
     */
    private boolean twoSectionsWhenTrueOtherwiseFour;

    private int numberlayoutColumns;

    /**
     * Creates a new <code>TypeEditorPage</code>.
     * 
     * @param editor The <code>TypeEditor</code> this page belongs to.
     * @param twoSectionsWhenTrueOtherwiseFour 2 or 4 sections
     * @param title The title shown at the top of the page when the page is selected.
     * @param pageId A unique ID for the page.
     */
    public TypeEditorPage(TypeEditor editor, boolean twoSectionsWhenTrueOtherwiseFour, String title, String pageId) {
        super(editor, pageId, title);
        this.twoSectionsWhenTrueOtherwiseFour = twoSectionsWhenTrueOtherwiseFour;
        numberlayoutColumns = 1;
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        super.createPageContent(formBody, toolkit);
        formBody.setLayout(createPageLayout(numberlayoutColumns, false));
        createGeneralPageInfoSection(formBody, toolkit);
        if (twoSectionsWhenTrueOtherwiseFour) {
            createContentForSplittedStructurePage(formBody, toolkit);
        } else {
            createContentForSingleStructurePage(formBody, toolkit);
        }
    }

    protected final int getNumberlayoutColumns() {
        return numberlayoutColumns;
    }

    protected final void setNumberlayoutColumns(int numberlayoutColumns) {
        this.numberlayoutColumns = numberlayoutColumns;
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
    // The method name is wrong as all pages are derived from this class
    // Please fix if you code a major issue
    protected abstract void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit);

    /**
     * The creation of the sections of this page should go here, assuming that all sections that are
     * to display in the editor are distributed over two pages.
     */
    // The method name is wrong as all pages are derived from this class
    // Please fix if you code a major issue
    protected abstract void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit);

}
