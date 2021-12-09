/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.abstraction.ABuildKind;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract implementation that can be used as a base class for real builder sets.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractBuilderSet implements IIpsArtefactBuilderSet {

    /**
     * Configuration property for this builder. IIpsArtefactBuilderSets that use this builder can
     * provide values of this property via the IIpsArtefactBuilderSetConfig object that is provided
     * by the initialize method of an IIpsArtefactBuilderSet.
     */
    public static final String CONFIG_PROPERTY_GENERATOR_LOCALE = "generatorLocale"; //$NON-NLS-1$

    /**
     * Configuration property setting that none mergeable filed should be marked as derived filed.
     */
    public static final String CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED = "markNoneMergeableResourcesAsDerived"; //$NON-NLS-1$

    private String id;
    private String label;
    private IIpsProject ipsProject;
    private IIpsArtefactBuilderSetConfig config;
    private LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> builders;

    public AbstractBuilderSet() {
        super();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean containsAggregateRootBuilder() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    @Override
    public boolean isInverseRelationLinkRequiredFor2WayCompositions() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    @Override
    public boolean isRoleNamePluralRequiredForTo1Relations() {
        return false;
    }

    @Override
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return builders.values().toArray(new IIpsArtefactBuilder[builders.size()]);
    }

    @Override
    public IIpsArtefactBuilderSetConfig getConfig() {
        return config;
    }

    @Override
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreRuntimeException {
        ArgumentCheck.notNull(config);
        this.config = config;
        builders = createBuilders();
    }

    @Override
    public Locale getLanguageUsedInGeneratedSourceCode() {
        String localeString = getConfig().getPropertyValueAsString(CONFIG_PROPERTY_GENERATOR_LOCALE);
        if (localeString == null) {
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
     */
    protected abstract LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() throws CoreRuntimeException;

    @Override
    public void afterBuildProcess(ABuildKind buildKind) throws CoreRuntimeException {
        // could be implemented in subclass
    }

    @Override
    public void beforeBuildProcess(ABuildKind buildKind) throws CoreRuntimeException {
        // could be implemented in subclass
    }

    @Override
    public <T extends IIpsArtefactBuilder> List<T> getBuildersByClass(Class<T> builderClass) {
        if (builders == null) {
            throw new IllegalStateException("No builders initialized yet"); //$NON-NLS-1$
        }
        List<T> buildersByClass = new ArrayList<>();
        for (IIpsArtefactBuilder builder : builders.values()) {
            if (builderClass.isAssignableFrom(builder.getClass())) {
                @SuppressWarnings("unchecked")
                T castedBuilder = (T)builder;
                buildersByClass.add(castedBuilder);
            }
        }
        return buildersByClass;
    }

    @Override
    public IIpsArtefactBuilder getBuilderById(IBuilderKindId kindId) {
        IIpsArtefactBuilder builder = builders.get(kindId);
        if (builder == null) {
            throw new RuntimeException("There is no builder for the kind ID: " + kindId); //$NON-NLS-1$
        }
        return builder;
    }

    @Override
    public <T extends IIpsArtefactBuilder> T getBuilderById(IBuilderKindId kindId, Class<T> builderClass) {
        IIpsArtefactBuilder builderById = getBuilderById(kindId);
        if (builderClass.isAssignableFrom(builderById.getClass())) {
            @SuppressWarnings("unchecked")
            T castedBuilder = (T)builderById;
            return castedBuilder;
        }
        throw new RuntimeException("There is no builder for kind ID: " + kindId + " of the type: " + builderClass); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    public boolean isTableBasedEnumValidationRequired() {
        return true;
    }

    @Override
    public boolean isMarkNoneMergableResourcesAsDerived() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED);
        if (propertyValueAsBoolean != null) {
            return propertyValueAsBoolean;
        } else {
            return true;
        }
    }

    @Override
    public void clean(IProgressMonitor monitor) {
        // default implementation does nothing
    }

}
