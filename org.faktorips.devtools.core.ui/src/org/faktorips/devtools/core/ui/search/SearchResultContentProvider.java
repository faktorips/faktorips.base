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

package org.faktorips.devtools.core.ui.search;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

// TODO remove if not necessary!!!

/**
 * @author Thorsten Guenther
 */
public class SearchResultContentProvider implements ITreeContentProvider {

    private ReferenceSearchResultPage page;
    private ReferenceSearchResult searchResult;

    public SearchResultContentProvider(ReferenceSearchResultPage page) {
        this.page = page;
    }

    private synchronized void initialize(ReferenceSearchResult result) {
        this.searchResult = result;

        if (searchResult == null) {
            return;
        }

        searchResult.setPage(page);
        searchResult.setActiveMatchedFilterFor(page.isFilterTestCase(), page.isFilterProductCmpt());
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Object[]) {
            Object[] elementArray = (Object[])parentElement;
            if (elementArray.length > 1) {
                IIpsElement[] children = new IIpsElement[elementArray.length - 1];
                System.arraycopy(elementArray, 1, children, 0, elementArray.length - 1);
                return children;
            }
        }
        return new IIpsElement[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IIpsElement) {
            if (element instanceof IProductCmptGeneration) {
                return ((IProductCmptGeneration)element).getProductCmpt();
            } else {
                // TODO was ist parent von PCType? supertype? kann man den ohne Suche finden?
                return ((IIpsElement)element).getParent();
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof Object[]) {
            return ((Object[])element).length > 1;
        }
        return false;
    }

    @Override
    public void dispose() {
        // nothing to do.
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ReferenceSearchResult) {
            ReferenceSearchResult searchResult = (ReferenceSearchResult)inputElement;
            return searchResult.getElements();
        }
        // in Eclipse 3.3 this method will always retrurn an empty array because the elementsChanged
        // Method populates the elements depending on the match filter to the view
        return new Object[0];
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        initialize((ReferenceSearchResult)newInput);
    }

    public synchronized void elementsChanged(Object[] updatedElements) {
        if (searchResult == null) {
            return;
        }

        AbstractTreeViewer viewer = (AbstractTreeViewer)page.getViewer();

        Set<Object> toRemove = new HashSet<Object>();
        Set<Object> toAdd = new HashSet<Object>();
        Set<Object> toUpdate = new HashSet<Object>();
        for (Object updatedElement : updatedElements) {
            if (page.getDisplayedMatchCount(updatedElement) > 0) {
                if (viewer.testFindItem(updatedElement) != null) {
                    toUpdate.add(updatedElement);
                } else {
                    toAdd.add(updatedElement);
                }
            } else {
                toRemove.add(updatedElement);
            }
        }

        if (toRemove.size() > 0) {
            viewer.remove(searchResult, toRemove.toArray());
        }

        for (Object element : toAdd) {
            viewer.add(searchResult, element);
        }

        for (Object element : toUpdate) {
            viewer.refresh(element);
        }
    }

    public void clear() {
        initialize(searchResult);
        page.getViewer().refresh();
    }

    /**
     * @return Returns the searchResult.
     */
    public ReferenceSearchResult getSearchResult() {
        return searchResult;
    }
}
