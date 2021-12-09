/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import org.faktorips.devtools.model.abstraction.AJavaProject;

/**
 * A listener that listens to changes to classpath contents that is either a Jar file in the
 * classpath or a directory containing class files is changed in any way.
 * 
 * @author Jan Ortmann
 */
@FunctionalInterface
public interface IClasspathContentsChangeListener {

    /**
     * Is called when the contents of the indicated project's classpath has changed.
     */
    public void classpathContentsChanges(AJavaProject project);

}
