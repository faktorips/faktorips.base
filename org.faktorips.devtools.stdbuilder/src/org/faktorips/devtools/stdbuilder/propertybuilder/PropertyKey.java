/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.propertybuilder;

import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

public interface PropertyKey {
    public String getKey();

    public QualifiedNameType getIpsObjectQNameType();

}