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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IpsElementNamesMatcher extends TypeSafeMatcher<Iterable<? extends IIpsElement>> {

    private final List<String> filenames;
    private boolean subset;

    private IpsElementNamesMatcher(boolean subset, String... filenames) {
        this.filenames = Arrays.asList(filenames);
        this.subset = subset;
    }

    public static Matcher<? super Iterable<? extends IIpsElement>> containsInOrder(final String... filenames) {
        return new IpsElementNamesMatcher(false, filenames);
    }

    public static Matcher<? super Iterable<? extends IIpsElement>> containsSubsetInOrder(final String... filenames) {
        return new IpsElementNamesMatcher(true, filenames);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a collection " + (subset ? "containing" : "matching") + " in order ");
        description.appendValueList("[", ", ", "]", filenames);
    }

    @Override
    protected void describeMismatchSafely(Iterable<? extends IIpsElement> items, Description description) {
        description.appendValueList("[", ", ", "]", map(items));
    }

    private List<String> map(Iterable<? extends IIpsElement> items) {
        ArrayList<String> result = new ArrayList<>();
        for (IIpsElement item : items) {
            result.add(getName(item));
        }
        return result;
    }

    @Override
    protected boolean matchesSafely(Iterable<? extends IIpsElement> elements) {
        Iterator<? extends IIpsElement> input = elements.iterator();
        for (String filename : filenames) {
            try {
                String nextToMatch;
                do {
                    nextToMatch = getName(input.next());
                } while (subset && !nextToMatch.equals(filename));
                if (!subset && !nextToMatch.equals(filename)) {
                    return false;
                }
            } catch (NoSuchElementException e) {
                return false;
            }
        }
        return subset || !input.hasNext();
    }

    private String getName(IIpsElement element) {
        if (element instanceof IIpsSrcFile) {
            return ((IIpsSrcFile)element).getQualifiedNameType().getName();
        } else if (element instanceof IIpsObject) {
            return ((IIpsObject)element).getQualifiedNameType().getName();
        }

        return element.getName();
    }
}