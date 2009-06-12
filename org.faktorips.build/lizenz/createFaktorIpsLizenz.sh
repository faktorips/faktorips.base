#!/bin/bash
#
# Create Faktor IPS license pdf document 
#

LICENSE_FILE_NAME=lizenzvertrag_fips

LICENSE_VERSION_TXT=version.txt
LICENSE_TEX_FILE=${LICENSE_FILE_NAME}.tex
LICENSE_PDF_FILE=${LICENSE_FILE_NAME}.pdf
LICENSE_AUX_FILE=${LICENSE_FILE_NAME}.aux

PDFLATEX_OUT=lizenzvertrag_fips.log.tmp
ERROR_LOG=error.log

#
# Methods
#

printUsage()
{
  echo "usage: $0 <version number>"
}

generateLizenzPdf()
{
  echo "generate $LICENSE_PDF_FILE ..."
  
  executePdflatex 1
  
  checkForWarnings
  if [ $? -eq 1 ] ; then
    echo "maybe there are warnings because of missing aux, run again"
    executePdflatex 2
  fi
  
  checkForWarnings
  if [ $? -eq 1 ] ; then
    echo "Error there are warnings in pdflatex log, see $PDFLATEX_OUT"
    exit 1
  fi
  
  rm $ERROR_LOG
  rm $PDFLATEX_OUT
  
  echo "successfully created $LICENSE_PDF_FILE"
}

executePdflatex()
{
  echo "execute pdflatex $1" 
  pdflatex $LICENSE_TEX_FILE > $PDFLATEX_OUT 2> $ERROR_LOG
  if [ $? -eq 1 -o $(cat $ERROR_LOG | wc -l) -gt 0 ] ; then  
    echo "Error in pdflatex, see $ERROR_LOG or $PDFLATEX_OUT"
    exit 1
  fi  
}

checkForWarnings()
{
 # check log file for warnings
 if [ $(cat $PDFLATEX_OUT | grep -i "warning" | wc -l) -gt 0 ] ; then
   return 1
 fi
 return 0
}

assertLatexEnviornment()
{
  # assert valid latex environment
  which pdflatex > /dev/null 2>&1
  if [ $? -eq 1 ] ; then  
    echo "Error pdflatex not found, please install latex"
    exit 1
  fi
}

#
# main
#

assertLatexEnviornment

# check input
if [ ! $# -eq 1 ] ; then
  echo "error no version given!"
  printUsage
  exit 1
fi

echo $1 > $LICENSE_VERSION_TXT

generateLizenzPdf

# cleanup
rm $LICENSE_VERSION_TXT
