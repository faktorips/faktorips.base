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