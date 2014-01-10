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

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Thrown when a cycle is detected in the product structure and so a structure tree can't be
 * constructed.
 * 
 * @author Thorsten Guenther
 */
public class CycleInProductStructureException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3945323856832361062L;

    private IIpsElement[] cyclePath;

    /**
     * Creates a new exception with the given path.
     */
    public CycleInProductStructureException(IIpsElement[] cyclePath) {
        this.cyclePath = cyclePath;
    }

    /**
     * Returns the path for this cycle. The content of the path depends on the creator of this
     * exception.
     */
    public IIpsElement[] getCyclePath() {
        return cyclePath;
    }

}
