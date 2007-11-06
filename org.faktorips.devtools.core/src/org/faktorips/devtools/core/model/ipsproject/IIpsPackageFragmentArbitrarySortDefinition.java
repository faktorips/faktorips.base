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

package org.faktorips.devtools.core.model.ipsproject;


/**
 * Get and set the <code>IIpsPackageFragmentSortDefinition</code>. The sort definition
 * is created manually by the user and stored in the filesystem (@see IpsPackageFragmentArbitrarySortDefinition.SORT_ORDER_FILE).
 * <p>
 * The qualified packages name matches a directory hierarchy in the filesystem. Each package segment name refers to a directory.
 * The sort order file contains the folders in the parent directory.
 * <p><
 * blockquote><pre>
 * example:
 * product.home.coverage => product/home/coverage
 * product.home.tables => product/home/tables
 * </pre></blockquote>
 * <p>
 * <blockquote><pre>
 * product/home/SORT_ORDER_FILE_NAME:
 * # comment
 * coverage
 * tables
 * </pre></blockquote>
 * <p>
 *
 * @author Markus Blum
 */
public interface IIpsPackageFragmentArbitrarySortDefinition extends IIpsPackageFragmentSortDefinition {

    /**
     * Get the package sort order as string.
     * A segment is a part of the qualified {@link IIpsPackageFragment} name.
     *
     * @return List with segment names in sort order.
     */
    public String[] getSegmentNames();

    /**
     * Set sort order of a {@link IIpsPackageFragment}.
     *
     * @param segments List with segment names in sort order.
     */
    public void setSegmentNames(String[] segments);
}
