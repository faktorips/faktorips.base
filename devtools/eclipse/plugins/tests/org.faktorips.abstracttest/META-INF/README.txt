The dependencies of JUnit, Mockito and Hamcrest are a bit tricky.
While JUnit has a require-bundle dependency to org.hamcrest.core,
the mockito-all bundle includes its own org.hamcrest packages, and even
exports them. This, together with the package-use and
package-splitting configurations in these bundles, leads to an linkage error when JUnit (and hence
org.hamcrest.core) is loaded AFTER mockito-all. To avoid these
linkage errors, org.hamcrest.core must be loaded BEFORE
org.mockito.mockito-all. The manifest of org.faktroips.abstracctest defines this and reexports the
hamcrest and mockito dependency, so all dependent test projects can use it.

The junit dependency is required as
Import-Package dependency because the bundle org.junit was renamed in
eclipse 4 (it was called org.junit4 in eclipse 3).