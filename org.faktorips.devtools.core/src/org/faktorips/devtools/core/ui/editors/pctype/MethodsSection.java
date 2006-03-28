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
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section to display and edit a type's methods.
 */
public class MethodsSection extends SimpleIpsPartsSection {
    
    public MethodsSection(
            IPolicyCmptType pcType, 
            Composite parent, 
            UIToolkit toolkit) {
        super(pcType, parent, Messages.MethodsSection_title, toolkit);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection#createIpsPartsComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new MethodsComposite((IPolicyCmptType)getIpsObject(), parent, toolkit);
    }
    
    /**
     * A composite that shows a policy component's methods in a viewer and 
     * allows to edit methods in a dialog, create new methods and delete methods.
     */
    class MethodsComposite extends IpsPartsComposite {
        
        private Button overrideButton;

        public MethodsComposite(IPolicyCmptType pcType, Composite parent, UIToolkit toolkit) {
            super(pcType, parent, toolkit);
        }
        
        public IPolicyCmptType getPcType() {
            return (IPolicyCmptType)getIpsObject();
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.ViewerButtonComposite#createButtons(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
         */
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
            	OverrideMethodDialog dialog = new OverrideMethodDialog(getPcType(), getShell());
                if (dialog.open()==Window.OK) {
                    getPcType().overrideMethods(dialog.getSelectedMethods());
                }
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createContentProvider()
         */
        protected IStructuredContentProvider createContentProvider() {
            return new MethodContentProvider();
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#newIpsPart()
         */
        protected IIpsObjectPart newIpsPart() {
            return getPcType().newMethod();
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createEditDialog(org.faktorips.devtools.core.model.IIpsObjectPart, org.eclipse.swt.widgets.Shell)
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new MethodEditDialog((IMethod)part, shell);
        }

        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#moveParts(int[], boolean)
         */
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPcType().moveMethods(indexes, up);
        }
        
        private class MethodContentProvider implements IStructuredContentProvider {
    		public Object[] getElements(Object inputElement) {
    			return getPcType().getMethods();
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
