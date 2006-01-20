package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.pctype.RelationLabelProvider;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;


/**
 * A section to display a product component's relations that belong to the
 * same policy component type relation. 
 */
public class RelationsSection extends IpsSection {
    
    // the name of the policy component type relation
    private String pcTypeRelationName;
    
    // the editor this section is part of
    private ProductCmptEditor editor;
    
    // the composite showing the relations
    private RelationsComposite composite;

    public RelationsSection(
            ProductCmptEditor editor,
            String pcTypeRelationName, 
            Composite parent, 
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(editor);
        ArgumentCheck.notNull(pcTypeRelationName);
        this.editor = editor;
        this.pcTypeRelationName = pcTypeRelationName;
        initControls();
        IRelation pcTypeRelation = getPcTypeRelation();
        if (pcTypeRelation!=null) {
            setText(new RelationLabelProvider().getText(pcTypeRelation));
            if (pcTypeRelation.getMaxCardinality().equals("1")) {
                setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }
        } else {
            setText(pcTypeRelationName);    
        }
        
    }
    
    private IRelation getPcTypeRelation() {
        try {
            return editor.getProductCmpt().findPcTypeRelation(pcTypeRelationName);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }
    
    /*
     * fuer drop target
     */
    private void newRelation(String target) {
    	IProductCmptRelation relation = ((IProductCmptGeneration)editor.getProductCmpt().getGenerations()[0]).newRelation(this.pcTypeRelationName);
    	relation.setTarget(target);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#initClientComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        composite = new RelationsComposite(client, toolkit);
        DropTarget target = new DropTarget(composite, DND.DROP_LINK);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropListener());
        
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#performRefresh()
     */
    protected void performRefresh() {
        composite.refresh();
    }

    private class RelationsComposite extends IpsPartsComposite {

        public RelationsComposite(Composite parent, UIToolkit toolkit) {
            super(editor.getIpsObject(), parent, toolkit);
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createContentProvider()
         */
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createLabelProvider()
         */
        protected ILabelProvider createLabelProvider() {
            return new LabelProvider();
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#newIpsPart()
         */
        protected IIpsObjectPart newIpsPart() {
            IProductCmptGeneration generation = (IProductCmptGeneration)editor.getActiveGeneration();
            return generation.newRelation(pcTypeRelationName);
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createEditDialog(org.faktorips.devtools.core.model.IIpsObjectPart, org.eclipse.swt.widgets.Shell)
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new RelationEditDialog((IProductCmptRelation)part, getShell());
        }
        
        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.ViewerButtonComposite#updateButtonEnabledStates()
         */
        protected void updateButtonEnabledStates() {
            super.updateButtonEnabledStates();
            if (getPcTypeRelation()==null) {
                newButton.setEnabled(false);
                return;
            }
        }
        
        class LabelProvider extends DefaultLabelProvider {

            public String getText(Object element) {
	            IProductCmptRelation relation = (IProductCmptRelation)element;
	            return relation.getName() 
	            + " [" + relation.getMinCardinality() +
	            	".." + relation.getMaxCardinality() + "]";
            }
        }
        
        class ContentProvider implements IStructuredContentProvider {

            /** 
             * Overridden method.
             * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
             */
            public Object[] getElements(Object inputElement) {
                IProductCmptGeneration generation = (IProductCmptGeneration)editor.getActiveGeneration();
                return generation.getRelations(pcTypeRelationName);
            }

            /** 
             * Overridden method.
             * @see org.eclipse.jface.viewers.IContentProvider#dispose()
             */
            public void dispose() {
            }

            /** 
             * Overridden method.
             * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
             */
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        }
        
    } // class RelationsComposite

    private class DropListener implements DropTargetListener {

		public void dragEnter(DropTargetEvent event) {
			event.detail = DND.DROP_LINK;
		}

		public void dragLeave(DropTargetEvent event) {
			// nothing to do
		}

		public void dragOperationChanged(DropTargetEvent event) {
			// nothing to do
		}

		public void dragOver(DropTargetEvent event) {
			// nothing to do
		}

		public void drop(DropTargetEvent event) {
			newRelation((String)event.data);
		}

		public void dropAccept(DropTargetEvent event) {
			event.detail = DND.DROP_LINK;
			
		}
		
    }
}

