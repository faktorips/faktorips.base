/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.projectproperties.sections.ProductDefinitionComposite;
import org.faktorips.devtools.core.ui.editors.projectproperties.sections.ProductDefinitionSection;

public class ProductDefinitionPropertiesPage extends ProjectPropertyPage {
    final static String PAGEID = "ProductDefinitionProperties";

    public ProductDefinitionPropertiesPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    public ProductDefinitionPropertiesPage(ProjectPropertyEditor projectPropertyEditor) {
        super(projectPropertyEditor, PAGEID, Messages.ProductDefinitionPropertiesPage_description);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        new ProductDefinitionComposite(formBody, getIIpsProjectProperties(), toolkit);
        new ProductDefinitionSection(getIIpsProjectProperties(), formBody, toolkit);
    }

    @Override
    protected String getPageName() {
        return Messages.ProductDefinitionPropertiesPage_description;
    }
}
