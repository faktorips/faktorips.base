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

/**
 * The label provider for the instance explorer, extending the {@link StyledCellLabelProvider}
 * The methods for {@link ILabelProvider} are delegated to the {@link DefaultLabelProvider}.
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
		defaultLabelProvider.setIspSourceFile2IpsObjectMapping(true);
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
			if (element instanceof InstanceViewerItem) {
				InstanceViewerItem item = (InstanceViewerItem) element;
				updateCell(cell, item);
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		} finally {
			super.update(cell);
		}
	}

	private void updateCell(ViewerCell cell, InstanceViewerItem item) throws CoreException {
			String elementName = getText(item);
			List<StyleRange> styleRanges = new ArrayList<StyleRange>();
			if (item.getDefiningMetaClass() != null && item.getDefiningMetaClass().length() > 0) {
				String typeSuffix = " - " + item.getDefiningMetaClass();  //$NON-NLS-1$
				StyleRange styledType = new StyleRange();
				styledType.start = elementName.length();
				styledType.length = typeSuffix.length();
				styledType.foreground = cell.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
				styledType.fontStyle = SWT.NORMAL;
				styleRanges.add(styledType);
				elementName += typeSuffix;
			}
			if (item.isDuplicateName()) {
				String pathSuffix = " - " + item.getIpsSrcFile().getParent().getCorrespondingResource().getFullPath();
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

	public Image getImage(Object element) {
		return defaultLabelProvider.getImage(element);
	}

	public String getText(Object element) {
		return defaultLabelProvider.getText(element);
	}


}
