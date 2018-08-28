package org.faktorips.devtools.stdbuilder.xpand.table.template

import org.faktorips.devtools.stdbuilder.xpand.table.model.XColumnRangeSearchStructure
import org.faktorips.devtools.stdbuilder.xpand.table.model.XIndex
import org.faktorips.devtools.stdbuilder.xpand.table.model.XTable

import static org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType.*
import static org.faktorips.devtools.stdbuilder.xpand.template.MethodNamesTmpl.*

import static extension org.faktorips.devtools.stdbuilder.xpand.table.template.HashKeyClassTmpl.*
import static extension org.faktorips.devtools.stdbuilder.xpand.template.ClassNamesTmpl.*

class TableTmpl {

    def static String body(XTable it) '''

        /**
        * «localizedJDoc("CLASS_DESCRIPTION")» «descriptionForJDoc»
        * «getAnnotations(ELEMENT_JAVA_DOC)»
        * @generated
        */
        «getAnnotations(TABLE_CLASS)»
        public class «name» extends «Table_(tableRowName)»{

            «FOR it : indexModelNodes» «fields» «ENDFOR»


            «constructors»
            «addRow»

            «initKeyMaps»

            «IF singleContentTable »
                «getInstanceForSingleContent»
            «ENDIF»

            «getInstanceForMultipleContents»

            «FOR nodes : indexModelNodes» «findMethods(nodes) »«ENDFOR»

            «FOR it : columnKeyIndexModelNodes» «body» «ENDFOR»
        }
    '''

    def private static  fields(XIndex it) '''
        /**
        * «localizedJDoc("FIELD_KEY_MAP")»
        * «getAnnotations(ELEMENT_JAVA_DOC)»
        * @generated
        */
        private «structureClass» «keySearchStructureName»;
    '''

    def private static  constructors(XTable it) '''
        /**
        * «localizedJDoc("CONSTRUCTOR_DEFAULT")»
        *
        * @generated
        */
        public «method(name)»{
            super();
        }

        /**
        * «localizedJDoc("CONSTRUCTOR_WITH_ROWS")»
        *
        * @generated
        */
        public «method(name, List_(tableRowName), " content")»{
            super();
            rows = new «ArrayList(tableRowName)»(content);
            «init()»;
        }
    '''

    def private static  addRow(XTable it) '''
        /**
        * «localizedJDoc("METHOD_ADD_ROW")»
        *
        * @generated
        */
        @Override
        protected void addRow(«List_("String")» values, «IRuntimeRepository()» productRepository) {
            «IF validColumns.size > 0»
                    String «FOR it : validColumns.indexed» columnValue = values.get(«key»);
                    «value.datatypeName» «value.attributeName» = «value.getNewInstanceFromExpression("columnValue", "productRepository")»;
                «ENDFOR»
            «ENDIF»
            rows.add(new «tableRowName» («columnNames»));
        }
    '''

    def private static  initKeyMaps(XTable it) '''
        /**
        * «localizedJDoc("METHOD_INIT_KEY_MAPS")»
        *
        * @generated
        */
        @Override
        protected final void «method("initKeyMaps")»{
            «IF hasIndices»
                «FOR node : indexModelNodes» «keyMapInitialization(node)» «ENDFOR»
                for(«tableRowName» row : rows){
                        «FOR it : indexModelNodes » «forLoop» «ENDFOR»
                }
            «ENDIF»
        }
    '''

    def private static  keyMapInitialization(XTable table, XIndex it) '''
        «keySearchStructureName» = «structureClass.genericClass».«structureClass.paramsWithBracket()» create(«rangeStructureParameter»);
    '''

    def private static forLoop(XIndex it) '''
        «IF hasRangeKeysNotFirst() »
            «FOR keyItem : keyItemsForInitKeyMap» «createWith(keyItem)» «ENDFOR»
        «ENDIF»
        «keySearchStructureName».put(
            «IF firstKeyRange»
                «FOR it : columnsForFirstKey»
                    row.«methodNameGetter»(),
                «ENDFOR»
            «ELSE»
                new «classOrTypeName»(
                    «FOR column : columnKeys.indexed»
                        row.«column.value.methodNameGetter»()«IF column.key < columnKeys.size-1», «ENDIF»
                    «ENDFOR»
                ),
            «ENDIF»
            «IF hasRangeKeysNotFirst()»
                «keyItemsForInitKeyMap.get(keyItemsForInitKeyMap.size - 1).searchStrucutreName»
            «ELSE»
                «resultStructureClassName».createWith(row)
            «ENDIF»
        );
    '''

    def private static  createWith(XIndex index, XColumnRangeSearchStructure it) '''
            «genericType» «searchStrucutreName» = «genericTypeClass».«genericTypeParams» createWith(
            «IF (rangeStructureParameter.length > 0) »
                «rangeStructureParameter»,
            «ENDIF»
            «FOR it : columns»
                row.«methodNameGetter»(),
            «ENDFOR»
            «IF prevSearchStructureName == ""»
                «index.resultStructureClassName».createWith(row)
            «ELSE»
                «prevSearchStructureName»
            «ENDIF»
        );
    '''

    def private static  getInstanceForSingleContent(XTable it) '''
        /**
        * «localizedJDoc("METHOD_GET_INSTANCE")»
        *
        * @generated
        */
        public static final «name» «method("getInstance", IRuntimeRepository(), "repository")»{
            return repository.«getTable(name+".class")»;
        }
    '''

    def private static  getInstanceForMultipleContents(XTable it) '''
        /**
        * «localizedJDoc("METHOD_GET_INSTANCE")»
        *
        * @generated
        */
        public static final «name» «method("getInstance", IRuntimeRepository(), "repository", "String", "qualifiedTableName")»{
            return («name») repository.«getTable("qualifiedTableName")»;
        }
    '''

    def private static  findMethods(XTable table, XIndex it) '''
        «findRow(table, it)»
        «IF uniqueKey»
            «findRowNullRowReturnedForEmtpyResult(table, it)»
            «findExistingRow(table, it)»
        «ENDIF»
    '''

    def private static  findRow(XTable table, XIndex it) '''
        /**
        * «IF uniqueKey» «localizedJDoc("METHOD_FIND_ROW")» «ELSE»«localizedJDoc("METHOD_FIND_ROWS")» «ENDIF»
        * «getAnnotations(ELEMENT_JAVA_DOC)»
        * @generated
        */
        public «table.getMethodReturnTypeFindRow(it)» «method(table.getMethodNameFindRow(it), methodParametersFindRow)»{
            «findRowMethodPart»«IF uniqueKey».getUnique(null);
            «ELSE».get();
            «ENDIF»
        }
    '''

    def private static  findRowNullRowReturnedForEmtpyResult(XTable table, XIndex it) '''
        /**
        * «localizedJDoc("METHOD_FIND_RETURN_NULL_ROW")»
        * «getAnnotations(ELEMENT_JAVA_DOC)»
        * @generated
        */
        public «table.tableRowName» «method(table.getMethodNameFindRowNullRowReturned(it), methodParametersFindRow)»{
            «findRowMethodPart».getUnique(«table.tableRowName».NULL_ROW);
        }
    '''

    def private static  findExistingRow(XTable table, XIndex it) '''
        /**
        * «localizedJDoc("METHOD_FIND_EXISTING_ROW")»
        *
        * @return «localizedJDoc("METHOD_FIND_EXISTING_ROW_RETURN")»
        * @throws «localizedJDoc("METHOD_FIND_EXISTING_ROW_THROW")»
        * «getAnnotations(ELEMENT_JAVA_DOC)»
        * @generated
        */
        public «table.tableRowName» «method(table.getMethodNameFindExistingRow(it), methodParametersFindRow)»{
            try{
                «findRowMethodPart».getUnique();
            }catch («NoSuchElementException()» e) {
                throw new «IllegalArgumentException()»(«findExistingRowExceptionMessage»);
            }
        }
    '''

    def private static  findRowMethodPart(XIndex it) '''
        return «keySearchStructureName»
        «IF hasColumnKeys()».get(new «classOrTypeName» («columnKeyNames»))
        «ENDIF»
        «FOR rangeName : rangeKeyNames» .get(«rangeName»)
        «ENDFOR»
    '''

}
