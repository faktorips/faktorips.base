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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;


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
		IpsSection general = new GeneralInfoSection(getPolicyCmptType(), formBody, toolkit); 
		
		Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
		IpsSection attributes = new AttributesSection(getPolicyCmptType(), members, toolkit);
        
        Composite rightComp = createGridComposite(toolkit, members, 1, true, GridData.FILL_BOTH);
		IpsSection relations = new RelationsSection(getPolicyCmptType(), rightComp, toolkit);
        IPolicyCmptType policyCmptType = getPolicyCmptType();
        IProductCmptType productCmptType;
        try {
            productCmptType = policyCmptType.findProductCmptType();
            IpsSection tblStructure = new TblStructureUsageSection(policyCmptType, productCmptType, rightComp, toolkit);
            
            general.setFocusSuccessor(attributes);
            attributes.setFocusSuccessor(relations);
            relations.setFocusSuccessor(tblStructure);            
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
	}
}
