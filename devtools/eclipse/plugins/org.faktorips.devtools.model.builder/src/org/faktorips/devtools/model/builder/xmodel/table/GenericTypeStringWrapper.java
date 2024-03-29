/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel.table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A String wrapper for both generic and non generic classes. If the class is not generic,
 * genericParams is an empty list.
 */
public class GenericTypeStringWrapper {

    private String genericClass;
    private List<GenericTypeStringWrapper> genericParams;

    public GenericTypeStringWrapper(String genericClass) {
        this.genericClass = genericClass;
        genericParams = new ArrayList<>();
    }

    public GenericTypeStringWrapper(String genericClass, String genericParam) {
        this.genericClass = genericClass;
        genericParams = new ArrayList<>();
        genericParams.add(new GenericTypeStringWrapper(genericParam));
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
        return addGenericsIfNecessary(genericClass);
    }

    private String addGenericsIfNecessary(String genType) {
        String result = genType;
        if (genericParams.size() > 0) {
            result += "<" + params() + ">";
        }
        return result;
    }

    private String params() {
        return genericParams.stream().map(GenericTypeStringWrapper::toString).collect(Collectors.joining(","));
    }

    public String paramsWithBracket() {
        return addGenericsIfNecessary("");
    }
}
