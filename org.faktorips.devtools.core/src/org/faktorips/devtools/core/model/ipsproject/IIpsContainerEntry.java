/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsproject;

import org.eclipse.core.runtime.CoreException;

/**
 * An ips object path container provides a way to indirectly reference a set of entries of type
 * archive or project. What entries the container contains is not fixed.
 * 
 * @since 3.1
 */
public interface IIpsContainerEntry extends IIpsObjectPathEntry {

    public String getDescription();

    public IIpsObjectPathEntry[] resolveEntries() throws CoreException;

}
