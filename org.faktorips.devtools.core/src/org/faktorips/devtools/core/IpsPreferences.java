/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import java.awt.GraphicsEnvironment;
import java.awt.im.InputContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.plugin.NamedDataTypeDisplay;
import org.faktorips.devtools.model.preferences.IIpsModelPreferences;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * The class gives access to the plugin's preferences.
 */
public class IpsPreferences implements IIpsModelPreferences {

    /**
     * Constant identifying the refactoring mode
     */
    public static final String REFACTORING_MODE = IpsPlugin.PLUGIN_ID + ".refactoringmode"; //$NON-NLS-1$

    /**
     * Constant identifying the refactoring mode direct
     */
    public static final String REFACTORING_MODE_DIRECT = "direct"; //$NON-NLS-1$

    /**
     * Constant identifying the refactoring mode explicit
     */
    public static final String REFACTORING_MODE_EXPLICIT = "explicit"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode
     */
    public static final String WORKING_MODE = IpsPlugin.PLUGIN_ID + ".workingmode"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode edit
     */
    public static final String WORKING_MODE_EDIT = "edit"; //$NON-NLS-1$

    /**
     * Constant identifying the working mode browse
     */
    public static final String WORKING_MODE_BROWSE = "browse"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for null-value representation
     */
    public static final String NULL_REPRESENTATION_STRING = IpsPlugin.PLUGIN_ID + ".nullRepresentationString"; //$NON-NLS-1$

    /**
     * Constant identifying the changes over time naming concept preference.
     */
    public static final String CHANGES_OVER_TIME_NAMING_CONCEPT = IpsPlugin.PLUGIN_ID + ".changesOverTimeConcept"; //$NON-NLS-1$

    /**
     * Constant identifying the default postfix for product component types
     */
    public static final String DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX = IpsPlugin.PLUGIN_ID
            + ".defaultProductCmptTypePostfix"; //$NON-NLS-1$

    /**
     * Constant identifying the preference for editing the runtime id.
     */
    public static final String MODIFY_RUNTIME_ID = IpsPlugin.PLUGIN_ID + ".modifyRuntimeId"; //$NON-NLS-1$

    /**
     * Constant identifying the enable generating preference.
     */
    public static final String ENABLE_GENERATING = IpsPlugin.PLUGIN_ID + ".enableGenerating"; //$NON-NLS-1$

    /**
     * Constant that identifies the navigate to model or source generating preference.
     */
    public static final String NAVIGATE_TO_MODEL_OR_SOURCE_CODE = IpsPlugin.PLUGIN_ID + ".navigateToModel"; //$NON-NLS-1$

    /**
     * Constant that identifies the advanced team functions in product definition explorer
     * preference.
     */
    public static final String ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER = IpsPlugin.PLUGIN_ID
            + ".advancedTeamFunctionsInProductDefExplorer"; //$NON-NLS-1$

    /**
     * Constant that identifies the easy context menu preferencee
     */
    public static final String SIMPLE_CONTEXT_MENU = IpsPlugin.PLUGIN_ID + ".simpleContextMenu"; //$NON-NLS-1$

    /**
     * Constant that identifies the preference for loading and validating {@link ITableContents}.
     */
    public static final String AUTO_VALIDATE_TABLES = IpsPlugin.PLUGIN_ID + ".autoValidateTables"; //$NON-NLS-1$

    /**
     * Constant that identifies the number of sections in type editors preference.
     */
    public static final String SECTIONS_IN_TYPE_EDITORS = IpsPlugin.PLUGIN_ID + ".sectionsInTypeEditors"; //$NON-NLS-1$

    /**
     * Constant that defines 2 sections in type editors preference. This constant is a value for the
     * property <code>SECTIONS_IN_TYPE_EDITORS</code>.
     */
    public static final String TWO_SECTIONS_IN_TYPE_EDITOR_PAGE = IpsPlugin.PLUGIN_ID + ".twoSections"; //$NON-NLS-1$

    /**
     * Constant that defines 4 sections in type editors preference. This constant is a value for the
     * property <code>SECTIONS_IN_TYPE_EDITORS</code>.
     */
    public static final String FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE = IpsPlugin.PLUGIN_ID + ".fourSections"; //$NON-NLS-1$

    /**
     * Constant identifying the IPS test runner max heap size preference.
     */
    public static final String IPSTESTRUNNER_MAX_HEAP_SIZE = IpsPlugin.PLUGIN_ID + ".ipsTestTunnerMaxHeapSize"; //$NON-NLS-1$

    /**
     * Constant identifying the named data display type. This setting was renamed, but since the
     * setting is stored in the prefsStore we should either never rename this string or migrate the
     * setting in the prefsStore.
     */
    public static final String NAMED_DATA_TYPE_DISPLAY = IpsPlugin.PLUGIN_ID + ".enumTypeDisplay"; //$NON-NLS-1$

    /**
     * Constant that identifies the locale to be used for formating values of specific datatypes.
     */
    public static final String DATATYPE_FORMATTING_LOCALE = IpsPlugin.PLUGIN_ID + ".datatypeFormattingLocale"; //$NON-NLS-1$

    /**
     * Constant that identifies the product copy wizard mode preference.
     */
    public static final String COPY_WIZARD_MODE = IpsPlugin.PLUGIN_ID + ".copyWizardMode"; //$NON-NLS-1$

    /**
     * Constant that identifies the product copy wizard copy mode preference.
     */
    public static final String COPY_WIZARD_MODE_COPY = "copy"; //$NON-NLS-1$

    /**
     * Constant that identifies the product copy wizard link mode preference.
     */
    public static final String COPY_WIZARD_MODE_LINK = "link"; //$NON-NLS-1$

    /**
     * Constant that identifies the product copy wizard smart mode preference.
     */
    public static final String COPY_WIZARD_MODE_SMARTMODE = "smartmode"; //$NON-NLS-1$

    /**
     * Constant that identifies the preference for delay time on change events.
     */
    public static final String CHANGE_EVENT_DELAY_TIME = IpsPlugin.PLUGIN_ID + ".changeEventDelayTime"; //$NON-NLS-1$
    /**
     * Default value in milliseconds for change event delay time
     */
    private static final int DEFAULT_DELAY_TIME = 200;

    private final DatatypeFormatter datatypeFormatter;

    private final IPreferenceStore prefStore;

    public IpsPreferences(IPreferenceStore prefStore) {
        ArgumentCheck.notNull(prefStore);
        this.prefStore = prefStore;
        prefStore.setDefault(NULL_REPRESENTATION_STRING, "<null>"); //$NON-NLS-1$
        prefStore.setDefault(CHANGES_OVER_TIME_NAMING_CONCEPT, IChangesOverTimeNamingConvention.FAKTOR_IPS);
        prefStore.setDefault(MODIFY_RUNTIME_ID, false);
        prefStore.setDefault(REFACTORING_MODE, REFACTORING_MODE_EXPLICIT);
        prefStore.setDefault(WORKING_MODE, WORKING_MODE_EDIT);
        prefStore.setDefault(ENABLE_GENERATING, true);
        prefStore.setDefault(IPSTESTRUNNER_MAX_HEAP_SIZE, ""); //$NON-NLS-1$
        prefStore.setDefault(NAMED_DATA_TYPE_DISPLAY, NamedDataTypeDisplay.NAME_AND_ID.getId());
        prefStore.setDefault(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER, false);
        prefStore.setDefault(SIMPLE_CONTEXT_MENU, true);
        prefStore.setDefault(AUTO_VALIDATE_TABLES, true);
        prefStore.setDefault(SECTIONS_IN_TYPE_EDITORS, TWO_SECTIONS_IN_TYPE_EDITOR_PAGE);
        prefStore.setDefault(CHANGE_EVENT_DELAY_TIME, DEFAULT_DELAY_TIME);
        prefStore.setDefault(COPY_WIZARD_MODE, COPY_WIZARD_MODE_SMARTMODE);

        setDefaultForDatatypeFormatting(prefStore);

        datatypeFormatter = new DatatypeFormatter(this);
    }

    /**
     * Retrieves the locale of the currently used keyboard layout via {@link InputContext} or the
     * java default locale if the inputContext is unavailable (e.g. in
     * headless/server-environments). The retrieved locale is used for datatype formating.
     * 
     * @param prefStore the preference store
     */
    private void setDefaultForDatatypeFormatting(IPreferenceStore prefStore) {
        Locale defaultLocale = null;
        if (GraphicsEnvironment.isHeadless()) {
            defaultLocale = Locale.getDefault();
        } else {
            try {
                InputContext inputContext = InputContext.getInstance();
                if (inputContext != null) {
                    defaultLocale = inputContext.getLocale();
                }
                // CSOFF: IllegalCatch
            } catch (Throwable t) {
                // We also want to catch errors because on a Linux system without a X-Server the
                // virtual machine throws an InternalError!
                IpsPlugin
                        .log(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID,
                                "Cannot load default locale from input context. Use system default locale. (Maybe there is no X Server)")); //$NON-NLS-1$
            }
            // CSON: IllegalCatch
            if (defaultLocale == null || IpsStringUtils.isBlank(defaultLocale.getLanguage())) {
                // FIPS-8512 on MacOS it is possible to configure a keyboard layout with only region
                // and no language, which will show up as a locale like "_US_UserDefined_252"
                defaultLocale = Locale.getDefault();
            }
        }
        prefStore.setDefault(DATATYPE_FORMATTING_LOCALE, defaultLocale.toString());
    }

    public void addChangeListener(IPropertyChangeListener listener) {
        prefStore.addPropertyChangeListener(listener);
    }

    public void removeChangeListener(IPropertyChangeListener listener) {
        prefStore.removePropertyChangeListener(listener);
    }

    /**
     * Returns the naming convention used in the GUI for product changes over time.
     */
    @Override
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
        String convention = IpsPlugin.getDefault().getPreferenceStore().getString(CHANGES_OVER_TIME_NAMING_CONCEPT);
        return IIpsModel.get().getChangesOverTimeNamingConvention(convention);
    }

    /**
     * Returns the string to represent null values to the user.
     */
    @Override
    public String getNullPresentation() {
        return prefStore.getString(NULL_REPRESENTATION_STRING);
    }

    /**
     * Sets the new presentation for <code>null</code>.
     */
    public void setNullPresentation(String newPresentation) {
        prefStore.setValue(NULL_REPRESENTATION_STRING, newPresentation);
    }

    /**
     * Returns the postfix used to create a default name for a product component type for a given
     * policy component type name.
     */
    public String getDefaultProductCmptTypePostfix() {
        return prefStore.getString(DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX);
    }

    /**
     * Returns date format for valid-from and effective dates.
     * <p>
     * To be consistent with other date formats and/or input fields this {@link DateFormat} uses the
     * locale used for all data type specific formats/fields.
     * 
     * @see #getDatatypeFormattingLocale()
     */
    @Override
    public DateFormat getDateFormat() {
        return getDateFormat(getDatatypeFormattingLocale());
    }

    /**
     * Convenience method to get a formatted date using the format returned by
     * {@link #getDateFormat()}
     */
    public String getFormattedDate(GregorianCalendar date) {
        return getDateFormat().format(date.getTime());
    }

    /**
     * Returns date format to format dates in specified locale.
     */
    public DateFormat getDateFormat(Locale locale) {
        /*
         * Workaround to display the year in four digits when using UK/US locales. DateFormat.SHORT
         * displays only two digits for the year number whereas DateFormat.MEDIUM displays Months as
         * a word (e.g. "April 1st 2003"). For the german locale DateFormat.MEDIUM works just fine.
         */
        DateFormat result;
        if (Locale.UK.equals(locale)) {
            result = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
        } else if (Locale.US.equals(locale)) {
            result = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
        } else {
            result = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        }
        result.setLenient(false);
        return result;
    }

    /**
     * Returns the value of the enable generating preference.
     */
    @Override
    public boolean isBuilderEnabled() {
        return prefStore.getBoolean(ENABLE_GENERATING);
    }

    public void setEnableGenerating(boolean generate) {
        prefStore.setValue(ENABLE_GENERATING, generate);
    }

    /**
     * Returns the max heap size in megabytes for the IPS test runner. This parameter specify the
     * maximum size of the memory allocation pool for the test runner. Will be used to set the
     * {@code Xmx} Java virtual machines option for the IPS test runner virtual machine.
     */
    @Override
    public String getIpsTestRunnerMaxHeapSize() {
        return prefStore.getString(IPSTESTRUNNER_MAX_HEAP_SIZE);
    }

    /**
     * Returns the named data type display setting. Specifies the text display of name data type
     * edit fields. E.g. display id or name only, or display both.
     * 
     * @see NamedDataTypeDisplay
     */
    public NamedDataTypeDisplay getNamedDataTypeDisplay() {
        String id = prefStore.getString(NAMED_DATA_TYPE_DISPLAY);
        NamedDataTypeDisplay dataTypeDisplay = NamedDataTypeDisplay.getValueById(id);
        if (dataTypeDisplay == null) {
            IpsPlugin.log(new IpsStatus("Unknown named data type with id: " + id //$NON-NLS-1$
                    + ". Use default named data type display.")); //$NON-NLS-1$
            dataTypeDisplay = NamedDataTypeDisplay.NAME_AND_ID;
        }
        return dataTypeDisplay;
    }

    /**
     * Sets the named data type display setting.
     * 
     * @throws NullPointerException if etDisplay is <code>null</code>
     */
    public void setNamedDataTypeDisplay(NamedDataTypeDisplay etDisplay) {
        ArgumentCheck.notNull(etDisplay);
        prefStore.setValue(NAMED_DATA_TYPE_DISPLAY, etDisplay.getId());
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
     * Sets the refactoring mode.
     */
    public void setRefactoringMode(String refactoringMode) {
        prefStore.setValue(REFACTORING_MODE, refactoringMode);
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

    public boolean isRefactoringModeDirect() {
        // TODO need to fix: FIPS-1029
        // return prefStore.getString(REFACTORING_MODE).equals(REFACTORING_MODE_DIRECT);
        return false;
    }

    public boolean isRefactoringModeExplicit() {
        // TODO need to fix: FIPS-1029
        // return prefStore.getString(REFACTORING_MODE).equals(REFACTORING_MODE_EXPLICIT);
        return true;
    }

    public boolean canModifyRuntimeId() {
        return prefStore.getBoolean(MODIFY_RUNTIME_ID);
    }

    /**
     * @deprecated Use {@link #isAvancedTeamFunctionsForProductDefExplorerEnabled()} instead
     */
    @Deprecated
    public boolean areAvancedTeamFunctionsForProductDefExplorerEnabled() {
        return isAvancedTeamFunctionsForProductDefExplorerEnabled();
    }

    public boolean isAvancedTeamFunctionsForProductDefExplorerEnabled() {
        return prefStore.getBoolean(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER);
    }

    public void setAvancedTeamFunctionsForProductDefExplorerEnabled(boolean enabled) {
        prefStore.setValue(ADVANCED_TEAM_FUNCTIONS_IN_PRODUCT_DEF_EXPLORER, enabled);
    }

    public boolean isSimpleContextMenuEnabled() {
        return prefStore.getBoolean(SIMPLE_CONTEXT_MENU) || IpsPlugin.getDefault().isProductDefinitionPerspective();
    }

    public void setSimpleContextMenuEnabled(boolean enabled) {
        prefStore.setValue(SIMPLE_CONTEXT_MENU, enabled);
    }

    /**
     * Returns whether the automatic validation of {@link ITableContents} is active (
     * <code>true</code>) or not. The automatic validation is set to true by default. Turning the
     * automatic table validation off results in faster refresh- and build times. As Errors in
     * tables might not be noticed, the ability to disable the automatic validation should be used
     * carefully. Turning the automatic validation off is useful for example if data in big tables
     * won't change.
     */
    @Override
    public boolean isAutoValidateTables() {
        return prefStore.getBoolean(AUTO_VALIDATE_TABLES);
    }

    /**
     * Activates (<code>true</code>) or deactivates(<code>false</code>) automatic validation of
     * {@link ITableContents}, according to the given parameter <code>enabled</code>.
     */
    public void setAutoValidateTables(boolean enabled) {
        prefStore.setValue(AUTO_VALIDATE_TABLES, enabled);
    }

    /**
     * Sets the number of sections displayed on a page of a type editor. Only the predefined values
     * TWO_SECTIONS_IN_TYPE_EDITOR_PAGE and FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE are allowed.
     */
    public void setSectionsInTypeEditors(String numberOfSections) {
        // identity on purpose!!
        if (!(numberOfSections.equals(TWO_SECTIONS_IN_TYPE_EDITOR_PAGE) || numberOfSections
                .equals(FOUR_SECTIONS_IN_TYPE_EDITOR_PAGE))) {
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
     * The default value is the locale of the default java locale instead of the configured eclipse
     * locale.
     * 
     * @return the currently configured locale for formating values of the data types Integer,
     *         Double, Date.
     */
    @Override
    public Locale getDatatypeFormattingLocale() {
        String localeString = prefStore.getString(DATATYPE_FORMATTING_LOCALE);
        return LocaleUtils.toLocale(localeString);
    }

    /**
     * Sets the locale used for formating values of the data types Integer, Double, Date.
     * 
     * @param locale the new locale to be used
     */
    public void setDatatypeFormattingLocale(Locale locale) {
        prefStore.setValue(DATATYPE_FORMATTING_LOCALE, locale.toString());
    }

    /**
     * Returns the formatter for Faktor-IPS data types.
     */
    @Override
    public DatatypeFormatter getDatatypeFormatter() {
        return datatypeFormatter;
    }

    /**
     * Returns date/time format for dates including the time of day.
     * <p>
     * To be consistent with other date formats and/or input fields this {@link DateFormat} uses the
     * locale used for all data type specific formats/fields.
     * 
     * @see #getDatatypeFormattingLocale()
     */
    public DateFormat getDateTimeFormat() {
        return getDateTimeFormat(getDatatypeFormattingLocale());
    }

    /**
     * Returns date/time format to format dates including the time of day in specified locale.
     */
    public DateFormat getDateTimeFormat(Locale locale) {
        DateFormat result;
        if (Locale.UK.equals(locale)) {
            result = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"); //$NON-NLS-1$
        } else if (Locale.US.equals(locale)) {
            result = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a"); //$NON-NLS-1$
        } else {
            result = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
        }
        result.setLenient(false);
        return result;
    }

    /**
     * Returns the delay time for change events in milliseconds. This value is used for setting the
     * delay time on calling a change event in edit fields e.g. for activating the validation.
     */
    public int getChangeEventDelayTime() {
        return prefStore.getInt(CHANGE_EVENT_DELAY_TIME);
    }

    /**
     * Sets the delay time in milliseconds for change events according to the given parameter
     * <code>delayTime</code>.
     */
    public void setChangeEventDelayTime(int delayTime) {
        prefStore.setValue(CHANGE_EVENT_DELAY_TIME, delayTime);
    }

    /**
     * Returns <code>true</code> if the currently mode for the product copy wizard is the copy mode,
     * <code>false</code> otherwise.
     */
    public boolean isCopyWizardModeCopy() {
        return prefStore.getString(COPY_WIZARD_MODE).equals(COPY_WIZARD_MODE_COPY);
    }

    /**
     * Returns <code>true</code> if the currently mode for the product copy wizard is the link mode,
     * <code>false</code> otherwise.
     */
    public boolean isCopyWizardModeLink() {
        return prefStore.getString(COPY_WIZARD_MODE).equals(COPY_WIZARD_MODE_LINK);
    }

    /**
     * Returns <code>true</code> if the currently mode for the product copy wizard is the smart
     * mode, <code>false</code> otherwise.
     */
    public boolean isCopyWizardModeSmartmode() {
        return prefStore.getString(COPY_WIZARD_MODE).equals(COPY_WIZARD_MODE_SMARTMODE);
    }

    /**
     * Sets the mode of the product copy wizard.
     */
    public void setCopyWizardMode(String copyWizardMode) {
        prefStore.setValue(COPY_WIZARD_MODE, copyWizardMode);
    }

}
