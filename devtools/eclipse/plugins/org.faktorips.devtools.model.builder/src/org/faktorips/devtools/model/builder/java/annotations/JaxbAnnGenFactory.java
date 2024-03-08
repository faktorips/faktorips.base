/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations;

import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.builder.java.annotations.enums.EnumDeclClassJaxbAnnGen;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.PolicyCmptImplClassAssociationJaxbAnnGen;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.PolicyCmptImplClassAttributeFieldJaxbGen;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.PolicyCmptImplClassJaxbAnnGen;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.PolicyCmptImplClassProductConfigurationJaxbGen;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;

public class JaxbAnnGenFactory implements IAnnotationGeneratorFactory {

    public JaxbAnnGenFactory() {
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        return switch (type) {
            case POLICY_CMPT_IMPL_CLASS -> new PolicyCmptImplClassJaxbAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD -> new PolicyCmptImplClassAssociationJaxbAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD -> new PolicyCmptImplClassAttributeFieldJaxbGen();
            case POLICY_CMPT_IMPL_CLASS_PRODUCTCONFIGURATION_FIELD -> new PolicyCmptImplClassProductConfigurationJaxbGen();
            case ENUM_CLASS -> new EnumDeclClassJaxbAnnGen();
            default -> null;
        };
    }

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        // need this complex way because the builderSet you could get from IpsProject is not
        // initialized when this method is called
        IIpsProjectProperties properties = ipsProject.getReadOnlyProperties();
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel().getIpsArtefactBuilderSetInfo(
                properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfig builderSetConfig = properties.getBuilderSetConfig().create(ipsProject,
                builderSetInfo);
        return JaxbSupportVariant.of(builderSetConfig.getPropertyValueAsString(
                JavaBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT)) != JaxbSupportVariant.None;
    }

    /**
     * This enum
     */
    public enum JaxbAnnotation {
        XmlAttribute,
        XmlElement,
        XmlElementWrapper,
        XmlIDREF,
        XmlJavaTypeAdapter {
            private static final String ADAPTERS_PACKAGE = "adapters.";

            @Override
            String getPartiallyQualifiedName() {
                return ANNOTATION_PACKAGE + ADAPTERS_PACKAGE + name();
            }
        },
        XmlRootElement;

        private static final String ANNOTATION_PACKAGE = "annotation.";

        String getPartiallyQualifiedName() {
            return ANNOTATION_PACKAGE + name();
        }

        /**
         * Generates the qualified class name of an javax/jakarta annotation, depending on the given
         * {@link JaxbSupportVariant}.
         *
         * @param setting The configured {@code generateJaxbSupport} setting of an Faktor-IPS
         *            project.
         * @return The qualified class name of an javax/jakarta annotation.
         */
        public String qualifiedNameFrom(JaxbSupportVariant setting) {
            return setting.getLibraryPackage() + getPartiallyQualifiedName();
        }
    }

    /**
     * The names of all Faktor-IPS {@code XmlAdapters}.
     */
    public enum IpsXmlAdapters {
        ProductConfigurationXmlAdapter,
        AbstractJaxbModelObject,
        DecimalAdapter,
        GregorianCalendarAdapter,
        LocalDateAdapter,
        LocalDateTimeAdapter,
        LocalTimeAdapter,
        MoneyAdapter,
        MonthAdapter,
        YearAdapter,
        MonthDayAdapter;

        /**
         * Generates the qualified class name of the Faktor-IPS {@code XmlAdapter} depending on the
         * given {@link JaxbSupportVariant}.
         *
         * @param setting The configured {@code generateJaxbSupport} setting of an Faktor-IPS
         *            project.
         * @return The qualified class name of the Faktor-IPS {@code XmlAdapter}
         */
        public String qualifiedNameFrom(JaxbSupportVariant setting) {
            return setting.getIpsPackage() + name();
        }
    }
}
