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

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.eclipse.jdt.core.IJavaProject;

/**
 * A listener that listens to changes to classpath contents that is either a Jar file in the
 * classpath or a directory containing class files is changed in any way.
 * 
 * @author Jan Ortmann
 */
public interface IClasspathContentsChangeListener {

    /**
     * Is called when the contents of the indicated Java project's classpath has changed.
     */
    public void classpathContentsChanges(IJavaProject project);
}
