package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section that displays rules for a product component.
 * 
 * @author Thorsten Guenter
 */
public class RulesSection extends SimpleIpsPartsSection {

	/**
	 * The page owning this section.
	 */
	private RulesPage page;
	
	/**
	 * Create a new Section to display rules.
	 * @param page The page owning this section.
	 * @param parent The composit which is parent for this section
	 * @param toolkit The toolkit to help creating the ui
	 */
    public RulesSection(
            RulesPage page, 
            Composite parent,
            UIToolkit toolkit) {
        super(page.getProductCmpt(), parent, Section.TITLE_BAR | Section.DESCRIPTION, Messages.RulesSection_title, toolkit);
        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RulesComposite((IProductCmpt)getIpsObject(), parent, toolkit);
    }
    
    /**
     * A composite that shows a policy component's attributes in a viewer and 
     * allows to edit attributes in a dialog, create new attributes and delete attributes.
     */
    public class RulesComposite extends IpsPartsComposite {

        public RulesComposite(IProductCmpt product, Composite parent, UIToolkit toolkit) {
            super(product, parent, false, false, false, false, false, toolkit);
        }
        
        /**
         * {@inheritDoc}
         */
        protected Viewer createViewer(Composite parent, UIToolkit toolkit) {
        	Tree tree = toolkit.getFormToolkit().createTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        	TreeViewer viewer = new TreeViewer(tree);
    		viewer.setContentProvider(createContentProvider());
    		ILabelProvider lp = createLabelProvider();
    		viewer.setLabelProvider(new MessageCueLabelProvider(lp));
        	return viewer;
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
        protected IIpsObjectPart newIpsPart() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return null;
        }
    	
    	
    	private class ContentProvider implements ITreeContentProvider {
    		public Object[] getElements(Object inputElement) {
    			ArrayList result = new ArrayList();
    			try {
					IPolicyCmptType type = page.getProductCmpt().findPolicyCmptType();
					while (type != null) {
						result.add(type);
						type = type.findSupertype();
					}
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
    			return (IPolicyCmptType[])result.toArray(new IPolicyCmptType[result.size()]);
    		}
    		
    		public void dispose() {
    			// nothing todo
    		}
    		
    		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    			// nothing todo
    		}

			/**
			 * {@inheritDoc}
			 */
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IPolicyCmptType) {
					return ((IPolicyCmptType)parentElement).getRules();
				}
				return new Object[0];
			}

			/**
			 * {@inheritDoc}
			 */
			public Object getParent(Object element) {
				if (element instanceof IValidationRule) {
					return ((IValidationRule)element).getParent();
				}
				return null;
			}

			/**
			 * {@inheritDoc}
			 */
			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}
    	}

    }

}
