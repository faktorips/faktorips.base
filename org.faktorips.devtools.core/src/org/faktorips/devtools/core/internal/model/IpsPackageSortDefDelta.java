/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * A simple Valueobject containing {@link IpsPackageFragment}s and their {@link IIpsPackageFragmentSortDefinition}.
 *
 * @author Markus Blum
 */
public class IpsPackageSortDefDelta {

    private List fragments;
    private List sortdDefs;

    /**
     * Create a new instance for changed sort orders in {@link IIpsProject}.
     *
     * Save changed sort orders in a list. The index of sort order in <code>sortOrdersNew</code> has to be
     * identical to <code>packages</code>. For each sort order exists only one entry of IIpsPackageFragment in the <code>packages</code> list (per parent).
     *
     * TODO Change content of <code>packages</code> to parent.
     *
     * @param packages List with {@link IIpsPackageFragment}s.
     * @param sortOrdersNew List with changed sort orders.
     */
    public IpsPackageSortDefDelta(IIpsPackageFragment[] packages, IIpsPackageFragmentSortDefinition[] sortOrdersNew) {

        // preconditions
        Assert.isNotNull(packages);
        Assert.isNotNull(sortOrdersNew);
        Assert.isTrue(packages.length == sortOrdersNew.length);

        this.fragments = new ArrayList(packages.length);
        this.sortdDefs = new ArrayList(sortOrdersNew.length);

        fragments.addAll(Arrays.asList(packages));
        sortdDefs.addAll(Arrays.asList(sortOrdersNew));
    }

    /**
     * Write delta to the filesystem.
     *
     * @throws CoreException
     *
     */
    public void fix() throws CoreException {

        Iterator iterSortdDefs = sortdDefs.iterator();
        for (Iterator iterFragment = fragments.iterator(); iterFragment.hasNext(); ) {
            IIpsPackageFragment element = (IIpsPackageFragment)iterFragment.next();
            element.setSortDefinition((IIpsPackageFragmentSortDefinition)iterSortdDefs.next());
        }
    }

}
