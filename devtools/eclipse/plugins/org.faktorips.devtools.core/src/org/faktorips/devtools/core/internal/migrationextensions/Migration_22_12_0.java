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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.eclipse.internal.IpsClasspathContainerInitializer;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Migration_22_12_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "22.12.0"; //$NON-NLS-1$

    public Migration_22_12_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                new LinkedHashSet<>(Arrays.asList(IIpsModel.get().getIpsObjectTypes())),
                VERSION,
                Messages.Migration_22_12_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        migrateProjectFile();
        migrateClasspathFile();

        reloadProjectData();
        return super.migrate(monitor);
    }

    private void migrateProjectFile() {
        IFile projectFile = getIpsProject().getProject().getFile(".project").unwrap();
        try {
            Document projectDocument = XmlUtil.parseDocument(projectFile.getContents());
            migrateBuilder(projectDocument);
            migrateNature(projectDocument);
            write(projectFile, projectDocument);
        } catch (SAXException | IOException | CoreException | TransformerException e) {
            throw new IpsException("Can't migrate " + projectFile, e);
        }
    }

    private void write(IFile file, Document document)
            throws TransformerException, UnsupportedEncodingException, CoreException {
        String encoding = getIpsProject().getXmlFileCharset();
        String contents = XmlUtil.nodeToString(document.getDocumentElement(), encoding,
                System.lineSeparator());
        InputStream is = new ByteArrayInputStream(contents.getBytes(encoding));
        file.setContents(is, true, true, new NullProgressMonitor());
    }

    private void migrateBuilder(Document projectDocument) {
        boolean migratedBuilder = false;
        Element buildSpecElement = XmlUtil.getFirstElement(projectDocument.getDocumentElement(), "buildSpec");
        List<Element> buildCommandElements = org.faktorips.runtime.internal.XmlUtil.getElements(buildSpecElement,
                "buildCommand");
        for (Element buildCommandElement : buildCommandElements) {
            Element nameElement = XmlUtil.getFirstElement(buildCommandElement, "name");
            String name = XmlUtil.getCDATAorTextContent(nameElement);
            if (IpsStringUtils.isNotBlank(name) && name.contains("ipsbuilder")) {
                if (!migratedBuilder) {
                    nameElement.setTextContent(IpsBuilder.BUILDER_ID);
                    migratedBuilder = true;
                } else {
                    buildSpecElement.removeChild(buildCommandElement);
                }
            }

        }
    }

    private void migrateNature(Document projectDocument) {
        boolean migratedNature = false;
        Element naturesElement = XmlUtil.getFirstElement(projectDocument.getDocumentElement(), "natures");
        List<Element> natureElements = org.faktorips.runtime.internal.XmlUtil.getElements(naturesElement,
                "nature");
        for (Element natureElement : natureElements) {
            String name = XmlUtil.getCDATAorTextContent(natureElement);
            if (IpsStringUtils.isNotBlank(name) && name.contains("ipsnature")) {
                if (!migratedNature) {
                    natureElement.setTextContent(IIpsProject.NATURE_ID);
                    migratedNature = true;
                } else {
                    naturesElement.removeChild(natureElement);
                }
            }

        }
    }

    private void migrateClasspathFile() {
        IFile classpathFile = getIpsProject().getProject().getFile(".classpath").unwrap();
        try {
            Document classpathDocument = XmlUtil.parseDocument(classpathFile.getContents());
            migrateClasspathContainer(classpathDocument);
            write(classpathFile, classpathDocument);
        } catch (SAXException | IOException | CoreException | TransformerException e) {
            throw new IpsException("Can't migrate " + classpathFile, e);
        }
    }

    private void migrateClasspathContainer(Document classpathDocument) {
        boolean migratedContainer = false;
        List<Element> classpathentryElements = org.faktorips.runtime.internal.XmlUtil.getElements(
                classpathDocument.getDocumentElement(),
                "classpathentry");
        for (Element classpathentryElement : classpathentryElements) {
            String path = classpathentryElement.getAttribute("path");
            if (IpsStringUtils.isNotBlank(path) && path.contains("ipsClasspathContainer")) {
                if (!migratedContainer) {
                    String[] split = path.split("/");
                    String newPath = IpsClasspathContainerInitializer.CONTAINER_ID;
                    if (split.length == 2) {
                        newPath += '/' + split[1];
                    }
                    classpathentryElement.setAttribute("path", newPath);
                    migratedContainer = true;
                } else {
                    classpathDocument.getDocumentElement().removeChild(classpathentryElement);
                }
            }

        }
    }

    private void reloadProjectData() {
        @SuppressWarnings("deprecation")
        IpsModel ipsModel = IpsModel.get();
        ipsModel.clearProjectSpecificCaches(getIpsProject());
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_22_12_0(ipsProject, featureId);
        }
    }
}
