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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * 
 * @author Jan Ortmann
 */
public class StructurePage extends IpsObjectEditorPage {

    final static String PAGEID = "Structure"; //$NON-NLS-1$

    /**
     * @param editor
     * @param id
     * @param tabPageName
     */
    public StructurePage(ProductCmptTypeEditor editor) {
        super(editor, PAGEID, "Structure Page");
    }
    
    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        new GeneralInfoSection(getProductCmptType(), formBody, toolkit); 
        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new AttributesSection(getProductCmptType(), members, toolkit);
        new AssociationsSection(getProductCmptType(), members, toolkit);
        new MethodsAndFormulaSection(getProductCmptType(), members, toolkit);
        new TableStructureUsageSection(getProductCmptType(), members, toolkit);
    }

}
