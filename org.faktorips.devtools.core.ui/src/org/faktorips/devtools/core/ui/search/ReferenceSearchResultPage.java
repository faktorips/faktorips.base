/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

public class ReferenceSearchResultPage extends AbstractTextSearchViewPage {
    private static final String FALSE = "false"; //$NON-NLS-1$
    private static final String TRUE = "true"; //$NON-NLS-1$
    private static final String KEY_FILTER_TEST_CASE = "org.faktorips.devtools.core.ui.search.referencesearchresultpage.filtertestcase"; //$NON-NLS-1$
    private static final String KEY_FILTER_PRODUCT_CMPT = "org.faktorips.devtools.core.ui.search.referencesearchresultpage.filterproductcmpt"; //$NON-NLS-1$

    private SearchResultLabelProvider labelProvider;
    private SearchResultContentProvider contentProvider;

    boolean filterTestCase = false;
    boolean filterProductCmpt = false;

    /**
     * Sorting the search result. Test cases will be sorted on the end of the result list.
     * 
     * @author Joerg Ortmann
     */
    public class ReferenceViewerSorter extends ViewerSorter {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            IIpsSrcFile src1 = getCorrespondingIpsSrcFile(e1);
            IIpsSrcFile src2 = getCorrespondingIpsSrcFile(e2);

            if (src1 == null || src2 == null) {
                return 0;
            }

            IpsObjectType type1 = src1.getIpsObjectType();
            IpsObjectType type2 = src2.getIpsObjectType();
            if (isTestCase(type1) && !isTestCase(type2)) {
                return 1;
            }
            if (!isTestCase(type1) && isTestCase(type2)) {
                return -1;
            }

            return src1.getName().compareToIgnoreCase(src2.getName());
        }

        private boolean isTestCase(IpsObjectType ipsObjectType) {
            return IpsObjectType.TEST_CASE.equals(ipsObjectType);
        }
    }

    public ReferenceSearchResultPage() {
        super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
    }

    @Override
    protected StructuredViewer getViewer() {
        // override so that it's visible in the package.
        return super.getViewer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void elementsChanged(Object[] objects) {
        contentProvider.elementsChanged(objects);
    }

    @Override
    protected void clear() {
        contentProvider.clear();
    }

    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        if (labelProvider == null) {
            labelProvider = new SearchResultLabelProvider();
        }
        if (contentProvider == null) {
            contentProvider = new SearchResultContentProvider(this);
        }
        viewer.setLabelProvider(labelProvider);
        viewer.setContentProvider(contentProvider);
        viewer.setSorter(new ReferenceViewerSorter());
        viewer.addDoubleClickListener(new TreeViewerDoubleclickListener(viewer));
        viewer.setUseHashlookup(true);
    }

    @Override
    protected void configureTableViewer(TableViewer viewer) {
        // nothing to do
    }

    @Override
    protected void fillContextMenu(IMenuManager mgr) {
        mgr.appendToGroup(IContextMenuConstants.GROUP_OPEN, new OpenEditorAction(getViewer()));
        IIpsSrcFile ipsSrcFile = getIpsSrcFileForSelection();
        if (ipsSrcFile != null && IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())) {
            mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, new ShowStructureAction(getViewer()));
        }
        super.fillContextMenu(mgr);
    }

    public IIpsSrcFile getIpsSrcFileForSelection() {
        Object selection = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
        // retrieve first element of the selection
        return getCorrespondingIpsSrcFile(selection);
    }

    private IIpsSrcFile getCorrespondingIpsSrcFile(Object selection) {
        if (selection instanceof Object[]) {
            selection = ((Object[])selection)[0];
        }
        if (selection instanceof IIpsObjectPart) {
            return ((IIpsObjectPart)selection).getIpsObject().getIpsSrcFile();
        }
        if (selection instanceof IIpsObject) {
            return ((IIpsObject)selection).getIpsSrcFile();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreState(IMemento memento) {
        super.restoreState(memento);
        if (memento != null) {
            filterTestCase = TRUE.equals(memento.getString(KEY_FILTER_TEST_CASE));
            filterProductCmpt = TRUE.equals(memento.getString(KEY_FILTER_PRODUCT_CMPT));
        } else {
            filterTestCase = TRUE.equals(getSettings().get(KEY_FILTER_TEST_CASE));
            filterProductCmpt = TRUE.equals(getSettings().get(KEY_FILTER_PRODUCT_CMPT));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        memento.putString(KEY_FILTER_PRODUCT_CMPT, filterProductCmpt ? TRUE : FALSE);
        memento.putString(KEY_FILTER_TEST_CASE, filterTestCase ? TRUE : FALSE);
    }

    public void setActiveMatchFilter(boolean testCase, boolean productCmpt) {
        filterTestCase = testCase;
        filterProductCmpt = productCmpt;
    }

    /**
     * @return Returns the filterTestCase.
     */
    public boolean isFilterTestCase() {
        return filterTestCase;
    }

    /**
     * @return Returns the filterProductCmpt.
     */
    public boolean isFilterProductCmpt() {
        return filterProductCmpt;
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        StructuredViewer viewer = getViewer();

        AbstractTextSearchResult result = getInput();
        if (result != null) {
            Tree tree = (Tree)viewer.getControl();
            int itemCount = tree.getItemCount();
            int fileCount = getInput().getElements().length;
            if (itemCount < fileCount) {
                String format = Messages.ReferenceSearchResultPage_labelFilterCountDetails;
                return NLS.bind(format, new Object[] { label, new Integer(itemCount), new Integer(fileCount) });
            }
        }
        return label;
    }
}
