package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section to display and edit a type's relations.
 */
public class RelationsSection extends SimpleIpsPartsSection {

    public RelationsSection(
            IPolicyCmptType pcType, 
            Composite parent,
            UIToolkit toolkit) {
        super(pcType, parent, Messages.RelationsSection_title, toolkit);
    }
    
    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }
    
    /** 
     * Overridden.
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RelationsComposite((IPolicyCmptType)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows a policy component's relations in a viewer and 
     * allows to edit relations in a dialog, create new relations and delete relations.
     */
    private class RelationsComposite extends IpsPartsComposite {

        RelationsComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createContentProvider()
         */
        protected IStructuredContentProvider createContentProvider() {
            return new RelationContentProvider();
        }

        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createLabelProvider()
         */
        protected ILabelProvider createLabelProvider() {
            return new RelationLabelProvider();
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#newIpsPart()
         */
        protected IIpsObjectPart newIpsPart() {
            return getPcType().newRelation();
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createEditDialog(org.faktorips.devtools.core.model.IIpsObjectPart, org.eclipse.swt.widgets.Shell)
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new RelationEditDialog((IRelation)part, shell);
        }
        
        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#moveParts(int[], boolean)
         */
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPcType().moveRelations(indexes, up);
        }
        
        private class RelationContentProvider implements IStructuredContentProvider {
    		public Object[] getElements(Object inputElement) {
    			 return getPcType().getRelations();
    		}
    		public void dispose() {
    			// nothing todo
    		}
    		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    			// nothing todo
    		}
    	}

	}
    
}
