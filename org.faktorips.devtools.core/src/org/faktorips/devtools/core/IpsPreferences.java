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

package org.faktorips.devtools.core;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences {

    /**
     * Constant identifying the working mode
     */
    public final static String WORKING_MODE = IpsPlugin.PLUGIN_ID + ".workingmode"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode edit
     */
    public final static String WORKING_MODE_EDIT = "edit"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode browse
     */
    public final static String WORKING_MODE_BROWSE = "browse"; //$NON-NLS-1$

    /**
     * Constant identifying the working date preference
     */
    public final static String WORKING_DATE = IpsPlugin.PLUGIN_ID + ".workingdate"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for null-value representation
     */
    public static final String NULL_REPRESENTATION_STRING = IpsPlugin.PLUGIN_ID + ".nullRepresentationString"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for editing generations with valid-from-dates in the
     * past.
     */
    public static final String EDIT_RECENT_GENERATION = IpsPlugin.PLUGIN_ID + ".editRecentGeneration"; //$NON-NLS-1$

    /**
     * Constant identifying the changes over time naming concept preference.
     */
    public final static String CHANGES_OVER_TIME_NAMING_CONCEPT = IpsPlugin.PLUGIN_ID + ".changesOverTimeConcept"; //$NON-NLS-1$

    /**
     * Constant identifying the default postfix for product component types
     */
    public final static String DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX = IpsPlugin.PLUGIN_ID
            + ".defaultProductCmptTypePostfix"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for editing the runtime id.
     */
    public final static String MODIFY_RUNTIME_ID = IpsPlugin.PLUGIN_ID + ".modifyRuntimeId"; //$NON-NLS-1$

    /**
     * Constant identifying the enable generating preference.
     */
    public final static String ENABLE_GENERATING = IpsPlugin.PLUGIN_ID + ".enableGenerating"; //$NON-NLS-1$

    /**
     * Constant that identifies the navigate to model or source generating preference.
     */
    public final static String NAVIGATE_TO_MODEL_OR_SOURCE_CODE = IpsPlugin.PLUGIN_ID + ".navigateToModel"; //$NON-NLS-1$

    /**
     * Constant that identifies the advanced team functions in product definition explorer
     * preference.
     */
    public final static String ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER = IpsPlugin.PLUGIN_ID
            + ".advancedTeamFunctionsInProductDefExplorer"; //$NON-NLS-1$

    /**
     * Constant that identifies the number of sections in type editors preference.
     */
    public final static String SECTIONS_IN_TYPE_EDITORS = IpsPlugin.PLUGIN_ID + ".sectionsInTypeEditors"; //$NON-NLS-1$

    /**
     * Constant that defines 2 sections in type editors preference. This constant is a value for the
     * property <code>SECTIONS_IN_TYPE_EDITORS</code>.
     */
    public final static String TWO_SECTIONS_IN_TYPE_EDITOR_PAGE = IpsPlugin.PLUGIN_ID + ".twoSections"; //$NON-NLS-1$

    /**
     * Constant that defines 4 sections in type editors preference. This constant is a value for the
     * property <code>SECTIONS_IN_TYPE_EDITORS</code>.
     */
    public final static String FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE = IpsPlugin.PLUGIN_ID + ".fourSections"; //$NON-NLS-1$

    /**
     * Constant identifying the IPS test runner max heap size preference.
     */
    public final static String IPSTESTRUNNER_MAX_HEAP_SIZE = IpsPlugin.PLUGIN_ID + ".ipsTestTunnerMaxHeapSize"; //$NON-NLS-1$

    /**
     * Constant identifying the enumeration display type.
     */
    public final static String ENUM_TYPE_DISPLAY = IpsPlugin.PLUGIN_ID + ".enumTypeDisplay"; //$NON-NLS-1$

    /**
     * Constant that identifies if the range edit fields (minimum, maximum and step) will be
     * displayed in one row.
     */
    public final static String RANGE_EDIT_FIELDS_IN_ONE_ROW = IpsPlugin.PLUGIN_ID + ".rangeEditFieldsInOneRow"; //$NON-NLS-1$

    private DatatypeFormatter datatypeFormatter;

    private IPreferenceStore prefStore;

    public IpsPreferences(IPreferenceStore prefStore) {
        ArgumentCheck.notNull(prefStore);
        this.prefStore = prefStore;
        prefStore.setDefault(NULL_REPRESENTATION_STRING, "<null>"); //$NON-NLS-1$
        prefStore.setDefault(CHANGES_OVER_TIME_NAMING_CONCEPT, IChangesOverTimeNamingConvention.FAKTOR_IPS);
        prefStore.setDefault(EDIT_RECENT_GENERATION, false);
        prefStore.setDefault(MODIFY_RUNTIME_ID, false);
        prefStore.setDefault(WORKING_MODE, WORKING_MODE_EDIT);
        prefStore.setDefault(ENABLE_GENERATING, true);
        prefStore.setDefault(IPSTESTRUNNER_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
        prefStore.setDefault(ENUM_TYPE_DISPLAY, EnumTypeDisplay.NAME_AND_ID.getId());
        prefStore.setDefault(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER, false);
        prefStore.setDefault(SECTIONS_IN_TYPE_EDITORS, TWO_SECTIONS_IN_TYPE_EDITOR_PAGE);
        prefStore.setDefault(RANGE_EDIT_FIELDS_IN_ONE_ROW, true);

        if (IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(prefStore.getString(WORKING_DATE))) {
            setWorkingDate(new GregorianCalendar());
        }
        datatypeFormatter = new DatatypeFormatter(this);
    }

    public void addChangeListener(IPropertyChangeListener listener) {
        prefStore.addPropertyChangeListener(listener);
    }

    public void removeChangeListener(IPropertyChangeListener listener) {
        prefStore.removePropertyChangeListener(listener);
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
     * Returns the postfix used to create a default name for a product component type for a given
     * policy component type name.
     */
    public final String getDefaultProductCmptTypePostfix() {
        return prefStore.getString(DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX);
    }

    /**
     * Returns a default locale date format for valid-from and effective dates.
     */
    public DateFormat getDateFormat() {
        return DateFormat.getDateInstance(DateFormat.MEDIUM);
    }

    /**
     * Convenience method to get the formatted working date using the format returned by
     * <code>getValidFromFormat</code>
     */
    public String getFormattedWorkingDate() {
        return getDateFormat().format(getWorkingDate().getTime());
    }

    /**
     * Returns whether generations with valid-from-date in the past can be edited or not.
     */
    public boolean canEditRecentGeneration() {
        return prefStore.getBoolean(EDIT_RECENT_GENERATION);
    }

    /**
     * Sets whether generations with valid-from-date in the past can be edited or not.
     */
    public void setEditRecentGeneration(boolean editRecentGeneration) {
        prefStore.setValue(EDIT_RECENT_GENERATION, editRecentGeneration);
    }

    /**
     * Returns the value of the enable generating preference.
     */
    public boolean getEnableGenerating() {
        return prefStore.getBoolean(ENABLE_GENERATING);
    }

    public void setEnableGenerating(boolean generate) {
        IpsPlugin.getDefault().getIpsPreferences().prefStore.setValue(ENABLE_GENERATING, generate);
    }

    /**
     * Returns the max heap size in megabytes for the IPS test runner. This parameter specify the
     * maximum size of the memory allocation pool for the test runner. Will be used to set the Xmx
     * Java virtual machines option for the IPS test runner virtual machine.
     */
    public String getIpsTestRunnerMaxHeapSize() {
        return prefStore.getString(IPSTESTRUNNER_MAX_HEAP_SIZE);
    }

    /**
     * Returns the enumeration type display. Specifies the text display of enumeration type edit
     * fields. E.g. display id or name only, or display both.
     * 
     * @see EnumTypeDisplay
     */
    public EnumTypeDisplay getEnumTypeDisplay() {
        String id = prefStore.getString(ENUM_TYPE_DISPLAY);
        EnumTypeDisplay enumTypeDisplay = (EnumTypeDisplay)EnumTypeDisplay.getEnumType().getEnumValue(id);
        if (enumTypeDisplay == null) {
            IpsPlugin.log(new IpsStatus("Unknown enum type with id: " + id //$NON-NLS-1$
                    + ". Use default enum type display."));//$NON-NLS-1$
            enumTypeDisplay = EnumTypeDisplay.DEFAULT;
        }
        return enumTypeDisplay;
    }

    /**
     * Sets the enum type display.
     * 
     * @throws NullPointerException if etDisplay is <code>null</code>
     */
    public void setEnumTypeDisplay(EnumTypeDisplay etDisplay) {
        ArgumentCheck.notNull(etDisplay);
        prefStore.setValue(ENUM_TYPE_DISPLAY, etDisplay.getId());
    }

    /**
     * Returns whether the navigation from product component to model is active (<code>true</code>)
     * or not.
     */
    public boolean canNavigateToModelOrSourceCode() {
        return prefStore.getBoolean(NAVIGATE_TO_MODEL_OR_SOURCE_CODE);
    }

    /**
     * Sets the working mode.
     */
    public void setWorkingMode(String workingMode) {
        prefStore.setValue(WORKING_MODE, workingMode);
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

    public boolean areAvancedTeamFunctionsForProductDefExplorerEnabled() {
        return prefStore.getBoolean(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER);
    }

    public void setAvancedTeamFunctionsForProductDefExplorerEnabled(boolean enabled) {
        prefStore.setValue(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER, enabled);
    }

    /**
     * Sets the number of sections displayed on a page of a type editor. Only the predefined values
     * TWO_SECTIONS_IN_TYPE_EDITOR_PAGE and FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE are allowed.
     */
    public void setSectionsInTypeEditors(String numberOfSections) {
        // identity on purpose!!
        if (!(numberOfSections == TWO_SECTIONS_IN_TYPE_EDITOR_PAGE || numberOfSections == FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE)) {
            throw new IllegalArgumentException(
                    "Valid argument values are the constants TWO_SECTIONS_IN_TYPE_EDITOR_PAGE or FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE of the IpsPreferences."); //$NON-NLS-1$
        }
        prefStore.setValue(SECTIONS_IN_TYPE_EDITORS, numberOfSections);
    }

    /**
     * Returns the number of sections that are displayed on one page of a type editor.
     */
    public String getSectionsInTypeEditors() {
        return prefStore.getString(SECTIONS_IN_TYPE_EDITORS);
    }

    /**
     * Returns <code>true</code> if the range edit fields (minimum, maximum, and step) should be
     * displayed in one row.
     */
    public boolean isRangeEditFieldsInOneRow() {
        return prefStore.getBoolean(RANGE_EDIT_FIELDS_IN_ONE_ROW);
    }

    /**
     * Sets if the range edit fields (minimum, maximum, and step) should be displayed in one row
     * <code>true</code>.
     */
    public void setRangeEditFieldsInOneRow(boolean enabled) {
        prefStore.setValue(RANGE_EDIT_FIELDS_IN_ONE_ROW, enabled);
    }

    /**
     * Returns the formatter for Faktor-IPS data types.
     */
    public DatatypeFormatter getDatatypeFormatter() {
        return datatypeFormatter;
    }

}
