/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Comparator;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * A Comparator implementation for IIpsPackageFragments. IIpsPackageFragments are sorted by lexical
 * comparison of the package name (<code>defaultComparator</code> = <code>true</code>) or by a sort
 * order file (<code>defaultComparator</code> = <code>false</code>).
 * 
 * @note <i>Natural ordering</i> is <i>inconsistent with equals</i>.
 * 
 * @author Markus Blum
 */
public class IpsPackageNameComparator implements Comparator<IIpsPackageFragment> {

    private boolean defaultComparator = true;

    /**
     * Set the sort mode with this constructor. A class that was instantiated with the default
     * constructor uses lexical comparison of the package name.
     * 
     * @param defaultComparator <code>true</code>, if compare is lexical (default sort order).
     */
    public IpsPackageNameComparator(boolean defaultComparator) {
        this.defaultComparator = defaultComparator;
    }

    /**
     * <i>Natural ordering</i> is <i>inconsistent with equals</i>.
     */
    @Override
    public int compare(IIpsPackageFragment pack1, IIpsPackageFragment pack2) {
        String[] segments1 = QNameUtil.getSegments(pack1.getName());
        String[] segments2 = QNameUtil.getSegments(pack2.getName());

        int length = 0;

        // Compare two IIpsPackageFragments by qualified name and level
        if (segments1.length <= segments2.length) {
            length = segments1.length;
        } else {
            length = segments2.length;
        }

        int cmp = 0;

        for (int i = 0; i < length; i++) {
            if (defaultComparator) {
                cmp = segments1[i].compareTo(segments2[i]);
            } else {
                cmp = compareBySortDefinition(pack1, segments1[i], segments2[i], i + 1);
            }

            // stop comparison if unequal
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    /**
     * Get the <code>IIpsPackageFragmentSortDefinition</code> from the parent fragment. Use
     * {@link IpsPackageFragmentDefaultSortDefinition} if no file exists for this package.
     * 
     * @param pack current <code>IIpsPackageFragment</code> .
     * @param parentName Full qualified parent package name.
     * @return SortDefinition An IIpsPackageFragmentSortDefinition implementation.
     */
    private IIpsPackageFragmentSortDefinition getSortDefinition(IIpsPackageFragment pack, String parentName) {
        IIpsPackageFragment parentPackage = pack.getRoot().getIpsPackageFragment(parentName);
        IpsModel model = getIpsModel();
        IIpsPackageFragmentSortDefinition sortDef = model.getSortDefinition(parentPackage);
        return sortDef;
    }

    /**
     * Get the full qualified parent package name of <code>childPackageName</code> of the selected
     * segment.
     * 
     * <p>
     * <blockquote>
     * 
     * <pre>
     * example:
     * String package = &quot;org.faktorips.devtools.core&quot;;
     * getParentNameOfSegment(package, 1) =&gt; &quot;org&quot;
     * getParentNameOfSegment(package, 2) =&gt; &quot;org.faktorips&quot;
     * getParentNameOfSegment(package, 3) =&gt; &quot;org.faktorips.devtools&quot;
     * 
     *</pre>
     * 
     * </blockquote>
     * 
     * @param childPackageName Name of the <code>IIpsPackageFragment</code>.
     * @param segments The selected segment hierarchy.
     * @return Full qualified parent package name.
     */
    private String getParentNameOfSegment(String childPackageName, int segments) {
        return QNameUtil.getSubSegments(childPackageName, segments);
    }

    private IpsModel getIpsModel() {
        return (IpsModel)IpsPlugin.getDefault().getIpsModel();
    }

    /**
     * Compare two IpsPackage segments by sort order.
     * 
     * @param pack1 The 1st IpsPackageFragment.
     * @param segmentName1 The current segment to check of IpsPackageFragment 1.
     * @param segmentName2 The current segment to check of IpsPackageFragment 2.
     * @param segmentNr The current segment number.
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     *         equal to, or greater than the second
     */
    private int compareBySortDefinition(IIpsPackageFragment pack1,
            String segmentName1,
            String segmentName2,
            int segmentNr) {

        IIpsPackageFragmentSortDefinition sortDef = getSortDefinition(pack1, getParentNameOfSegment(pack1.getName(),
                segmentNr));

        if (sortDef == null) {
            return segmentName1.compareTo(segmentName2);
        }

        if (segmentName1.equals(segmentName2)) {
            return 0;
        } else {
            return sortDef.compare(segmentName1, segmentName2);
        }
    }

}
