/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.eclipse.internal.IpsClasspathContainerInitializer;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.EmptyIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.options.IpsEnumMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

public class Migration_23_6_0 extends MarkAsDirtyMigration {

    public static final String MSGCODE_IPS_VERSION_TOO_OLD = "IPS_VERSION_TOO_OLD"; //$NON-NLS-1$

    private static final String VERSION_23_6_0 = "23.6.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_JAXB_VARIANT = "generateJaxbSupport"; //$NON-NLS-1$

    private final IpsMigrationOption<JaxbSupportVariant> jaxbVariantOption;

    public Migration_23_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                Set.of(IIpsModel.get().getIpsObjectTypes()),
                VERSION_23_6_0,
                Messages.Migration_23_6_0_description);
        String oldValue = projectToMigrate.getReadOnlyProperties().getBuilderSetConfig()
                .getPropertyValue(MIGRATION_OPTION_JAXB_VARIANT);
        oldValue = IpsStringUtils.isBlank(oldValue) ? "false" : oldValue.toLowerCase();
        JaxbSupportVariant defaultVariant = switch (oldValue) {
            case "none", "false" -> JaxbSupportVariant.None;
            default -> JaxbSupportVariant.ClassicJAXB;
        };
        jaxbVariantOption = new IpsEnumMigrationOption<>(
                MIGRATION_OPTION_JAXB_VARIANT,
                Messages.Migration_23_6_0_jaxbVariant,
                defaultVariant,
                JaxbSupportVariant.class);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList canMigrate() {
        String minRequiredVersionNumber = getIpsProject().getReadOnlyProperties()
                .getMinRequiredVersionNumber(EmptyIpsFeatureVersionManager.INSTANCE.getFeatureId());
        int majorVersion = Integer
                .parseInt(minRequiredVersionNumber.substring(0, minRequiredVersionNumber.indexOf('.')));
        if (majorVersion < 21) {
            return MessageList.of(Message.newError(MSGCODE_IPS_VERSION_TOO_OLD,
                    MessageFormat.format(
                            Messages.Migration_IpsVersionTooOld,
                            minRequiredVersionNumber, getIpsProject().getName(), VERSION_23_6_0,
                            Migration_21_6_0.VERSION)));
        }
        return super.canMigrate();
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws InvocationTargetException {
        JaxbSupportVariant selectedValue = jaxbVariantOption.getSelectedValue();
        selectedValue = selectedValue == null ? JaxbSupportVariant.None : selectedValue;

        IIpsProject ipsProject = getIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel()
                .getIpsArtefactBuilderSetInfo(properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();

        builderSetConfig.setPropertyValue(MIGRATION_OPTION_JAXB_VARIANT, selectedValue.toString(),
                Messages.Migration_Option_JAXB_Variant_Description);
        MigrationUtil.updateAllIpsArtefactBuilderSetDescriptions(builderSetInfo, builderSetConfig);

        ipsProject.setProperties(properties);

        if (ipsProject.getJavaProject().unwrap() instanceof IJavaProject javaProject) {
            updateClasspath(monitor, selectedValue, javaProject);
        }
        updateManifest();
        return super.migrate(monitor);
    }

    private void updateClasspath(IProgressMonitor monitor, JaxbSupportVariant selectedValue, IJavaProject javaProject) {
        try {
            IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
            for (int i = 0; i < rawClasspath.length; i++) {
                IClasspathEntry classpathEntry = rawClasspath[i];
                IPath path = classpathEntry.getPath();
                if (IpsClasspathContainerInitializer.CONTAINER_ID.equals(path.segment(0))) {
                    Set<String> bundleIds = new LinkedHashSet<>();
                    if (path.segmentCount() > 1) {
                        bundleIds.addAll(Arrays.asList(path.segment(1).split(",")));
                    }
                    switch (selectedValue) {
                        case ClassicJAXB:
                            bundleIds.remove(IpsClasspathContainerInitializer.JAKARTA_BUNDLE);
                            bundleIds.add(IpsClasspathContainerInitializer.CLASSIC_JAXB_BUNDLE);
                            break;
                        case JakartaXmlBinding:
                            bundleIds.remove(IpsClasspathContainerInitializer.CLASSIC_JAXB_BUNDLE);
                            bundleIds.add(IpsClasspathContainerInitializer.JAKARTA_BUNDLE);
                            break;
                        default:
                            bundleIds.remove(IpsClasspathContainerInitializer.CLASSIC_JAXB_BUNDLE);
                            bundleIds.remove(IpsClasspathContainerInitializer.JAKARTA_BUNDLE);
                    }
                    path = path.uptoSegment(1);
                    if (!bundleIds.isEmpty()) {
                        path = path.append(bundleIds.stream().collect(Collectors.joining(",")));
                    }
                    rawClasspath[i] = JavaCore.newContainerEntry(path, new IAccessRule[0],
                            classpathEntry.getExtraAttributes(), false);
                }
            }
            javaProject.setRawClasspath(rawClasspath, monitor);
        } catch (JavaModelException e) {
            // can't change the classpath
            IpsLog.log(e);
        }
    }

    @Override
    public Collection<IpsMigrationOption<?>> getOptions() {
        return Collections.singleton(jaxbVariantOption);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_23_6_0(ipsProject, featureId);
        }
    }
}
