/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.ipsproject;

/**
 * Get and set the <code>IIpsPackageFragmentSortDefinition</code>. The sort definition is created
 * manually by the user and stored in the file system.
 * <p>
 * The qualified packages name matches a directory hierarchy in the file system. Each package
 * segment name refers to a directory. The sort order file contains the folders in the parent
 * directory.
 * <p>
 * <blockquote>
 * 
 * <pre>
 * example:
 * product.home.coverage => product/home/coverage
 * product.home.tables => product/home/tables
 * </pre>
 * 
 * </blockquote>
 * <p>
 * <blockquote>
 * 
 * <pre>
 * product/home/SORT_ORDER_FILE_NAME:
 * # comment
 * coverage
 * tables
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Markus Blum
 */
public interface IIpsPackageFragmentArbitrarySortDefinition extends IIpsPackageFragmentSortDefinition {

    /**
     * Get the package sort order as string. A segment is a part of the qualified
     * {@link IIpsPackageFragment} name.
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
