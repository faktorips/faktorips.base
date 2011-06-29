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

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentArbitrarySortDefinition;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageNameComparator;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * Presentationmodel for {@link IpsPackageSortDefDialog}.
 * <p>
 * Save the {@link IIpsPackageFragment} hierarchy in a Map: key = parent; value = Array of children
 * <p>
 * The methods getChildIpsPackageFragments and getDefautlPackageFragments are used for the
 * {@link IpsPackageSortDefContentProvider}. The moveXXX methods are used for shifting a child
 * element (IIpsPackageFragment).
 * 
 * @author Markus Blum
 */
public class IpsProjectSortOrdersPM implements ITreeContentProvider {

    protected static final Object[] EMPTY_ARRAY = new Object[0];

    private IIpsProject project;

    /** Lookup table parent IIpsPackageFragment -> children sorted */
    private Map<IIpsPackageFragment, List<IIpsPackageFragment>> fragmentHierarchy = new HashMap<IIpsPackageFragment, List<IIpsPackageFragment>>();

    /** Set default sort order or not */
    private boolean restoreDefault;

    public IpsProjectSortOrdersPM(IIpsProject project) {
        Assert.isNotNull(project);
        this.project = project;
        restoreDefault = false;
    }

    /**
     * Move a IIpsPackageFragment 1 position up in the child hierarchy.
     * 
     * @param fragment The selected {@link IIpsPackageFragment}.
     */
    public void moveOneUp(IIpsPackageFragment fragment) {
        moveUp(fragment, 1);
    }

    /**
     * Move a IIpsPackageFragment 1 position down in the child hierarchy.
     * 
     * @param fragment The selected {@link IIpsPackageFragment}.
     */
    public void moveOneDown(IIpsPackageFragment fragment) {
        moveDown(fragment, 1);
    }

    /**
     * Move a IIpsPackageFragment <code>shift</code> positions up in the child hierarchy.
     * IpsDefaultPackageFragments are not allowed to be moved!
     * 
     * @param fragment The selected {@link IIpsPackageFragment}.
     * @param shift Move <code>shift</code> positions up. <code>shift</code> hast to be greater than
     *            <code>0</code>.
     */
    public void moveUp(IIpsPackageFragment fragment, int shift) {
        restoreDefault = false;
        // don't move DefaultPackageFragments
        if ((shift > 0) && (!fragment.isDefaultPackage())) {

            IIpsPackageFragment parent = fragment.getParentIpsPackageFragment();
            if (fragmentHierarchy.containsKey(parent)) {
                List<IIpsPackageFragment> list = fragmentHierarchy.get(parent);

                int pos = list.indexOf(fragment);
                IIpsPackageFragment shiftObj = list.remove(pos);
                int newPos = pos - shift;
                list.add((newPos > 0) ? newPos : 0, shiftObj);
            }
        }
    }

    /**
     * Returns <code>true</code> if the given {@link IIpsPackageFragment} is the first one in its
     * parent and thus no moveUp-Operation is possible. Returns <code>false</code> if the package
     * fragment is not the first or if no state can be determined (e.g. package fragment is not part
     * of this order's package hierarchy).
     * 
     * @param fragment the fragment
     * @return <code>true</code> if the fragment is the first in its parent, <code>false</code> if
     *         it isn't or if no state can be determined.
     */
    public boolean isFirstInParent(IIpsPackageFragment fragment) {
        IIpsPackageFragment parent = fragment.getParentIpsPackageFragment();
        if (fragmentHierarchy.containsKey(parent)) {
            List<IIpsPackageFragment> list = fragmentHierarchy.get(parent);
            return list.isEmpty() ? false : list.indexOf(fragment) == 0;
        } else {
            return false;
        }
    }

    /**
     * Returns <code>true</code> if the given {@link IIpsPackageFragment} is the last one in its
     * parent and thus no moveDown-Operation is possible. Returns <code>false</code> if the package
     * fragment is not the last or if no state can be determined (e.g. package fragment is not part
     * of this order's package hierarchy).
     * 
     * @param fragment the fragment
     * @return <code>true</code> if the fragment is the last in its parent, <code>false</code> if it
     *         isn't or if no state can be determined.
     */
    public boolean isLastInParent(IIpsPackageFragment fragment) {
        IIpsPackageFragment parent = fragment.getParentIpsPackageFragment();
        if (fragmentHierarchy.containsKey(parent)) {
            List<IIpsPackageFragment> list = fragmentHierarchy.get(parent);
            return list.isEmpty() ? false : list.indexOf(fragment) == (list.size() - 1);
        } else {
            return false;
        }
    }

    /**
     * Move a IIpsPackageFragment <code>shift</code> positions down in the child hierarchy. v are
     * not allowed to be moved!
     * 
     * @param fragment The selected {@link IIpsPackageFragment}.
     * @param shift Move <code>shift</code> positions down. <code>shift</code> hast to be greater
     *            than <code>0</code>.
     */
    public void moveDown(IIpsPackageFragment fragment, int shift) {
        restoreDefault = false;
        // don't move DefaultPackageFragments
        if ((shift > 0) && (!fragment.isDefaultPackage())) {

            IIpsPackageFragment parent = fragment.getParentIpsPackageFragment();

            if (fragmentHierarchy.containsKey(parent)) {
                List<IIpsPackageFragment> list = fragmentHierarchy.get(parent);

                Assert.isNotNull(list);

                int pos = list.indexOf(fragment);
                IIpsPackageFragment shiftObj = list.remove(pos);
                int newPos = ((pos + shift) < list.size()) ? (pos + shift) : list.size();
                list.add(newPos, shiftObj);
            }
        }
    }

    /**
     * Modelaction for ITreeContentProvider.getElements. Get all IpsDefaultPackageFragments
     * (substitute for IpsPackageFragmentRoot) of the selected IpsProject.
     * 
     * @note IpsArchives are ignored!
     * @return IpsDefaultPackageFragments of the selected IpsProject.
     */
    private Object[] getDefaultPackageFragments() throws CoreException {

        // roots are not sorted
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        List<IIpsPackageFragment> filtered = new ArrayList<IIpsPackageFragment>(roots.length);

        for (IIpsPackageFragmentRoot root : roots) {
            if (root.isBasedOnSourceFolder()) {
                filtered.add(root.getDefaultIpsPackageFragment());
            }
        }
        return filtered.toArray(new IIpsPackageFragment[filtered.size()]);
    }

    /**
     * Modelaction for ITreeContentProvider.getChildren. Get all children of the IIpsPackageFragment
     * <code>fragment</code>.
     * 
     * @param parent Parent IIpsPackageFragment.
     * @return All children of IIpsPackageFragment <code>fragment</code>.
     */
    private Object[] getChildIpsPackageFragments(IIpsPackageFragment parent) throws CoreException {
        IIpsPackageFragment[] fragments;

        if (fragmentHierarchy.containsKey(parent)) {
            List<IIpsPackageFragment> list = fragmentHierarchy.get(parent);
            fragments = list.toArray(new IIpsPackageFragment[list.size()]);
        } else {

            if (restoreDefault == false) {
                fragments = parent.getSortedChildIpsPackageFragments();
            } else {
                IIpsPackageFragment[] unsortedFragments = parent.getChildIpsPackageFragments();
                List<IIpsPackageFragment> unsortedList = Arrays.asList(unsortedFragments);

                IpsPackageNameComparator comparator = new IpsPackageNameComparator(true);
                Collections.sort(unsortedList, comparator);

                fragments = unsortedList.toArray(new IIpsPackageFragment[unsortedList.size()]);
            }

            List<IIpsPackageFragment> list = new ArrayList<IIpsPackageFragment>();
            list.addAll(Arrays.asList(fragments));
            fragmentHierarchy.put(parent, list);
        }

        return fragments;
    }

    /**
     * Modelaction for restorePressed. Sort the IIpsPackageFragments by default sort order.
     */
    public void restore() {
        restoreDefault = true;
        // force building the treeViewer with new sort order.
        fragmentHierarchy.clear();
    }

    /**
     * Check the projects sort order for changes. The result is a delta between the cached sort
     * definitions and the dialog model.
     * 
     * @param packagesList Add the IIpsPackageFragment <code>pack</code> to list if its sort order
     *            has changed.
     * @param sortDefOrderList Add the new sort order IIPsPackageFragmentSortDefinition for
     *            <code>pack</code>.
     * @param restore Create delta for restore default sort order.
     */
    private void checkSortOrder(IIpsPackageFragment parent,
            List<IIpsPackageFragment> packagesList,
            List<IIpsPackageFragmentSortDefinition> sortDefOrderList,
            boolean restore) throws CoreException {

        IIpsPackageFragment[] children = parent.getSortedChildIpsPackageFragments();

        if (restore == false) {
            // create delta
            List<IIpsPackageFragment> sortDefNew = fragmentHierarchy.get(parent);

            /*
             * sortDefNew may be null because the cache depends on changes in the treeviewer. The
             * cached elements are compared with the resources in the filesystem.
             */

            // check current package first, then descent the hierarchy tree.
            if ((sortDefNew != null) && (!isEqualSortOrder(sortDefNew, Arrays.asList(children)))) {
                /*
                 * the IIpsPackgeFragment method setSortDefinition excpects the changed package as
                 * argument. We have to save a child here and not the parent node.
                 */
                packagesList.add(children[0]);
                sortDefOrderList.add(toSortDefinition(sortDefNew));
            }
        } else {
            // force deletion of sort order files.
            if (children.length > 0) {
                packagesList.add(children[0]);
                sortDefOrderList.add(null);
            }
        }

        // traverse tree
        for (IIpsPackageFragment currentPack : children) {
            checkSortOrder(currentPack, packagesList, sortDefOrderList, restore);
        }
    }

    /**
     * Create a new IIpsPackageFragmentSortDefinition object.
     * 
     * @return new IIpsPackageFragmentSortDefinition
     */
    private IIpsPackageFragmentSortDefinition toSortDefinition(List<IIpsPackageFragment> newSortDef) {
        String[] fragmentNames = new String[newSortDef.size()];
        int i = 0;

        for (IIpsPackageFragment element : newSortDef) {
            fragmentNames[i++] = QNameUtil.getUnqualifiedName(element.getName());
        }

        IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
        sortDef.setSegmentNames(fragmentNames);
        return sortDef;
    }

    /**
     * Compare two sort orders.
     * 
     * @param sortDefNew New sort order.
     * @param sortDefOld sort order from the cache.
     * @return <code>true</code> if sort order is equal.
     */
    private boolean isEqualSortOrder(List<IIpsPackageFragment> sortDefNew, List<IIpsPackageFragment> sortDefOld) {
        if (sortDefOld.size() != sortDefNew.size()) {
            return false;
        }

        Iterator<IIpsPackageFragment> iterSortdDefsNew = sortDefNew.iterator();
        for (IIpsPackageFragment elementOld : sortDefOld) {
            IIpsPackageFragment elementNew = iterSortdDefsNew.next();
            if (!elementNew.equals(elementOld)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void dispose() {
        fragmentHierarchy.clear();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to implement
    }

    /**
     * @note IpsArchives are ignored
     */
    public void saveSortDefDelta() throws CoreException {
        List<IIpsPackageFragment> packagesList = new ArrayList<IIpsPackageFragment>();
        List<IIpsPackageFragmentSortDefinition> sortOrdersNewList = new ArrayList<IIpsPackageFragmentSortDefinition>();

        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();

        for (IIpsPackageFragmentRoot root : roots) {
            if (root.isBasedOnSourceFolder()) {
                IIpsPackageFragment currentPack = root.getDefaultIpsPackageFragment();

                checkSortOrder(currentPack, packagesList, sortOrdersNewList, restoreDefault);
            }
        }

        Iterator<IIpsPackageFragmentSortDefinition> iterSortdDefs = sortOrdersNewList.iterator();
        for (IIpsPackageFragment element : packagesList) {
            element.setSortDefinition(iterSortdDefs.next());
        }
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)parentElement;

            try {
                return getChildIpsPackageFragments(fragment);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return EMPTY_ARRAY;
            }
        }

        return EMPTY_ARRAY;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            return fragment.getParentIpsPackageFragment();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;

            // no wrapper-sortOrderPM function needed.
            try {
                return fragment.hasChildIpsPackageFragments();
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }

        return false;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IpsProjectSortOrdersPM) {
            IpsProjectSortOrdersPM sortOrderPO = (IpsProjectSortOrdersPM)inputElement;

            try {
                return sortOrderPO.getDefaultPackageFragments();
            } catch (CoreException e) {
                return EMPTY_ARRAY;
            }
        }

        return EMPTY_ARRAY;
    }

}
