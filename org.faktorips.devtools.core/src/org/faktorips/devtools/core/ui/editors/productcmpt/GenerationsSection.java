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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section that displays a timed pdobject's generations.
 */
public class GenerationsSection extends SimpleIpsPartsSection {

	/**
	 * The page owning this section.
	 */
	private GenerationsPage page;
	
	/**
	 * Create a new Section to display generations.
	 * @param page The page owning this section.
	 * @param parent The composit which is parent for this section
	 * @param toolkit The toolkit to help creating the ui
	 */
    public GenerationsSection(
            GenerationsPage page, 
            Composite parent,
            UIToolkit toolkit) {
        super(page.getProductCmpt(), parent, Section.TITLE_BAR | Section.DESCRIPTION, 
        		IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural(), toolkit);
        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new GenerationsComposite((ITimedIpsObject)getIpsObject(), parent, toolkit);
    }
    
    /**
     * Set the active generation (which means, the generation to show/edit) to the editor. If the 
     * generation to set would not be editable, the user is asked if a switch is really wanted.
     */
    private void setActiveGeneration(IProductCmptGeneration generation, boolean automatic) {
    	if (generation != null) {
			IProductCmpt prod = page.getProductCmptEditor().getProductCmpt();
			IProductCmptGeneration editableGeneration = (IProductCmptGeneration) prod
					.getGenerationByEffectiveDate(IpsPlugin.getDefault()
							.getIpsPreferences().getWorkingDate());
	    	boolean select = generation.equals(editableGeneration);
	    	if (!select && !automatic) {
	    		String genName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
	    		String title = Messages.bind(Messages.GenerationsSection_titleShowGeneration, genName);
	    		Object[] args = new Object[3];
	    		args[0] = genName;
	    		args[1] = generation.getName();
	    		args[2] = IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate();
	    		String message = Messages.bind(Messages.GenerationsSection_msgShowGeneration, args);	    		
	    		select = MessageDialog.openConfirm(page.getSite().getShell(), title, message);
	    	}
			if (select || automatic) {
				page.getProductCmptEditor().setActiveGeneration(generation);
				if (!automatic) {
					page.getProductCmptEditor().setActivePage(PropertiesPage.PAGE_ID);
				}
			}
    	}
    }
    
    private IProductCmptGeneration getActiveGeneration() {
    	
    	return (IProductCmptGeneration)page.getProductCmptEditor().getActiveGeneration();
    }

    
    
    /**
     * A composite that shows a policy component's attributes in a viewer and 
     * allows to edit attributes in a dialog, create new attributes and delete attributes.
     */
    public class GenerationsComposite extends IpsPartsComposite implements IDeleteListener {

        public GenerationsComposite(ITimedIpsObject ipsObject, Composite parent,
                UIToolkit toolkit) {
            super(ipsObject, parent, false, true, true, false, true, toolkit);

            super.setEditDoubleClickListenerEnabled(false);
            
            getViewer().getControl().addMouseListener(new MouseAdapter() {
				public void mouseDoubleClick(MouseEvent e) {
					Object selected = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
					if (selected instanceof IProductCmptGeneration) {
						setActiveGeneration((IProductCmptGeneration)selected, false);
					}
				}
            });
            
			addDeleteListener(this);
			
			final MyPropertyChangeListener changeListener = new MyPropertyChangeListener(getViewer(), this);

			IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(changeListener);
			
			getViewer().getControl().addDisposeListener(new DisposeListener() {

				public void widgetDisposed(DisposeEvent e) {
					IpsPlugin.getDefault().getPreferenceStore()
							.removePropertyChangeListener(changeListener);
				}
			});
        }
        
        public ITimedIpsObject getTimedPdObject() {
            return (ITimedIpsObject)getIpsObject();
        }
        
        /**
         * {@inheritDoc}
         */
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }
        
        /**
         * {@inheritDoc}
         */
        protected ILabelProvider createLabelProvider() {
			return new LabelProvider();
		}

        /**
         * {@inheritDoc}
         */
        protected IIpsObjectPart newIpsPart() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new GenerationEditDialog((IProductCmptGeneration)part, shell);
        }

		/**
		 * {@inheritDoc}
		 */
		public void aboutToDelete(IIpsObjectPart part) {
			if (page.getProductCmpt().getGenerations().length == 2) {
				super.deleteButton.setEnabled(false);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void deleted(IIpsObjectPart part) {
			setActiveGeneration(getSelectedGeneration(), true);
		}
		
		private IProductCmptGeneration getSelectedGeneration() {
			IIpsObjectPart selected = getSelectedPart();
			if (selected instanceof IProductCmptGeneration) {
				return (IProductCmptGeneration)selected;
			}
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		protected void updateButtonEnabledStates() {
			super.updateButtonEnabledStates();
			deleteButton.setEnabled(!(page.getProductCmpt().getGenerations().length == 1));
		}
    	
    	private class ContentProvider implements IStructuredContentProvider {
    		public Object[] getElements(Object inputElement) {
    			 return getTimedPdObject().getGenerations();
    		}
    		public void dispose() {
    			// nothing todo
    		}
    		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    			// nothing todo
    		}
    	}
    	
    	private class LabelProvider extends DefaultLabelProvider  {

			public String getText(Object element) {
				if (!(element instanceof IProductCmptGeneration)) {
					return super.getText(element);
				}
				
				if (element.equals(getActiveGeneration())) {
					return super.getText(element) + Messages.GenerationsSection_displayPostfix;
				}
				
				return super.getText(element);
			}

			public Image getImage(Object element) {
				if (element instanceof IProductCmptGeneration) {
					IProductCmptGeneration generation = (IProductCmptGeneration)element;

					Image image = super.getImage(element); 
					if (((ProductCmptEditor)page.getEditor()).isEditableGeneration(generation)) {
						return image;
					}
					else {
						return new Image(Display.getDefault(), image, SWT.IMAGE_DISABLE);
					}
				}
				else {
					return super.getImage(element);
				}
			}
    	}
    }
    
    private class MyPropertyChangeListener implements IPropertyChangeListener {
		private Viewer viewer;

		private GenerationsComposite composite;

		public MyPropertyChangeListener(Viewer viewer,
				GenerationsComposite generationsComposite) {
			this.viewer = viewer;
			this.composite = generationsComposite;
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (viewer.getControl().isDisposed()) {
				IpsPlugin
						.log(new IpsStatus(
								"Disposed GenerationsSections is listening for property changes.")); //$NON-NLS-1$
				return;
			}

			String property = event.getProperty();
			if (property.equals(IpsPreferences.WORKING_DATE)
					|| property
							.equals(IpsPreferences.EDIT_GENERATION_WITH_SUCCESSOR)
					|| property.equals(IpsPreferences.EDIT_RECENT_GENERATION)) {
				viewer.refresh();
				composite.updateButtonEnabledStates();
			}
		}
	}

}
