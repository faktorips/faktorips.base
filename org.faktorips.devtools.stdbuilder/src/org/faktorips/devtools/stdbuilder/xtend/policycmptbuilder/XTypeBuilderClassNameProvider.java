/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.policycmptbuilder;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.xmodel.XType;

public class XTypeBuilderClassNameProvider {
    private final BuilderJavaClassNameProvider builderNameProvider;
    private final DefaultJavaClassNameProvider defNameProvider;
    private XType type;
    private IIpsSrcFile ipsSrcFile;

    public XTypeBuilderClassNameProvider(XType type) {
        builderNameProvider = new BuilderJavaClassNameProvider();
        defNameProvider = new DefaultJavaClassNameProvider(type.getGeneratorConfig().isGeneratePublishedInterfaces(type.getIpsProject()));
        this.type = type;
        this.ipsSrcFile = type.getIpsObjectPartContainer().getIpsSrcFile();
    }

    public BuilderJavaClassNameProvider getBuilderNameProvider() {
        return builderNameProvider;
    }

    public DefaultJavaClassNameProvider getDefNameProvider() {
        return defNameProvider;
    }

    /**
     * No import
     * 
     * @return name of the builder class
     */
    public String getName() {
        return QNameUtil.getUnqualifiedName(builderNameProvider.getImplClassName(ipsSrcFile));
    }

    /**
     * No import
     * 
     * @return name of the XType that is built by the builder.
     */
    public String getTypeName() {
        return QNameUtil.getUnqualifiedName(builderNameProvider.getDeclClassName(ipsSrcFile));
    }

    /**
     * Import
     * 
     * @return name of the XType that is built by the builder.
     */
    public String getTypeClassName() {
        return type.addImport(type.getJavaClassNaming().getQualifiedClassName(ipsSrcFile, BuilderAspect.IMPLEMENTATION,
                defNameProvider));
    }

    /**
     * Import
     * 
     * @return name of the published interface of XType that is built by the builder. If no
     *         published interface is generated, the implementation class name is returned.
     */
    public String getTypePublishedInterfaceName() {
        if (type.getGeneratorConfig().isGeneratePublishedInterfaces(type.getIpsProject())) {
            return type.addImport(type.getJavaClassNaming().getQualifiedClassName(ipsSrcFile, BuilderAspect.INTERFACE,
                    defNameProvider));
        } else {
            return getTypeClassName();
        }

    }

    public String getFactoryImplClassName() {
        type.addImport(type.getQualifiedName(BuilderAspect.IMPLEMENTATION));
        return getName() + ".Factory";
    }

    public String getVariableName(String name) {
        return StringUtils.uncapitalize(name);
    }
}
