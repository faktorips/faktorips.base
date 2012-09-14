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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Handles the imports for generated java files. Imports can be added using {@link #add(String)}.
 * All added statements can be retrieved using the {@link #getImports()} method.
 * <p>
 * Primitive types (e.g. java.lang.boolean) will not be added to the imports.
 * <p>
 * Checks whether classes with the same unqualified name have already been imported. Thus avoids
 * "double" imports and provides information about class name conflicts by means of the
 * {@link #requiresQualifiedClassName(ImportStatement)} method. e.g. if you require Integer
 * (implicitly as java.lang.Integer) in your code but also use a class "some.other.package.Integer"
 * one of the two must be qualified. {@link #requiresQualifiedClassName(ImportStatement)} will
 * return <code>true</code> or <code>false</code> respectively.
 * 
 * @author widmaier
 */
public class ImportHandler {

    private final static String JAVA_LANG_PACKAGE = "java.lang"; //$NON-NLS-1$

    private Map<String, ImportStatement> classNameToImportStatementMap;
    private Map<String, ImportStatement> implicitlyImportedClassNamesMap;

    private final String ownPackage;

    public ImportHandler(String ownPackage) {
        this.ownPackage = ownPackage;
        this.classNameToImportStatementMap = new LinkedHashMap<String, ImportStatement>();
        this.implicitlyImportedClassNamesMap = new LinkedHashMap<String, ImportStatement>();
    }

    public Set<ImportStatement> getImports() {
        return new LinkedHashSet<ImportStatement>(classNameToImportStatementMap.values());
    }

    /**
     * Adds the given import statement and returns the qualified or unqualified class name depending
     * on whether it is required.
     * 
     * @param importStatement the import statement to be imported
     * @return the unqualified class name normally. Returns the qualified class name if there is a
     *         class name conflict (two classes with same unqualified name are used in the same
     *         class).
     * @see #requiresQualifiedClassName(ImportStatement)
     */
    public String addImportAndReturnClassName(String importStatement) {
        ImportStatement statement = add(importStatement);
        if (requiresQualifiedClassName(statement)) {
            return statement.getQualifiedName();
        } else {
            return statement.getUnqualifiedName();
        }
    }

    public ImportStatement add(String qualifiedName) {
        ImportStatement importStatement = new ImportStatement(qualifiedName);
        String packageName = importStatement.getPackage();
        if (isImplicitPackage(packageName)) {
            registerImportStatementIfPossible(implicitlyImportedClassNamesMap, importStatement);
        } else {
            registerImportStatementIfPossible(classNameToImportStatementMap, importStatement);
        }
        return importStatement;
    }

    private void registerImportStatementIfPossible(Map<String, ImportStatement> registeredImportsMap,
            ImportStatement importStatement) {
        if (!isInConflictWithImportedClassName(importStatement) && !isInConflictWithImplicitClassName(importStatement)) {
            registeredImportsMap.put(importStatement.getUnqualifiedName(), importStatement);
        }
    }

    private boolean isInConflictWithImportedClassName(Map<String, ImportStatement> registeredImportsMap,
            ImportStatement importStatement) {
        if (isImported(registeredImportsMap, importStatement)) {
            return false;
        } else {
            return registeredImportsMap.containsKey(importStatement.getUnqualifiedName());
        }
    }

    private boolean isImported(Map<String, ImportStatement> registeredImportsMap, ImportStatement importStatement) {
        String unqualifiedName = importStatement.getUnqualifiedName();
        ImportStatement registeredImportStatement = registeredImportsMap.get(unqualifiedName);
        return importStatement.equals(registeredImportStatement);
    }

    /**
     * Returns <code>true</code> for package names that do not require an import statement,
     * <code>false</code> for all others. Package that do not require an import statement are
     * "java.lang", the home package (the package this {@link ImportHandler} is created with) and
     * the empty package. The latter case is interpreted as java.lang.
     * 
     * @param packageName the package name to test
     */
    private boolean isImplicitPackage(String packageName) {
        return packageName.equals(JAVA_LANG_PACKAGE) || StringUtils.isEmpty(packageName) || isHomePackage(packageName);
    }

    private boolean isHomePackage(String packageName) {
        return packageName.equals(ownPackage);
    }

    /**
     * Returns <code>true</code> if the class name (defined by the given import statement) should be
     * qualified in generated code. <code>false</code> if the unqualified class name may be used.
     * e.g. if you use Integer (implicitly as java.lang.Integer) in you code but also use a class
     * "some.other.Package.Integer" one of the two must be qualified.
     * 
     * @param importStatement the import statement to test
     */
    public boolean requiresQualifiedClassName(ImportStatement importStatement) {
        return (!isImported(classNameToImportStatementMap, importStatement) && !isImported(
                implicitlyImportedClassNamesMap, importStatement))
                || isInConflictWithImportedClassName(importStatement)
                || isInConflictWithImplicitClassName(importStatement);
    }

    private boolean isInConflictWithImportedClassName(ImportStatement importStatement) {
        return isInConflictWithImportedClassName(classNameToImportStatementMap, importStatement);
    }

    private boolean isInConflictWithImplicitClassName(ImportStatement importStatement) {
        return isInConflictWithImportedClassName(implicitlyImportedClassNamesMap, importStatement);
    }

    public boolean remove(String importStatement) {
        return classNameToImportStatementMap.remove(new ImportStatement(importStatement).getUnqualifiedName()) != null;
    }

    public String getOwnPackage() {
        return ownPackage;
    }

}