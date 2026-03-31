/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.base;

import java.nio.file.Path;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;

/**
 * An implementation of {@link IIpsArtefactBuilder} that generates a XML file for a specific
 * IpsObject. It has methods to get a file location for the generated object.
 */
public interface IXmlFileBuilder extends IIpsArtefactBuilder {

    /**
     * Returns the relative path to the generated XML file.
     *
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to generate
     * @return the relative path to the generated XML file
     */
    Path getXmlContentRelativeFile(IIpsSrcFile ipsSrcFile);

    /**
     * Returns the handle to the file where the xml content for the given ips source file is stored.
     */
    AFile getXmlContentFile(IIpsSrcFile ipsSrcFile);

}