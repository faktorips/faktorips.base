/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xmodel.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * A String wrapper for both generic and non generic classes. If the class is not generic,
 * genericParams is an empty list.
 */
public class GenericTypeStringWrapper {

    private String genericClass;
    private List<GenericTypeStringWrapper> genericParams;

    public GenericTypeStringWrapper(String genericClass) {
        this.genericClass = genericClass;
        this.genericParams = new ArrayList<GenericTypeStringWrapper>();
    }

    public GenericTypeStringWrapper(String genericClass, String genericParam) {
        this.genericClass = genericClass;
        this.genericParams = new ArrayList<GenericTypeStringWrapper>();
        this.genericParams.add(new GenericTypeStringWrapper(genericParam));
    }

    public GenericTypeStringWrapper(String genericClass, List<GenericTypeStringWrapper> genericParams) {
        this.genericClass = genericClass;
        this.genericParams = genericParams;
    }

    public String getGenericClass() {
        return genericClass;
    }

    public List<GenericTypeStringWrapper> getGenericParams() {
        return genericParams;
    }

    @Override
    public String toString() {
        String genType = genericClass;
        if (genericParams.size() > 0) {
            genType += "<" + StringUtils.join(genericParams, ",") + ">";
        }
        return genType;
    }

    public String paramsWithBracket() {
        String paramsWithBracket = "";
        if (genericParams.size() > 0) {
            paramsWithBracket += "<" + StringUtils.join(genericParams, ",") + ">";
        }
        return paramsWithBracket;
    }
}
