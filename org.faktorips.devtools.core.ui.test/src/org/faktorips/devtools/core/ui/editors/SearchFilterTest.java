/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.devtools.core.ui.editors.SearchBar.SearchFilter;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SearchFilterTest {

    private TableViewer tableViewer;
    private ITableLabelProvider labelProvider;
    private SearchFilter searchFilter;
    private Object parentElement = "table";
    private Object element = "row";

    @Before
    public void setUp() {
        tableViewer = new TableViewer(new Shell());
        Table table = tableViewer.getTable();
        new TableColumn(table, SWT.LEFT);
        new TableColumn(table, SWT.LEFT);
        new TableColumn(table, SWT.LEFT);
        tableViewer.setColumnProperties(new String[] { "ColumnA", "ColumnB", "ColumnC" });
        labelProvider = mock(ITableLabelProvider.class);
        tableViewer.setLabelProvider(labelProvider);

        searchFilter = new SearchFilter();
    }

    private void mockRow(String value0, String value1, String value2) {
        when(labelProvider.getColumnText(element, 0)).thenReturn(value0);
        when(labelProvider.getColumnText(element, 1)).thenReturn(value1);
        when(labelProvider.getColumnText(element, 2)).thenReturn(value2);
    }

    @Test
    public void testFilterMatchesEmpty() {
        assertThat("", matchesRow("foo", "bar", "baz"));
    }

    @Test
    public void testFilterMatchesSingleColumnExactly() {
        assertThat("foo", matchesRow("", "foo", "bar"));
    }

    @Test
    public void testFilterDoesNotMatchPartially() {
        assertThat("foo", doesNotMatchRow("", "foobar", "bar"));
    }

    @Test
    public void testFilterMatchesSingleColumnWithSingleWildcard() {
        assertThat("foo?", matchesRow("", "foob", "bar"));
    }

    @Ignore("Eclipse's SearchPattern does not appear to take the Javadoc of SearchPattern.RULE_PATTERN_MATCH seriously and treats ? like * when it's at the end of the search term")
    @Test
    public void testFilterDoesNotMatchPartiallyWithSingleWildcardAtTheEnd() {
        assertThat("foo?", doesNotMatchRow("", "foobar", "bar"));
    }

    @Test
    public void testFilterDoesNotMatchPartiallyWithSingleWildcard() {
        assertThat("foo?r", doesNotMatchRow("", "foobar", "bar"));
    }

    @Test
    public void testFilterMatchesWithSingleWildcard() {
        assertThat("foo?ar", matchesRow("", "foobar", "bar"));
    }

    @Test
    public void testFilterMatchesSingleColumnWithWildcard() {
        assertThat("foo*", matchesRow("", "foobar", "bar"));
    }

    @Test
    public void testFilterMatchesMultipleColumnsWithWildcard() {
        assertThat("foo*bar", matchesRow("", "foo", "bar"));
    }

    @Test
    public void testFilterMatchesMultipleColumnsPartiallyWithWildcard() {
        assertThat("foo*bar", matchesRow("", "fool", "bart"));
    }

    @Test
    public void testFilterMatchesMultipleAdjacentColumnsWithSeparator() {
        assertThat("foo|bar", matchesRow("", "foo", "bar"));
    }

    @Test
    public void testFilterMatchesMultipleColumnsWithSeparator() {
        assertThat("foo|bar", matchesRow("foo", "", "bar"));
    }

    @Test
    public void testFilterDoesNotMatchMultipleColumnsWithSeparatorWhenInWrongOrder() {
        assertThat("foo|bar", doesNotMatchRow("bar", "foo", "baz"));
    }

    @Test
    public void testFilterMatchesMultipleColumnsWithSeparatorAndWildcards() {
        assertThat("f*|bar", matchesRow("foo", "", "bar"));
    }

    @Test
    public void testFilterDoesNotMatchMultipleColumnsWithSeparatorAndWildcardsIfWildcardDoesNotMatch() {
        assertThat("f*|bar", doesNotMatchRow("zoo", "", "bar"));
    }

    private Matcher<String> matchesRow(String column0, String column1, String column2) {
        return new RowPatternDoesMatchMatcher(column0, column2, column1);
    }

    private Matcher<String> doesNotMatchRow(String column0, String column1, String column2) {
        return new RowPatternDoesNotMatchMatcher(column0, column2, column1);
    }

    private final class RowPatternDoesMatchMatcher extends RowPatternMatcher {

        private RowPatternDoesMatchMatcher(String column0, String column2, String column1) {
            super(column0, column2, column1);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("matches ");
            super.describeTo(description);
        }

        @Override
        protected void describeMismatchSafely(String item, Description mismatchDescription) {
            super.describeMismatchSafely(item, mismatchDescription);
            mismatchDescription.appendText(" didn't match");
        }
    }

    private final class RowPatternDoesNotMatchMatcher extends RowPatternMatcher {

        private RowPatternDoesNotMatchMatcher(String column0, String column2, String column1) {
            super(column0, column2, column1);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("does not match ");
            super.describeTo(description);
        }

        @Override
        protected boolean matchesSafely(String pattern) {
            return !super.matchesSafely(pattern);
        }

        @Override
        protected void describeMismatchSafely(String item, Description mismatchDescription) {
            super.describeMismatchSafely(item, mismatchDescription);
            mismatchDescription.appendText(" did match");
        }
    }

    private class RowPatternMatcher extends TypeSafeMatcher<String> {
        private final String column0;
        private final String column2;
        private final String column1;

        private RowPatternMatcher(String column0, String column2, String column1) {
            super(String.class);
            this.column0 = column0;
            this.column2 = column2;
            this.column1 = column1;
        }

        @Override
        public void describeTo(Description description) {
            description
                    .appendText("the row \"" + column0 + "\", \"" + column1 + "\", \"" + column2 + "\" ");
        }

        @Override
        protected boolean matchesSafely(String pattern) {
            mockRow(column0, column1, column2);
            searchFilter.setPattern(pattern);
            return searchFilter.select(tableViewer, parentElement, element);
        }

        @Override
        protected void describeMismatchSafely(String item, Description mismatchDescription) {
            mismatchDescription.appendValue(item);
        }
    }

}
