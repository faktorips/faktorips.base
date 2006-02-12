package org.faktorips.devtools.core;

import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.util.XmlUtil;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences {
    
	/**
     * Constant identifiying the working date preference
     */
    public final static String WORKING_DATE = IpsPlugin.PLUGIN_ID + ".workingdate";
	
    public static final String NULL_REPRESENTATION_STRING = IpsPlugin.PLUGIN_ID + ".nullRepresentationString";

    /**
     * Constant identifying the changes over time naming concept preference.
     */
    public final static String CHANGES_OVER_TIME_NAMING_CONCEPT = IpsPlugin.PLUGIN_ID + ".changesOverTimeConcept";
    
    /**
     * Returns the working date preference.
     */
    // TODO static Zugriff entfernen
    public final static GregorianCalendar getWorkingDate() {
        String date = IpsPlugin.getDefault().getPreferenceStore().getString(WORKING_DATE);
        if (StringUtils.isEmpty(date)) {
            return new GregorianCalendar();
        }
        try {
            return XmlUtil.parseXmlDateStringToGregorianCalendar(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the naming convention used in the GUI for product changes over time.  
     */
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
    	String convention = IpsPlugin.getDefault().getPreferenceStore().getString(CHANGES_OVER_TIME_NAMING_CONCEPT);
    	if (StringUtils.isEmpty(convention)) {
    		convention = IChangesOverTimeNamingConvention.VAA;
        	IpsPlugin.getDefault().getPreferenceStore().setValue(NULL_REPRESENTATION_STRING, convention);
    	}
    	return IpsPlugin.getDefault().getIpsModel().getChangesOverTimeNamingConvention(convention);
    }
    
    /**
     * Returns the string to represent null values to the user. 
     */
    public final String getNullPresentation() {
    	String ret = IpsPlugin.getDefault().getPreferenceStore().getString(NULL_REPRESENTATION_STRING);
    	if (StringUtils.isEmpty(ret)) {
    		ret = "<null>";
        	IpsPlugin.getDefault().getPreferenceStore().setValue(NULL_REPRESENTATION_STRING, ret);
    	}
    	return ret;
    }

    public IpsPreferences() {
    }

}
