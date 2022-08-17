/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.Map;

import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.model.IIpsElement;

/**
 * A {@link IWorkbenchAdapterProvider} is able to provides a map of {@link IWorkbenchAdapter} for
 * several IIpsElements. You have to register the provider with the extension point
 * <code>org.faktorips.devtools.core.ui.adapterprovider</code>. There should be an adapter for every
 * {@link IIpsElement} in your plugin.
 * <p>
 * You do best by building you adapter map within constructor call so every call of
 * {@link #getAdapterMap()} only have to provide the cached map.
 * 
 * @author dirmeier
 */
public interface IWorkbenchAdapterProvider {

    /**
     * Providing a map of {@link IpsElementWorkbenchAdapter}s. The adapters are not provided for the
     * public interface but for the concrete implementation.
     * 
     * @return A map mapping {@link IIpsElement}s to {@link IpsElementWorkbenchAdapter}s
     */
    Map<Class<? extends IIpsElement>, IpsElementWorkbenchAdapter> getAdapterMap();
}
