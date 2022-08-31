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

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsProjectRefEntry.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectRefEntry extends IpsObjectPathEntry implements IIpsProjectRefEntry {

    /** the IPS project referenced by this entry */
    private IIpsProject referencedIpsProject;

    /**
     * Special handling of project names in NWDI environment (SAP NetWeaver Development
     * Infrastructure)
     */
    private boolean useNWDITrackPrefix;

    public IpsProjectRefEntry(IpsObjectPath path) {
        super(path);
    }

    public IpsProjectRefEntry(IpsObjectPath ipsObjectPath, IIpsProject referencedIpsProject) {
        super(ipsObjectPath);
        this.referencedIpsProject = referencedIpsProject;
    }

    /**
     * Returns a description of the XML format.
     */
    public static final String getXmlFormatDescription() {
        return "Project Reference:" + System.lineSeparator() //$NON-NLS-1$
                + "  <" + XML_ELEMENT + System.lineSeparator() //$NON-NLS-1$
                + "     type=\"project\"" + System.lineSeparator() //$NON-NLS-1$
                + "     referencedIpsProject=\"base\">      The other project used by this project." //$NON-NLS-1$
                + System.lineSeparator()
                + "  </" + XML_ELEMENT + ">" + System.lineSeparator(); //$NON-NLS-1$ //$NON-NLS-2$
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
    public boolean exists(QualifiedNameType qnt) {
        if (referencedIpsProject == null) {
            return false;
        }
        return referencedIpsProject.findIpsSrcFile(qnt) != null;
    }

    @Override
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType type) {
        return Collections.emptyList();
    }

    @Override
    public void initFromXml(Element element, AProject project) {
        super.initFromXml(element, project);
        initUseNWDITrackPrefix(element);
        String projectName = element.getAttribute("referencedIpsProject"); //$NON-NLS-1$
        if (isUseNWDITrackPrefix()) {
            projectName = createNWDIProjectName(projectName, project.getName());
        }
        if (!IpsStringUtils.isEmpty(projectName)) {
            referencedIpsProject = IIpsModel.get().getIpsProject(projectName);
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
        Pattern pInstance = Pattern.compile(".*[0-9]+~.*"); //$NON-NLS-1$
        boolean hasInstanceNumber = pInstance.matcher(currProjectName).find();
        Pattern p = createNWDIProjectNamePattern(hasInstanceNumber);
        Matcher mCurr = p.matcher(currProjectName);

        // CSOFF: BooleanExpressionComplexity
        if (!mCurr.find()
                || !((hasInstanceNumber && mCurr.groupCount() == 3) || !hasInstanceNumber && mCurr.groupCount() == 2)) {
            // CSON: BooleanExpressionComplexity
            // exit and don't fix project name, because current project doesn't match nwds project
            // name pattern
            return refProjectName;
        }

        // eval the nwds prefix (track name) and instance number
        String currTrackName;
        // note that the instance number is optional
        String currInstance = null;
        currTrackName = mCurr.group(1);
        if (hasInstanceNumber) {
            currInstance = mCurr.group(2);
        }

        // get ref project name without nwds prefix
        Matcher mRef = p.matcher(refProjectName);
        String refProjectNameRelative = getNWDIProjectRelativeName(hasInstanceNumber, mRef, refProjectName);

        // build the ref project name using the current nwds track prefix
        return currTrackName + separator + (currInstance != null ? currInstance + separator : "") //$NON-NLS-1$
                + refProjectNameRelative;
    }

    private static String getNWDIProjectRelativeName(boolean hasInstanceNumber, Matcher mRef, String refProjectName) {
        if (mRef.find() && mRef.groupCount() == (hasInstanceNumber ? 3 : 2)) {
            return mRef.group((hasInstanceNumber ? 3 : 2));
        }
        return refProjectName;
    }

    private static Pattern createNWDIProjectNamePattern(boolean hasInstanceNumber) {
        Pattern p = null;
        if (hasInstanceNumber) {
            // with instance number NWDS 72
            p = Pattern.compile("(.*)~([0-9]+)~(.*)"); //$NON-NLS-1$
        } else {
            // without instance number since NWDS 73
            p = Pattern.compile("(.[^~]*)~(.*)"); //$NON-NLS-1$
        }
        return p;
    }

    @Override
    public Element toXml(Document doc) {
        Element element = super.toXml(doc);
        element.setAttribute(XML_ATTRIBUTE_TYPE, TYPE_PROJECT_REFERENCE);
        element.setAttribute("referencedIpsProject", //$NON-NLS-1$
                referencedIpsProject == null ? "" : referencedIpsProject.getName()); //$NON-NLS-1$
        if (useNWDITrackPrefix) {
            // store attribute only if nwdi support is needed
            element.setAttribute("useNWDITrackPrefix", Boolean.toString(useNWDITrackPrefix)); //$NON-NLS-1$
        }
        return element;
    }

    @Override
    public MessageList validate() {
        MessageList result = new MessageList();
        IIpsProject project = getReferencedIpsProject();
        if (project == null) {
            String text = Messages.IpsProjectRefEntry_noReferencedProjectSpecified;
            Message msg = new Message(MSGCODE_PROJECT_NOT_SPECIFIED, text, Message.ERROR, this);
            result.add(msg);
            return result;
        }
        if (!project.exists()) {
            String text = MessageFormat.format(Messages.IpsProjectRefEntry_msgMissingReferencedProject,
                    project.getName());
            Message msg = new Message(MSGCODE_MISSING_PROJECT, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ProjectRefEntry[" + (referencedIpsProject == null ? "null" : referencedIpsProject.getName()) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public boolean containsResource(String path) {
        return false;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return null;
    }
}
