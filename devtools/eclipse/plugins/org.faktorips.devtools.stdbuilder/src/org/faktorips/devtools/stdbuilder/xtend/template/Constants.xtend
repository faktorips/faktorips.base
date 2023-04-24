package org.faktorips.devtools.stdbuilder.xtend.template

import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode
import org.faktorips.runtime.IModelObject
import org.faktorips.runtime.internal.ValueToXmlHelper

class Constants {

    def static XML_TAG_VALUE(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_VALUE"
    }

    def static XML_TAG_DATA(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_DATA"
    }

    def static XML_TAG_VALUE_SET(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_VALUE_SET"
    }

    def static XML_TAG_ATTRIBUTE_VALUE(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_ATTRIBUTE_VALUE"
    }

    def static XML_TAG_CONFIGURED_DEFAULT(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_CONFIGURED_DEFAULT"
    }

    def static XML_TAG_CONFIGURED_VALUE_SET(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_CONFIGURED_VALUE_SET"
    }

    def static XML_TAG_ALL_VALUES(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_ALL_VALUES"
    }

    def static XML_TAG_ENUM(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_ENUM"
    }

    def static XML_TAG_RANGE(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_RANGE"
    }

    def static XML_TAG_STEP(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_STEP"
    }

    def static XML_TAG_LOWER_BOUND(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_LOWER_BOUND"
    }

    def static XML_TAG_UPPER_BOUND(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_UPPER_BOUND"
    }

    def static XML_TAG_TABLE_CONTENT_NAME(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_TABLE_CONTENT_NAME"
    }

    def static XML_TAG_ROW(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_ROW"
    }

    def static XML_TAG_ROWS(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_ROWS"
    }

    def static XML_TAG_COLUMN_TABLE_REFERENCE(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_TAG_COLUMN_TABLE_REFERENCE"
    }

    def static XML_ATTRIBUTE_ATTRIBUTE(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_ATTRIBUTE_ATTRIBUTE"
    }

    def static XML_ATTRIBUTE_CONTAINS_NULL(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_ATTRIBUTE_CONTAINS_NULL"
    }

    def static XML_ATTRIBUTE_EMPTY(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".XML_ATTRIBUTE_EMPTY"
    }

    def static CONFIGURED_VALUE_SET_PREFIX(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".CONFIGURED_VALUE_SET_PREFIX"
    }

    def static CONFIGURED_DEFAULT_PREFIX(AbstractGeneratorModelNode it) {
        addImport(ValueToXmlHelper.name) + ".CONFIGURED_DEFAULT_PREFIX"
    }

    def static CONTINUE_VALIDATION(AbstractGeneratorModelNode it) {
        addStaticImport(IModelObject.name, "CONTINUE_VALIDATION");
    }

    def static STOP_VALIDATION(AbstractGeneratorModelNode it) {
        addStaticImport(IModelObject.name, "STOP_VALIDATION");
    }
}
