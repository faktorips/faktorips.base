/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.search.IpsElementsSearchViewPage;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

public class ReferenceSearchResultPage extends IpsElementsSearchViewPage {

    private static final String FALSE = "false"; //$NON-NLS-1$
    private static final String TRUE = "true"; //$NON-NLS-1$
    private static final String KEY_FILTER_TEST_CASE = "org.faktorips.devtools.core.ui.search.referencesearchresultpage.filtertestcase"; //$NON-NLS-1$
    private static final String KEY_FILTER_PRODUCT_CMPT = "org.faktorips.devtools.core.ui.search.referencesearchresultpage.filterproductcmpt"; //$NON-NLS-1$

    boolean filterTestCase = false;
    boolean filterProductCmpt = false;

    /**
     * Sorting the search result. Test cases will be sorted on the end of the result list.
     * 
     * @author Joerg Ortmann
     */
    public class ReferenceViewerSorter extends ViewerSorter {

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
                return NLS.bind(format, new Object[] { label, Integer.valueOf(itemCount), Integer.valueOf(fileCount) });
            }
        }
        return label;
    }

    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        super.configureTreeViewer(viewer);
        viewer.setSorter(new ReferenceViewerSorter());
        viewer.addDoubleClickListener(new TreeViewerDoubleclickListener(viewer));
    }

}
