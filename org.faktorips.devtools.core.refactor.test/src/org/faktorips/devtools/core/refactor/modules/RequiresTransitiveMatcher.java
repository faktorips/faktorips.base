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

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.ModuleDirective;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

final class RequiresTransitiveMatcher extends TypeSafeMatcher<IJavaProject> {
    private final String requiredModuleName;
    private final boolean transitive;
    private boolean isRequired;
    private boolean isTransitive;

    private RequiresTransitiveMatcher(String requiredModuleName, boolean transitive) {
        this.requiredModuleName = requiredModuleName;
        this.transitive = transitive;
    }

    public static Matcher<IJavaProject> requires(String requiredModuleName) {
        return new RequiresTransitiveMatcher(requiredModuleName, false);
    }

    public static Matcher<IJavaProject> requiresTransitive(String requiredModuleName) {
        return new RequiresTransitiveMatcher(requiredModuleName, true);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a Java project that");
        if (transitive) {
            description.appendText(" transitively");
        }
        description.appendText(" requires the module ");
        description.appendText(requiredModuleName);
    }

    @Override
    protected void describeMismatchSafely(IJavaProject item, Description mismatchDescription) {
        if (isRequired) {
            if (transitive) {
                mismatchDescription.appendText("the requires statement misses the transitive modifier");
            } else {
                mismatchDescription.appendText("the requires statement has the transitive modifier");
            }
        } else {
            mismatchDescription.appendText(requiredModuleName);
            mismatchDescription.appendText("is not required");
        }
    }

    @Override
    protected boolean matchesSafely(IJavaProject javaProject) {
        isRequired = false;
        try {
            Optional<RequiresDirective> requiresDirective = findRequiresDirective(javaProject,
                    requiredModuleName);
            if (requiresDirective.isEmpty()) {
                return false;
            }
            isRequired = true;
            isTransitive = Modules.isTransitive(requiresDirective.get());
            return isTransitive == transitive;
        } catch (JavaModelException e) {
            return false;
        }
    }

    private static ModuleDeclaration getModuleDeclaration(IJavaProject javaProject) throws JavaModelException {
        return SharedASTProviderCore
                .getAST(javaProject.getModuleDescription().getCompilationUnit(), SharedASTProviderCore.WAIT_YES,
                        null)
                .getModule();
    }

    private static Optional<RequiresDirective> findRequiresDirective(IJavaProject javaProject,
            String requiredModuleName)
            throws JavaModelException {
        ModuleDeclaration moduleDeclaration = getModuleDeclaration(javaProject);
        @SuppressWarnings("unchecked")
        List<ModuleDirective> moduleStatements = moduleDeclaration.moduleStatements();
        return moduleStatements.stream()
                .filter(RequiresDirective.class::isInstance)
                .map(RequiresDirective.class::cast)
                .filter(r -> r.getName().getFullyQualifiedName().equals(requiredModuleName))
                .findFirst();
    }
}
