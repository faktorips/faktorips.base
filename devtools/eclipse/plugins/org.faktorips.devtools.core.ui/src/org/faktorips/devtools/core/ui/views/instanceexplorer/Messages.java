/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.instanceexplorer.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String InstanceExplorer_tooltipRefreshContents;
    public static String InstanceExplorer_tooltipClear;
    public static String InstanceExplorer_tooltipSubtypeSearch;
    public static String InstanceExplorer_enumContainsValues;
    public static String InstanceExplorer_noInstancesFoundInProject;
    public static String InstanceExplorer_infoMessageEmptyView;
    public static String InstanceExplorer_tryToSearchSubtypes;
    public static String InstanceExplorer_noMetaClassFound;
    public static String InstanceExplorer_waitingLabel;

}
