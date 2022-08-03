/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.matcher;

import org.faktorips.devtools.abstraction.AFile;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ExistsAFileMatcher extends TypeSafeMatcher<AFile> {

    public static Matcher<? super AFile> exists() {
        return new ExistsAFileMatcher();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("exists");
    }

    @Override
    protected boolean matchesSafely(AFile file) {
        return file.exists();
    }
}
