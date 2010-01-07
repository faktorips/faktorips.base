/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.Map;

import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.internal.model.IpsElement;

/**
 * A {@link IWorkbenchAdapterProvider} is able to provides a map of {@link IWorkbenchAdapter} for
 * several IIpsElements. You have to register the provider with the extension point
 * <code>org.faktorips.devtools.core.ui.adapterprovider</code>. There should be an adapter for every
 * {@link IpsElement} in your plugin.
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
     * @return A map mapping {@link IpsElement}s to {@link IpsElementWorkbenchAdapter}s
     */
    public Map<Class<? extends IpsElement>, IpsElementWorkbenchAdapter> getAdapterMap();
}
