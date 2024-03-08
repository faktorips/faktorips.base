/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.java;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

final class RefactoringStatusOkMatcher extends TypeSafeMatcher<RefactoringStatus> {

    static Matcher<RefactoringStatus> isOk() {
        return new RefactoringStatusOkMatcher();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a successful refactoring");
    }

    @Override
    protected boolean matchesSafely(RefactoringStatus item) {
        return item.isOK();
    }
}