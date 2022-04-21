/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;

public class Deprecation {

    private final String sinceVersionString;
    private final boolean forRemoval;

    public Deprecation(String sinceVersionString, boolean forRemoval) {
        this.sinceVersionString = IpsStringUtils.isBlank(sinceVersionString) ? null : sinceVersionString;
        this.forRemoval = forRemoval;
    }

    public Optional<String> getSinceVersion() {
        return Optional.ofNullable(sinceVersionString);
    }

    public boolean isMarkedForRemoval() {
        return forRemoval;
    }

    public static Optional<Deprecation> of(AnnotatedDeclaration annotatedDeclaration) {
        Deprecated deprecated = annotatedDeclaration.get(Deprecated.class);
        return of(deprecated);
    }

    public static Optional<Deprecation> of(Method method) {
        Deprecated deprecated = method.getAnnotation(Deprecated.class);
        return of(deprecated);
    }

    public static Optional<Deprecation> of(Field field) {
        Deprecated deprecated = field.getAnnotation(Deprecated.class);
        return of(deprecated);
    }

    private static Optional<Deprecation> of(Deprecated deprecated) {
        if (deprecated != null) {
            String sinceVersionString = null;
            boolean forRemoval = false;
            try {
                Method sinceMethod = deprecated.getClass().getDeclaredMethod("since");
                sinceVersionString = (String)ModelElement.invokeMethod(sinceMethod, deprecated);
                Method forRemovalMethod = deprecated.getClass().getDeclaredMethod("forRemoval");
                forRemoval = (boolean)ModelElement.invokeMethod(forRemovalMethod, deprecated);
            } catch (NoSuchMethodException | SecurityException e) {
                // still Java 8
            }
            return Optional.of(new Deprecation(sinceVersionString, forRemoval));
        }
        return Optional.empty();
    }

}
