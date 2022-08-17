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

import org.apache.commons.lang.StringUtils;

/**
 * This class represents an import statement for the Xtend builder.
 */
public class AbstractImportStatement {

    protected static final String SEPERATOR = ".";

    private final String packageName;

    private final String className;

    public AbstractImportStatement(String qualifiedName) {
        String[] packageAndClassName = splitPackageAndClassName(qualifiedName);
        packageName = packageAndClassName[0];
        className = packageAndClassName[1];
    }

    public AbstractImportStatement(Class<?> clazz) {
        packageName = clazz.getPackage().getName();
        className = clazz.getSimpleName();
    }

    protected String getPackageName() {
        return packageName;
    }

    protected String getClassName() {
        return className;
    }

    private static String[] splitPackageAndClassName(String qualifiedName) {
        String[] result = new String[2];
        int lastIndexOf = qualifiedName.lastIndexOf(SEPERATOR);
        if (lastIndexOf > 0) {
            result[0] = qualifiedName.substring(0, lastIndexOf);
            result[1] = qualifiedName.substring(lastIndexOf + 1);
        } else {
            result[0] = StringUtils.EMPTY;
            result[1] = qualifiedName;
        }
        return result;
    }

    /**
     * Gets the full qualified name of the import statement.
     * 
     * @return Returns the qualifiedName.
     */
    public String getQualifiedName() {
        if (StringUtils.isEmpty(packageName)) {
            return className;
        } else {
            return packageName + SEPERATOR + className;
        }
    }

    /**
     * Gets the package name of the import statement
     * 
     * @return The package part of the import statement
     */
    public String getPackage() {
        return packageName;
    }

    public String getUnqualifiedName() {
        return className;
    }

}
