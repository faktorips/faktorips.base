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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RetryRule implements TestRule {

    private final int retryCount;

    public RetryRule(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new StatementExtension(description, base);
    }

    private final class StatementExtension extends Statement {
        private final Description description;
        private final Statement base;

        private StatementExtension(Description description, Statement base) {
            this.description = description;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            Throwable failure = null;

            for (int i = 0; i < retryCount; i++) {
                try {
                    base.evaluate();
                    return;
                    // CSOFF: Illegal Catch
                } catch (Throwable t) {
                    // CSON: Illegal Catch
                    failure = t;
                    System.err.println("Retry " + (i + 1) + "/" + retryCount
                            + " for test " + description.getDisplayName());
                }
            }
            if (failure != null) {
                throw failure;
            }
            throw new RuntimeException(
                    "RetryRule run out of tries (" + retryCount + "), but had no underlying Exception.");
        }
    }
}
