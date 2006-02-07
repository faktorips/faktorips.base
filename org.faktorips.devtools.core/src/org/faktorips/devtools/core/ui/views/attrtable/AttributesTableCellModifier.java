package org.faktorips.devtools.core.ui.views.attrtable;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Item;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;

public class AttributesTableCellModifier implements ICellModifier {

    private TableViewer tableViewer;

    public AttributesTableCellModifier(TableViewer parent) {
        this.tableViewer = parent;
    }
    
    public boolean canModify(Object element, String property) {
        return (Integer.parseInt(property) > 0);
    }

    public Object getValue(Object element, String property) {
        int colIndex = Integer.parseInt(property);
        Object obj = ((Object[])element)[colIndex];
        if (colIndex > 0) {
            IConfigElement ce = (IConfigElement)obj;
            return ce.getValue();
        }
        else {
            return ((IProductCmpt)obj).getName();
        }
    }

    public void modify(Object element, String property, Object value) {
        int colIndex = Integer.parseInt(property);
        Object obj;
        if (element instanceof Item) {
            obj = ((Object[])((Item)element).getData())[colIndex];
        }
        else {
            obj = ((Object[])element)[colIndex];
        }
        
        if (colIndex > 0) {
            IConfigElement ce = (IConfigElement)obj;
            ce.setValue(value.toString());
            IpsPlugin.getDefault().getWorkbench().saveAllEditors(false);
            tableViewer.refresh();
        }
    }

}
