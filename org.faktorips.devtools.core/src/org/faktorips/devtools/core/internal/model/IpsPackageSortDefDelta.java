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

/**
 *
 * @author Markus Blum
 */
public class IpsPackageSortDefDelta {

    private List fragments;
    private List sortdDefs;

    /**
     * @param packages
     * @param sortOrdersNew
     */
    public IpsPackageSortDefDelta(IIpsPackageFragment[] packages, IIpsPackageFragmentSortDefinition[] sortOrdersNew) {

        Assert.isNotNull(packages);
        Assert.isNotNull(sortOrdersNew);

        this.fragments = new ArrayList(packages.length);
        this.sortdDefs = new ArrayList(sortOrdersNew.length);

        fragments.addAll(Arrays.asList(packages));
        sortdDefs.addAll(Arrays.asList(sortOrdersNew));
    }

    /**
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
