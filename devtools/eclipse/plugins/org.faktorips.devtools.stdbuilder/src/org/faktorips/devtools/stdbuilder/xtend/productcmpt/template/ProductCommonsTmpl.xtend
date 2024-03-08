package org.faktorips.devtools.stdbuilder.xtend.productcmpt.template

import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductClass

import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.DefaultAndAllowedValuesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAssociationTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductAttributeTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.TableUsagesTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*
import static org.faktorips.devtools.stdbuilder.xtend.template.MethodNames.*
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass

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
             *«inheritDoc»
             *
             * @generated
             */
            @Override
            public void «writePropertiesToXml(Element + " element")» {
                «IF hasSupertype»
                    super.«writePropertiesToXml("element")»;
                «ELSEIF attributes.size == 0 && configuredAttributes.size == 0»
                    // no attributes to write
                «ENDIF» 
                «IF it instanceof XProductCmptClass»
                element.setAttribute("productCmptType", "«name»");
                element.setAttribute("runtimeId", «id»);
                «ENDIF»
                «FOR it : inAlphabeticalOrder(attributes)» «writeAttributeToXmlMethodCall» «ENDFOR»
                «FOR it : inAlphabeticalOrder(configuredAttributes)» «writeAttributeToXmlMethodCall» «ENDFOR»
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