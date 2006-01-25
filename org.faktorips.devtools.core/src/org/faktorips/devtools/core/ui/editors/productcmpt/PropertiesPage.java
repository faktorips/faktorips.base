package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


/**
 *
 */
public class PropertiesPage extends IpsObjectEditorPage {
    
    final static String PAGE_ID = "Properties"; //$NON-NLS-1$

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
		new ProductAttributesSection(generation, left, toolkit);
		new PolicyAttributesSection(generation, left, toolkit);
		new FormulasSection(generation, left, toolkit);
		
		Composite right = createGridComposite(toolkit, formBody, 1, true, GridData.FILL_BOTH);
	    Section relationsSection = toolkit.getFormToolkit().createSection(right, Section.TITLE_BAR | Section.DESCRIPTION);
	    relationsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    relationsSection.setText(Messages.PropertiesPage_relations);
	    
		String[] pcTypeRelations = getPcTypeRelations(generation);
		if (pcTypeRelations.length==0) {
		    Label label = toolkit.createLabel(relationsSection, Messages.PropertiesPage_noRelationsDefined);		    
		    relationsSection.setClient(label);
		} else {
			for (int i=0; i<pcTypeRelations.length; i++) {
			    IRelation relation = findPcTypeRelation((pcTypeRelations[i]));
			    if (relation!=null && relation.isProductRelevant()) {
				    new RelationsSection(getProductCmptEditor(), pcTypeRelations[i], right, toolkit);
			    }
			}
		}
    }
    
    private IRelation findPcTypeRelation(String pcTypeRelationName) {
        try {
            return getProductCmptEditor().getProductCmpt().findPcTypeRelation(pcTypeRelationName);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    private void createRelationSections(Composite parent) {
        
    }
    
    /**
     * Returns all PcType relations that are defined either in the generation
     * or in the PcType the generation is based on.
     */
    private String[] getPcTypeRelations(IProductCmptGeneration generation) {
        List result = new ArrayList();
        try {
            IPolicyCmptType pcType = generation.getProductCmpt().findPolicyCmptType();
            if (pcType!=null) {
                IRelation[] pcTypeRelations = pcType.getRelations();
                for (int i=0; i<pcTypeRelations.length; i++) {
                    result.add(pcTypeRelations[i].getName());
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
		IProductCmptRelation[] relations = generation.getRelations();
        for (int i=0; i<relations.length; i++) {
            if (!result.contains(relations[i].getPcTypeRelation())) {
                result.add(relations[i].getPcTypeRelation());
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#refresh()
     */
    protected void refresh() {
        super.refresh();
    }
    
    

}
