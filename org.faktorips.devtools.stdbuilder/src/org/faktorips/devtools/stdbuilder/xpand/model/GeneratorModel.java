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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.Locale;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.builder.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.type.IType;

/**
 * 
 * @author widmaier
 */
public class GeneratorModel {

    private XClass xClass;
    private final JavaClassNaming classNaming;

    // Model Service
    // ImportHandler

    public GeneratorModel(JavaClassNaming classNaming) {
        this.classNaming = classNaming;
    }

    public XClass getXClass() {
        return xClass;
    }

    public boolean removeImport(String importStatement) {
        // TODO Auto-generated method stub
        return false;
    }

    public void addImport(String importStatement) {
        // TODO Auto-generated method stub

    }

    public Locale getLanguageUsedInGeneratedSourceCode() {
        // TODO Auto-generated method stub
        return null;
    }

    public IPath getRelativeJavaFile(IIpsSrcFile ipsSrcFile) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUnqualifiedClassName(IType type) {
        return classNaming.getUnqualifiedClassName(type.getIpsSrcFile());
    }

    public String getPackage(IType type) {
        return classNaming.getPackageName(type.getIpsSrcFile());
    }

    public String getQualifiedClassName(IType type) {
        return classNaming.getQualifiedClassName(type.getIpsSrcFile());
    }

    public String getQualifiedClassNameForInterface(IType type) {
        return classNaming.getUnqualifiedClassName(type.getIpsSrcFile());
    }

    public boolean isGeneratePropertyChange() {
        // getBuilderSet().getConfig()
        // .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER).booleanValue();
        return false;
    }

}
