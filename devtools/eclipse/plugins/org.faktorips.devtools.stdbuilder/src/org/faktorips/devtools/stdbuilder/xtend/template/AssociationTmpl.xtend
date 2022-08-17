package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType
import org.faktorips.devtools.stdbuilder.xmodel.XAssociation

import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class AssociationTmpl {

    def static getNumOf(XAssociation it) '''
        «IF oneToMany && !derived && !constrain»
           /**
            *«inheritDocOrJavaDocIf(genInterface(), "METHOD_GET_NUM_OF", getName(true))»
           «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
            *
            * @generated
            */
           «getAnnotations(AnnotatedJavaElementType.DEPRECATION)»
           «overrideAnnotationForPublishedMethodImplementation()»
           public int «method(methodNameGetNumOf)»
              «IF genInterface()»;
              «ELSE»
                  {
                      return «fieldName».size();
                  }
              «ENDIF»
           «ENDIF»
    '''

    def static abstractMethods(XAssociation it) '''
        «IF isGenerateAbstractGetter(genInterface())»
            «IF oneToMany»
                /**
                *«localizedJDoc("METHOD_GET_MANY", getName(true), descriptionForJDoc)»
               «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
                *
                * @generated
                */
                «getAnnotationsForPublishedInterface(annotatedJavaElementTypeForGetter, genInterface())»
                «overrideAnnotationForPublishedMethodImplementation()»
                public «IF sourceModelNode.abstract && !genInterface()»abstract«ENDIF» «List_("? extends " + targetInterfaceName)» «method(methodNameGetter)»
                «IF sourceModelNode.abstract || genInterface()»;«ELSE» {
«««                 non-abstract classes may not contain an abstract method, so a dummy body has to be generated
                        return «Collections()».emptyList();
                    }
                «ENDIF»

                /**
                *«localizedJDoc("METHOD_GET_NUM_OF", getName(true))»
               «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
                *
                * @generated
                */
                «overrideAnnotationForPublishedMethodImplementation()»
                public «IF sourceModelNode.abstract && !genInterface()»abstract«ENDIF» int «method(methodNameGetNumOf)»
                «IF sourceModelNode.abstract || genInterface()»;«ELSE» {
«««                 non-abstract classes may not contain an abstract method, so a dummy body has to be generated
                        return 0;
                    }
                «ENDIF»
            «ELSE»
                /**
                *«localizedJDoc("METHOD_GET_ONE", name, descriptionForJDoc)»
               «getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC)»
                *
                * @generated
                */
                «getAnnotationsForPublishedInterface(annotatedJavaElementTypeForGetter, genInterface())»
                «overrideAnnotationForPublishedMethodImplementation()»
                public «IF !genInterface()»abstract«ENDIF» «targetInterfaceName» «method(methodNameGetter)»;
            «ENDIF»
        «ENDIF»
    '''

}
