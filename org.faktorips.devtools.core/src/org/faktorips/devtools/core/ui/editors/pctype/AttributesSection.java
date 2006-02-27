package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section to display and edit a type's attributes.
 */
public class AttributesSection extends SimpleIpsPartsSection {

    public AttributesSection(IPolicyCmptType pcType, Composite parent, UIToolkit toolkit) {
        super(pcType, parent, Messages.AttributesSection_title, toolkit);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection#createIpsPartsComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new AttributesComposite((IPolicyCmptType)getIpsObject(), parent, toolkit);
    }
    
    /**
     * A composite that shows a policy component's attributes in a viewer and 
     * allows to edit attributes in a dialog, create new attributes and delete attributes.
     */
    public class AttributesComposite extends IpsPartsComposite {

        public AttributesComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
            super.addDeleteListener(new IDeleteListener() {
			
				public void aboutToDelete(IIpsObjectPart part) {
					String msg = Messages.AttributesSection_deleteMessage;
					boolean delete = MessageDialog.openQuestion(getShell(), Messages.AttributesSection_deleteTitle, msg);
					IValidationRule rule = findValidationRule(part);
					if (delete && rule != null) {
						rule.delete();
					}
					else if (!delete && rule != null) {
						rule.setCheckValueAgainstValueSetRule(false);
					}
				}
			
				private IValidationRule findValidationRule(IIpsObjectPart part) {
					String name = part.getName();
					IValidationRule[] rules = getPcType().getRules();
					for (int i = 0; i < rules.length; i++) {
						if (!rules[i].isCheckValueAgainstValueSetRule()) {
							continue;
						}
						String[] attributes = rules[i].getValidatedAttributes();
						if (attributes.length == 1 && attributes[0].equals(name)) {
							return rules[i];
						}
					}
					return null;
				}
			});
        }
        
        public IPolicyCmptType getPcType() {
            return (IPolicyCmptType)getPdObject();
        }
        
        protected ILabelProvider createLabelProvider() {
            return new AttributeLabelProvider();
        }
        
        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createContentProvider()
         */ 
        protected IStructuredContentProvider createContentProvider() {
            return new AttributeContentProvider();
        }

        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#newIpsPart()
         */ 
        protected IIpsObjectPart newIpsPart() {
            IAttribute a = getPcType().newAttribute();
            a.setProductRelevant(getPcType().isConfigurableByProductCmptType());
            return a;
        }

        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createEditDialog(org.faktorips.devtools.core.model.IIpsObjectPart, org.eclipse.swt.widgets.Shell)
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IAttribute)part, shell);
        }
        
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPcType().moveAttributes(indexes, up);
        }
        
    	private class AttributeContentProvider implements IStructuredContentProvider {
    		public Object[] getElements(Object inputElement) {
    			 return getPcType().getAttributes();
    		}
    		public void dispose() {
    			// nothing todo
    		}
    		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    			// nothing todo
    		}
    	}

    	private class AttributeLabelProvider extends DefaultLabelProvider {
    	    
            public String getText(Object element) {
                IAttribute a = (IAttribute)element;
                return a.getName() + " : " + a.getDatatype(); //$NON-NLS-1$
            }
    	}
    }
}
