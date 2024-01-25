/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is able to write an {@link IpsObjectPath} to XML or create an {@link IpsObjectPath}
 * from XML.
 *
 * @author dicker
 */
public class IpsObjectPathXmlPersister {

    /**
     * Xml element name for IPS object path.
     */
    public static final String XML_TAG_NAME = "IpsObjectPath"; //$NON-NLS-1$ ;

    public static final String ATTRIBUTE_NAME_USE_MANIFEST = "useManifest"; //$NON-NLS-1$

    private static final String ATTRIBUTE_NAME_OUTPUT_FOLDER_DERIVED_SOURCES = "outputFolderDerivedSources"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NAME_BASE_PACKAGE_MERGABLE = "basePackageMergable"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NAME_OUTPUT_FOLDER_MERGABLE_SOURCES = "outputFolderMergableSources"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NAME_BASE_PACKAGE_DERIVED = "basePackageDerived"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NAME_OUTPUT_DEFINED_PER_SRC_FOLDER = "outputDefinedPerSrcFolder"; //$NON-NLS-1$

    /**
     * Stores the given {@link IpsObjectPath} in the given {@link Document}, which represents the
     * .ipsproject file.
     *
     * @return the {@link Element} within the given Document, which represents the
     *             {@link IpsObjectPath} in the .ipsproject file.
     */
    public Element store(Document doc, IpsObjectPath path) {
        Element element = doc.createElement(IpsObjectPathXmlPersister.XML_TAG_NAME);
        boolean usingManifest = path.isUsingManifest();
        if (usingManifest) {
            element.setAttribute(ATTRIBUTE_NAME_USE_MANIFEST, Boolean.toString(usingManifest));
            return element;
        }

        element.setAttribute(ATTRIBUTE_NAME_OUTPUT_DEFINED_PER_SRC_FOLDER,
                Boolean.toString(path.isOutputDefinedPerSrcFolder()));
        element.setAttribute(ATTRIBUTE_NAME_OUTPUT_FOLDER_MERGABLE_SOURCES,
                path.getOutputFolderForMergableSources() == null ? IpsStringUtils.EMPTY
                        : PathUtil.toPortableString(path
                                .getOutputFolderForMergableSources().getProjectRelativePath()));
        element.setAttribute(ATTRIBUTE_NAME_BASE_PACKAGE_MERGABLE, path.getBasePackageNameForMergableJavaClasses());
        element.setAttribute(ATTRIBUTE_NAME_OUTPUT_FOLDER_DERIVED_SOURCES,
                path.getOutputFolderForDerivedSources() == null ? IpsStringUtils.EMPTY
                        : PathUtil.toPortableString(path
                                .getOutputFolderForDerivedSources().getProjectRelativePath()));
        element.setAttribute(ATTRIBUTE_NAME_BASE_PACKAGE_DERIVED, path.getBasePackageNameForDerivedJavaClasses());

        // entries
        for (IIpsObjectPathEntry entry : path.getEntries()) {
            Element entryElement = ((IpsObjectPathEntry)entry).toXml(doc);
            element.appendChild(entryElement);
        }

        return element;

    }

    /**
     * Reads and returns the {@link IpsObjectPath} from the .ipsproject file.
     *
     * @param ipsProject the {@link IIpsProject} of the {@link IpsObjectPath}
     * @param element the {@link Element}, the {@link IpsObjectPath} is stored
     * @throws IllegalStateException if the {@link IpsObjectPath} should be managed within the
     *             manifest, this method must not be called.
     */
    public IpsObjectPath read(IIpsProject ipsProject, Element element) {
        if (isUsingManifest(element)) {
            throw new IllegalStateException(
                    "Calling read within the XmlIpsObectPathPersistor is not allowed, if the manifest should be read."); //$NON-NLS-1$
        }

        IpsObjectPath path = new IpsObjectPath(ipsProject);
        path.setUsingManifest(false);

        path.setBasePackageNameForMergableJavaClasses(element.getAttribute(ATTRIBUTE_NAME_BASE_PACKAGE_MERGABLE));
        path.setBasePackageNameForDerivedJavaClasses(element.getAttribute(ATTRIBUTE_NAME_BASE_PACKAGE_DERIVED));
        String outputFolderMergedSourcesString = element.getAttribute(ATTRIBUTE_NAME_OUTPUT_FOLDER_MERGABLE_SOURCES);
        if (outputFolderMergedSourcesString.equals(IpsStringUtils.EMPTY)) {
            path.setOutputFolderForMergableSources(null);
        } else {
            path.setOutputFolderForMergableSources(ipsProject.getProject().getFolder(
                    java.nio.file.Path.of(outputFolderMergedSourcesString)));
        }
        String outputFolderDerivedSourcesString = element.getAttribute(ATTRIBUTE_NAME_OUTPUT_FOLDER_DERIVED_SOURCES);
        if (outputFolderDerivedSourcesString.equals(IpsStringUtils.EMPTY)) {
            path.setOutputFolderForDerivedSources(null);
        } else {
            path.setOutputFolderForDerivedSources(ipsProject.getProject().getFolder(
                    java.nio.file.Path.of(outputFolderDerivedSourcesString)));
        }
        path.setOutputDefinedPerSrcFolder(
                Boolean.parseBoolean(element.getAttribute(ATTRIBUTE_NAME_OUTPUT_DEFINED_PER_SRC_FOLDER)));

        // init entries
        NodeList nl = element.getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry[] entries = new IIpsObjectPathEntry[nl.getLength()];
        for (int i = 0; i < nl.getLength(); i++) {
            Element entryElement = (Element)nl.item(i);
            entries[i] = IpsObjectPathEntry.createFromXml(path, entryElement, ipsProject.getProject());
        }
        path.setEntries(entries);
        return path;
    }

    private boolean isUsingManifest(Element element) {
        return Boolean.parseBoolean(element.getAttribute(ATTRIBUTE_NAME_USE_MANIFEST));
    }

    /**
     * Returns a description of the xml format.
     */
    public String getXmlFormatDescription() {
        return IpsObjectPathXmlPersister.XML_TAG_NAME
                + " : " //$NON-NLS-1$
                + System.lineSeparator()
                + "The IpsObjectPath defines where Faktor-IPS searches for model and product definition files/objects for this project." //$NON-NLS-1$
                + System.lineSeparator()
                + "Basically it is the same concept as the Java classpath." //$NON-NLS-1$
                + System.lineSeparator()
                + "<" + XML_TAG_NAME + " " //$NON-NLS-1$ //$NON-NLS-2$
                + System.lineSeparator()
                + " " + ATTRIBUTE_NAME_USE_MANIFEST //$NON-NLS-1$
                + "                             Boolean flag that indicates whether the IpsObjectPath is managed within the manifest.mf. If this optional attribute is set to \"true\", then no other Attribute oder Child is allowed." //$NON-NLS-1$
                + System.lineSeparator()
                + " " + ATTRIBUTE_NAME_OUTPUT_DEFINED_PER_SRC_FOLDER //$NON-NLS-1$
                + "               Boolean flag that indicates if there are separate output folders for each source folder" //$NON-NLS-1$
                + System.lineSeparator()
                + " " + ATTRIBUTE_NAME_OUTPUT_FOLDER_MERGABLE_SOURCES //$NON-NLS-1$
                + "             The output folder for the generated artefacts that will not be deleted during a " //$NON-NLS-1$
                + "clean build cycle but may be merged with the generated content during a build cycle" //$NON-NLS-1$
                + System.lineSeparator()
                + " " + ATTRIBUTE_NAME_BASE_PACKAGE_MERGABLE //$NON-NLS-1$
                + "                     The base package for generated and merable java files" //$NON-NLS-1$
                + System.lineSeparator()
                + " " + ATTRIBUTE_NAME_OUTPUT_FOLDER_DERIVED_SOURCES //$NON-NLS-1$
                + "              The output folder for the generated artefacts that will be deleted during a clean build " //$NON-NLS-1$
                + "cycle and newly generated during each build cycle" //$NON-NLS-1$
                + System.lineSeparator()
                + " " + ATTRIBUTE_NAME_BASE_PACKAGE_DERIVED //$NON-NLS-1$
                + "                      The base package for generated derived java files" //$NON-NLS-1$
                + System.lineSeparator()
                + "The IpsObjectPath is defined through one or more entries." //$NON-NLS-1$
                + System.lineSeparator()
                + "Currently the following entry types are supported:" //$NON-NLS-1$
                + System.lineSeparator()
                + " " + System.lineSeparator() //$NON-NLS-1$
                + IpsSrcFolderEntry.getXmlFormatDescription() + System.lineSeparator()
                + " " + System.lineSeparator() //$NON-NLS-1$
                + IpsProjectRefEntry.getXmlFormatDescription() + " " + System.lineSeparator() //$NON-NLS-1$
                + IpsArchiveEntry.getXmlFormatDescription() + " " + System.lineSeparator() //$NON-NLS-1$
                + "Maven:" + System.lineSeparator() //$NON-NLS-1$
                + "  <" + IpsObjectPathEntry.XML_ELEMENT + System.lineSeparator() //$NON-NLS-1$
                + "     container=\"JDTClasspathContainer\"                      When using maven, the referenced projects can be replaced by the Maven-Classpath-Container." //$NON-NLS-1$
                + System.lineSeparator()
                + "     path=\"org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER\"      This entry should be the last entry in the IpsObjectPath." //$NON-NLS-1$
                + System.lineSeparator()
                + "     reexported=\"false\" type=\"container\" />" + System.lineSeparator()//$NON-NLS-1$
                + "  </" + IpsObjectPathEntry.XML_ELEMENT + ">" + System.lineSeparator(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
