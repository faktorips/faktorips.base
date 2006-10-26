/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.versionmanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.osgi.framework.Version;

/**
 * Version manager for the core-feature of FaktorIps. 
 * 
 * The information about the compatibility of a version of the feature is stored in files named
 * "compatiblity.&lt;version&gt;".
 * <p>
 * &lt;version&gt; is the version of the feature the compatibility is for, 0.9.38 for example.
 * <p>
 * The content of the file is a plain list of all versions of the feature the version namend in the 
 * filenam is compatible to. Each version has to be seperated by a newline. 
 * <p>
 * The files are handled as properties-files, so be aware of the encoding.  
 * 
 * @author Thorsten Guenther
 */
public class CoreVersionManager implements IIpsFeatureVersionManager {

    private String version;
    private Properties compatibleVersions;
    
    /**
     * {@inheritDoc}
     */
    public void setFeatureId(String featureId) {
    }

    /**
     * {@inheritDoc}
     */
    public String getFeatureId() {
        return "org.faktorips.feature";
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentVersion() {
        if (version == null) {
            version = (String)Platform.getBundle("org.faktorips.devtools.core").getHeaders().get("Bundle-Version");
        }
        return version;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCurrentVersionCompatibleWith(String otherVersion) {
        if (compareToCurrentVersion(otherVersion) > 0) {
            return false;
        }
        
        if (compareToCurrentVersion(otherVersion) == 0) {
            return true;
        }
        
        if (compatibleVersions == null) {
            InputStream in = getClass().getClassLoader().getResourceAsStream("org/faktorips/devtools/core/internal/model/versionmanager/compatibility." + getCurrentVersion()); //$NON-NLS-1$
            compatibleVersions = new Properties();
            if (in != null) {
                try {
                    compatibleVersions.load(in);
                }
                catch (IOException e) {
                    IpsPlugin.log(e);
                }
            }
        }

        return compatibleVersions.get(otherVersion) != null;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareToCurrentVersion(String otherVersion) {
        Version outer = Version.parseVersion(otherVersion);
        Version inner = Version.parseVersion(getCurrentVersion());
        return outer.compareTo(inner);
    }

}
