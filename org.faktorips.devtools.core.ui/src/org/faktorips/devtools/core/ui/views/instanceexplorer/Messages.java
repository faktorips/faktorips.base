/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.eclipse.osgi.util.NLS;

/**
 * Message for i18n in InstanceExplorer
 * 
 * @author dirmeier
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.instanceexplorer.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    /**
     * 
     */
    public static String InstanceExplorer_tooltipRefreshContents;
    /**
	 * 
	 */
    public static String InstanceExplorer_tooltipClear;
    /**
	 * 
	 */
    public static String InstanceExplorer_tooltipSubtypeSearch;
    /**
	 * 
	 */
    public static String InstanceExplorer_enumContainsValues;
    /**
	 * 
	 */
    public static String InstanceExplorer_noInstancesFoundInProject;
    /**
	 * 
	 */
    public static String InstanceExplorer_infoMessageEmptyView;
    /**
	 * 
	 */
    public static String InstanceExplorer_tryToSearchSubtypes;
    /**
	 * 
	 */
    public static String InstanceExplorer_noMetaClassFound;
}
