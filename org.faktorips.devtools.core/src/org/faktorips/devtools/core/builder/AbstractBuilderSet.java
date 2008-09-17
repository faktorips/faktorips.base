/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract implementation that can be used as a base class for real builder sets.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractBuilderSet implements IIpsArtefactBuilderSet {

    /**
     * Configuration property for this builder. IIpsArtefactBuilderSets that use this builder
     * can provide values of this property via the IIpsArtefactBuilderSetConfig object that 
     * is provided by the initialize method of an IIpsArtefactBuilderSet.
     */
    public final static String CONFIG_PROPERTY_GENERATOR_LOCALE = "generatorLocale"; //$NON-NLS-1$
    
    
    private String id;
    private String label;
    private IIpsProject ipsProject;
    private IIpsArtefactBuilderSetConfig config;
    private IIpsArtefactBuilder[] builders;
    
    public AbstractBuilderSet() {
        super();
    }
    
    /**
     * Constructor for testing purposes.
     */
    public AbstractBuilderSet(IIpsArtefactBuilder[] builders) {
        this.builders = builders;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public String toString() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsAggregateRootBuilder() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    public boolean isInverseRelationLinkRequiredFor2WayCompositions() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    public boolean isRoleNamePluralRequiredForTo1Relations() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return builders;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilderSetConfig getConfig() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException {
        ArgumentCheck.notNull(config);
        this.config = config;
        builders = createBuilders();
    }

    /**
     * {@inheritDoc}
     */
    public Locale getLanguageUsedInGeneratedSourceCode() {
        String localeString = getConfig().getPropertyValueAsString(CONFIG_PROPERTY_GENERATOR_LOCALE);
        if(localeString == null){
            return Locale.ENGLISH;
        }
        return getLocale(localeString);
    }

    public static Locale getLocale(String s) {
        StringTokenizer tokenzier = new StringTokenizer(s, "_"); //$NON-NLS-1$
        if (!tokenzier.hasMoreTokens()) {
            return Locale.ENGLISH;
        }
        String language = tokenzier.nextToken();
        if (!tokenzier.hasMoreTokens()) {
            return new Locale(language);
        }
        String country = tokenzier.nextToken();
        if (!tokenzier.hasMoreTokens()) {
            return new Locale(language, country);
        }
        String variant = tokenzier.nextToken();
        return new Locale(language, country, variant);
    }

    /**
     * Template method to create the set's builders.
     * 
     * @throws CoreException
     */
    protected abstract IIpsArtefactBuilder[] createBuilders() throws CoreException;

   /**
     * {@inheritDoc}
     */
    public void afterBuildProcess(int buildKind) throws CoreException {
    }

    /**
     * {@inheritDoc}
     */
    public void beforeBuildProcess(int buildKind) throws CoreException {
    }

    /**
     * For testing purposes.
     */
    public IIpsArtefactBuilder getBuilder(Class builderClass) {
        for (int i = 0; i < builders.length; i++) {
            if (builders[i].getClass().equals(builderClass)) {
                return builders[i];
            }
        }
        throw new RuntimeException("No builder of class " + builderClass + " defined."); //$NON-NLS-1$ //$NON-NLS-2$
    }

	public boolean isTableBasedEnumValidationRequired() {
		return true;
	}

}
