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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsPackageFragmentSortDefinition;

/**
 * Use lexical sort order as IpsPackageFragmentDefaultSortDefinition.
 * This sort definition is not intended to be saved to the file system.
 *
 * @author Markus Blum
 */
public class IpsPackageFragmentDefaultSortDefinition implements IIpsPackageFragmentSortDefinition {

    /**
     * {@inheritDoc}
     */
    public int compare(String segment1, String segment2) {
        return segment1.compareTo(segment2);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentSortDefinition copy() {
        return new IpsPackageFragmentDefaultSortDefinition();
    }

    /**
     * {@inheritDoc}
     */
    public void initPersistenceContent(String content, String charset) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public String toPersistenceContent() {
        return new String("");
    }

}
