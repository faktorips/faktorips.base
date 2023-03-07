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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Handles the imports for generated java files. Imports can be added using {@link #add(String)}.
 * All added statements can be retrieved using the {@link #getImports()} method.
 * <p>
 * Types of java.lang will not be added to the imports.
 * <p>
 * Checks whether classes with the same unqualified name have already been imported. Thus avoids
 * "double" imports and provides information about class name conflicts by means of the
 * {@link #requiresQualifiedClassName(ImportStatement)} method. e.g. if you require Integer
 * (implicitly as java.lang.Integer) in your code but also use a class "some.other.package.Integer"
 * one of the two must be qualified. {@link #requiresQualifiedClassName(ImportStatement)} will
 * return <code>true</code> or <code>false</code> respectively.
 * <p>
 * TODO does not work for Classes in the own package that have the same unqualified name as a
 * java.lang class. e.g. "my.package.Integer". "my.package.Integer" would be used instead of
 * "java.lang.Integer" (no import statements are possible for both of them), and then causes compile
 * errors. To fix this each and every java.lang Class would have to be qualified in the code to
 * avoid collisions.
 * 
 * @author widmaier
 */
public class ImportHandler {

    private static final String GENERIC_START = "<";

    private static final String GENERIC_SEPERATOR = ", ";

    private static final String GENERIC_END = ">";

    private static final String JAVA_LANG_PACKAGE = "java.lang"; //$NON-NLS-1$

    private Map<String, ImportStatement> classNameToImportStatementMap;
    private Map<String, ImportStatement> implicitlyImportedClassNamesMap;
    private Map<String, StaticImportStatement> qualifiedPropertyNameToStaticImportStatementMap;

    private final String ownPackage;

    private final Set<String> superTypeNames;

    public ImportHandler(String ownPackage, Set<String> superTypeNames) {
        this.ownPackage = ownPackage;
        this.superTypeNames = superTypeNames;
        classNameToImportStatementMap = new LinkedHashMap<>();
        implicitlyImportedClassNamesMap = new LinkedHashMap<>();
        qualifiedPropertyNameToStaticImportStatementMap = new LinkedHashMap<>();
    }

    /**
     * Returns the set of imports this handler collected
     * 
     * @return The set of import statements collected by this import handler
     */
    public Set<ImportStatement> getImports() {
        return new LinkedHashSet<>(classNameToImportStatementMap.values());
    }

    /**
     * Returns the set of static imports this handler collected
     * 
     * @return The set of static import statements collected by this import handler
     */
    public Set<StaticImportStatement> getStaticImports() {
        return new LinkedHashSet<>(qualifiedPropertyNameToStaticImportStatementMap.values());
    }

    /**
     * Adds the given import statement and returns the qualified or unqualified class name depending
     * on whether it is required.
     * 
     * @param importStatement the import statement to be imported
     * @return the unqualified class name normally. Returns the qualified class name if there is a
     *             class name conflict (two classes with same unqualified name are used in the same
     *             class).
     * @see #requiresQualifiedClassName(ImportStatement)
     */
    public String addImportAndReturnClassName(String importStatement) {
        TypeDeclaration statements = addImportStatements(importStatement);
        return statements.getClassName(this);
    }

    private TypeDeclaration addImportStatements(String importStatement) {
        int genericStart = importStatement.indexOf(GENERIC_START);
        int genericEnd = importStatement.lastIndexOf(GENERIC_END);
        if (genericStart == -1 || genericEnd == -1) {
            return new TypeDeclaration(add(importStatement));
        } else {
            String typePart = importStatement.substring(0, genericStart);
            String genericPart = importStatement.substring(genericStart + 1, genericEnd);
            String[] generics = genericPart.split(GENERIC_SEPERATOR);
            List<TypeDeclaration> genericTypeDeclarations = getTypeWithGenericDeclarations(generics);
            return new TypeWithGenericsDeclaration(add(typePart), genericTypeDeclarations);
        }
    }

    private List<TypeDeclaration> getTypeWithGenericDeclarations(String[] generics) {
        List<TypeDeclaration> genericTypeDeclarations = new ArrayList<>();
        for (String genericImportDeclaration : generics) {
            TypeDeclaration typeDeclaration = addImportStatements(genericImportDeclaration);
            genericTypeDeclarations.add(typeDeclaration);
        }
        return genericTypeDeclarations;
    }

    /**
     * Adds the given static import statement and returns the unqualified name of the imported
     * element.
     * 
     * @param qualifiedName The qualified name of the class you want to add to the import handler
     * @param element The element in the class you want to import, may be '*'
     * @return the unqualified name of the imported element as given, for convenient use
     */
    public String addStaticImportAndReturnElementName(String qualifiedName, String element) {
        addStatic(qualifiedName, element);
        return element;
    }

    /**
     * Add a new static import statement.
     * 
     * @param qualifiedName The qualified name of the class you want to add to the import handler
     * @param element The element in the class you want to import, may be '*'
     * 
     * @return the import statement created and stored in this handler or {@link Optional#empty()}
     *             if it's not necessary to import
     */
    public Optional<StaticImportStatement> addStatic(String qualifiedName, String element) {
        String className = qualifiedName.trim();
        if (superTypeNames.contains(className)) {
            return Optional.empty();
        }
        StaticImportStatement importStatement = new StaticImportStatement(className, element.trim());
        registerImportStatementIfPossible(qualifiedPropertyNameToStaticImportStatementMap, importStatement);
        return Optional.of(importStatement);
    }

    private void registerImportStatementIfPossible(
            Map<String, StaticImportStatement> registeredImportsMap,
            StaticImportStatement importStatement) {
        if (!isInConflictWithImportedClassName(registeredImportsMap, importStatement)) {
            registeredImportsMap.put(importStatement.getUnqualifiedName(), importStatement);
        }
    }

    /**
     * Add a new import statement.
     * 
     * @param qualifiedName The qualified name of the class you want to add to the import handler
     * 
     * @return The import statement created and stored in this handler
     */
    public ImportStatement add(String qualifiedName) {
        ImportStatement importStatement = new ImportStatement(qualifiedName.trim());
        String packageName = importStatement.getPackage();
        if (isImplicitPackage(packageName)) {
            registerImportStatementIfPossible(implicitlyImportedClassNamesMap, importStatement);
        } else {
            registerImportStatementIfPossible(classNameToImportStatementMap, importStatement);
        }
        return importStatement;
    }

    /**
     * Add a new import statements.
     * 
     * @param qualifiedNames The qualified names of the class you want to add to the import handler
     * 
     * @return The import statement created and stored in this handler
     */
    public ImportStatement[] add(String... qualifiedNames) {
        ImportStatement[] importStatements = new ImportStatement[qualifiedNames.length];
        int i = 0;
        for (String qualifiedName : qualifiedNames) {
            ImportStatement importStatement = add(qualifiedName);
            importStatements[i] = importStatement;
        }
        return importStatements;
    }

    private void registerImportStatementIfPossible(
            Map<String, ImportStatement> registeredImportsMap,
            ImportStatement importStatement) {
        if (!isInConflictWithImportedClassName(classNameToImportStatementMap, importStatement)
                && !isInConflictWithImportedClassName(implicitlyImportedClassNamesMap, importStatement)) {
            registeredImportsMap.put(importStatement.getUnqualifiedName(), importStatement);
        }
    }

    private <I extends AbstractImportStatement> boolean isInConflictWithImportedClassName(
            Map<String, I> registeredImportsMap,
            I importStatement) {
        if (isImported(registeredImportsMap, importStatement)) {
            return false;
        } else {
            return registeredImportsMap.containsKey(importStatement.getUnqualifiedName());
        }
    }

    private <I extends AbstractImportStatement> boolean isImported(Map<String, I> registeredImportsMap,
            I importStatement) {
        String unqualifiedName = importStatement.getUnqualifiedName();
        I registeredImportStatement = registeredImportsMap.get(unqualifiedName);
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
        return packageName.equals(JAVA_LANG_PACKAGE) || IpsStringUtils.isEmpty(packageName)
                || isHomePackage(packageName);
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
                || isInConflictWithImportedClassName(classNameToImportStatementMap, importStatement)
                || isInConflictWithImportedClassName(implicitlyImportedClassNamesMap, importStatement);
    }

    /**
     * Remove an existing import statement.
     * 
     * @param qualifiedName The qualified name of the class you want to remove from import
     * @return True if the removement was successfull
     */
    public boolean remove(String qualifiedName) {
        return classNameToImportStatementMap.remove(new ImportStatement(qualifiedName).getUnqualifiedName()) != null;
    }

    /**
     * Returns the own package of this import handler. For this package no import statement will be
     * created.
     * 
     */
    public String getOwnPackage() {
        return ownPackage;
    }

    private static class TypeDeclaration {

        private final ImportStatement importStatement;

        public TypeDeclaration(ImportStatement importStatement) {
            this.importStatement = importStatement;
        }

        public String getClassName(ImportHandler importHandler) {
            if (importHandler.requiresQualifiedClassName(importStatement)) {
                return importStatement.getQualifiedName();
            } else {
                return importStatement.getUnqualifiedName();
            }
        }

    }

    private static class TypeWithGenericsDeclaration extends TypeDeclaration {

        private final List<TypeDeclaration> genericDeclarations;

        public TypeWithGenericsDeclaration(ImportStatement importStatement,
                List<TypeDeclaration> genericTypeDeclarations) {
            super(importStatement);
            genericDeclarations = genericTypeDeclarations;
        }

        @Override
        public String getClassName(ImportHandler importHandler) {
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append(super.getClassName(importHandler));
            resultBuilder.append(GENERIC_START);
            for (Iterator<TypeDeclaration> iterator = genericDeclarations.iterator(); iterator.hasNext();) {
                TypeDeclaration typeDeclaration = iterator.next();
                resultBuilder.append(typeDeclaration.getClassName(importHandler));
                if (iterator.hasNext()) {
                    resultBuilder.append(GENERIC_SEPERATOR);
                }
            }
            resultBuilder.append(GENERIC_END);
            return resultBuilder.toString();
        }

    }

}
