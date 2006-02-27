package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 *
 */
public class ForeignKeysSection extends SimpleIpsPartsSection{

    /**
     * @param parent
     * @param style
     * @param toolkit
     */
    public ForeignKeysSection(ITableStructure table, Composite parent, UIToolkit toolkit) {
        super(table, parent, Messages.ForeignKeysSection_title, toolkit);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection#createIpsPartsComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ForeignKeysComposite(getIpsObject(), parent, toolkit);
    }
    
    private class ForeignKeysComposite extends IpsPartsComposite {

        public ForeignKeysComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
        }

        public ITableStructure getTableStructure() {
            return (ITableStructure)getPdObject();
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createContentProvider()
         */
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        protected IIpsObjectPart newIpsPart() {
            return getTableStructure().newForeignKey();
        }

        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new KeyEditDialog((IForeignKey)part, shell);
        }
        
        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#moveParts(int[], boolean)
         */
        protected int[] moveParts(int[] indexes, boolean up) {
            return getTableStructure().moveForeignKeys(indexes, up);
        }
        
		private class ContentProvider implements IStructuredContentProvider {
			public Object[] getElements(Object inputElement) {
				 return getTableStructure().getForeignKeys();
			}
			public void dispose() {
				// nothing todo
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// nothing todo
			}
		}
	
    } // class ForeignKeysComposite

}
