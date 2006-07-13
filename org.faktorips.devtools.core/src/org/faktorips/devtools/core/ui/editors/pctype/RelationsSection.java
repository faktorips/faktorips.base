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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.editors.pctype.relationwizard.NewPcTypeRelationWizard;
import org.faktorips.util.memento.Memento;

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
    	private Button wizardNewButton;
    	
        RelationsComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
        	// create default buttons without the new button, 
        	//   because the new button will be overridden with wizard functionality
            super(pdObject, parent, false, true, true, true, true, toolkit);
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

        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createButtons(Composite buttons, UIToolkit toolkit)
         */
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
        	createNewWizardButton(buttons, toolkit);
        	super.createButtons(buttons, toolkit);
    		return true;
        }
        
        /**
         * Creates the "New..." button to initiate the new-relation-wizard.
         */
        private void createNewWizardButton(Composite buttons, UIToolkit toolkit) {
        	wizardNewButton = toolkit.createButton(buttons, Messages.RelationEditDialog_labelNewRelationWizard);
        	wizardNewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
    		wizardNewButton.addSelectionListener(new SelectionListener() {
    			public void widgetSelected(SelectionEvent e) {
    				try {
    					newWizardClicked();
    				} catch (Exception ex) {
    					IpsPlugin.logAndShowErrorDialog(ex);
    				}
    			}
    			public void widgetDefaultSelected(SelectionEvent e) {
    			}
    		});
        }
        
        /**
		 * Open the new-relation-wizard
		 */
		private void newWizardClicked() {
			IIpsSrcFile file = getIpsObject().getIpsSrcFile();
			boolean dirty = file.isDirty();
			Memento memento = getIpsObject().newMemento();
			IIpsObjectPart newRelation = newIpsPart();
			WizardDialog dialog = new WizardDialog(getShell(), new NewPcTypeRelationWizard((IRelation)newRelation));
	        dialog.open();
            if (dialog.getReturnCode()==Window.CANCEL) {
            	getIpsObject().setState(memento);
                if (!dirty) {
                    file.markAsClean();
                }
            }
            refresh();
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
