package org.faktorips.devtools.core.ui.views.instanceexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.views.InstanceIpsSrcFileViewItem;

/**
 * The label provider for the instance explorer, extending the {@link StyledCellLabelProvider} The
 * methods for {@link ILabelProvider} are delegated to the {@link DefaultLabelProvider}.
 * 
 * @author dirmeier
 * 
 */
public class InstanceLabelProvider extends StyledCellLabelProvider implements ILabelProvider {

    private DefaultLabelProvider defaultLabelProvider = new DefaultLabelProvider();

    private boolean subTypeSearch = true;

    /**
     * Default Constructor
     */
    public InstanceLabelProvider() {
    }

    protected boolean isSubTypeSearch() {
        return subTypeSearch;
    }

    protected void setSubTypeSearch(boolean subTypeSearch) {
        this.subTypeSearch = subTypeSearch;
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        try {
            if (element instanceof InstanceIpsSrcFileViewItem) {
                InstanceIpsSrcFileViewItem item = (InstanceIpsSrcFileViewItem)element;
                updateCell(cell, item);
            } else if (element instanceof String) {
                cell.setText((String)element);
            }

        } catch (CoreException e) {
            IpsPlugin.log(e);
        } finally {
            super.update(cell);
        }
    }

    private void updateCell(ViewerCell cell, InstanceIpsSrcFileViewItem item) throws CoreException {
        String elementName = getText(item);
        List<StyleRange> styleRanges = new ArrayList<StyleRange>();
        if (item.isInstanceOfMetaClass()) {
            String typeSuffix = " - " + item.getDefiningMetaClass(); //$NON-NLS-1$
            StyleRange styledType = new StyleRange();
            styledType.start = elementName.length();
            styledType.length = typeSuffix.length();
            styledType.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
            styledType.fontStyle = SWT.NORMAL;
            styleRanges.add(styledType);
            elementName += typeSuffix;
        }
        if (item.isDuplicateName()) {
            String pathSuffix = " - " + item.getIpsSrcFile().getParent().getEnclosingResource().getFullPath();
            StyleRange styledPath = new StyleRange();
            styledPath.start = elementName.length();
            styledPath.length = pathSuffix.length();
            styledPath.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
            styledPath.fontStyle = SWT.NORMAL;
            styleRanges.add(styledPath);
            elementName += pathSuffix;
        }
        cell.setText(elementName);
        cell.setStyleRanges(styleRanges.toArray(new StyleRange[styleRanges.size()]));
        cell.setImage(getImage(item));
    }

    @Override
    public Image getImage(Object element) {
        return defaultLabelProvider.getImage(element);
    }

    @Override
    public String getText(Object element) {
        return defaultLabelProvider.getText(element);
    }

}
