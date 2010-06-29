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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

public class StructurePage extends IpsObjectEditorPage {

    final static String PAGE_ID = "Structure"; //$NON-NLS-1$

    public StructurePage(TableStructureEditor editor) {
        super(editor, PAGE_ID, Messages.StructurePage_title);
    }

    TableStructureEditor getTableEditor() {
        return (TableStructureEditor)getEditor();
    }

    ITableStructure getTableStructure() {
        return getTableEditor().getTableStructure();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        new GeneralInfoSection(getTableStructure(), formBody, toolkit);
        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new ColumnsSection(getTableStructure(), members, toolkit);
        new UniqueKeysSection(getTableStructure(), members, toolkit);
        new RangesSection(getTableStructure(), members, toolkit);
        new ForeignKeysSection(getTableStructure(), members, toolkit);
    }

}
