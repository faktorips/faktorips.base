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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.preferencepages.ArchiveComposite;

public class LibrariesPropertiesPage extends ProjectPropertyPage {
    final static String PAGEID = "Datatypes";
    private IIpsObjectPath ipsObjectPath;

    public LibrariesPropertiesPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        // TODO Auto-generated constructor stub
    }

    public LibrariesPropertiesPage(ProjectPropertyEditor projectPropertyEditor) throws CoreException {
        super(projectPropertyEditor, PAGEID, Messages.BuildPathPropertiesPage_archive);
        IIpsProject currentIpsProject = projectPropertyEditor.getIpsProject();
        ipsObjectPath = currentIpsProject.getIpsObjectPath();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        Composite members = createGridComposite(toolkit, formBody, 1, false, GridData.FILL_BOTH);
        ArchiveComposite archiveComposite = new ArchiveComposite(members);
        archiveComposite.init(ipsObjectPath);
        // new PredefinedDatatypesSection(getIIpsProjectProperties(), members, toolkit);

    }

    @Override
    protected String getPageName() {
        return Messages.BuildPathPropertiesPage_archive;
    }

}
