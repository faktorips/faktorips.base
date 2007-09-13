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
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentDefaultSortDefinition;
import org.faktorips.devtools.core.internal.model.IpsPackageSortDefDelta;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 *
 * @author Markus Blum
 */
public class IpsProjectSortOrdersPM {

    private IIpsProject project;

    // Lookuptable parent IIpsPackageFragment name -> children sorted
    private Map fragmentHierarchy = new HashMap();

    public IpsProjectSortOrdersPM(IIpsProject project) {
        this.project = project;
    }

    public void moveOneUp(IIpsPackageFragment fragment) {
        moveUp(fragment, 1);
    }

    public void moveOneDown(IIpsPackageFragment fragment) {
        moveDown(fragment, 1);
    }

    public void moveUp(IIpsPackageFragment fragment, int shift) {
        // don't move DefaultPackageFragments
        if ((shift > 0) && (!fragment.isDefaultPackage())) {

            IIpsPackageFragment parent = fragment.getParentIpsPackageFragment();

            if (fragmentHierarchy.containsKey(parent)) {
                ArrayList list = (ArrayList)fragmentHierarchy.get(parent);

                Assert.isNotNull(list);

                int pos = list.indexOf(fragment);
                IIpsPackageFragment shiftObj = (IIpsPackageFragment)list.remove(pos);
                int newPos = pos - shift;
                list.add((newPos > 0) ? newPos : 0, shiftObj);
            }
        }
    }

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
     * @return
     * @throws CoreException
     */
    public Object[] getDefaultPackageFragments() throws CoreException {

        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        List filtered = new ArrayList(roots.length);

        // TODO Remove this line, if IpsArchive's are sorted.
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].isBasedOnSourceFolder()) {
                filtered.add(roots[i].getDefaultIpsPackageFragment());
            }
        }
        return (IIpsPackageFragment[])filtered.toArray(new IIpsPackageFragment[filtered.size()]);
    }

    /**
     * @param fragment
     * @return
     * @throws CoreException
     */
    public Object[] getChildIpsPackageFragments(IIpsPackageFragment parent) throws CoreException {
        IIpsPackageFragment[] fragments;

        if (fragmentHierarchy.containsKey(parent)) {
            List list = (List)fragmentHierarchy.get(parent);
            fragments = (IIpsPackageFragment[])list.toArray(new IIpsPackageFragment[list.size()]);
        } else {
            fragments = parent.getChildIpsPackageFragments();

            List list = new ArrayList();
            list.addAll(Arrays.asList(fragments));
            fragmentHierarchy.put(parent, list);
        }

        return fragments;
    }

    /**
     *
     */
    public void restore() {
        fragmentHierarchy.clear();
    }

    /**
     * @param fragment
     * @return
     */
    public IIpsPackageFragmentSortDefinition toSortDefinition(IIpsPackageFragment fragment) {

        /*
         * transform key: fragmentHierarchy uses parent as key! we want to build a
         * IIpsPackageFragmentSortDefinition object of the current fragment: the fragment is a
         * member of the SortDefinition.
         */
        IIpsPackageFragment parent;
        if (fragment.isDefaultPackage()) {
            parent = fragment;
        } else {
            parent = fragment.getParentIpsPackageFragment();
        }

        List children = (List)fragmentHierarchy.get(parent);

        // TODO check error here?
        if (children != null) {
            String[] fragmentNames = new String[children.size()];
            int i = 0;

            for (Iterator iter = children.iterator(); iter.hasNext();) {
                IIpsPackageFragment element = (IIpsPackageFragment)iter.next();
                fragmentNames[i++] = QNameUtil.getUnqualifiedName(element.getName());
            }

            IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
            sortDef.setSegmentNames(fragmentNames);
            return sortDef;
        }

        return new IpsPackageFragmentDefaultSortDefinition();
    }

    /**
     * @return
     * @throws CoreException
     */
    public IpsPackageSortDefDelta createSortDefDelta() throws CoreException {
        IpsPackageSortDefDelta delta = new IpsPackageSortDefDelta();
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();

        for (int i = 0; i < roots.length; i++) {
            // TODO Don't ignore ipsArchives.
            if (roots[i].isBasedOnSourceFolder()) {
                IIpsPackageFragment currentPack = roots[i].getDefaultIpsPackageFragment();

                compare(currentPack, currentPack.getSortDefinition(), delta);
            }
        }

        return delta;
    }

    /**
     * @param pack
     * @param currentSortDef
     * @param delta
     * @throws CoreException
     */
    private void compare(IIpsPackageFragment pack, IIpsPackageFragmentSortDefinition currentSortDef, IpsPackageSortDefDelta delta) throws CoreException {
        // check current objects first, then descent the hierarchy tree.

        IIpsPackageFragmentSortDefinition newSortDef = toSortDefinition(pack);
        if (!newSortDef.equals(currentSortDef)) {
            delta.add(pack, newSortDef);
        }

        // recursion
        IIpsPackageFragment[] children = pack.getChildIpsPackageFragments();

        for (int i = 0; i < children.length; i++) {
            IIpsPackageFragment currentPack = children[i];
            compare(currentPack, currentPack.getSortDefinition(), delta);
        }
    }

}
