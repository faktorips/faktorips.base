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

import org.eclipse.core.resources.IFile;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ExistsFileMatcher extends TypeSafeMatcher<IFile> {

    public static Matcher<? super IFile> exists() {
        return new ExistsFileMatcher();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("exists");
    }

    @Override
    protected boolean matchesSafely(IFile file) {
        return file.exists();
    }
}
