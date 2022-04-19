package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XTableUsage


import static extension org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductCommonsTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class TableUsagesTmpl{

    def package static memberField (XTableUsage it) '''
        /**
         * «localizedJDoc("FIELD_TABLE_USAGE", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(DEPRECATION)»
        private String «field(fieldName)» = null;
    '''

    def package static propertyField (XTableUsage it) '''
        /**
         * «localizedJDoc("PROPERTY_TABLE_USAGE", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
         «getAnnotations(DEPRECATION)»
         public static final String «field(constantNameTable)» = "«name»";
    '''

    def package static getterAndSetter (XTableUsage it) '''
        «getter»
        «setter»
    '''

    def private static getter (XTableUsage it) '''
        /**
         * «localizedJDoc("METHOD_GET_TABLE", name)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
         «getAnnotations(TABLE_USAGE_GETTER)»
        public «tableClassName» «method(methodNameGetter)» {
            if («fieldName» == null) {
                return null;
            }
            return «castFromTo("ITable<?>", tableClassName)»getRepository().getTable(«fieldName»);
        }
    '''

    def private static setter (XTableUsage it) '''
        /**
         * «localizedJDoc("METHOD_SET_TABLE_NAME", fieldName.toFirstUpper)»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
        «getAnnotations(DEPRECATION)»
        public void «method(methodNameSetter, "String", "tableName")» {
            «checkRepositoryModifyable»
            this.«fieldName» = tableName;
        }
    '''

    def package static doInitFromXmlMethodCall (XTableUsage it) '''
        «methodNameDoInitFromXml»(tableUsageMap);
    '''

    def package static doInitFromXmlMethod (XTableUsage it) '''
        /**
         * @generated
         */
        private void «method(methodNameDoInitFromXml, Map("String", Element), " tableUsageMap")» {
            Element element = tableUsageMap.get(«constantNameTable»);
            if (element != null) {
                «fieldName» = «ValueToXmlHelper».«getValueFromElement("element", XML_TAG_TABLE_CONTENT_NAME)»;
            }
        }
    '''

    def package static writeTableUsages (XTableUsage it) '''
            writeTableUsageToXml(element, «constantNameTable», «fieldName»);
    '''
}