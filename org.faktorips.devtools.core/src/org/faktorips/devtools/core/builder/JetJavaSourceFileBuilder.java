/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * A specialization of JavaSourceFileBuilder that accepts a generator class provided by JET. This
 * builder can be configured an instantiation time. There is no need for so subclassing. This
 * builder creates and keeps a <code>LocalizedStringSet</code> that is assigned to the content
 * generator when content generation starts. The <code>LocalizedStringSet</code> is created based on
 * the content generator class. If one wants to use the string set within the content generator a
 * property file with the name as the content generator java source file has to be created and keep
 * in the same directory.
 * 
 * @author Peter Erzberger
 */
public class JetJavaSourceFileBuilder extends JavaSourceFileBuilder {

    private Class generatorClass;
    private IpsObjectType ipsObjectType;
    private String classNamePrefix;
    private String classNameSuffix;

    /**
     * Creates a new JetJavaSourceFileBuilder.
     * 
     * @param builderSet see super class constructor
     * @param kindId see super class constructor
     * @param generatorClass the generator class that is responsible for source content generation.
     *            The generator class must extend the JetJavaContentGenerator
     * @param ipsObjectType the ips object type this builder creates java source files for
     * @param enableMerge if set to true merging will be enable
     * @param classNamePrefix is used to create the unqualified name. prefix + IpsObject.getName() =
     *            unqualified name. The classNameSuffix can be applied in addition to the prefix.
     * @param classNameSuffix is used to create the unqualified name. IpsObject.getName() + suffix =
     *            unqualified name. The classNamePrefix can be applied in addition to the suffix.
     */
    public JetJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet, String kindId, Class generatorClass,
            IpsObjectType ipsObjectType, boolean enableMerge, String classNamePrefix, String classNameSuffix) {
        super(builderSet, kindId, new LocalizedStringsSet(generatorClass));
        ArgumentCheck.notNull(generatorClass, this);
        ArgumentCheck.notNull(ipsObjectType, this);
        this.ipsObjectType = ipsObjectType;
        this.generatorClass = generatorClass;
        this.classNamePrefix = classNamePrefix;
        this.classNameSuffix = classNameSuffix;
        setMergeEnabled(enableMerge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return Messages.JetJavaSourceFileBuilder_name;
    }

    /**
     * Instantiates a content generator, assigns this builder and a <code>LocalizedStringSet</code>
     * to it and calls the generate method of it.
     * 
     * @see org.faktorips.devtools.core.builder.JavaSourceFileBuilder#generate(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public String generate() throws CoreException {

        try {
            JetJavaContentGenerator generator = (JetJavaContentGenerator)generatorClass.newInstance();
            generator.setJavaSourceFileBuilder(this);
            return generator.generate(getIpsSrcFile());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder#isBuilderFor(org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile)
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return ipsObjectType.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * 
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.builder.JavaSourceFileBuilder#getUnqualifiedClassName(org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile)
     */
    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) {
        StringBuffer buf = new StringBuffer();

        if (classNamePrefix != null) {
            buf.append(classNamePrefix);
        }

        buf.append(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()));

        if (classNameSuffix != null) {
            buf.append(classNameSuffix);
        }
        return buf.toString();
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        // TODO AW: Not implemented yet.
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        // TODO AW: Not implemented yet.
        throw new RuntimeException("Not implemented yet.");
    }

}
