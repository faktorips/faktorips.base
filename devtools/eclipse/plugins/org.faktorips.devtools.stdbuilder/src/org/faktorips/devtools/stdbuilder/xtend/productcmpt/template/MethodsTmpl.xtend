package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.model.builder.xmodel.XMethod

import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.*

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*

class MethodsTmpl {

    def package static formulaMethod (XMethod it) '''
        /** 
         *«inheritDocOrTextIf(genInterface, description)»
        «IF formulaOptional && (!published || genInterface())»
            «IF description.length > 0»
                *<p>
            «ENDIF»
            *«localizedJDoc("METHOD_GETFORMULAR")»
            *@see #«methodNameIsFormulaAvailable»
        «ENDIF»
       «getAnnotations(ELEMENT_JAVA_DOC)»
        *
        * @generated
        */

        «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overrides)»
        «getModifier(genInterface())» «javaClassName» «method(methodName, methodParameters)» throws «FormulaExecutionException»
        «IF !isGenerateMethodBody(genInterface())»;
        «ELSE»
        {
            «IF formulaCompiling.compileToXml »
                «IF formulaOptional»
                    if («methodNameIsFormulaAvailable»()) {
                «ENDIF»
                    return («notPrimitiveJavaClassName»)getFormulaEvaluator().evaluate("«methodName»" «FOR it : methodParameters», «name»«ENDFOR»);
                «IF formulaOptional»
                    }
                «ENDIF»
            «ENDIF»
            «IF formulaOptional»
                throw new «FormulaExecutionException»(this, "«name»" «FOR it : methodParameters», «name»«ENDFOR»);
            «ENDIF»
        }
        «ENDIF»
        «IF overloadsFormula && !genInterface»
            «val overloaded = overloadedFormulaMethod»
                /**
                 *«inheritDoc»
                 *
                 * @generated
                 */
                @Override
                «overloaded.getModifier(genInterface)» «overloaded.javaClassName» «overloaded.methodName»(«FOR it : overloadedFormulaMethod.parameters SEPARATOR  ", "»«javaClassName» «name»«ENDFOR») throws «FormulaExecutionException()» {
                    // TODO a delegation to the method «methodName»(«FOR it : parameters SEPARATOR  ", "»«javaClassName»«ENDFOR») needs to be implemented here
                    // And make sure to disable the regeneration of this method.
                    throw new RuntimeException("Not implemented yet.");
                }

        «ENDIF»

        «IF formulaOptional && !overloadsFormula»
            /**
            «IF published && !genInterface»
             *«inheritDoc»
            «ELSE»
             *«localizedJDoc("METHOD_IS_FORMULAR_AVAILABLE")»
            «ENDIF»
           «getAnnotations(ELEMENT_JAVA_DOC)»
            *
            * @generated
            */
            «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, !genInterface && overrides && published)»
            «getModifier(false)» boolean «method(methodNameIsFormulaAvailable)»
            «IF genInterface»;«ELSE»
            {
                return «isFormulaAvailable("\"" + formularName + "\"")»;
            }
            «ENDIF»
        «ENDIF»
    '''

    def package static method (XMethod it) '''
        /**
        «IF published && !genInterface»
         *«inheritDoc»
        «ELSEIF description.length > 0»
         *«description»
        «ENDIF»
        «getAnnotations(ELEMENT_JAVA_DOC)»
         *
         * @generated
         */
         «overrideAnnotationForPublishedMethodOrIf(!genInterface && published, overrides)»
        «getModifier(genInterface)» «javaClassName» «method(methodName, methodParameters)» «IF abstract || genInterface»
            ;
        «ELSE»
            {
                // TODO implement method!
                «IF !returnVoid»
                    return «defaultReturnValue»;
                «ENDIF»
            }
        «ENDIF»
    '''

}