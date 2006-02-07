package org.faktorips.devtools.core.ui.search;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;

public class ReferencesToProductSearchResultPage extends AbstractTextSearchViewPage {
    SearchResultLabelProvider labelProvider;
    SearchResultContentProvider contentProvider;
    
    public ReferencesToProductSearchResultPage() {
        super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
    }
    
    protected synchronized void elementsChanged(Object[] objects) {
        super.getViewer().refresh();
        super.getViewPart().updateLabel();
    }

    protected void clear() {
        super.getViewer().refresh();
        super.getViewPart().updateLabel();
    }

    protected void configureTreeViewer(TreeViewer viewer) {
        if (labelProvider == null) {
            labelProvider = new SearchResultLabelProvider();
        }
        if (contentProvider == null) {
            contentProvider = new SearchResultContentProvider();
        }
        viewer.setLabelProvider(labelProvider);
        viewer.setContentProvider(contentProvider);
    }

    protected void configureTableViewer(TableViewer viewer) {
        //nothing to do
    }

    protected void fillContextMenu(IMenuManager mgr) {
        mgr.appendToGroup(IContextMenuConstants.GROUP_OPEN, new OpenEditorAction(getViewer()));
        mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, new ShowStructureAction(getViewer()));
        super.fillContextMenu(mgr);
    }

    public IIpsSrcFile getIpsSrcFileForSelection() {
        Object selection = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
        if (selection instanceof IProductCmptGeneration) {
            return ((IProductCmptGeneration)selection).getProductCmpt().getIpsSrcFile();
        }
        else if (selection instanceof Object[]) {
            return ((IProductCmpt)((Object[])selection)[0]).getIpsSrcFile();
        }
        return null;
    }
}
