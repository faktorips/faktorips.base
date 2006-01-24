package org.faktorips.devtools.core;

import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.PMChangesInTimeNamingConvention;
import org.faktorips.devtools.core.model.IChangesInTimeNamingConvention;
import org.faktorips.util.XmlUtil;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences {
    
    /**
     * Constant identifiying the working date preference
     */
    public final static String WORKING_DATE = IpsPlugin.PLUGIN_ID + ".workingdate";
    
    /**
     * Returns the working date preference.
     */
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
     * Returns the naming convention used for product changes over time. 
     */
    public final static IChangesInTimeNamingConvention getChangesInTimeNamingConvention() {
    	return PMChangesInTimeNamingConvention.getInstance();
    }
    
    private IpsPreferences() {
    }

}
