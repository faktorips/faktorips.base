/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.projectproperties.sections.DefinedDatatypesSection;
import org.faktorips.devtools.core.ui.editors.projectproperties.sections.PredefinedDatatypesSection;

public class DatatypesPropertiesPage extends ProjectPropertyPage {
    final static String PAGEID = "Datatypes";

    public DatatypesPropertiesPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        // TODO Auto-generated constructor stub
    }

    public DatatypesPropertiesPage(ProjectPropertyEditor projectPropertyEditor) {
        super(projectPropertyEditor, PAGEID, Messages.DatatypePropertiesPage_description);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new PredefinedDatatypesSection(getIIpsProjectProperties(), members, toolkit);
        new DefinedDatatypesSection(getIIpsProjectProperties(), members, toolkit);
    }

    @Override
    protected String getPageName() {
        return Messages.DatatypePropertiesPage_description;
    }

}
