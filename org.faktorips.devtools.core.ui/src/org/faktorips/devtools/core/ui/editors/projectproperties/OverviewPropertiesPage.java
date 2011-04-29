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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.projectproperties.sections.GeneratorSection;
import org.faktorips.devtools.core.ui.editors.projectproperties.sections.OverviewSection;

public class OverviewPropertiesPage extends ProjectPropertyPage {
    final static String PAGEID = "Overview";

    public OverviewPropertiesPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        // TODO Auto-generated constructor stub
    }

    public OverviewPropertiesPage(ProjectPropertyEditor projectPropertyEditor) {
        super(projectPropertyEditor, PAGEID, Messages.OverviewPropertiesPage_description);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, true));
        Composite members = createGridComposite(toolkit, formBody, 1, true, GridData.FILL_BOTH);
        new OverviewSection(getIIpsProjectProperties(), members, toolkit);
        new GeneratorSection(getProjectPropertyEditor().getIpsProject(), getIIpsProjectProperties()
                .getBuilderSetConfig(), getIIpsProjectProperties(), members, toolkit);
    }

    @Override
    protected String getPageName() {
        return Messages.OverviewPropertiesPage_description;
    }
}
