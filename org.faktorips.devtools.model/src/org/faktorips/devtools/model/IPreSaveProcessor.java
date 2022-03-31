/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model;

import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Implementations of this interface are called whenever an {@link IpsSrcFile} is
 * {@link IpsSrcFile#save(org.eclipse.core.runtime.IProgressMonitor) saved} to allow
 * additional processing.
 * <p>
 * Every {@link IPreSaveProcessor} is responsible for a specific {@link IpsObjectType}, which it
 * must return from its {@link #getIpsObjectType()} method. Its {@link #process(IIpsObject)} method
 * will only be called für {@link IIpsObject IIpsObjects} of that type.
 * 
 * @since 21.12
 */
public interface IPreSaveProcessor {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_PRE_SAVE_PROCESSOR}.
     */
    public static final String EXTENSION_POINT_ID_PRE_SAVE_PROCESSOR = "preSaveProcessor"; //$NON-NLS-1$

    /**
     * Processes the given {@link IIpsObject}.
     *
     * @param ipsObject the {@link IIpsObject} about to be saved
     */
    public void process(IIpsObject ipsObject);

    /**
     * Returns the {@link IpsObjectType} this {@link IPreSaveProcessor} can process.
     */
    public IpsObjectType getIpsObjectType();
}
