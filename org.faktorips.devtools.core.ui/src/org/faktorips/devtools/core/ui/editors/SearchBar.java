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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.controller.fields.DefaultEditField;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Search bar for {@link TableViewer}, filtering the table viewer's content by matching the entered
 * search pattern to the table's displayed cell values.
 */
public class SearchBar {

    private final Text searchField;

    private SearchPmo searchPmo;

    private BindingContext bindingContext;

    private UIToolkit toolkit;

    /**
     * Create the search bar. The {@link TableViewer} to be filtered is usually created a little
     * later and then set with {@link #setFilterTo(TableViewer)}.
     */
    public SearchBar(Composite formBody, UIToolkit toolkit) {
        this.toolkit = toolkit;
        Composite searchPanel = toolkit.createLabelEditColumnComposite(formBody);
        GridLayout layout = (GridLayout)searchPanel.getLayout();
        // avoid double boarder
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        toolkit.createLabel(searchPanel, Messages.SearchSelectionBar_searchBarTitle);
        this.searchField = toolkit.createText(searchPanel, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH | SWT.NULL);
        searchField.setMessage(Messages.SearchBar_searchFieldHint);
        searchPmo = new SearchPmo();
        bindingContext = new BindingContext();
        DefaultEditField<?> editField = (DefaultEditField<?>)bindingContext.bindContent(searchField, searchPmo,
                SearchPmo.PROPERTY_PATTERN);
        editField.setSupportsNullStringRepresentation(false);
        formBody.addDisposeListener($ -> bindingContext.dispose());
    }

    /**
     * Sets the table viewer filtered by this search bar.
     */
    public void setFilterTo(final TableViewer tableViewer) {
        bindingContext.add(new PropertyChangeBinding<>(searchField, searchPmo, SearchPmo.PROPERTY_PATTERN,
                String.class) {

            @Override
            protected void propertyChanged(String oldValue, String newValue) {
                tableViewer.refresh(false);
            }
        });
        tableViewer.addFilter(searchPmo.filter);
    }

    public void setEnabled(boolean enabled) {
        toolkit.setEnabled(searchField, enabled);
    }

    public static class SearchPmo extends PresentationModelObject {

        private static final String PROPERTY_PATTERN = "pattern"; //$NON-NLS-1$

        private SearchFilter filter;

        private String filterString = StringUtils.EMPTY;

        public SearchPmo() {
            filter = new SearchFilter();
        }

        public String getPattern() {
            return filterString;
        }

        public void setPattern(String pattern) {
            String oldPattern = getPattern();
            filter.setPattern(pattern);
            filterString = pattern;
            notifyListeners(new PropertyChangeEvent(this, PROPERTY_PATTERN, oldPattern, pattern));
        }
    }

    static class SearchFilter extends ViewerFilter {

        private static final String COLUMN_SEPARATOR = "\\|"; //$NON-NLS-1$
        private static final Pattern WILDCARD_PATTERN = Pattern.compile("[\\*\\?]+"); //$NON-NLS-1$
        private final List<SearchPattern> searchPatterns = new ArrayList<>();
        private boolean containsWildcard;

        public void setPattern(String pattern) {
            searchPatterns.clear();
            Arrays.stream(pattern.split(COLUMN_SEPARATOR))
                    .map(SearchFilter::camelCaseWildcardPattern)
                    .forEach(searchPatterns::add);
            containsWildcard = WILDCARD_PATTERN.matcher(pattern).find();
        }

        private static SearchPattern camelCaseWildcardPattern(String stringPattern) {
            SearchPattern searchPattern = new SearchPattern(SearchPattern.RULE_BLANK_MATCH
                    | SearchPattern.RULE_CAMELCASE_MATCH | SearchPattern.RULE_PATTERN_MATCH);
            searchPattern.setPattern(stringPattern);
            return searchPattern;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            TableViewer tableViewer = (TableViewer)viewer;
            int columnCount = tableViewer.getTable().getColumnCount();
            String[] values = new String[columnCount];
            int matches = 0;

            for (int i = 0; i < columnCount && matches < searchPatterns.size(); i++) {
                values[i] = getLabelProvider(tableViewer, i).getColumnText(element, i);
                if (searchPatterns.get(matches).matches(values[i])) {
                    matches++;
                }
            }
            if (matches == searchPatterns.size()) {
                return true;
            } else if (containsWildcard && searchPatterns.size() == 1) {
                return searchPatterns.get(0).matches(String.join(IpsStringUtils.EMPTY, values));
            }
            return false;
        }

        protected ITableLabelProvider getLabelProvider(TableViewer tableViewer, int i) {
            IBaseLabelProvider labelProvider = tableViewer.getLabelProvider();
            if (labelProvider instanceof ITableLabelProvider) {
                return (ITableLabelProvider)labelProvider;
            } else {
                CellLabelProvider columnLabelProvider = tableViewer.getLabelProvider(i);
                if (columnLabelProvider instanceof ITableLabelProvider) {
                    return (ITableLabelProvider)columnLabelProvider;
                }
            }
            throw new IllegalStateException("Table must have a ITableLabelProvider to be filtered by the search bar"); //$NON-NLS-1$
        }

    }
}
