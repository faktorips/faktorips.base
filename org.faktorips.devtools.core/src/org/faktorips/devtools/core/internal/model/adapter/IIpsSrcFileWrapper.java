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

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * Objects wrapping an {@link IIpsSrcFile} should implement this interface to enable eclipse getting
 * the wrapped {@link IIpsSrcFile}. e.g. this is the case for structural model elements as well as
 * viewer items providing additional information for an IPS source file.
 * 
 * @author dirmeier
 */
public interface IIpsSrcFileWrapper extends IAdaptable {

    /**
     * Return the wrapped {@link IIpsSrcFile}
     * 
     * @return the wrapped {@link IIpsSrcFile}
     */
    public IIpsSrcFile getWrappedIpsSrcFile();

}
