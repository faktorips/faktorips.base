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

import java.util.Objects;

/**
 * This class represents an import statement for the Xtend builder.
 */
public class ImportStatement extends AbstractImportStatement {

    public ImportStatement(String qualifiedName) {
        super(qualifiedName);
    }

    public ImportStatement(Class<?> clazz) {
        super(clazz);
    }

    public static ImportStatement newInstance(String qName) {
        return new ImportStatement(qName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getClassName() == null) ? 0 : getClassName().hashCode());
        return prime * result + ((getPackageName() == null) ? 0 : getPackageName().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ImportStatement)) {
            return false;
        }
        ImportStatement other = (ImportStatement)obj;
        return Objects.equals(getClassName(), other.getClassName())
                && Objects.equals(getPackageName(), other.getPackageName());
    }

    @Override
    public String toString() {
        return "ImportStatement [" + getQualifiedName() + "]";
    }

}
