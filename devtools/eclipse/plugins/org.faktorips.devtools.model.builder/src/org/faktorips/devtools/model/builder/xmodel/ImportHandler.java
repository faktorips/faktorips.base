/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.faktorips.devtools.abstraction.Abstractions;
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

    /**
     * Eclipse debug trace option for verbose debug logging of import handler decisions. Enable in
     * Eclipse by setting <code>org.faktorips.devtools.model.builder/trace/importhandler=true</code>
     * in the .options file or via Debug Configurations -> Tracing.
     */
    public static final boolean TRACE_IMPORT_HANDLER;

    private static final String GENERIC_START = "<";

    private static final String GENERIC_SEPERATOR = ", ";

    private static final String GENERIC_END = ">";

    private static final String JAVA_LANG_PACKAGE = "java.lang"; //$NON-NLS-1$

    static {
        TRACE_IMPORT_HANDLER = Boolean
                .parseBoolean(Abstractions.getDebugOption("org.faktorips.devtools.model.builder/trace/importhandler"));
    }

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

        if (TRACE_IMPORT_HANDLER && (ownPackage == null || ownPackage.isEmpty())) {
            System.out.println(String.format(
                    "WARNING: ImportHandler created with empty/null ownPackage! [Thread: %s, Instance: @%s]",
                    Thread.currentThread().getName(), Integer.toHexString(System.identityHashCode(this))));
        }
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
        boolean conflictWithExplicit = isInConflictWithImportedClassName(classNameToImportStatementMap,
                importStatement);
        boolean conflictWithImplicit = isInConflictWithImportedClassName(implicitlyImportedClassNamesMap,
                importStatement);
        boolean hasConflict = conflictWithExplicit || conflictWithImplicit;

        if (TRACE_IMPORT_HANDLER && "Process".equals(importStatement.getUnqualifiedName())) {
            String mapType = (registeredImportsMap == classNameToImportStatementMap) ? "explicit" : "implicit";
            System.out.println(String.format(
                    """
                            ImportHandler.registerImportStatementIfPossible(%s map): %s [Thread: %s, Instance: @%s, OwnPkg: '%s']
                              Conflict with explicit map: %s
                              Conflict with implicit map: %s
                              no conflict: %s""",
                    mapType, importStatement.getQualifiedName(),
                    Thread.currentThread().getName(), Integer.toHexString(System.identityHashCode(this)),
                    ownPackage != null ? ownPackage : "NULL",
                    conflictWithExplicit, conflictWithImplicit, !hasConflict));
        }

        if (!hasConflict) {
            registeredImportsMap.put(importStatement.getUnqualifiedName(), importStatement);
        }
    }

    private <I extends AbstractImportStatement> boolean isInConflictWithImportedClassName(
            Map<String, I> registeredImportsMap,
            I importStatement) {
        boolean imported = isImported(registeredImportsMap, importStatement);
        boolean hasUnqualifiedNameConflict = registeredImportsMap.containsKey(importStatement.getUnqualifiedName());
        boolean result = !imported && hasUnqualifiedNameConflict;

        if (TRACE_IMPORT_HANDLER && hasUnqualifiedNameConflict
                && "Process".equals(importStatement.getUnqualifiedName())) {
            String mapType = (registeredImportsMap == classNameToImportStatementMap) ? "explicit" : "implicit";
            I existingImport = registeredImportsMap.get(importStatement.getUnqualifiedName());
            System.out.println(String.format(
                    """
                            ImportHandler.isInConflictWithImportedClassName(%s map): %s for %s [Thread: %s, Instance: @%s, OwnPkg: '%s']
                              Already imported: %s, Has unqualified name conflict: %s
                              Existing import: %s, New import: %s""",
                    mapType, result, importStatement.getQualifiedName(),
                    Thread.currentThread().getName(), Integer.toHexString(System.identityHashCode(this)),
                    ownPackage != null ? ownPackage : "NULL",
                    imported, hasUnqualifiedNameConflict,
                    existingImport != null ? existingImport.getQualifiedName() : "null",
                    importStatement.getQualifiedName()));
        }

        return result;
    }

    private <I extends AbstractImportStatement> boolean isImported(Map<String, I> registeredImportsMap,
            I importStatement) {
        String unqualifiedName = importStatement.getUnqualifiedName();
        I registeredImportStatement = registeredImportsMap.get(unqualifiedName);
        boolean result = importStatement.equals(registeredImportStatement);

        if (TRACE_IMPORT_HANDLER && registeredImportStatement != null && !result && "Process".equals(unqualifiedName)) {
            String mapType = (registeredImportsMap == classNameToImportStatementMap) ? "explicit" : "implicit";
            System.out.println(String.format(
                    """
                            ImportHandler.isImported(%s map): Found unqualified name '%s' but different qualified names [Thread: %s, Instance: @%s, OwnPkg: '%s']
                              Registered: %s
                              Checking:   %s
                              is Imported: %s""",
                    mapType, unqualifiedName,
                    Thread.currentThread().getName(), Integer.toHexString(System.identityHashCode(this)),
                    ownPackage != null ? ownPackage : "NULL",
                    registeredImportStatement.getQualifiedName(),
                    importStatement.getQualifiedName(),
                    result));
        }

        return result;
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
        boolean notImportedInExplicitMap = !isImported(classNameToImportStatementMap, importStatement);
        boolean notImportedInImplicitMap = !isImported(implicitlyImportedClassNamesMap, importStatement);
        boolean conflictInExplicitMap = isInConflictWithImportedClassName(classNameToImportStatementMap,
                importStatement);
        boolean conflictInImplicitMap = isInConflictWithImportedClassName(implicitlyImportedClassNamesMap,
                importStatement);

        boolean notImported = notImportedInExplicitMap && notImportedInImplicitMap;
        boolean hasConflict = conflictInExplicitMap || conflictInImplicitMap;
        boolean result = notImported || hasConflict;

        if (TRACE_IMPORT_HANDLER && "Process".equals(importStatement.getUnqualifiedName())) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("ImportHandler.requiresQualifiedClassName() decision for: ")
                    .append(importStatement.getQualifiedName())
                    .append("\n  Unqualified name: ").append(importStatement.getUnqualifiedName())
                    .append("\n  Package: ").append(importStatement.getPackage())
                    .append("\n  Own package: ").append(ownPackage)
                    .append("\n  Thread: ").append(Thread.currentThread().getName())
                    .append(" (ID: ").append(Thread.currentThread().threadId()).append(")")
                    .append("\n  ImportHandler instance: @").append(Integer.toHexString(System.identityHashCode(this)))
                    .append("\n  --- State checks ---")
                    .append("\n  Not imported in explicit map: ").append(notImportedInExplicitMap)
                    .append("\n  Not imported in implicit map: ").append(notImportedInImplicitMap)
                    .append("\n  Conflict in explicit map: ").append(conflictInExplicitMap)
                    .append("\n  Conflict in implicit map: ").append(conflictInImplicitMap)
                    .append("\n  --- Computed values ---")
                    .append("\n  Not imported at all: ").append(notImported)
                    .append("\n  Has conflict: ").append(hasConflict)
                    .append("\n  --- Map contents ---")
                    .append("\n  Explicit imports (").append(classNameToImportStatementMap.size()).append("): ")
                    .append(classNameToImportStatementMap.keySet())
                    .append("\n  Implicit imports (").append(implicitlyImportedClassNamesMap.size()).append("): ")
                    .append(implicitlyImportedClassNamesMap.keySet())
                    .append("\n  --- RESULT ---")
                    .append("\n  Requires qualification: ").append(result);

            System.out.println(logMessage.toString());
        }

        return result;
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
