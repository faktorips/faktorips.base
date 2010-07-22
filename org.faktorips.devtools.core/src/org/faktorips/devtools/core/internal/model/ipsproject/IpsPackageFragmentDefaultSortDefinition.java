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
package org.faktorips.devtools.core.internal.model.ipsproject;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;

/**
 * Use lexical sort order as IpsPackageFragmentDefaultSortDefinition. This sort definition is not
 * intended to be saved to the file system.
 * 
 * @author Markus Blum
 */
public class IpsPackageFragmentDefaultSortDefinition implements IIpsPackageFragmentSortDefinition {

    @Override
    public int compare(String segment1, String segment2) {
        return segment1.compareTo(segment2);
    }

    @Override
    public IIpsPackageFragmentSortDefinition copy() {
        return new IpsPackageFragmentDefaultSortDefinition();
    }

    @Override
    public void initPersistenceContent(String content) throws CoreException {
        // nothing to do
    }

    @Override
    public String toPersistenceContent() {
        return ""; //$NON-NLS-1$
    }

}
