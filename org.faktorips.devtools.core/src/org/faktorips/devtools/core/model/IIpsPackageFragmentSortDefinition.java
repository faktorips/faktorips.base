/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
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

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;


/**
 * Sort <code>IIpsPackageFragment</code>s by qualified name.
 * <p>
 * In order to load the IIpsPackageFragmentSortDefinition call <code>initPersistenceConent</code>:
 * <p>
 * <blockquote><pre>
 *  IpsPackageFragmentSortDefinition sortDef = new IpsPackageFragmentSortDefinition();
 *  sortDef.initPersistenceConent(content);
 * </pre></blockquote>
 * <p>
 * For saving the IpsPackageFragmentSortDefinition call <code>initPersistenceConent</code> and write the result to a file or database:
 * <blockquote><pre>
 * String content = sortDef.toPersistenceContent();
 * IFile file = folder.getFile(SORT_ORDER_FILE);
 * byte[] bytes = content.getBytes();
 * ByteArrayInputStream is= new ByteArrayInputStream(bytes);
 * file.create(is, false, null);
 * </pre></blockquote>
 * <p>
 *
 * @author Markus Blum
 */
public interface IIpsPackageFragmentSortDefinition {

    /**
     * Compare two segments of a qualified <code>IIpsPackageFragment</code> name.
     *
     * @param segment1 the first segment to be compared.
     * @param segment2 the second segment to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    public int compare(String segment1, String segment2);

    /**
     * Get current sort order configuration as <code>String</code>
     *
     * @return Sort order of <code>IIpsPackageFragment</code> as <code>String</code>.
     */
    public String toPersistenceContent();

    /**
     * Load the sort order <code>content</code>.
     *
     * @param content Sort order file as <code>String</code>.
     *
     * @throws CoreException
     */
    public void initPersistenceContent(String content) throws CoreException;

    /**
     * Create a deep copy of the current sort order.
     *
     * @return Create a copy of the current instance.
     */
    public IIpsPackageFragmentSortDefinition copy();

}
