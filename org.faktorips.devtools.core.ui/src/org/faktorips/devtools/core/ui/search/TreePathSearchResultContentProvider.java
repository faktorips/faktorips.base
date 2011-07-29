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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

/**
 * @author Thorsten Guenther
 */
public class TreePathSearchResultContentProvider implements ITreeContentProvider, ISearchResultListener {

    private static class IpsElementSearchTreeNode {

        private final Set<IIpsElement> children;

        protected IpsElementSearchTreeNode() {
            this.children = new HashSet<IIpsElement>();
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

    private IpsElementsSearchViewPage page;
    private ISearchResult searchResult;

    private Map<IIpsElement, IpsElementSearchTreeNode> ipsElementTree;

    public TreePathSearchResultContentProvider(IpsElementsSearchViewPage page) {
        this.page = page;
    }

    @Override
    public void searchResultChanged(SearchResultEvent e) {
        if (e instanceof RemoveAllEvent) {
            ipsElementTree = new HashMap<IIpsElement, TreePathSearchResultContentProvider.IpsElementSearchTreeNode>();
            return;
        }
        if (e instanceof MatchEvent) {
            MatchEvent matchEvent = (MatchEvent)e;
            if (matchEvent.getKind() == MatchEvent.ADDED) {
                addMatches(matchEvent.getMatches());
            } else {
                // TODO removeElements(matchEvent.getMatches());
            }
            return;
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

        IIpsElement parent = getIpsElementParent(element);

        if (parent != null) {
            addMatchedElement(parent, element);
        }

        IpsElementSearchTreeNode ipsElementSearchTreeNode = ipsElementTree.get(element);
        if (ipsElementSearchTreeNode == null) {
            ipsElementSearchTreeNode = new IpsElementSearchTreeNode();
            ipsElementTree.put(element, ipsElementSearchTreeNode);
        }

        if (child != null) {
            ipsElementSearchTreeNode.addChild(child);
        }
    }

    protected IIpsElement getIpsElementParent(IIpsElement element) {
        if (element instanceof IIpsProject) {
            return null;
        }
        if (element instanceof IIpsObjectPart) {
            IIpsObjectPart ipsObjectPart = (IIpsObjectPart)element;
            return ipsObjectPart.getIpsObject();
        }
        if (element instanceof IProductCmptGeneration) {
            return ((IProductCmptGeneration)element).getProductCmpt();
        }
        if (element instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)element;
            return ipsSrcFile.getIpsPackageFragment();
        }
        if (element instanceof IIpsPackageFragment) {
            return ((IIpsPackageFragment)element).getIpsProject();
        }
        if (element instanceof IIpsObject) {
            IIpsObject ipsSrcFile = (IIpsObject)element;
            return ipsSrcFile.getIpsPackageFragment();
        }

        return null;
    }

    private synchronized void initialize(ISearchResult result) {
        this.searchResult = result;

        if (searchResult == null) {
            return;
        }

        ipsElementTree = new HashMap<IIpsElement, TreePathSearchResultContentProvider.IpsElementSearchTreeNode>();
        addElements(((AbstractTextSearchResult)result).getElements());

    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (ipsElementTree.containsKey(parentElement)) {
            Object[] children = ipsElementTree.get(parentElement).getChildren().toArray();
            return children;
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IIpsElement) {
            IIpsElement ipsElementParent = getIpsElementParent((IIpsElement)element);
            return ipsElementParent;
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (ipsElementTree.containsKey(element)) {
            boolean hasChildren = ipsElementTree.get(element).hasChildren();
            return hasChildren;
        }
        return false;
    }

    @Override
    public void dispose() {
        // nothing to do.
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof AbstractTextSearchResult) {
            AbstractTextSearchResult searchResult = (AbstractTextSearchResult)inputElement;
            return searchResult.getElements();
        }
        // in Eclipse 3.3 this method will always retrurn an empty array because the elementsChanged
        // Method populates the elements depending on the match filter to the view
        return new Object[0];
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        initialize((ISearchResult)newInput);

    }

    public synchronized void elementsChanged(Object[] updatedElements) {
        if (searchResult == null) {
            return;
        }

        AbstractTreeViewer viewer = (AbstractTreeViewer)page.getViewer();

        Set<Object> toRemove = new HashSet<Object>();
        Set<IIpsElement> toAdd = new HashSet<IIpsElement>();
        Set<Object> toUpdate = new HashSet<Object>();
        for (Object updatedObject : updatedElements) {
            IIpsElement updatedElement = null;
            if (updatedObject instanceof Object[]) {
                Object[] objects = (Object[])updatedObject;
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

        if (toRemove.size() > 0) {
            viewer.remove(searchResult, toRemove.toArray());
        }

        for (IIpsElement element : toAdd) {

            add(viewer, element);
        }

        for (Object element : toUpdate) {
            viewer.refresh(element);
        }
    }

    protected void add(AbstractTreeViewer viewer, IIpsElement element) {
        IIpsElement parent = getIpsElementParent(element);
        if (parent != null && viewer.testFindItem(parent) == null) {
            add(viewer, parent);
        }

        if (!ipsElementTree.containsKey(element)) {
            ipsElementTree.put(element, new IpsElementSearchTreeNode());
        }

        if (parent != null) {
            ipsElementTree.get(parent).addChild(element);
            viewer.add(parent, element);
        } else {
            viewer.add(searchResult, element);
        }

    }

    public void clear() {
        initialize(searchResult);
        page.getViewer().refresh();
    }

    /**
     * @return Returns the searchResult.
     */
    public ISearchResult getSearchResult() {
        return searchResult;
    }

}
