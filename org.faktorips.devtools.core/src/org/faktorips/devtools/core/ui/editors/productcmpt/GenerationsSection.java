package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Locale;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section that displays a timed pdobject's generations.
 */
public class GenerationsSection extends SimpleIpsPartsSection {

    public GenerationsSection(
            ITimedIpsObject ipsObject, 
            Composite parent,
            UIToolkit toolkit) {
        super(ipsObject, parent, Section.TITLE_BAR | Section.DESCRIPTION, 
        	IpsPreferences.getChangesInTimeNamingConvention().getGenerationConceptNamePlural(Locale.getDefault()), toolkit);
    }

    /** 
     * Overridden.
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new GenerationsComposite((ITimedIpsObject)getIpsObject(), parent, toolkit);
    }
    
    /**
     * A composite that shows a policy component's attributes in a viewer and 
     * allows to edit attributes in a dialog, create new attributes and delete attributes.
     */
    public class GenerationsComposite extends IpsPartsComposite {

        public GenerationsComposite(ITimedIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, false, false, false, false, false, toolkit);
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
