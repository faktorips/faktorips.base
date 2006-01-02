package org.faktorips.devtools.core;

import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.faktorips.util.XmlUtil;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences {
    
    /**
     * Constant identifiying the working date preference
     */
    public final static String WORKING_DATE = "org.openips.workingdate";
    
    /**
     * Constant identifiying the working date preference
     */
    public final static String GENERATED_JAVA_SOURCECODE_LANGUAGE = "org.openips.generated_javasourcecode_language";
    
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

    private IpsPreferences() {
    }

}
