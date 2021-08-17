/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.ModuleDirective;
import org.eclipse.jdt.core.dom.ModuleModifier;
import org.eclipse.jdt.core.dom.ModuleModifier.ModuleModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class Modules {

    private static final String MODULE_INFO_JAVA_FILE_NAME = "module-info.java";

    private Modules() {
        // Utility class
    }

    /**
     * Adds the given required modules to the given Java project's
     * {@value #MODULE_INFO_JAVA_FILE_NAME} file.
     *
     * @param javaProject a Java project that has a {@value #MODULE_INFO_JAVA_FILE_NAME} file
     * @param requiredModules a list of fully qualified module names
     * @throws CoreException if the Java project has no valid {@value #MODULE_INFO_JAVA_FILE_NAME}
     *             file or its modification fails
     */
    public static void addRequired(IJavaProject javaProject, boolean transitive, List<String> requiredModules)
            throws CoreException {
        ICompilationUnit moduleCompilationUnit = getModuleCompilationUnit(javaProject);

        CompilationUnit astRoot = SharedASTProviderCore.getAST(moduleCompilationUnit, SharedASTProviderCore.WAIT_YES,
                null);
        AST ast = astRoot.getAST();
        ASTRewrite rewrite = ASTRewrite.create(ast);
        ModuleDeclaration moduleDeclaration = astRoot.getModule();
        ListRewrite listRewrite = rewrite.getListRewrite(moduleDeclaration,
                ModuleDeclaration.MODULE_DIRECTIVES_PROPERTY);
        List<String> requiredModulesToAdd = new ArrayList<>(requiredModules);
        Set<RequiresDirective> requiresDirectivesToRewrite = new HashSet<>();
        RequiresDirective lastModuleRequiresDirective = checkExistingRequiresDirectives(moduleDeclaration, transitive,
                requiredModulesToAdd,
                requiresDirectivesToRewrite);
        if (!requiredModulesToAdd.isEmpty() || !requiresDirectivesToRewrite.isEmpty()) {
            for (String requiredModule : requiredModulesToAdd) {
                RequiresDirective requiresDirective = newRequiresDirective(ast, transitive, requiredModule);
                if (lastModuleRequiresDirective != null) {
                    listRewrite.insertAfter(requiresDirective, lastModuleRequiresDirective, null);
                } else {
                    listRewrite.insertLast(requiresDirective, null);
                }
            }
            for (RequiresDirective requiresDirective : requiresDirectivesToRewrite) {
                RequiresDirective newRequiresDirective = newRequiresDirective(ast, transitive,
                        requiresDirective.getName().getFullyQualifiedName());
                listRewrite.replace(requiresDirective, newRequiresDirective, null);
            }
            applyRewrite(moduleCompilationUnit, rewrite);
        }
    }

    @SuppressWarnings("restriction")
    private static void applyRewrite(ICompilationUnit moduleCompilationUnit, ASTRewrite rewrite)
            throws JavaModelException, CoreException {
        try {
            CompilationUnitChange cuChange = new CompilationUnitChange(MODULE_INFO_JAVA_FILE_NAME,
                    moduleCompilationUnit);
            TextEdit resultingEdits = rewrite.rewriteAST();
            org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility.addTextEdit(cuChange,
                    MODULE_INFO_JAVA_FILE_NAME, resultingEdits);
            cuChange.perform(new NullProgressMonitor());
        } catch (IllegalArgumentException e) {
            IpsLog.log(e);
            throw new CoreException(new IpsStatus(e));
        }
    }

    private static RequiresDirective checkExistingRequiresDirectives(ModuleDeclaration moduleDeclaration,
            boolean transitive,
            List<String> requiredModulesToAdd,
            Set<RequiresDirective> requiresDirectivesToRewrite) {
        RequiresDirective lastModuleRequiresDirective = null;
        @SuppressWarnings("unchecked")
        List<ModuleDirective> moduleStatements = moduleDeclaration.moduleStatements();
        for (ModuleDirective directive : moduleStatements) {
            if (directive instanceof RequiresDirective) {
                RequiresDirective requiresDirective = (RequiresDirective)directive;
                Name name = requiresDirective.getName();
                if (requiredModulesToAdd.remove(name.getFullyQualifiedName())
                        && isTransitive(requiresDirective) != transitive) {
                    requiresDirectivesToRewrite.add(requiresDirective);
                }
                lastModuleRequiresDirective = requiresDirective;
            }
        }
        return lastModuleRequiresDirective;
    }

    private static ICompilationUnit getModuleCompilationUnit(IJavaProject javaProject)
            throws JavaModelException, CoreException {
        IModuleDescription moduleDescription = javaProject.getModuleDescription();
        if (moduleDescription == null) {
            throw new CoreException(new IpsStatus(IStatus.ERROR,
                    "Java project " + javaProject.getElementName() + " has no " + MODULE_INFO_JAVA_FILE_NAME));
        }
        ICompilationUnit moduleCompilationUnit = moduleDescription.getCompilationUnit();
        return moduleCompilationUnit;
    }

    private static RequiresDirective newRequiresDirective(AST ast, boolean transitive, String requiredModule) {
        RequiresDirective exp = ast.newRequiresDirective();
        if (transitive) {
            ModuleModifier transitiveModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
            getModifiers(exp).add(transitiveModifier);
        }
        exp.setName(ast.newName(requiredModule));
        return exp;
    }

    public static boolean isTransitive(RequiresDirective requiresDirective) {
        return getModifiers(requiresDirective).stream().anyMatch(ModuleModifier::isTransitive);
    }

    @SuppressWarnings("unchecked")
    private static List<ModuleModifier> getModifiers(RequiresDirective requiresDirective) {
        return requiresDirective.modifiers();
    }
}
