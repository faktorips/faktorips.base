package org.faktorips.devtools.stdbuilder.xtend.builder.templateimport org.faktorips.devtools.stdbuilder.xmodel.builder.XPBuilderclass CommonBuilderNames {    def static getResult() {        "getResult()"    }    /**
     * This method returns the name of the policy field if this builder doesn't have super type,
     * else returns the name of the getter method from the super type and append the String to cast
     * it to the required policy class.
     * 
     * @return the name of the policy field or super call
     */    def static safeGetResult(XPBuilder<?, ?, ?> it) {        if(hasSupertype)(
            if(generatePublishedInterfaces) "((" + typeImplClassName + ")" + getResult() + ")" else getResult()
        )        else            variableName    }    def static from() { "from" }    def static from(String p1) {        from() + "(" + p1 + ")"    }    def static from(String p1, String runtimeRepository) {        from() + "(" + p1 + ", " + runtimeRepository + ")"    }    def static builder() {        "builder()"    }    def static builder(String p1) {        "builder(" + p1 + ")"    }    def static builder(String p1, String p2) {        "builder(" + p1 + ", " + p2 + ")"    }    def static builder(String p1, String p2, String p3) {        "builder(" + p1 + ", " + p2 + ", " + p3 + ")"    }    def static builder(String p1, String p2, String p3, String p4) {        "builder(" + p1 + ", " + p2 + ", " + p3 + ", " + p4 + ")"    }    def static builder(String p1, String p2, String p3, String p4, String p5) {        "builder(" + p1 + ", " + p2 + ", " + p3 + ", " + p4 + ", " + p5 + ")"    }    def static modify() {        "modify()"    }    def static modify(String p1) {        "modify(" + p1 + ")"    }    def static add() {        "add()"    }        // ---------------------------------
    // Class Names
    // ---------------------------------
    def static addAssociationBuilder() { "AddAssociationBuilder" }    def static associationBuilder() { "AssociationBuilder" }        // ---------------------------------
    // Methods in IProductComponent
    // ---------------------------------
    def static getGenerationBase(String paramName) { "getGenerationBase(" + paramName + ")" }    def static getLatestProductComponentGeneration() { "getLatestProductComponentGeneration()" }        // ---------------------------------
    // Methods in IConfigurableModelObject
    // ---------------------------------
    def static getProductComponentFromId(String argument) { "getProductComponent(" + argument + ")" }        // ---------------------------------
    // Methods in IRuntimeRepository
    // ---------------------------------
    def static getProductComponent(String idParam) { "getProductComponent(" + idParam + ")" }}