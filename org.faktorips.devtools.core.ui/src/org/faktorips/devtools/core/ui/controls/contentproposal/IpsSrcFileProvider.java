/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.contentproposal;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * This interface provides {@link IIpsSrcFile ips source files}.
 * 
 * @author dicker
 */
public interface IpsSrcFileProvider {

    /**
     * Returns an array of the {@link IIpsSrcFile ips source files}, which should be provided.
     */
    IIpsSrcFile[] getProvidedIpsSrcFiles();

}