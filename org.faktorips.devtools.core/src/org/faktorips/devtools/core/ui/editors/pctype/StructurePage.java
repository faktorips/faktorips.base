/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * The structure page contain the general information section, the attributes section
 * and the relations section.
 */
public class StructurePage extends PctEditorPage {
    
    final static String PAGEID = "Structure"; //$NON-NLS-1$

    public StructurePage(PctEditor editor) {
        super(editor, PAGEID, Messages.StructurePage_title);
    }
    
	protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(1, false));
		new GeneralInfoSection(getPolicyCmptType(), formBody, toolkit); 
		
		Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
		new AttributesSection(getPolicyCmptType(), members, toolkit);
		new RelationsSection(getPolicyCmptType(), members, toolkit);
	}

}
