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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;

/**
 * Implements the old interface {@link IJavaPackageStructure} and provides the same package names
 * using the new {@link JavaClassNaming} class.
 * 
 * @author widmaier
 */
public class JavaPackageStructureAdapter implements IJavaPackageStructure {

    @Override
    public String getPackage(IIpsArtefactBuilder builder, IIpsSrcFile ipsSrcFile) throws CoreException {
        if (builder instanceof JavaSourceFileBuilder) {
            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builder;
            return getPackageName(ipsSrcFile, javaBuilder.isBuildingPublishedSourceFile(),
                    builder.buildsDerivedArtefacts());
        }
        return getPackageName(ipsSrcFile, false, builder.buildsDerivedArtefacts());
    }

    protected String getPackageName(IIpsSrcFile ipsSrcFile, boolean published, boolean mergable) {
        JavaClassNaming naming = new JavaClassNaming(published, mergable);
        return naming.getPackageName(ipsSrcFile);
    }
}
