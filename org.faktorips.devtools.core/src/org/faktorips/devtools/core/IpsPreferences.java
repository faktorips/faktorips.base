/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.eclipse.jface.preference.IPreferenceStore;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences {
    
	/**
     * Constant identifiying the working mode
     */
    public final static String WORKING_MODE = IpsPlugin.PLUGIN_ID + ".workingmode"; //$NON-NLS-1$
	
	/**
     * Constant identifiying the working mode edit
     */
    public final static String WORKING_MODE_EDIT = "edit"; //$NON-NLS-1$

    /**
     * Constant identifiying the working mode browse
     */
    public final static String WORKING_MODE_BROWSE = "browse"; //$NON-NLS-1$
    
	/**
     * Constant identifiying the working date preference
     */
    public final static String WORKING_DATE = IpsPlugin.PLUGIN_ID + ".workingdate"; //$NON-NLS-1$
	
    /**
     * Constant identifying the preference for null-value representation
     */
    public static final String NULL_REPRESENTATION_STRING = IpsPlugin.PLUGIN_ID + ".nullRepresentationString"; //$NON-NLS-1$
    
    /**
     * Constant identifiying the preference for editing genrations with
     * valid-from-dates in the past.
     */
    public static final String EDIT_RECENT_GENERATION = IpsPlugin.PLUGIN_ID + ".editRecentGeneration"; //$NON-NLS-1$

    /**
     * Constant identifying the changes over time naming concept preference.
     */
    public final static String CHANGES_OVER_TIME_NAMING_CONCEPT = IpsPlugin.PLUGIN_ID + ".changesOverTimeConcept"; //$NON-NLS-1$
    
    /**
     * Constant identifying the default postfix for product component types
     */
    public final static String DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX = IpsPlugin.PLUGIN_ID + ".defaultProductCmptTypePostfix"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for editing the runtime id.
     */
    public final static String MODIFY_RUNTIME_ID = IpsPlugin.PLUGIN_ID + ".modifyRuntimeId"; //$NON-NLS-1$

    /**
     * Constant identifying the enable generating preference.
     */
    public final static String ENABLE_GENERATING = IpsPlugin.PLUGIN_ID + ".enableGenerating"; //$NON-NLS-1$
    
    public final static String NAVIGATE_TO_MODEL = IpsPlugin.PLUGIN_ID + ".navigateToModel"; //$NON-NLS-1$
    
    /**
     * Constant identifying the ips test runner max heap size preference.
     */
    public final static String IPSTESTRUNNER_MAX_HEAP_SIZE = IpsPlugin.PLUGIN_ID + ".ipsTestTunnerMaxHeapSize"; //$NON-NLS-1$
    
    
    private IPreferenceStore prefStore;
    
    public IpsPreferences(IPreferenceStore prefStore) {
    	ArgumentCheck.notNull(prefStore);
    	this.prefStore = prefStore;
    	prefStore.setDefault(NULL_REPRESENTATION_STRING, "<null>"); //$NON-NLS-1$
    	prefStore.setDefault(WORKING_DATE, XmlUtil.gregorianCalendarToXmlDateString(new GregorianCalendar()));
    	prefStore.setDefault(CHANGES_OVER_TIME_NAMING_CONCEPT, IChangesOverTimeNamingConvention.VAA);
    	prefStore.setDefault(EDIT_RECENT_GENERATION, false);
    	prefStore.setDefault(MODIFY_RUNTIME_ID, false);
    	prefStore.setDefault(WORKING_MODE, WORKING_MODE_EDIT);
    	prefStore.setDefault(ENABLE_GENERATING, true);
        prefStore.setDefault(IPSTESTRUNNER_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
    }

    /**
     * Returns the working date preference.
     */
    public final GregorianCalendar getWorkingDate() {
    	String date = IpsPlugin.getDefault().getIpsPreferences().prefStore.getString(WORKING_DATE);
        try {
            return XmlUtil.parseXmlDateStringToGregorianCalendar(date);
        } catch (Exception e) {
            return new GregorianCalendar();
        }
    }

    /**
	 * Set the working date to the given one.
	 */
	public final void setWorkingDate(GregorianCalendar newDate) {
		prefStore.setValue(WORKING_DATE, XmlUtil.gregorianCalendarToXmlDateString(newDate));
	}
    
    /**
     * Returns the naming convention used in the GUI for product changes over time.  
     */
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
    	String convention = IpsPlugin.getDefault().getPreferenceStore().getString(CHANGES_OVER_TIME_NAMING_CONCEPT);
    	return IpsPlugin.getDefault().getIpsModel().getChangesOverTimeNamingConvention(convention);
    }
    
    /**
     * Returns the string to represent null values to the user. 
     */
    public final String getNullPresentation() {
    	return prefStore.getString(NULL_REPRESENTATION_STRING);
    }
    
    /**
     * Sets the new presentation for <code>null</code>.
     */
    public final void setNullPresentation(String newPresentation) {
        prefStore.setValue(NULL_REPRESENTATION_STRING, newPresentation);
    }

    /**
     * Returns the postfix used to create a default name for a product component type for a 
     * given policy component type name.
     */
    public final String getDefaultProductCmptTypePostfix() {
    	return prefStore.getString(DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX);
    }

    /**
     * Returns a default locale date format for valid-from and effective dates.
     */
    public DateFormat getValidFromFormat() {
    	return DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
    }
    
    /**
     * Convenience method to get the formatted working date using the format
     * returned by <code>getValidFromFormat</code>
     */
    public String getFormattedWorkingDate() {
    	return getValidFromFormat().format(getWorkingDate().getTime());
    }
    
    /**
     * Returns whether generations with valid-from-date in the past can be edited or not.
     */
    public boolean canEditRecentGeneration() {
    	return prefStore.getBoolean(EDIT_RECENT_GENERATION);
    }
    
    /**
     * Returns the value of the enable generating preference.
     */
    public boolean getEnableGenerating(){
        return prefStore.getBoolean(ENABLE_GENERATING); 
    }
    public void setEnableGenerating(boolean generate){
        IpsPlugin.getDefault().getIpsPreferences().prefStore
        .setValue(ENABLE_GENERATING, generate);
    }
    
    /**
     * Returns the max heap size in megabytes for the ips test runner. This parameter specify the
     * maximum size of the memory allocation pool for the test runner. Will be used to set the Xmx
     * Java virtual machines option for the ips test runner virtual machine.
     */
    public String getIpsTestRunnerMaxHeapSize(){
        return prefStore.getString(IPSTESTRUNNER_MAX_HEAP_SIZE);
    }
    
    /**
     * Returns whether the navigation from product component to model is active
     * (<code>true</code>) or not.
     */
    public boolean canNavigateToModel() {
    	return prefStore.getBoolean(NAVIGATE_TO_MODEL);
    }
    
    /**
	 * Returns <code>true</code> if the currently set working mode is edit, <code>false</code>
	 * otherwise
	 */
	public boolean isWorkingModeEdit() {
		return prefStore.getString(WORKING_MODE).equals(WORKING_MODE_EDIT);
	}

	/**
	 * Returns <code>true</code> if the currently set working mode is browse, <code>false</code>
	 * otherwise.
	 */
	public boolean isWorkingModeBrowse() {
		return prefStore.getString(WORKING_MODE).equals(WORKING_MODE_BROWSE);
	}
	
	public boolean canModifyRuntimeId() {
		return prefStore.getBoolean(MODIFY_RUNTIME_ID);
	}
}
