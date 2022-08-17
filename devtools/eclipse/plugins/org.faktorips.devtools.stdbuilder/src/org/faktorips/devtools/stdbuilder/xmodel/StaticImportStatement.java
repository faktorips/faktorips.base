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

public class StaticImportStatement extends AbstractImportStatement {

    private final String element;

    /**
     * Creates a new static import statement.
     * 
     * @param qualifiedName The qualified name of the class you want to add to the import handler
     * @param element The element in the class you want to import, may be '*'
     */
    public StaticImportStatement(String qualifiedName, String element) {
        super(qualifiedName);
        this.element = element;
    }

    public String getElement() {
        return element;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getClassName() == null) ? 0 : getClassName().hashCode());
        result = prime * result + ((getPackageName() == null) ? 0 : getPackageName().hashCode());
        return prime * result + ((element == null) ? 0 : element.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StaticImportStatement)) {
            return false;
        }
        StaticImportStatement other = (StaticImportStatement)obj;
        return Objects.equals(getClassName(), other.getClassName())
                && Objects.equals(getPackageName(), other.getPackageName())
                && Objects.equals(getElement(), other.getElement());
    }

    @Override
    public String toString() {
        return "StaticImportStatement [" + getQualifiedName() + SEPERATOR + element + "]";
    }

    @Override
    public String getUnqualifiedName() {
        return super.getUnqualifiedName() + SEPERATOR + element;
    }

}
