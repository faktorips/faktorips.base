/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeBehaviourPage extends ProductCmptTypeEditorPage {

    public ProductCmptTypeBehaviourPage(ProductCmptTypeEditor editor) {
        super(editor, true, Messages.BehaviourPage_title_behaviour, "PolicyCmptTypeBehaviourPage"); //$NON-NLS-1$
        setNumberlayoutColumns(2);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new MethodsAndFormulaSection(getProductCmptType(), parentContainer, toolkit);
        new TableStructureUsageSection(getProductCmptType(), parentContainer, toolkit);

    }

    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new MethodsAndFormulaSection(getProductCmptType(), parentContainer, toolkit);
        new TableStructureUsageSection(getProductCmptType(), parentContainer, toolkit);
    }

    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        // Nothing to do.
    }

}
