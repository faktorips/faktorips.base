/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import java.util.Observable;
import java.util.Observer;

/**
 * This is just another name for the {@link Observer} interface to mark a listener for the
 * {@link DeferredStructuredContentProvider}. We use the benefit of the thread safe implementation
 * of {@link Observer} and {@link Observable}.
 * 
 * @author Cornelius Dirmeier
 */
public interface ICollectorFinishedListener extends Observer {

    // at the moment this is only a wrapper for observer to give a proper name
    // maybe it becomes a real listener once

}
