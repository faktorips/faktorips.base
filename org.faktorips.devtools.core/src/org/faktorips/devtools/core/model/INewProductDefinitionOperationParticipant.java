/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * A {@link INewProductDefinitionOperationParticipant} will be called by a
 * {@code NewProductDefinitionOperation} to allow additional changes to the created objects by
 * plugins.
 * <p>
 * <strong>This feature is experimental and the interface may change in future releases.</strong>
 * 
 * @since 3.11
 */
public interface INewProductDefinitionOperationParticipant {
    String CONFIG_ELEMENT_ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
    String CONFIG_ELEMENT_ID_PARTICIPANT = "Participant"; //$NON-NLS-1$
    String EXTENSION_POINT_ID_NEW_PRODUCT_DEFINITION_OPERATION = "newProductDefinitionOperation"; //$NON-NLS-1$

    /**
     * This method is called by a {@code NewProductDefinitionOperation} after it has finished
     * creating an {@link IIpsSrcFile}, so all default values have been set. Participants can then
     * modify the file before it is saved by the operation.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to manipulate
     * @param monitor A progress monitor to show your progress
     */
    void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor);
}
