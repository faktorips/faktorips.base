/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

public interface IIpsObjectPart extends IIpsObjectPartContainer {

    String PROPERTY_ID = "id"; //$NON-NLS-1$

    /**
     * The part's id that uniquely identifies it in it's parent.
     */
    String getId();

    /**
     * Deletes the part by removing it from it's container and firing a part-removed event.
     */
    @Override
    void delete();

    /**
     * Returns whether the part was deleted (<code>true</code>) or not.
     */
    boolean isDeleted();

    /**
     * {@inheritDoc}
     * <p>
     * Ensures that the ID of this part is maintained.
     */
    @Override
    void copyFrom(IIpsObjectPartContainer source);

}
