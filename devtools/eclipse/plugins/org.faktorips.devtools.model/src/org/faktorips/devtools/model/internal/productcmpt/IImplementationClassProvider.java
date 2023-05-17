/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * An {@link IImplementationClassProvider} determines the name of the class used at runtime for an
 * {@link IProductCmpt}.
 *
 * @since 23.6
 */
public interface IImplementationClassProvider {

    /**
     * Returns the name of the class used at runtime for the given {@link IProductCmpt}.
     */
    String getImplementationClassQualifiedName(IProductCmpt productCmpt);

}
