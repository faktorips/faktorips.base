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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.eclipse.util.EclipseIOUtil;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.EmptyIpsFeatureVersionManager;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class Migration_21_6_0 extends MarkAsDirtyMigration {

    public static final String MSGCODE_IPS_VERSION_TOO_OLD = "IPS_VERSION_TOO_OLD"; //$NON-NLS-1$

    public static final String VERSION = "21.6.0"; //$NON-NLS-1$

    public Migration_21_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                new LinkedHashSet<>(Arrays.asList(IIpsModel.get().getIpsObjectTypes())),
                VERSION,
                Messages.Migration_21_6_0_description);
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
        if (majorVersion < 20) {
            return new MessageList(Message.newError(MSGCODE_IPS_VERSION_TOO_OLD,
                    MessageFormat.format(
                            Messages.Migration_21_6_0_IpsVersionTooOld,
                            minRequiredVersionNumber, getIpsProject().getName(), VERSION)));
        }
        return super.canMigrate();
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        updatePluginId(".project", monitor); //$NON-NLS-1$
        updatePluginId(".classpath", monitor); //$NON-NLS-1$
        updatePluginId(".ipsproject", monitor); //$NON-NLS-1$
        reloadProjectData();
        return super.migrate(monitor);
    }

    private void reloadProjectData() {
        @SuppressWarnings("deprecation")
        IpsModel ipsModel = IpsModel.get();
        ipsModel.clearProjectSpecificCaches(getIpsProject());
    }

    private void updatePluginId(String fileName, IProgressMonitor monitor) {
        IFile file = getIpsProject().getProject().getFile(fileName).unwrap();
        update(file, c -> c.replace(IpsPlugin.PLUGIN_ID, IpsModelActivator.PLUGIN_ID), monitor);
    }

    /* private */ static void update(IFile file, UnaryOperator<String> contentChange, IProgressMonitor monitor) {
        String content = read(file);
        content = contentChange.apply(content);
        write(file, content, monitor);
    }

    private static String read(IFile file) {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(file.getContents(), file.getCharset()));) {
            return bufferedReader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
        } catch (IOException | CoreException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    private static void write(IFile file, String content, IProgressMonitor monitor) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(file.getCharset()));) {
            EclipseIOUtil.writeToFile(file, inputStream, true, true, monitor);
        } catch (IOException | CoreException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_21_6_0(ipsProject, featureId);
        }
    }
}
