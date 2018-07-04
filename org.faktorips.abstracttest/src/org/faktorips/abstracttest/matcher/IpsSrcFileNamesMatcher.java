package org.faktorips.abstracttest.matcher;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IpsSrcFileNamesMatcher extends TypeSafeMatcher<IIpsElement[]> {
    private final String[] filenames;
    private Deque<String> namesToMatch;

    private IpsSrcFileNamesMatcher(String[] filenames) {
        this.filenames = filenames;
        namesToMatch = new LinkedList<String>(Arrays.asList(filenames));
    }

    public static Matcher<? super IIpsElement[]> containsInOrder(final String... filenames) {
        return new IpsSrcFileNamesMatcher(filenames);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("An array containing in order ");
        description.appendValueList("[", ", ", "]", filenames);
    }

    @Override
    protected boolean matchesSafely(IIpsElement[] elements) {
        for (IIpsElement element : elements) {
            String name = null;
            if (element instanceof IIpsSrcFile) {
                name = ((IIpsSrcFile)element).getQualifiedNameType().getFileName();
            }
            if (element instanceof IIpsPackageFragment) {
                name = ((IIpsPackageFragment)element).getLastSegmentName();
            }
            if (namesToMatch.contains(name)) {
                if (!namesToMatch.pop().equals(name)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return namesToMatch.isEmpty();
    }
}