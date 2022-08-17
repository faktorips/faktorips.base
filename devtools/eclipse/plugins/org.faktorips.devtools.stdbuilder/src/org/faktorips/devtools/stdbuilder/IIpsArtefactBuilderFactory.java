/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;

/**
 * An Interface for factories that are capable of creating an {@link IIpsArtefactBuilder}. It is
 * used for the extension point "org.faktorips.devtools.stdbuilder.artefactBuilderFactory", that
 * allows to register custom factories and thus custom artifact builders with the FaktorIPS standard
 * builder set.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public interface IIpsArtefactBuilderFactory {
    /**
     * Creates an {@link IIpsArtefactBuilder} for the given builder set.
     * 
     * @param builderSet the new builder's builder set
     * @return a custom IPS artifact builder.
     */
    IIpsArtefactBuilder createBuilder(StandardBuilderSet builderSet);
}
