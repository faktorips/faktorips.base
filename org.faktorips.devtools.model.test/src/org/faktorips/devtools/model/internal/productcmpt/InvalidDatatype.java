/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class InvalidDatatype implements ValueDatatype {

    public static final String QUALIFIED_NAME = "InvalidDatatype";

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isParsable(String value) {
        return true;
    }

    @Override
    public String getName() {
        return getQualifiedName();
    }

    @Override
    public String getQualifiedName() {
        return QUALIFIED_NAME;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public MessageList checkReadyToUse() {
        MessageList ml = new MessageList();

        ml.add(new Message("", "", Message.ERROR));

        return ml;
    }

    @Override
    public int compareTo(Datatype o) {
        return -1;
    }

    @Override
    public boolean hasNullObject() {
        return false;
    }

    @Override
    public boolean isNull(String value) {
        return false;
    }

    @Override
    public boolean supportsCompare() {
        return false;
    }

    @Override
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        return 0;
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public Object getValue(String value) {
        throw new RuntimeException("not supported");
    }

}