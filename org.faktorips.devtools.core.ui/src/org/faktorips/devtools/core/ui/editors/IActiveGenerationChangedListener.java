/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;

/**
 * Listeners that is notified if the active generation changes in a timed ips object editor.
 * 
 * @author Markus Blum
 */
public interface IActiveGenerationChangedListener {

    /**
     * Get the current generation after changes <code>generation</code>.
     * 
     * @param generation current generation.
     */
    public void activeGenerationChanged(IIpsObjectGeneration generation);

}
