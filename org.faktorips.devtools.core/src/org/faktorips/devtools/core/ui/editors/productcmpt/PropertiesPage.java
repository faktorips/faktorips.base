package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


/**
 *
 */
public class PropertiesPage extends IpsObjectEditorPage {
    
    final static String PAGE_ID = "Properties"; //$NON-NLS-1$

    private ProductAttributesSection productAttributesSection;
	private FormulasSection formulasSection;
	private PolicyAttributesSection policyAttributesSection;
	private RelationsSection relationsSection;
    
    public PropertiesPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, Messages.PropertiesPage_properties);
    }

    ProductCmptEditor getProductCmptEditor() {
        return (ProductCmptEditor)getEditor();
    }
    
    IProductCmpt getProductCmpt() {
        return getProductCmptEditor().getProductCmpt(); 
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#createPageContent(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		GridLayout layout = new GridLayout(2, true);
		layout.verticalSpacing = VERTICAL_SECTION_SPACE;
		layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;
		formBody.setLayout(layout);
		
		IProductCmptGeneration generation = (IProductCmptGeneration)getProductCmptEditor().getActiveGeneration();
		Composite left = createGridComposite(toolkit, formBody, 1, true, GridData.FILL_BOTH);
		productAttributesSection = new ProductAttributesSection(generation, left, toolkit);
		formulasSection = new FormulasSection(generation, left, toolkit);
		
		Composite right = createGridComposite(toolkit, formBody, 1, true, GridData.FILL_BOTH);
		policyAttributesSection = new PolicyAttributesSection(generation, right, toolkit);
		relationsSection = new RelationsSection(generation, right, toolkit, getEditorSite());
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#refresh()
     */
    protected void refresh() {
        super.refresh();
    }
    
    protected void setActiveGeneration(IProductCmptGeneration generation) {
		productAttributesSection.setActiveGeneration(generation);
		formulasSection.setActiveGeneration(generation);
		policyAttributesSection.setActiveGeneration(generation);
		relationsSection.setActiveGeneration(generation);
    }
    
    protected void setEnabled(boolean enabled) {
		productAttributesSection.setEnabled(enabled);
		formulasSection.setEnabled(enabled);
		policyAttributesSection.setEnabled(enabled);
		relationsSection.setEnabled(enabled);
    }
}
