/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

public class DefaultGenericEnumDatatype extends GenericEnumDatatype {

    private Class<?> adaptedClass;

    public DefaultGenericEnumDatatype(Class<?> adaptedClass) {
        super();
        this.adaptedClass = adaptedClass;
    }

    @Override
    public Class<?> getAdaptedClass() {
        return adaptedClass;
    }

    @Override
    public String getAdaptedClassName() {
        return adaptedClass.getName();
    }

    @Override
    public String getIdByName(String valueName) {
        return Arrays.stream(getAllValueIds(false))
                .filter(id -> StringUtils.equals(getValueName(id), valueName))
                .findFirst()
                .orElse(null);
    }
}
