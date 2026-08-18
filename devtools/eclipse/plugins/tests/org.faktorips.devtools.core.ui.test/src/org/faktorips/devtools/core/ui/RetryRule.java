/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;

public class RetryRule implements TestExecutionExceptionHandler {

    private static final int DEFAULT_RETRY_COUNT = 3;

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Class<?> testClass = context.getRequiredTestClass();
        Object testInstance = context.getRequiredTestInstance();
        Method testMethod = context.getRequiredTestMethod();

        Throwable lastFailure = throwable;
        for (int i = 1; i < DEFAULT_RETRY_COUNT; i++) {
            try {
                for (Method m : AnnotationSupport.findAnnotatedMethods(
                        testClass, AfterEach.class, HierarchyTraversalMode.BOTTOM_UP)) {
                    m.invoke(testInstance);
                }
                for (Method m : AnnotationSupport.findAnnotatedMethods(
                        testClass, BeforeEach.class, HierarchyTraversalMode.TOP_DOWN)) {
                    m.invoke(testInstance);
                }
                testMethod.invoke(testInstance);
                return;
            } catch (InvocationTargetException e) {
                lastFailure = e.getCause() != null ? e.getCause() : e;
                System.err.println("Retry " + i + "/" + (DEFAULT_RETRY_COUNT - 1)
                        + " for test " + context.getDisplayName());
            }
        }
        throw lastFailure;
    }
}
