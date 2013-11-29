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

package org.faktorips.runtime.internal.tableindex;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link ResultStructure ResultStructures} are the leaves in the tree of nested
 * {@link SearchStructure SearchStructures}. Each {@link ResultStructure} defines a result of a
 * search and thus holds a set of values. It also implements the {@link Mergeable} interface to be
 * able to combine the values of two result structures.
 */
public class ResultStructure<R> extends SearchStructure<R> implements Mergeable<ResultStructure<R>> {

    private final Set<R> resultSet;

    ResultStructure() {
        resultSet = new HashSet<R>();
    }

    ResultStructure(R result) {
        this();
        resultSet.add(result);
    }

    ResultStructure(Set<R> result) {
        resultSet = result;
    }

    /**
     * Creates a new {@link ResultSet} with the given resultValue as its only result value.
     */
    public static <R> ResultStructure<R> createWith(R resultValue) {
        return new ResultStructure<R>(resultValue);
    }

    /**
     * Creates a new {@link ResultSet} with the given set of result values.
     */
    public static <R> ResultStructure<R> createWith(Set<R> resultValues) {
        return new ResultStructure<R>(resultValues);
    }

    @Override
    public SearchStructure<R> get(Object key) {
        return this;
    }

    @Override
    public Set<R> get() {
        return Collections.unmodifiableSet(resultSet);
    }

    public void merge(ResultStructure<R> otherStructure) {
        resultSet.addAll(otherStructure.resultSet);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((resultSet == null) ? 0 : resultSet.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        ResultStructure<R> other = (ResultStructure<R>)obj;
        if (resultSet == null) {
            if (other.resultSet != null) {
                return false;
            }
        } else if (!resultSet.equals(other.resultSet)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResultStructure [resultSet=" + resultSet + "]";
    }

}