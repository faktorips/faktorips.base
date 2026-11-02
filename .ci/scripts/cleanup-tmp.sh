#!/bin/bash
# Removes temp files and directories that the tests leave behind in /tmp.
# Every pattern requires digits after the prefix, so only the random suffixes created by the tests match.
find /tmp -mindepth 1 -maxdepth 1 -user "$(id -un)" \( \
    -name 'xmltest[0-9]*.xml' -o -name 'testJson[0-9]*.json' -o -name 'MANIFEST[0-9]*MF' \
    -o -name '[ab][0-9]*.log' \
    -o -name 'Workspace[0-9]*' -o -name 'OffRoot[0-9]*' -o -name 'testDir[0-9]*' \
    -o -name 'PlainJavaImplementationTest[0-9]*' \
    -o -name 'baz[0-9]*' -o -name 'dep[0-9]*' -o -name 'foobar[0-9]*' \
    -o -name '20[0-9][0-9]_[0-9][0-9]_[0-9][0-9]_*' \
\) -exec rm -rf {} +

# show what is left, so that leftovers not covered by the patterns above are visible in the build log
echo "Remaining content of /tmp:"
du -sh /tmp/* 2>/dev/null | sort -h
