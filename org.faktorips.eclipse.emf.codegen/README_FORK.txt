This plug-In is a fork of the eclipse emf plug-In org.eclipse.emf.codegen in version 2.5
including the interesting changes of Version 2.10 (weren't really much changes at all).

The code is cleaned up, all deprecated parts were removed. The new version is only able to merge java code with at least JLS 3 (java 5).

As a new feature it is possible to configure some annotations that would be generated for every method and every fields.
This is used for example to mark every generated artifact with @SuppressWarnings.

For further information and discussion read FIPS-2650 (https://jira.faktorzehn.de/browse/FIPS-2650)


The license of this plug-In is EPL 1.0! 