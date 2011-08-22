/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsProjectRefEntry.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectRefEntry extends IpsObjectPathEntry implements IIpsProjectRefEntry {

    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return "Project Reference:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "     type=\"project\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "     referencedIpsProject=\"base\">      The other project used by this project." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "  </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /** the ips project referenced by this entry */
    private IIpsProject referencedIpsProject;

    /**
     * special handling of project names in nwdi environment (SAP NetWeaver Development
     * Infrastructure)
     */
    private boolean useNWDITrackPrefix;

    public IpsProjectRefEntry(IpsObjectPath path) {
        super(path);
    }

    public IpsProjectRefEntry(IpsObjectPath path, IIpsProject referencedIpsProject) {
        super(path);
        this.referencedIpsProject = referencedIpsProject;
    }

    @Override
    public IIpsProject getReferencedIpsProject() {
        return referencedIpsProject;
    }

    /**
     * Returns <code>true</code> if the stored project name must be converted using the nwdi
     * convention.
     */
    public boolean isUseNWDITrackPrefix() {
        return useNWDITrackPrefix;
    }

    @Override
    public String getType() {
        return TYPE_PROJECT_REFERENCE;
    }

    @Override
    public String getIpsPackageFragmentRootName() {
        return null;
    }

    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return null;
    }

    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        if (referencedIpsProject == null) {
            return false;
        }
        return referencedIpsProject.findIpsSrcFile(qnt) != null;
    }

    @Override
    public void findIpsSrcFilesInternal(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {
        if (referencedIpsProject != null) {
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsSrcFiles(type, packageFragment,
                    result, visitedEntries);
        }
    }

    @Override
    protected IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType, Set<IIpsObjectPathEntry> visitedEntries)
            throws CoreException {
        if (referencedIpsProject == null) {
            return null;
        }
        return ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsSrcFile(nameType, visitedEntries);
    }

    @Override
    public void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        if (referencedIpsProject != null) {
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsSrcFilesStartingWith(type, prefix,
                    ignoreCase, result, visitedEntries);
        }
    }

    @Override
    public void initFromXml(Element element, IProject project) {
        initUseNWDITrackPrefix(element);
        String projectName = element.getAttribute("referencedIpsProject"); //$NON-NLS-1$
        if (isUseNWDITrackPrefix()) {
            projectName = createNWDIProjectName(projectName, project.getName());
        }
        if (!StringUtils.isEmpty(projectName)) {
            referencedIpsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
        }
    }

    private void initUseNWDITrackPrefix(Element element) {
        useNWDITrackPrefix = Boolean.parseBoolean(element.getAttribute("useNWDITrackPrefix")); //$NON-NLS-1$
    }

    /**
     * Converts the given project name using the nwdi project name convention. The nwdi names are
     * stored in the following format: track~instance~projectname. The prefix "track~instance~" will
     * be replaced from the given refProjectName by the prefix of the currProjectName.
     * <p>
     * The returned projectName contains always the nwdi prefix (track plus instance number ) of the
     * currProjectName but if the current project name doesn't use this prefix then the given
     * refProjectName will be returned without using any prefix.
     * <p>
     * This dirty functionality is necessary because in nwdi environment the project name could be
     * differ if the project is stored in different developer tracks or instances.
     */
    public static String createNWDIProjectName(String refProjectName, String currProjectName) {
        String separator = "~"; //$NON-NLS-1$
        Pattern p = Pattern.compile("(.*)~([0-9]+)~(.*)"); //$NON-NLS-1$

        Matcher mCurr = p.matcher(currProjectName);
        if (!mCurr.find()) {
            return refProjectName;
        }
        if (!(mCurr.groupCount() == 3)) {
            return refProjectName;
        }
        String currTrackName;
        String currInstance;
        currTrackName = mCurr.group(1);
        currInstance = mCurr.group(2);

        Matcher mRef = p.matcher(refProjectName);
        String refProjectNameRelative = refProjectName;
        if (mRef.find() && mRef.groupCount() == 3) {
            refProjectNameRelative = mRef.group(3);
        }
        return currTrackName + separator + currInstance + separator + refProjectNameRelative;
    }

    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_PROJECT_REFERENCE); //$NON-NLS-1$
        element.setAttribute("referencedIpsProject", referencedIpsProject == null ? "" : referencedIpsProject.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        if (useNWDITrackPrefix) {
            // store attribute only if nwdi support is needed
            element.setAttribute("useNWDITrackPrefix", Boolean.toString(useNWDITrackPrefix)); //$NON-NLS-1$ 
        }
        return element;
    }

    @Override
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        IIpsProject project = getReferencedIpsProject();
        if (project == null) {
            String text = Messages.IpsProjectRefEntry_noReferencedProjectSpecified;
            Message msg = new Message(MSGCODE_PROJECT_NOT_SPECIFIED, text, Message.ERROR, this);
            result.add(msg);
            return result;
        }
        if (!project.exists()) {
            String text = NLS.bind(Messages.IpsProjectRefEntry_msgMissingReferencedProject, project.getName());
            Message msg = new Message(MSGCODE_MISSING_PROJECT, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ProjectRefEntry[" + (referencedIpsProject == null ? "null" : referencedIpsProject.getName()) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Interprets the given path as project-relative path.
     */
    @Override
    public InputStream getRessourceAsStream(String path) throws CoreException {
        return getReferencedIpsProject().getResourceAsStream(path);
    }

}
