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

import java.util.Comparator;

import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 *
 * @author Markus Blum
 */
public class IpsPackageNameComparator implements Comparator {

    /**
     *
     */
    public IpsPackageNameComparator () {
    }

    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2) {
        IIpsPackageFragment pack1 = (IIpsPackageFragment)o1;
        IIpsPackageFragment pack2 = (IIpsPackageFragment)o2;

        String[] segments1 = QNameUtil.getSegments(pack1.getName());
        String[] segments2 = QNameUtil.getSegments(pack2.getName());

         int length = 0;

        // Compare two IIpsPackageFragments by qualified name and level
        if (segments1.length <= segments2.length) {
            length = segments1.length;
        } else {
            length = segments2.length;
        }

        for (int i = 0; i < length; i++) {

            IIpsPackageFragmentSortDefinition sortDef = getSortDefinition(pack1, QNameUtil.getSubSegments(pack1.getName(), i+1));

            int c = comparePackages(sortDef, segments1[i], segments2[i]);

            if (c!=0) {
                return c;
            }
        }

        return 0;
    }

    /**
     * Get the <code>IIpsPackageFragmentSortDefinition</code> from parent.
     *
     * @param pack <code>IIpsPackageFragment</code> current.
     * @return SortDefinition
     */
    private IIpsPackageFragmentSortDefinition getSortDefinition(IIpsPackageFragment pack, String subSegmentName) {
        IpsPackageFragment subPack = new IpsPackageFragment(pack.getRoot(), subSegmentName);

        IIpsPackageFragmentSortDefinition sortDef = ((IpsPackageFragment) subPack).getSortDefinitionInternal();

        return sortDef;
    }

    /**
     * Compare 2 segments of a package. Don't read the sort order if the segments are identical:
     * the segments are part of the same package path (folder) so we don't have to ask for the sort order of containing
     * <code>IIpsPackageFragment</code>s.
     *
     * @param sortDef Sortdefinition of <code>IIpsPackageFragment</code>s.
     * @param segmentName1 Segment (Folder) name of a qualified package name.
     * @param segmentName2 Segment (Folder) name of a qualified package name.
     * @return  a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second
     */
    private int comparePackages(IIpsPackageFragmentSortDefinition sortDef, String segmentName1, String segmentName2) {

        if (sortDef==null) {
            return segmentName1.compareTo(segmentName2);
        }

        if (segmentName1.equals(segmentName2)) {
            return 0;
        } else {
            return sortDef.compare(segmentName1, segmentName2);
        }
    }
}
