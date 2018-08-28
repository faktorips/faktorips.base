package org.faktorips.devtools.stdbuilder.xpand.productcmpt.template

import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductClass


import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.DefaultAndAllowedValuesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.ProductAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.productcmpt.template.TableUsagesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.template.ClassNamesTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.CommonGeneratorExtensionsTmpl.*
import static org.faktorips.devtools.stdbuilder.xpand.template.MethodNamesTmpl.*

class ProductCommonsTmpl {


    def package static checkRepositoryModifyable (AbstractGeneratorModelNode it) '''
        if («getRepository» != null && !«getRepository».«isModifiable») {
            throw new «IllegalRepositoryModificationException»();
        }
    '''

    // Even though config elements and tables cannot be added to product components (as of now),
    // the providing methods are implemented in XProductClass and always return empty lists in that case.
    // Thus the below code is also valid for product component code.
    // Moreover, when config elements and tables can finally be made "static", the below template will work
    // without changes and generate the correct product component code.

    def package static writeToXmlMethods (XProductClass it) '''
        «IF generateToXmlSupport»
            /**
             * «inheritDoc»
             *
             * @generated
             */
            @Override
            protected void «writePropertiesToXml(Element + " element")» {
                «IF hasSupertype»
                    super.«writePropertiesToXml("element")»;
                «ELSEIF attributes.size == 0 && configuredAttributes.size == 0»
                    // no attributes to write
                «ENDIF»
                «FOR it : attributes» «writeAttributeToXmlMethodCall» «ENDFOR»

                «FOR it : configuredAttributes» «writeAttributeToXmlMethodCall» «ENDFOR»
            }
                «FOR it : attributes» «writeAttributeToXmlMethod» «ENDFOR»

                «FOR it : configuredAttributes» «writeAttributeToXmlMethod» «ENDFOR»


            «IF  containsNotDerivedOrConstrainingAssociations»
                /**
                 * @generated
                 */
                @Override
                protected void «writeReferencesToXml(Element + " element")» {
                    super.«writeReferencesToXml("element")»;
                    «FOR it : associations» «writeReferencesToXmlMethodCall» «ENDFOR»
                }
            «ENDIF»

            «FOR it : associations» «writeReferencesToXmlMethod» «ENDFOR»

            «IF containsTables»
                /**
                 * @generated
                 */
                @Override
                protected void «writeTableUsagesToXml(Element + " element")» {
                    super.«writeTableUsagesToXml("element")»;
                    «FOR it : tables» «writeTableUsages» «ENDFOR»
                }
            «ENDIF»
        «ENDIF»
    '''
}