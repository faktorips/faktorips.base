package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class FormulasSection extends IpsSection {

    private IProductCmptGeneration generation;
    private FormulasComposite composite;
    
    public FormulasSection(
            IProductCmptGeneration generation,
            Composite parent,
            UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(generation);
        
        this.generation = generation;
        initControls();
        setText(Messages.FormulasSection_calculationFormulas);
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#initClientComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        composite = new FormulasComposite(client, toolkit);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#performRefresh()
     */
    protected void performRefresh() {
        composite.refresh();
    }
    
    private class FormulasComposite extends IpsPartsComposite {

        public FormulasComposite(Composite parent, UIToolkit toolkit) {
            super(generation.getIpsObject(), parent, false, true, false, false, false, toolkit);
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
            // TODO Auto-generated method stub
            return null;
        }

        /** 
         * Overridden method.
         * @throws CoreException
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createEditDialog(org.faktorips.devtools.core.model.IIpsObjectPart, org.eclipse.swt.widgets.Shell)
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new FormulaEditDialog((IConfigElement)part, shell);
        }
        
		private class ContentProvider implements IStructuredContentProvider {
			
		    public Object[] getElements(Object inputElement) {
				 return generation.getConfigElements(ConfigElementType.FORMULA);
			}
			
			public void dispose() {
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		}
		
		private class LabelProvider extends DefaultLabelProvider {
		    
		    public String getText(Object element) {
	            IConfigElement ce = (IConfigElement)element;
	            return ce.getName();
	        }
		}
    
    }
    

}
