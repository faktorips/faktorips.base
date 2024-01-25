/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.NoVersionIdProductCmptNamingStrategyFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;

/**
 * Allows easy creation of {@linkplain IIpsProject IPS projects} with different configurations.
 */
public class IpsProjectBuilder {

    private final List<String> predefinedDatatypes = new ArrayList<>();

    private final List<Locale> supportedLocales = new ArrayList<>();

    private String name;

    private IProjectDescription description;

    // for backwards compatibility of existing tests, the default differs from the one in regular
    // projects
    private boolean changingOverTimeDefault = true;

    /**
     * Following methods can be overridden by subclasses of {@link AbstractIpsPluginTest}:
     * <ul>
     * <li>
     * {@link AbstractIpsPluginTest#setTestArtefactBuilderSet(IIpsProjectProperties, IIpsProject)}
     * <li>{@link AbstractIpsPluginTest#addIpsCapabilities(AProject)}
     * <li>{@link AbstractIpsPluginTest#addJavaCapabilities(IProject)}
     * </ul>
     * Therefore, we need to call these methods on the {@link AbstractIpsPluginTest} instance.
     */
    private final AbstractIpsPluginTest ipsPluginTest;

    public IpsProjectBuilder(AbstractIpsPluginTest ipsPluginTest) {
        this.ipsPluginTest = ipsPluginTest;

        // Default name
        name = UUID.randomUUID().toString();
    }

    public IpsProjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    public IpsProjectBuilder description(IProjectDescription description) {
        this.description = description;
        return this;
    }

    public IpsProjectBuilder predefinedDatatypes(Datatype... datatypes) {
        for (Datatype datatype : datatypes) {
            predefinedDatatypes.add(datatype.getName());
        }
        return this;
    }

    public IpsProjectBuilder predefinedDatatypes(List<Datatype> datatypes) {
        for (Datatype datatype : datatypes) {
            predefinedDatatypes.add(datatype.getName());
        }
        return this;
    }

    public IpsProjectBuilder supportedLocales(Locale... locales) {
        supportedLocales.addAll(Arrays.asList(locales));
        return this;
    }

    public IpsProjectBuilder supportedLocales(List<Locale> locales) {
        supportedLocales.addAll(locales);
        return this;
    }

    public IpsProjectBuilder changingOverTimeDefault(boolean changingOverTime) {
        changingOverTimeDefault = changingOverTime;
        return this;
    }

    public IIpsProject build() {
        return newIpsProject();
    }

    private IIpsProject newIpsProject() {
        String newName = name;
        if (Abstractions.isEclipseRunning()) {
            ICoreRunnable runnable = $ -> {
                AProject project = new PlatformProjectBuilder().name(name).description(description).build();
                ipsPluginTest.addJavaCapabilities(project);
                ipsPluginTest.addIpsCapabilities(project);
            };
            AWorkspace workspace = Abstractions.getWorkspace();
            workspace.run(runnable, null);
        } else {
            PlainJavaProject project = (PlainJavaProject)Abstractions.getWorkspace().getRoot().getProject(name);
            project.create();
            try {
                ipsPluginTest.addJavaCapabilities(project);
            } catch (CoreException e) {
                throw new IpsException(e);
            }
            ipsPluginTest.addIpsCapabilities(project);
            newName = project.getName();
        }

        IIpsProject ipsProject = IIpsModel.get().getIpsProject(newName);
        IIpsProjectProperties properties = ipsProject.getProperties();

        properties.setChangingOverTimeDefault(changingOverTimeDefault);
        properties.setValidateIpsSchema(false);
        setProductCmptNamingStrategy(ipsProject, properties);
        addSupportedLanguages(properties);
        addPredefinedDatatypes(properties);

        ipsProject.setProperties(properties);

        return ipsProject;
    }

    private void setProductCmptNamingStrategy(IIpsProject ipsProject, IIpsProjectProperties properties) {
        IProductCmptNamingStrategy productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategyFactory()
                .newProductCmptNamingStrategy(ipsProject);
        properties.setProductCmptNamingStrategy(productCmptNamingStrategy);
    }

    private void addSupportedLanguages(IIpsProjectProperties properties) {
        // Add supported languages
        if (supportedLocales.size() > 0) {
            for (Locale locale : supportedLocales) {
                properties.addSupportedLanguage(locale);
            }
            properties.setDefaultLanguage(supportedLocales.toArray(new Locale[supportedLocales.size()])[0]);
        }
    }

    private void addPredefinedDatatypes(IIpsProjectProperties properties) {
        // Add predefined datatypes
        if (predefinedDatatypes.size() > 0) {
            String[] projectDatatypes = properties.getPredefinedDatatypesUsed();
            String[] newProjectDatatypes = Arrays.copyOf(projectDatatypes,
                    projectDatatypes.length + predefinedDatatypes.size());
            for (int i = 0, j = projectDatatypes.length; i < predefinedDatatypes.size(); i++, j++) {
                newProjectDatatypes[j] = predefinedDatatypes.get(i);
            }
            properties.setPredefinedDatatypesUsed(newProjectDatatypes);
        }
    }

}
