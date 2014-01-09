/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

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
