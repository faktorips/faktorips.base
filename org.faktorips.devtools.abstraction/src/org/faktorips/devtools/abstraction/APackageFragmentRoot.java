/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import java.nio.file.Path;

/**
 * A package fragment root is a folder or archive file containing {@link AJavaElement Java elements}
 * and packages.
 */
public interface APackageFragmentRoot extends AJavaElement {

    Path getOutputLocation();

}
