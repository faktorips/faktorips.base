/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;
import org.faktorips.devtools.core.ui.search.reference.ReferenceSearchResult;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * 
 * An implementation of the {@link ITreeContentProvider} for the searches in Faktor-IPS
 * 
 * @author Stefan Dicker
 * @author Thorsten Guenther
 */
public class IpsSearchResultTreePathContentProvider implements ITreeContentProvider, ISearchResultListener {

    private IpsElementsSearchViewPage page;
    private ISearchResult searchResult;

    private Map<IIpsElement, IpsElementSearchTreeNode> ipsElementTree;

    public IpsSearchResultTreePathContentProvider(IpsElementsSearchViewPage page) {
        this.page = page;
    }

    @Override
    public void searchResultChanged(SearchResultEvent e) {
        if (e instanceof RemoveAllEvent) {
            ipsElementTree = new HashMap<>();
            return;
        }
        if (e instanceof MatchEvent matchEvent) {
            if (matchEvent.getKind() == MatchEvent.ADDED) {
                addMatches(matchEvent.getMatches());
            }
        }
    }

    private void addElements(Object[] elements) {
        for (Object object : elements) {
            IIpsElement element = (IIpsElement)object;
            addMatchedElement(element, null);
        }
    }

    private void addMatches(Match[] matches) {
        for (Match match : matches) {
            IIpsElement element = (IIpsElement)match.getElement();
            addMatchedElement(element, null);
        }
    }

    private void addMatchedElement(IIpsElement element, IIpsElement child) {
        IpsElementSearchTreeNode ipsElementSearchTreeNode = ipsElementTree.computeIfAbsent(element, e -> {
            IIpsElement parent = getParentOfIpsElement(e);
            if (parent != null) {
                addMatchedElement(parent, e);
            }
            return new IpsElementSearchTreeNode();
        });

        if (child != null) {
            ipsElementSearchTreeNode.addChild(child);
        }
    }

    private IIpsElement getParentOfIpsElement(IIpsElement element) {
        if (element instanceof IIpsProject) {
            return null;
        }
        if (element instanceof IIpsObject ipsSrcFile) {
            return ipsSrcFile.getIpsPackageFragment();
        }
        return element.getParent();
    }

    private synchronized void initialize(ISearchResult result) {
        searchResult = result;

        if (searchResult == null) {
            return;
        }

        ipsElementTree = new HashMap<>();
        addElements(((AbstractTextSearchResult)result).getElements());

    }

    public Object[] getFoundIpsElements() {
        if (searchResult instanceof IpsSearchResult) {
            return ((IpsSearchResult)searchResult).getIpsElements();
        }
        if (searchResult instanceof ReferenceSearchResult) {
            return ((ReferenceSearchResult)searchResult).getAllElements();
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (ipsElementTree.containsKey(parentElement)) {
            return ipsElementTree.get(parentElement).getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IIpsElement) {
            return getParentOfIpsElement((IIpsElement)element);
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (ipsElementTree.containsKey(element)) {
            return ipsElementTree.get(element).hasChildren();
        }
        return false;
    }

    @Override
    public void dispose() {
        // nothing to do.
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof AbstractTextSearchResult currentSearchResult) {
            return currentSearchResult.getElements();
        }
        // in Eclipse 3.3 this method will always return an empty array because the elementsChanged
        // Method populates the elements depending on the match filter to the view
        return new Object[0];
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        initialize((ISearchResult)newInput);

    }

    /**
     * handles the changed Elements
     */
    protected synchronized void elementsChanged(Object[] updatedElements) {
        if (searchResult == null) {
            return;
        }

        AbstractTreeViewer viewer = (AbstractTreeViewer)page.getViewer();

        Set<Object> toRemove = new HashSet<>();
        Set<IIpsElement> toAdd = new HashSet<>();
        Set<Object> toUpdate = new HashSet<>();
        for (Object updatedObject : updatedElements) {
            IIpsElement updatedElement = null;
            if (updatedObject instanceof Object[] objects) {
                if (objects[0] instanceof IIpsElement) {
                    updatedElement = (IIpsElement)objects[0];
                }

            }
            if (updatedObject instanceof IIpsElement) {
                updatedElement = (IIpsElement)updatedObject;
            }

            if (updatedElement == null) {
                continue;
            }

            if (page.getDisplayedMatchCount(updatedObject) > 0) {
                if (viewer.testFindItem(updatedElement) != null) {
                    toUpdate.add(updatedElement);
                } else {
                    toAdd.add(updatedElement);
                }
            } else {
                toRemove.add(updatedElement);
            }
        }

        doRemove(viewer, toRemove);
        doAdd(viewer, toAdd);
        doUpdate(viewer, toUpdate);
    }

    private void doRemove(AbstractTreeViewer viewer, Set<Object> toRemove) {
        if (toRemove.size() > 0) {
            viewer.remove(searchResult, toRemove.toArray());
        }
    }

    private void doUpdate(AbstractTreeViewer viewer, Set<Object> toUpdate) {
        for (Object element : toUpdate) {
            viewer.refresh(element);
        }
    }

    private void doAdd(AbstractTreeViewer viewer, Set<IIpsElement> toAdd) {
        for (IIpsElement element : toAdd) {
            add(viewer, element);
        }
    }

    private void add(AbstractTreeViewer viewer, IIpsElement element) {
        IIpsElement parent = getParentOfIpsElement(element);
        if (parent != null && viewer.testFindItem(parent) == null) {
            add(viewer, parent);
        }

        ipsElementTree.computeIfAbsent(element, $ -> new IpsElementSearchTreeNode());

        if (parent != null) {
            ipsElementTree.get(parent).addChild(element);
            viewer.add(parent, element);
        } else {
            viewer.add(searchResult, element);
        }

    }

    protected void clear() {
        initialize(searchResult);
        page.getViewer().refresh();
    }

    private static class IpsElementSearchTreeNode {

        private final Set<IIpsElement> children;

        protected IpsElementSearchTreeNode() {
            children = new HashSet<>();
        }

        protected void addChild(IIpsElement child) {
            children.add(child);
        }

        protected Set<IIpsElement> getChildren() {
            return Collections.unmodifiableSet(children);
        }

        protected boolean hasChildren() {
            return !children.isEmpty();
        }

    }
}
