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

package org.faktorips.devtools.core.builder.naming;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * Implementations of this interface provide an unqualified java class name. This name may differs
 * from several aspects as building interface or not.
 * 
 * @author dirmeier
 */
public interface IJavaClassNameProvider {

    public String getImplClassName(IIpsSrcFile ipsSrcFile);

    public String getInterfaceName(IIpsSrcFile ipsSrcFile);

}
