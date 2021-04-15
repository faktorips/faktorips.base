/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.filter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.filter.IProductCmptPropertyFilter;
import org.faktorips.devtools.core.ui.filter.IPropertyVisibleController;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.util.ArgumentCheck;

/**
 * Default implementation of {@link IPropertyVisibleController}.
 * 
 * @author Alexander Weickmann
 */
public class PropertyVisibleController implements IPropertyVisibleController {

    /*
     * The outer map is necessary as otherwise, the property mappings would be overwritten as soon
     * as multiple editors are opened.
     */
    private final Map<Control, Map<IProductCmptProperty, Set<Control>>> propertyControlMappings = new HashMap<>();

    private final Set<IProductCmptPropertyFilter> filters = new HashSet<>();

    private Runnable refreshCallback;

    /**
     * {@inheritDoc}
     * <p>
     * For each registered {@link IProductCmptProperty product component property}, each
     * {@link IProductCmptPropertyFilter product component property filter} added via
     * {@link #addFilter(IProductCmptPropertyFilter)} is asked whether the controls associated to
     * the {@link IProductCmptProperty product component property} are filtered from the UI.
     * <p>
     * If at least one {@link IProductCmptPropertyFilter product component property filter} returns
     * {@code true}, the controls are made invisible. If all {@link IProductCmptPropertyFilter
     * product component property filters} return {@code false}, the control is made visible.
     */
    @Override
    public void updateUI(boolean refresh) {
        updateControlVisibility();
        if (refresh) {
            refreshCallback.run();
        }
    }

    private void updateControlVisibility() {
        for (Control containerControl : propertyControlMappings.keySet()) {
            for (IProductCmptProperty property : propertyControlMappings.get(containerControl).keySet()) {
                updateControlVisibility(containerControl, property);
            }
        }
    }

    private void updateControlVisibility(Control containerControl, IProductCmptProperty property) {
        boolean filtered = isFiltered(property);
        for (Control control : propertyControlMappings.get(containerControl).get(property)) {
            if (!control.isDisposed()) {
                control.setVisible(!filtered);
                Object layoutData = control.getLayoutData();
                ((GridData)layoutData).exclude = filtered;
            }
        }
    }

    @Override
    public boolean addPropertyControlMapping(Control containerControl,
            IProductCmptProperty property,
            Control... controls) {

        // Check the validity of the provided controls
        ArgumentCheck.isTrue(controls.length > 0);
        for (Control control : controls) {
            if (!(control.getLayoutData() instanceof GridData)) {
                throw new IllegalArgumentException();
            }
        }

        // Get the mappings for the outer control
        Map<IProductCmptProperty, Set<Control>> propertyMappings = propertyControlMappings
                .computeIfAbsent(containerControl, $ -> new HashMap<>());

        // Build a set from the provided controls
        Set<Control> controlSet = new HashSet<>();
        controlSet.addAll(Arrays.asList(controls));

        // Check whether there is an equal existing mapping
        Set<Control> existingMapping = propertyMappings.get(property);
        if (existingMapping != null && existingMapping.equals(controlSet)) {
            return false;
        }

        // Create the mapping
        propertyMappings.put(property, controlSet);
        updateControlVisibility(containerControl, property);
        return true;
    }

    @Override
    public boolean removePropertyControlMapping(Control containerControl) {
        return propertyControlMappings.remove(containerControl) != null;
    }

    @Override
    public boolean addFilter(IProductCmptPropertyFilter filter) {
        return filters.add(filter);
    }

    @Override
    public boolean removeFilter(IProductCmptPropertyFilter filter) {
        return filters.remove(filter);
    }

    @Override
    public boolean isFiltered(IProductCmptProperty property) {
        for (IProductCmptPropertyFilter filter : filters) {
            if (filter.isFiltered(property)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addFilters(List<IProductCmptPropertyFilter> filters) {
        for (IProductCmptPropertyFilter filter : filters) {
            addFilter(filter);
        }
    }

    @Override
    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

}
