/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

public interface IGeneratedJavaElement {

    IJavaElement getJavaElement(IType javaType);

}
