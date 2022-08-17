package org.faktorips.devtools.stdbuilder.xtend.table.template

import org.faktorips.devtools.stdbuilder.xmodel.table.XIndex

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*

class HashKeyClassTmpl {

    def package static body(XIndex it) '''
        «val hashKeyClassName = classOrTypeName»
        /**
        *«localizedJDoc("CLASS_DESCRIPTION")»
        *
        * @generated
        */
        private static final class «hashKeyClassName»{

            «fields»
            «constructor»
            «calHashCode»
            «equals(it)»
            «hashCode(it)»
        }
    '''

    def private static fields(XIndex it) '''
        «FOR it : columnKeys»

                /**
                * @generated
                */
                private final «datatypeName» «field(attributeName)»;
        «ENDFOR»

        /**
        *«localizedJDoc("FIELD_HASHCODE")»
        *
        * @generated
        */
        private final int «field("hashCode")»;
    '''

    def private static constructor(XIndex it) '''
        «val hashKeyClassName = classOrTypeName»
            /**
            *«localizedJDoc("CONSTRUCTOR")»
            *
            * @generated
            */
            private «method(hashKeyClassName, constructorParameters)»{
                «FOR it : constructorParameters»
                    this.«name» = «name»;
                «ENDFOR»
                hashCode = calculateHashCode();
            }
    '''

    def private static calHashCode(XIndex it) '''
        /**
        * @generated
        */
        private int «method("calculateHashCode")»{
            int result = 17;
            «FOR it : columnKeys»
                result = 37 * result + ((«attributeName» == null) ? 0 : «attributeName».hashCode());
            «ENDFOR»
            return result;
        }
    '''

    def private static equals(XIndex it) '''
        «val hashKeyClassName = classOrTypeName»
            /**
            *«localizedJDoc("METHOD_EQUALS")»
               *
               * @generated
               */
               @Override
               public boolean «method("equals", "Object", "o")»{
                   if (o instanceof «hashKeyClassName»){
                       «hashKeyClassName» other = («hashKeyClassName») o;
                       return
                       «FOR key : columnKeys.indexed»
                           «Objects».equals(«key.value.attributeName», other.«key.value.attributeName») «IF key.key < columnKeys.size -1»&&«ENDIF»
                       «ENDFOR»;
                   }
                   return false;
               }
    '''

    def package static hashCode(XIndex it) '''
        /**
        *«localizedJDoc("METHOD_HASHCODE")»
        *
        * @generated
        */
        @Override
        public int «method("hashCode")»{
            return hashCode;
        }
    '''

}
