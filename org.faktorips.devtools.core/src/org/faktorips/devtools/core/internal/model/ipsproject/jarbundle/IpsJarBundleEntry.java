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

package org.faktorips.devtools.core.internal.model.ipsproject.jarbundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsContainer4JdtClasspathContainer;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An {@link IpsJarBundleEntry} is ab {@link IpsObjectPathEntry} that references a ziped version of
 * an IPS bundle. An IPS bundle needs to have a MANIFEST.MF containing all necessary information
 * about the included IPS objects and the generated source code. The structure of the MANIFEST.MF is
 * explained in detail in {@link IpsBundleManifest}.
 * <p>
 * Normally an {@link IpsJarBundleEntry} is resolved by using a classpath container like
 * {@link IpsContainer4JdtClasspathContainer}. Mostly for testing purposes this entry has also the
 * ability to be initialized by the {@link IpsObjectPath} in the {@link IpsProjectProperties}. To
 * use this approach you have to setup the following entry:
 * <p>
 * <code>
 * &lt;Entry type="jarbundle" bundlePath="path/to/grundmodell.jar"/&gt;
 * </code>
 * 
 * @author dirmeier
 */
public class IpsJarBundleEntry extends IpsObjectPathEntry {

    private static final String XML_ATTRIBUTE_PATH = "bundlePath"; //$NON-NLS-1$

    private IpsJarBundle ipsJarBundle;

    /**
     * This constructor creates a new {@link IpsJarBundleEntry} for the given {@link IpsObjectPath}
     * and reads the all information from a jar file specified by the jarPath parameter.
     * <p>
     * The path have to be an absolute path.
     * 
     * @param ipsObjectPath The parent {@link IpsObjectPath} of this entry
     * @param jarPath The absolut path to a jar file this entry should represent.
     */
    public IpsJarBundleEntry(IpsObjectPath ipsObjectPath, IPath jarPath) {
        super(ipsObjectPath);
        initJarBundle(jarPath);
    }

    /**
     * This constructor does not initialize the {@link IpsJarBundle}. If you use this constructor
     * you have to make sure that the jar bundle is initialized afterwards for example by using
     * {@link #initFromXml(Element, IProject)}
     * 
     * @param ipsObjectPath The parent {@link IpsObjectPath} of this entry
     */
    public IpsJarBundleEntry(IpsObjectPath ipsObjectPath) {
        super(ipsObjectPath);
    }

    private void initJarBundle(IPath jarPath) {
        try {
            JarFileFactory jarFileFactory = new JarFileFactory(jarPath);
            ipsJarBundle = new IpsJarBundle(getIpsProject(), jarFileFactory);
            ipsJarBundle.initJarFile();
        } catch (IOException e) {
            IpsPlugin.log(e);
        }
    }

    void setIpsJarBundle(IpsJarBundle ipsJarBundle) {
        this.ipsJarBundle = ipsJarBundle;
    }

    @Override
    public String getType() {
        return TYPE_JARBUNDLE;
    }

    @Override
    public String getIpsPackageFragmentRootName() {
        return ipsJarBundle.getLocation().lastSegment();
    }

    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return ipsJarBundle.getRoot();
    }

    IpsJarBundle getJarBundle() {
        return ipsJarBundle;
    }

    @Override
    public MessageList validate() throws CoreException {
        MessageList messageList = new MessageList();
        if (ipsJarBundle == null || !ipsJarBundle.isValid()) {
            messageList.newError(MSGCODE_MISSING_JARBUNDLE, Messages.IpsJarBundleEntry_msg_invalid, this, null);
        }
        return messageList;
    }

    @Override
    public InputStream getRessourceAsStream(String path) throws CoreException {
        return ipsJarBundle.getResourceAsStream(path);
    }

    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        return ipsJarBundle.contains(qnt);
    }

    @Override
    protected IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType, Set<IIpsObjectPathEntry> visitedEntries)
            throws CoreException {
        if (ipsJarBundle.contains(nameType)) {
            return getIpsPackageFragmentRoot().getIpsPackageFragment(nameType.getPackageName()).getIpsSrcFile(
                    nameType.getFileName());
        } else {
            return null;
        }
    }

    @Override
    public void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefixParam,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {
        String prefix = prefixParam;
        if (ignoreCase) {
            prefix = prefixParam.toLowerCase();
        }
        for (QualifiedNameType qnt : ipsJarBundle.getQNameTypes()) {
            String name = qnt.getUnqualifiedName();
            if (ignoreCase) {
                name = name.toLowerCase();
            }
            if (name.startsWith(prefix)) {
                IIpsSrcFile file = findIpsSrcFile(qnt);
                if (file.exists()) {
                    result.add(file);
                }
            }
        }
    }

    @Override
    public void initFromXml(Element element, IProject project) {
        String fullPath = element.getAttribute(XML_ATTRIBUTE_PATH);
        Path jarPath = new Path(fullPath);
        initJarBundle(jarPath);
    }

    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(IpsObjectPathEntry.XML_ELEMENT);
        element.setAttribute("type", TYPE_JARBUNDLE); //$NON-NLS-1$
        element.setAttribute(XML_ATTRIBUTE_PATH, ipsJarBundle.getLocation().toPortableString());
        return element;
    }

}
