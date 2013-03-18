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

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
     * Xml element name for ips object path.
     */
    public static final String XML_TAG_NAME = "IpsObjectPath"; //$NON-NLS-1$;

    static final String ATTRIBUTE_NAME_USE_MANIFEST = "useManifest"; //$NON-NLS-1$

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
     *         {@link IpsObjectPath} in the .ipsproject file.
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
                path.getOutputFolderForMergableSources() == null ? StringUtils.EMPTY : path
                        .getOutputFolderForMergableSources().getProjectRelativePath().toString());
        element.setAttribute(ATTRIBUTE_NAME_BASE_PACKAGE_MERGABLE, path.getBasePackageNameForMergableJavaClasses());
        element.setAttribute(ATTRIBUTE_NAME_OUTPUT_FOLDER_DERIVED_SOURCES,
                path.getOutputFolderForDerivedSources() == null ? StringUtils.EMPTY : path
                        .getOutputFolderForDerivedSources().getProjectRelativePath().toString());
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
        if (outputFolderMergedSourcesString.equals(StringUtils.EMPTY)) {
            path.setOutputFolderForMergableSources(null);
        } else {
            path.setOutputFolderForMergableSources(ipsProject.getProject().getFolder(
                    new Path(outputFolderMergedSourcesString)));
        }
        String outputFolderDerivedSourcesString = element.getAttribute(ATTRIBUTE_NAME_OUTPUT_FOLDER_DERIVED_SOURCES);
        if (outputFolderDerivedSourcesString.equals(StringUtils.EMPTY)) {
            path.setOutputFolderForDerivedSources(null);
        } else {
            path.setOutputFolderForDerivedSources(ipsProject.getProject().getFolder(
                    new Path(outputFolderDerivedSourcesString)));
        }
        path.setOutputDefinedPerSrcFolder(Boolean.valueOf(
                element.getAttribute(ATTRIBUTE_NAME_OUTPUT_DEFINED_PER_SRC_FOLDER)).booleanValue());

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
                + SystemUtils.LINE_SEPARATOR
                + "The IpsObjectPath defines where Faktor-IPS searches for model and product definition files/objects for this project." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "Basically it is the same concept as the Java classpath." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "<" + XML_TAG_NAME + " " //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR
                + " " + ATTRIBUTE_NAME_USE_MANIFEST + "                             Boolean flag that indicates whether the IpsObjectPath is managed within the manifest.mf. If this optional attribute is set to \"true\", then no other Attribute oder Child is allowed." //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR
                + " " + ATTRIBUTE_NAME_OUTPUT_DEFINED_PER_SRC_FOLDER + "               Boolean flag that indicates if there are separate output folders for each source folder" //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR
                + " " + ATTRIBUTE_NAME_OUTPUT_FOLDER_MERGABLE_SOURCES + "             The output folder for the generated artefacts that will not be deleted during a " + //$NON-NLS-1$ //$NON-NLS-2$
                "clean build cycle but may be merged with the generated content during a build cycle" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " " + ATTRIBUTE_NAME_BASE_PACKAGE_MERGABLE + "                     The base package for generated and merable java files" //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR
                + " " + ATTRIBUTE_NAME_OUTPUT_FOLDER_DERIVED_SOURCES + "              The output folder for the generated artefacts that will be deleted during a clean build " + //$NON-NLS-1$ //$NON-NLS-2$
                "cycle and newly generated during each build cycle" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " " + ATTRIBUTE_NAME_BASE_PACKAGE_DERIVED + "                      The base package for generated derived java files" //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR
                + "The IpsObjectPath is defined through one or more entries." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "Currently the following entry types are supported:" //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsSrcFolderEntry.getXmlFormatDescription() + SystemUtils.LINE_SEPARATOR
                + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsProjectRefEntry.getXmlFormatDescription() + " " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + IpsArchiveEntry.getXmlFormatDescription();
    }
}
