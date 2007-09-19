/***************************************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 *
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 *
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentArbitrarySortDefinition;
import org.faktorips.devtools.core.internal.model.IpsPackageSortDefDelta;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * Presentationmodel for {@link IpsPackageSortDefDialog}.
 *
 * Save the {@link IIpsPackageFragment} hierarchy in a Map:
 * key = parent; value = Array of children
 *
 * The methods getChildIpsPackageFragments and getDefautlPackageFragments are used for the {@link IpsPackageSortDefContentProvider}.
 * The moveXXX methods are used for shifting a child element (IIpsPackageFragment).
 *
 * @author Markus Blum
 */
public class IpsProjectSortOrdersPM {

    private IIpsProject project;

    // Lookup table parent IIpsPackageFragment -> children sorted
    private Map fragmentHierarchy = new HashMap();

    public IpsProjectSortOrdersPM(IIpsProject project) {
        Assert.isNotNull(project);
        this.project = project;
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
     * Move a IIpsPackageFragment <code>shift</code> positions up in the child hierarchy. IpsDefaultPackageFragments are not allowed to be moved!
     *
     * @param fragment The selected {@link IIpsPackageFragment}.
     * @param shift Move <code>shift</code> positions up. <code>shift</code> hast to be greater than <code>0</code>.
     */
    public void moveUp(IIpsPackageFragment fragment, int shift) {
        // don't move DefaultPackageFragments
        if ((shift > 0) && (!fragment.isDefaultPackage())) {

            IIpsPackageFragment parent = fragment.getParentIpsPackageFragment();
            if (fragmentHierarchy.containsKey(parent)) {
                ArrayList list = (ArrayList)fragmentHierarchy.get(parent);

                int pos = list.indexOf(fragment);
                IIpsPackageFragment shiftObj = (IIpsPackageFragment)list.remove(pos);
                int newPos = pos - shift;
                list.add((newPos > 0) ? newPos : 0, shiftObj);
            }
        }
    }

    /**
     * Move a IIpsPackageFragment <code>shift</code> positions down in the child hierarchy. v are not allowed to be moved!
     *
     * @param fragment The selected {@link IIpsPackageFragment}.
     * @param shift Move <code>shift</code> positions down. <code>shift</code> hast to be greater than <code>0</code>.
     */
    public void moveDown(IIpsPackageFragment fragment, int shift) {
        // don't move DefaultPackageFragments
        if ((shift > 0) && (!fragment.isDefaultPackage())) {

            IIpsPackageFragment parent = fragment.getParentIpsPackageFragment();

            if (fragmentHierarchy.containsKey(parent)) {
                ArrayList list = (ArrayList)fragmentHierarchy.get(parent);

                Assert.isNotNull(list);

                int pos = list.indexOf(fragment);
                IIpsPackageFragment shiftObj = (IIpsPackageFragment)list.remove(pos);
                int newPos = ((pos + shift) < list.size()) ? (pos + shift) : list.size();
                list.add(newPos, shiftObj);
            }
        }
    }

    /**
     * Modelaction for ITreeContentProvider.getElements.
     * Get all IpsDefaultPackageFragments (substitute for IpsPackageFragmentRoot) of the
     * selected IpsProject.
     *
     * @note IpsArchives are ignored!
     *
     * @return IpsDefaultPackageFragments of the selected IpsProject.
     * @throws CoreException
     */
    public Object[] getDefaultPackageFragments() throws CoreException {

        // roots are not sorted
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        List filtered = new ArrayList(roots.length);

        // TODO Don't ignore ipsArchives.
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].isBasedOnSourceFolder()) {
                filtered.add(roots[i].getDefaultIpsPackageFragment());
            }
        }
        return (IIpsPackageFragment[])filtered.toArray(new IIpsPackageFragment[filtered.size()]);
    }

    /**
     * Modelaction for ITreeContentProvider.getChildren.
     * Get all children of the IIpsPackageFragment <code>fragment</code>.
     *
     * @param fragment Parent IIpsPackageFragment.
     * @return All children of IIpsPackageFragment <code>fragment</code>.
     * @throws CoreException
     */
    public Object[] getChildIpsPackageFragments(IIpsPackageFragment parent) throws CoreException {
        IIpsPackageFragment[] fragments;

        if (fragmentHierarchy.containsKey(parent)) {
            List list = (List)fragmentHierarchy.get(parent);
            fragments = (IIpsPackageFragment[])list.toArray(new IIpsPackageFragment[list.size()]);
        } else {
            fragments = parent.getSortedChildIpsPackageFragments();

            List list = new ArrayList();
            list.addAll(Arrays.asList(fragments));
            fragmentHierarchy.put(parent, list);
        }

        return fragments;
    }

    /**
     * Modelaction for restorePressed. Sort the IIpsPackageFragments by default sort order.
     *
     * @throws CoreException
     */
    public void restore() throws CoreException {
       // TODO Set default sort order
       fragmentHierarchy.clear();
     }

    /**
     * Modelaction for okPressed.
     *
     * Check if sort order has changed. Create the delegate object IpsPackageSortDefDelta for saving the changes.
     *
     * Delete the current sort orders of the IpsProject if restore is <code>true</code> => default sort order.
     *
     * @return IpsPackageSortDefDelta with changed sort orders.
     * @throws CoreException
     */
    public IpsPackageSortDefDelta createSortDefDelta(boolean restore) throws CoreException {
        List packagesList = new ArrayList();
        List sortOrdersNewList = new ArrayList();

        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();

        for (int i = 0; i < roots.length; i++) {
            // TODO Don't ignore ipsArchives.
            if (roots[i].isBasedOnSourceFolder()) {
                IIpsPackageFragment currentPack = roots[i].getDefaultIpsPackageFragment();

                checkSortOrder(currentPack, packagesList, sortOrdersNewList, restore);
            }
        }

        IIpsPackageFragment[] packages = (IIpsPackageFragment[])packagesList.toArray(new IIpsPackageFragment[packagesList.size()]);
        IIpsPackageFragmentSortDefinition[] sortDefs = (IIpsPackageFragmentSortDefinition[])sortOrdersNewList.toArray(new IIpsPackageFragmentSortDefinition[sortOrdersNewList.size()]);

        return new IpsPackageSortDefDelta(packages, sortDefs);
    }

    /**
     * Check the projects sort order for changes. The result is a delta between the cached sort definitions and the dialog model.
     *
     * @param pack Current IIpsPackageFragment.
     * @param packagesList Add the IIpsPackageFragment <code>pack</code> to list if its sort order has changed.
     * @param sortDefOrderList Add the new sort order IIPsPackageFragmentSortDefinition for <code>pack</code>.
     * @param restore Create delta for restore default sort order.
     * @throws CoreException
     */
    private void checkSortOrder(IIpsPackageFragment parent, List packagesList, List sortDefOrderList, boolean restore) throws CoreException {

        IIpsPackageFragment[] children = parent.getSortedChildIpsPackageFragments();

        if (restore == false) {
            // create delta
            List sortDefNew = (List)fragmentHierarchy.get(parent);

            /*
             * sortDefNew may be null because the cache depends on changes in the treeviewer. The cached elements are compared with the
             * resources in the filesystem.
             */

            // check current package first, then descent the hierarchy tree.
            if ((sortDefNew != null) && (!isEqualSortOrder(sortDefNew, Arrays.asList(children)))) {
                /* the IIpsPackgeFragment method setSortDefinition excpects the changed package as argument. We have to save a child here
                 * and not the parent node.
                 *
                 * TODO Check Interface get/setSortDefinition in order to change argument to parent package.
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
        for (int i = 0; i < children.length; i++) {
            IIpsPackageFragment currentPack = children[i];
            checkSortOrder(currentPack, packagesList, sortDefOrderList, restore);
        }
    }

    /**
     * Create a new IIpsPackageFragmentSortDefinition object.
     *
     * @param fragment Sorted IIpsPackageFragments.
     * @return new IIpsPackageFragmentSortDefinition
     */
    public IIpsPackageFragmentSortDefinition toSortDefinition(List newSortDef) {

        if (newSortDef == null) {
            // TODO Throw Exception here.
            return null;
        }

        String[] fragmentNames = new String[newSortDef.size()];
        int i = 0;

        for (Iterator iter = newSortDef.iterator(); iter.hasNext();) {
            IIpsPackageFragment element = (IIpsPackageFragment)iter.next();
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
    private boolean isEqualSortOrder(List sortDefNew, List sortDefOld) {

        if (sortDefOld.size() != sortDefNew.size()) {
            return false;
        }

        Iterator iterSortdDefsNew = sortDefNew.iterator();
        for (Iterator iterSortdDefsOld = sortDefOld.iterator(); iterSortdDefsOld.hasNext(); ) {
            IIpsPackageFragment elementNew = (IIpsPackageFragment) iterSortdDefsNew.next();
            IIpsPackageFragment elementOld = (IIpsPackageFragment) iterSortdDefsOld.next();

            if (!elementNew.equals(elementOld)) {
                return false;
            }

        }

        return true;
    }

}
