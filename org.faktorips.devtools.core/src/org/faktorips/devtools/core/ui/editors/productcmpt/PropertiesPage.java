package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ICellModifier;
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
		new FormulasSection(generation, left, toolkit);
		
		Composite right = createGridComposite(toolkit, formBody, 1, true, GridData.FILL_BOTH);
		new PolicyAttributesSection(generation, right, toolkit);

		Section relationsSection = toolkit.getFormToolkit().createSection(right, Section.TITLE_BAR | Section.DESCRIPTION);
	    relationsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    relationsSection.setText(Messages.PropertiesPage_relations);
	    
		String[] pcTypeRelations = getPcTypeRelations(generation);
		if (pcTypeRelations.length==0) {
		    Label label = toolkit.createLabel(relationsSection, Messages.PropertiesPage_noRelationsDefined);		    
		    relationsSection.setClient(label);
		} else {
//			Tree tree = new Tree(relationsSection, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//			tree.setHeaderVisible(true);
//
//			TreeColumn col = new TreeColumn(tree, SWT.LEAD);
//			col.setText("Beziehung");
//			col.setWidth(100);
//			col.setResizable(true);
//			col = new TreeColumn(tree, SWT.LEAD);
//			col.setText("min. Kard.");
//			col.setWidth(100);
//			col.setResizable(true);
//			col = new TreeColumn(tree, SWT.LEAD);
//			col.setText("max. Kard.");
//			col.setWidth(100);
//			col.setResizable(true);
//			
//			CellEditor[] editors = new CellEditor[3];
//			editors[0] = null;
//			editors[1] = new TextCellEditor(tree);
//			editors[2] = new TextCellEditor(tree);
//			
//			TreeViewer treeViewer = new TreeViewer(tree);
//			treeViewer.setContentProvider(new RelationsContentProvider());
//			treeViewer.setLabelProvider(new RelationsLabelProvider());
//			treeViewer.setInput(generation);
//			relationsSection.setClient(tree);
//
//			treeViewer.setColumnProperties(new String[] {"0", "1", "2"});
//			treeViewer.setCellEditors(editors);
//			treeViewer.setCellModifier(new RelationTreeModifier());
			
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
    
    
    private class RelationTreeModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			int colCount = Integer.parseInt(property);
			return colCount > 0;
		}

		public Object getValue(Object element, String property) {
			int colCount = Integer.parseInt(property);
			return "" + colCount; //$NON-NLS-1$
		}

		public void modify(Object element, String property, Object value) {
			// TODO Auto-generated method stub
			
		}
    	
    }

}
