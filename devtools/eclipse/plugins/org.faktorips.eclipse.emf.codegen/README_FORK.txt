This plug-In is a fork of the eclipse emf plug-In org.eclipse.emf.codegen version 2.5. 
It also includes the interesting changes of Version 2.10 (which weren't many at all).

The code is cleaned up, all deprecated parts were removed. The new version is only able to merge java code with at least JLS 3 (java 5).

As a new feature it is possible to configure custom annotations that will be generated for every method and every field.
This allows marking all generated artifacts with an @SuppressWarnings annotation.

For further information and discussion read FIPS-2650 (https://jira.faktorzehn.de/browse/FIPS-2650)


The license of this plug-In is EPL 1.0! 