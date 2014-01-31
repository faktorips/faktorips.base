/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

public interface IGeneratedJavaElement {

    public abstract IJavaElement getJavaElement(IType javaType);

}