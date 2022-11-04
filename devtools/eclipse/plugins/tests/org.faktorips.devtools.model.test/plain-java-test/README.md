# Plain-Java Tests of org.faktorips.devtools.model

This project is used to run the tests of `org.faktorips.devtools.model.test` as plain java JUnit tests instead of as plugin tests.

## Usage in Eclipse

 1. import the project as a maven project

    * the `.project` and `.classpath` file should link the `src` folder automatically to the org.faktorips.devtools.model `src` folder

 2. run /base/.launch/plain-java-tests/faktorips-model-test.launch
    (it contains `org.faktorips.devtools.model` as an additional folder in its dependencies, so that the `PlainJavaRegistryProvider` can find its `plugin.xml` in the workspace

## Configuration

If there are problems with the configuration:

 1. import the project as a maven project
 2. edit the classpath:

    * right-click on the project -> Build Path -> Configure Build Path...
    * Link Source...
    * right next to "Linked folder location" click on Variables -> New
        * Name: FIPS_MODEL_TEST_LOC
        * Location: ${PROJECT_LOC}/../src
    * Folder name: src
    * click "Finish"
    * activate "Allow output folders for source folders"
    * add "Output folder":
        * toggle "Specific output folder" and set to: target/test-classes
    * toggle "Contains test sources" to yes
    * click "Apply and Close"

