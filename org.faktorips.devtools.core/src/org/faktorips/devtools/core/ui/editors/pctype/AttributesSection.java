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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
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
        private Button overrideButton;

        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);
    		overrideButton = toolkit.createButton(buttons, Messages.MethodsSection_button);
    		overrideButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
    		overrideButton.addSelectionListener(new SelectionListener() {
    			public void widgetSelected(SelectionEvent e) {
    				try {
    					overrideClicked();
    				} catch (Exception ex) {
    					IpsPlugin.logAndShowErrorDialog(ex);
    				}
    			}
    			public void widgetDefaultSelected(SelectionEvent e) {
    			}
    		});
    		return true;
        }
        
        private void overrideClicked() {
            try {
            	OverrideAttributeDialog dialog = new OverrideAttributeDialog(getPcType(), getShell());
                if (dialog.open()==Window.OK) {
                    getPcType().overrideAttributes(dialog.getSelectedAttributes());
                }
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

        public AttributesComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
            super.addDeleteListener(new IDeleteListener() {
			
				public void aboutToDelete(IIpsObjectPart part) {
					IValidationRule rule = findValidationRule(part);

					if (rule == null) {
						// nothing to do if no special rule is defined.
						return;
					}
					
					String msg = Messages.AttributesSection_deleteMessage;
					boolean delete = MessageDialog.openQuestion(getShell(), Messages.AttributesSection_deleteTitle, msg);
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

				public void deleted(IIpsObjectPart part) {
					// nothing to do.
				}
			});
        }
        
        public IPolicyCmptType getPcType() {
            return (IPolicyCmptType)getIpsObject();
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
