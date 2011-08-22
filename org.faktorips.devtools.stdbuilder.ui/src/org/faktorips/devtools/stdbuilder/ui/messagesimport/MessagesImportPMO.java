/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.ui.messagesimport;

import java.beans.PropertyChangeEvent;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class MessagesImportPMO extends PresentationModelObject {

    public final static String PROPERTY_FILE_NAME = "fileName"; //$NON-NLS-1$

    public final static String PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT = "ipsPackageFragmentRoot"; //$NON-NLS-1$

    public final static String PROPERTY_LOCALE = "locale"; //$NON-NLS-1$

    public final static String PROPERTY_AVAILABLE_LOCALES = "availableLocales"; //$NON-NLS-1$

    private String fileName = ""; //$NON-NLS-1$

    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private ISupportedLanguage locale;

    private Set<ISupportedLanguage> availableLocales;

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        String oldValue = this.fileName;
        this.fileName = fileName;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FILE_NAME, oldValue, fileName));
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param ipsPackageFragmentRoot The ipsPackageFragmentRoot to set.
     */
    public void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot ipsPackageFragmentRoot) {
        IIpsPackageFragmentRoot oldValue = this.ipsPackageFragmentRoot;
        this.ipsPackageFragmentRoot = ipsPackageFragmentRoot;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT, oldValue,
                ipsPackageFragmentRoot));
    }

    /**
     * @return Returns the ipsPackageFragmentRoot.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return ipsPackageFragmentRoot;
    }

    /**
     * @param locale The locale to set.
     */
    public void setLocale(ISupportedLanguage locale) {
        ISupportedLanguage oldValue = this.locale;
        this.locale = locale;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_LOCALE, oldValue, locale));
    }

    /**
     * @return Returns the locale.
     */
    public ISupportedLanguage getLocale() {
        return locale;
    }

    /**
     * @param availableLocales The availableLocales to set.
     */
    public void setAvailableLocales(Set<ISupportedLanguage> availableLocales) {
        Set<ISupportedLanguage> oldValue = this.availableLocales;
        this.availableLocales = availableLocales;
        if (getLocale() == null && availableLocales.size() > 0) {
            setLocale(availableLocales.iterator().next());
        }
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_AVAILABLE_LOCALES, oldValue, availableLocales));
    }

    /**
     * @return Returns the availableLocales.
     */
    public Set<ISupportedLanguage> getAvailableLocales() {
        return availableLocales;
    }

}
