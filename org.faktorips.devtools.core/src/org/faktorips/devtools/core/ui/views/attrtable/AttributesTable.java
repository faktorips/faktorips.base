package org.faktorips.devtools.core.ui.views.attrtable;



import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.views.IpsResourceChangeListener;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 *
 */
public class AttributesTable extends ViewPart {

    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.attributesTable"; //$NON-NLS-1$
    private TableViewer tableViewer;
    
	public AttributesTable() {
		super();
	}

    /**
     * Overridden.
     */
	public void createPartControl(Composite parent) {
        tableViewer = new TableViewer(parent);
        Table table = (Table)tableViewer.getControl();

        tableViewer.setContentProvider(new AttributeContentProvider());
        tableViewer.setLabelProvider(new AttributeLabelProvider());
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        tableViewer.setCellModifier(new AttributesTableCellModifier(tableViewer));

        // create column for product-name
        TableColumn col = new TableColumn(table, SWT.LEAD);
        col.setWidth(100);        
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IpsResourceChangeListener(tableViewer), IResourceChangeEvent.POST_CHANGE);
    }

	public void setFocus() {
        //nothing to do.
	}
    
    protected void show(IPolicyCmptType type) {
        IAttribute[] attributes = type.getAttributes();
        
        Table table = (Table)tableViewer.getTable();

        int columnCount = table.getColumnCount();

        String[] header = new String[attributes.length+1];
        header[0] = "0"; //$NON-NLS-1$
        CellEditor[] editors = new CellEditor[attributes.length + 1];
        editors[0] = null;

        TableColumn col;
        for (int i = 0; i < attributes.length; i ++) {
            if (attributes[i].isProductRelevant()) {
                if (i+1 < columnCount) {
                    // column already exists - get it
                    col = table.getColumn(i+1);
                }
                else {
                    // not enougth columns - create a new one
                    col = new TableColumn(table, SWT.LEAD);
                }
                
                col.setWidth(100);
                header[i+1] = "" + (i+1); //$NON-NLS-1$
                editors[i+1] = new TextCellEditor(table);
                col.setText(attributes[i].getName());
                col.setImage(attributes[i].getConfigElementType().getImage());
                col.setMoveable(true);
                col.setResizable(true);
            }
        }
        tableViewer.setColumnProperties(header);
        tableViewer.setCellEditors(editors);

        // hide all columns which are not used at the moment
        for (int i = attributes.length + 1; i < columnCount; i++) {
            col = table.getColumn(i);
            col.setWidth(0);
            col.setMoveable(false);
            col.setResizable(false);
        }
        
        tableViewer.setInput(type);
    }

}
