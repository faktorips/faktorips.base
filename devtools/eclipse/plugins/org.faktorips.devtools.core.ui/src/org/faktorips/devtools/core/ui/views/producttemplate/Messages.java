/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.producttemplate.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String InheritCardinalitiesFromTemplateAction_tooltip;

    public static String InheritCardinalitiesFromTemplateAction_label;

    public static String SwitchTemplatePropertyValueOperation_progress;

    public static String SetTemplateValueStatusOperation_progress;

    public static String TemplatePropertyUsageView_ClearActionTooltip;

    public static String TemplatePropertyUsageView_DifferingValues_label;

    public static String TemplatePropertyUsageView_InheritedValue_labelWithoutValue;

    public static String TemplatePropertyUsageView_DifferingValues_fallbackLabel;

    public static String TemplatePropertyUsageView_DifferingValues_valueLabel;

    public static String TemplatePropertyUsageView_DifferingValues_sameValueLabel;

    public static String TemplatePropertyUsageView_DifferingValues_deletedValueLabel;

    public static String TemplatePropertyUsageView_InheritedValue_label;

    public static String TemplatePropertyUsageView_InheritedValue_fallbackLabel;

    public static String TemplatePropertyUsageView_toolTipRefreshContents;

}
