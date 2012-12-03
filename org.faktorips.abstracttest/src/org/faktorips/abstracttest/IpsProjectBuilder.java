/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.Util;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.productcmpt.NoVersionIdProductCmptNamingStrategyFactory;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;

/**
 * Allows easy creation of {@linkplain IIpsProject IPS projects} with different configurations.
 */
public class IpsProjectBuilder {

    private static final String OUTPUT_FOLDER_NAME_DERIVED = "extension"; //$NON-NLS-1$

    private static final String OUTPUT_FOLDER_NAME_MERGABLE = "src"; //$NON-NLS-1$

    private static final String BASE_PACKAGE_NAME_DERIVED = "org.faktorips.sample.model"; //$NON-NLS-1$

    private static final String BASE_PACKAGE_NAME_MERGABLE = "org.faktorips.sample.model"; //$NON-NLS-1$

    private final Set<String> predefinedDatatypes = new LinkedHashSet<String>();

    private final Set<Locale> supportedLocales = new LinkedHashSet<Locale>();

    private String name;

    public IpsProjectBuilder() {
        // Default predefined datatypes
        predefinedDatatypes.add(Datatype.DECIMAL.getName());
        predefinedDatatypes.add(Datatype.MONEY.getName());
        predefinedDatatypes.add(Datatype.INTEGER.getName());
        predefinedDatatypes.add(Datatype.PRIMITIVE_INT.getName());
        predefinedDatatypes.add(Datatype.PRIMITIVE_LONG.getName());
        predefinedDatatypes.add(Datatype.PRIMITIVE_BOOLEAN.getName());
        predefinedDatatypes.add(Datatype.STRING.getName());
        predefinedDatatypes.add(Datatype.BOOLEAN.getName());

        // Default supported locales
        supportedLocales.add(Locale.GERMAN);
        supportedLocales.add(Locale.US);

        // Default name
        name = UUID.randomUUID().toString();
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

    public IpsProjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    public IIpsProject build() throws CoreException {
        return newIpsProject();
    }

    private IIpsProject newIpsProject() throws CoreException {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject project = new PlatformProjectBuilder().name(name).build();
                addJavaCapabilities(project);
                addIpsCapabilities(project, predefinedDatatypes.toArray(new String[predefinedDatatypes.size()]));
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(name);
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setProductCmptNamingStrategy(new NoVersionIdProductCmptNamingStrategyFactory()
                .newProductCmptNamingStrategy(ipsProject));
        if (supportedLocales.size() > 0) {
            for (Locale locale : supportedLocales) {
                properties.addSupportedLanguage(locale);
            }
            properties.setDefaultLanguage(supportedLocales.toArray(new Locale[supportedLocales.size()])[0]);
            ipsProject.setProperties(properties);
        }

        return ipsProject;
    }

    static void addIpsCapabilities(IProject project, String[] predefinedDatatypes) throws CoreException {
        Util.addNature(project, IIpsProject.NATURE_ID);
        IFolder rootFolder = project.getFolder("productdef");
        rootFolder.create(true, true, null);
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(true);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(rootFolder);
        entry.setSpecificBasePackageNameForMergableJavaClasses(BASE_PACKAGE_NAME_MERGABLE);
        entry.setSpecificOutputFolderForMergableJavaFiles(project.getFolder(OUTPUT_FOLDER_NAME_MERGABLE));
        entry.setSpecificBasePackageNameForDerivedJavaClasses(BASE_PACKAGE_NAME_DERIVED);
        entry.setSpecificOutputFolderForDerivedJavaFiles(project.getFolder(OUTPUT_FOLDER_NAME_DERIVED));

        ipsProject.setIpsObjectPath(path);

        IIpsProjectProperties properties = ipsProject.getProperties();
        setTestArtefactBuilderSet(properties, ipsProject);
        properties.setPredefinedDatatypesUsed(predefinedDatatypes);
        properties
                .setMinRequiredVersionNumber(
                        "org.faktorips.feature", (String)Platform.getBundle("org.faktorips.devtools.core").getHeaders().get("Bundle-Version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ipsProject.setProperties(properties);
    }

    static void setTestArtefactBuilderSet(IIpsProjectProperties properties, IIpsProject project) throws CoreException {
        // Create the builder set for the project
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { new TestArtefactBuilder() });
        builderSet.setIpsProject(project);

        // Set the builder set id in the project's properties
        properties.setBuilderSetId(TestIpsArtefactBuilderSet.ID);

        // Add the new builder set to the builder set infos of the IPS model
        IpsModel ipsModel = (IpsModel)IpsPlugin.getDefault().getIpsModel();
        IIpsArtefactBuilderSetInfo[] builderSetInfos = ipsModel.getIpsArtefactBuilderSetInfos();
        List<IIpsArtefactBuilderSetInfo> newBuilderSetInfos = new ArrayList<IIpsArtefactBuilderSetInfo>(
                builderSetInfos.length + 1);
        for (IIpsArtefactBuilderSetInfo info : builderSetInfos) {
            newBuilderSetInfos.add(info);
        }
        newBuilderSetInfos.add(new TestArtefactBuilderSetInfo(builderSet));
        ipsModel.setIpsArtefactBuilderSetInfos(newBuilderSetInfos
                .toArray(new IIpsArtefactBuilderSetInfo[newBuilderSetInfos.size()]));
    }

    /**
     * Creates a new Java Project for the given platform project.
     */
    static IJavaProject addJavaCapabilities(IProject project) throws CoreException {
        IJavaProject javaProject = JavaCore.create(project);
        // add Java nature
        Util.addNature(project, JavaCore.NATURE_ID);
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        // create bin folder and set as output folder.
        IFolder binFolder = project.getFolder("bin");
        if (!binFolder.exists()) {
            binFolder.create(true, true, null);
        }
        IFolder srcFolder = project.getFolder(OUTPUT_FOLDER_NAME_MERGABLE);
        javaProject.setOutputLocation(binFolder.getFullPath(), null);
        if (!srcFolder.exists()) {
            srcFolder.create(true, true, null);
        }
        IFolder extFolder = project.getFolder(OUTPUT_FOLDER_NAME_DERIVED);
        if (!extFolder.exists()) {
            extFolder.create(true, true, null);
        }
        IPackageFragmentRoot srcRoot = javaProject.getPackageFragmentRoot(srcFolder);
        IPackageFragmentRoot extRoot = javaProject.getPackageFragmentRoot(extFolder);
        IClasspathEntry[] entries = new IClasspathEntry[2];
        entries[0] = JavaCore.newSourceEntry(srcRoot.getPath());
        entries[1] = JavaCore.newSourceEntry(extRoot.getPath());
        javaProject.setRawClasspath(entries, null);
        addSystemLibraries(javaProject);
        return javaProject;
    }

    static void addSystemLibraries(IJavaProject javaProject) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaRuntime.getDefaultJREContainerEntry();
        javaProject.setRawClasspath(newEntries, null);
    }

    private static class TestArtefactBuilder extends AbstractArtefactBuilder {

        public TestArtefactBuilder() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {

        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return false;
        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {

        }

        @Override
        public boolean isBuildingInternalArtifacts() {
            return false;
        }

    }

}
