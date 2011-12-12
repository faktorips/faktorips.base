/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.IProductCmptPropertyFilter;
import org.faktorips.devtools.core.ui.IPropertyVisibleController;
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
    private final Map<Control, Map<IProductCmptProperty, Set<Control>>> propertyControlMappings = new HashMap<Control, Map<IProductCmptProperty, Set<Control>>>();

    private final Set<IProductCmptPropertyFilter> filters = new HashSet<IProductCmptPropertyFilter>();

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
     * <p>
     * Finally, {@link Composite#layout()} is called on the parents of the controls in order to
     * adapt the layout with respect to the new conditions.
     */
    @Override
    public void updateUI() {
        updateControlVisibility();
        relayoutParents();
    }

    private void updateControlVisibility() {
        for (Control containerControl : propertyControlMappings.keySet()) {
            for (IProductCmptProperty property : propertyControlMappings.get(containerControl).keySet()) {
                boolean filtered = false;
                for (IProductCmptPropertyFilter filter : filters) {
                    filtered = filter.isFiltered(property);
                    if (filtered) {
                        break;
                    }
                }
                for (Control control : propertyControlMappings.get(containerControl).get(property)) {
                    control.setVisible(!filtered);
                    Object layoutData = control.getLayoutData();
                    ((GridData)layoutData).exclude = filtered;
                }
            }
        }
    }

    private void relayoutParents() {
        Set<Composite> parents = new HashSet<Composite>();
        for (Control containerControl : propertyControlMappings.keySet()) {
            for (IProductCmptProperty property : propertyControlMappings.get(containerControl).keySet()) {
                for (Control control : propertyControlMappings.get(containerControl).get(property)) {
                    parents.add(control.getParent());
                }
            }
        }
        for (Composite parent : parents) {
            parent.layout();
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
        Map<IProductCmptProperty, Set<Control>> propertyMappings = propertyControlMappings.get(containerControl);
        if (propertyMappings == null) {
            propertyMappings = new HashMap<IProductCmptProperty, Set<Control>>();
            propertyControlMappings.put(containerControl, propertyMappings);
        }

        // Build a set from the provided controls
        Set<Control> controlSet = new HashSet<Control>();
        controlSet.addAll(Arrays.asList(controls));

        // Check whether there is an equal existing mapping
        Set<Control> existingMapping = propertyMappings.get(property);
        if (existingMapping != null && existingMapping.equals(controlSet)) {
            return false;
        }

        // Create the mapping
        propertyMappings.put(property, controlSet);
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

}
