package org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder.template

class ProductBuilderNamesTmpl {

    // ---------------------------------
    // Methods in IRuntimeRepository
    // ---------------------------------
    def package static putProductCmptGeneration(String generationName) {
        "putProductCmptGeneration(" + generationName + ")"
    }

    def package static getProductCmptGeneration(String id, String effectiveDate) {
        "getProductComponentGeneration(" + id + ", " + effectiveDate + ")"
    }

    def package static getLatestProductComponentGeneration(String product) {
        "getLatestProductComponentGeneration(" + product + ")"
    }

    def package static getExistingProductComponent(String id) {
        "getExistingProductComponent(" + id + ")"
    }

    // ---------------------------------
    // Methods in ProductComponent / Generation
    // ---------------------------------
    def package static setValidFrom(String arg) {
        "setValidFrom (" + arg + ")"
    }
}
