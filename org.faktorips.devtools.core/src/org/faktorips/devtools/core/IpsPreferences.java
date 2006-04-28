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
import java.util.Locale;

import org.eclipse.jface.preference.IPreferenceStore;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences {
    
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
     * Contant identifying the preference for editing generations with succesors
     * (regardless whether the valid-from-date of the generation lies in the past or not).
     */
    public final static String EDIT_GENERATION_WITH_SUCCESSOR = IpsPlugin.PLUGIN_ID + ".editGenerationWithSuccessor"; //$NON-NLS-1$
    
    /**
     * Constant identifying the enable generating preference.
     */
    public final static String ENABLE_GENERATING = IpsPlugin.PLUGIN_ID + ".enableGenerating"; //$NON-NLS-1$
    
    public final static String NAVIGATE_TO_MODEL = IpsPlugin.PLUGIN_ID + ".navigateToModel"; //$NON-NLS-1$
    
    private IPreferenceStore prefStore;
    
    public IpsPreferences(IPreferenceStore prefStore) {
    	ArgumentCheck.notNull(prefStore);
    	this.prefStore = prefStore;
    	prefStore.setDefault(NULL_REPRESENTATION_STRING, "<null>"); //$NON-NLS-1$
    	prefStore.setDefault(WORKING_DATE, XmlUtil.gregorianCalendarToXmlDateString(new GregorianCalendar()));
    	prefStore.setDefault(CHANGES_OVER_TIME_NAMING_CONCEPT, IChangesOverTimeNamingConvention.VAA);
    	prefStore.setDefault(EDIT_RECENT_GENERATION, false);
    	if (Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage())) {
        	prefStore.setDefault(DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX, "Typ"); //$NON-NLS-1$
    	} else {
    		prefStore.setDefault(DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX, "Type"); //$NON-NLS-1$
    	}
    	prefStore.setDefault(ENABLE_GENERATING, true);
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
		IpsPlugin.getDefault().getIpsPreferences().prefStore
				.setValue(WORKING_DATE, XmlUtil
						.gregorianCalendarToXmlDateString(newDate));
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
     * Returns whether generations with succesor generations can be edited.
     */
    public boolean canEditGenerationsWithSuccesor() {
    	return prefStore.getBoolean(EDIT_GENERATION_WITH_SUCCESSOR);
    }
    
    /**
     * Returns the value of the enable generating preference.
     */
    public boolean getEnableGenerating(){
    	return prefStore.getBoolean(ENABLE_GENERATING); 
    }
    
    /**
     * Returns whether the navigation from product component to model is active
     * (<code>true</code>) or not.
     */
    public boolean canNavigateToModel() {
    	return prefStore.getBoolean(NAVIGATE_TO_MODEL);
    }
}
