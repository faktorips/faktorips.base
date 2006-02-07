package org.faktorips.devtools.core.ui.views.attrtable;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.product.IConfigElement;

public class AttributeLabelProvider implements ITableLabelProvider {

    private ArrayList listeners;
    
    public Image getImage(Object element) {
        // not used
        return null;
    }

    public String getText(Object element) {
        // not used
        return Messages.AttributeLabelProvider_undefined;
    }

    public void addListener(ILabelProviderListener listener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }
        
        this.listeners.add(listener);
    }

    public void dispose() {
        this.listeners = null;
    }

    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    public void removeListener(ILabelProviderListener listener) {
        this.listeners.remove(listener);
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        Object[] values = (Object[])element;
        if (values.length > columnIndex) {
            Object item = values[columnIndex];
            
            if (columnIndex == 0) {
                return ((IIpsElement)item).getName(); 
            }
            else {
                return ((IConfigElement)item).getValue();
            }
        }
        else {
            return Messages.AttributeLabelProvider_invalid;
        }
    }
}
