package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
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
        	IpsPreferences.getChangesInTimeNamingConvention().getGenerationConceptNamePlural(Locale.getDefault()), toolkit);
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
    private void setActiveGeneration(IProductCmptGeneration generation) {
    	if (generation != null) {
			IProductCmpt prod = page.getProductCmptEditor().getProductCmpt();
			IProductCmptGeneration editableGeneration  = (IProductCmptGeneration)prod.getGenerationByEffectiveDate(IpsPreferences.getWorkingDate());
	    	boolean select = generation.equals(editableGeneration);
	    	if (!select) {
	    		String genName = IpsPreferences.getChangesInTimeNamingConvention().getGenerationConceptNameSingular(Locale.getDefault());
	    		select = MessageDialog.openConfirm(page.getSite().getShell(), genName + " anzeigen?", "Die ausgewählte " + genName + " hat ein Gültig-Ab-Datum (" + generation.getName() + "), welches nicht mit dem aktuellen Arbeitsdatum (" + IpsPreferences.getWorkingDate().getTime().toLocaleString() + ") übereinstimmt. Wenn diese " + genName + " angezeigt wird, kann sie nicht bearbeitet werden. Soll die " + genName + " angezeigt werden?");
	    	}
			if (select) {
				page.getProductCmptEditor().setActiveGeneration(generation);
				page.getProductCmptEditor().setActivePage(PropertiesPage.PAGE_ID);
			}
    	}
    }
    
    /**
     * A composite that shows a policy component's attributes in a viewer and 
     * allows to edit attributes in a dialog, create new attributes and delete attributes.
     */
    public class GenerationsComposite extends IpsPartsComposite {

        public GenerationsComposite(ITimedIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, false, false, false, false, false, toolkit);
            getViewer().getControl().addMouseListener(new MouseAdapter() {

				public void mouseDoubleClick(MouseEvent e) {
					Object selected = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
					if (selected instanceof IProductCmptGeneration) {
						setActiveGeneration((IProductCmptGeneration)selected);
					}
				}
            	
            });
        }
        
        public ITimedIpsObject getTimedPdObject() {
            return (ITimedIpsObject)getPdObject();
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
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#newIpsPart()
         */ 
        protected IIpsObjectPart newIpsPart() {
            return null;
        }

        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createEditDialog(org.faktorips.devtools.core.model.IIpsObjectPart, org.eclipse.swt.widgets.Shell)
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return null;
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

    }

}
