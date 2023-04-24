package org.faktorips.devtools.stdbuilder.xtend.table.template

import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.table.XColumn
import org.faktorips.devtools.stdbuilder.xmodel.table.XTableRow

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

import static extension org.faktorips.devtools.stdbuilder.xtend.table.template.ColumnAttributeExtensionTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.Constants.*

class TableRowTmpl {

    def static String body(XTableRow it) '''
        
        /**
         *«localizedJDoc("CLASS_DESCRIPTION")»
         *
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
        «IF !getAnnotations(ELEMENT_JAVA_DOC).nullOrEmpty»*«ENDIF»
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        public class «name»«IF generateToXmlSupport» implements «IToXmlSupport»«ENDIF»{
            «nullrowconstant»
            «attributes»
            «constructors»
            «methods»
            «writeToXmlMethod»
        }
        
    '''

    def private static nullrowconstant(XTableRow it) '''
        /**
         * @generated
         */
        public static final «name» NULL_ROW = new «name»(«nullExpressions»);
    '''

    def private static attributes(XTableRow it) '''
        
        «FOR it : validColumns» «attribute» «ENDFOR»
        
    '''

    def private static attribute(XColumn it) '''
        «IF datatypeName !== null»
            /**
            «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
            «IF !getAnnotations(ELEMENT_JAVA_DOC).nullOrEmpty»*«ENDIF»
             * @generated
             */
            «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
            private final «datatypeName» «attributeName»;
        «ENDIF»
    '''

    def private static constructors(XTableRow it) '''
        /**
         *«localizedJDoc("CONSTRUCTOR")»
         *
        «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
        «IF !getAnnotations(ELEMENT_JAVA_DOC).nullOrEmpty»*«ENDIF»
         * @generated
         */
        «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
        public «method(name, constructorParameters)»{
            «FOR it : constructorParameters»
                this.«name» = «name»;
            «ENDFOR»
        }
    '''

    def private static methods(XTableRow it) '''
        «FOR it : validColumns» «getterMethod» «ENDFOR»
        «toStringMethod»
    '''

    def private static getterMethod(XColumn it) '''
        /**
        «getAnnotations(ELEMENT_JAVA_DOC, true)»
        «IF !getAnnotations(ELEMENT_JAVA_DOC).nullOrEmpty»*«ENDIF»
         * @generated
         */
        «getAnnotations(TABLE_ROW_CLASS_COLUMN_GETTER)»
        public «datatypeName» «method(methodNameGetter)»{
            return «attributeName»;
        }
    '''

    def private static toStringMethod(XTableRow it) '''
        /**
         * @generated
         */
        «overrideAnnotationIf(true)»
        public String «method("toString")»{
            return «attributeNames»;
        }
    '''

    def package static writeToXmlMethod(XTableRow it) '''
        «IF generateToXmlSupport»
            /**
             *«inheritDoc»
             *
             * @generated
             */
            @Override
            public void «writePropertiesToXml(Element + " element")» {
                Element rowElement = element.getOwnerDocument().createElement(«XML_TAG_ROW»);
                «FOR column : it.validColumns»
                    «ValueToXmlHelper».«column.addToElement(column.getToStringExpression(column.attributeName) , "rowElement", XML_TAG_VALUE)»;
                «ENDFOR»
                element.appendChild(rowElement);
            }
        «ENDIF»
    '''

}
