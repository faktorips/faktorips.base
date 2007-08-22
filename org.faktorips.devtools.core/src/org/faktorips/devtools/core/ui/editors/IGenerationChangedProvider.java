/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.core.model.IIpsObjectGeneration;

/**
 * Support IActiveGenerationChangedListener objects. A registered listner will be notified
 * if an {@link IIpsObjectGeneration} changes.
 *
 * @author Markus Blum
 */
public interface IGenerationChangedProvider {

    /**
     * Register a listener.
     *
     * @param listener new listener.
     */
    public void addListener(IActiveGenerationChangedListener listener);

    /**
     * Remove a listener.
     *
     * @param listener listener to remove.
     */
    public void removeListener(IActiveGenerationChangedListener listener);
}
