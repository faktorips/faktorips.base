#!/bin/bash
#
# Create Faktor IPS license and datenschutzbestimmung pdf document 
#

LICENSE_FILE_NAME=lizenz/lizenzvertrag_fips
LICENSE_FILE_NAME_PDF=lizenz/FaktorIPS_Lizenzvertrag.pdf
DATENSCHUTZ_FILE_NAME=datenschutzbestimmung/datenschutzbestimmung
DATENSCHUTZ_FILE_NAME_PDF=datenschutzbestimmung/FaktorIPS_Datenschutzbestimmung.pdf

SCRIPT_DIR=$(cd $(dirname $0); pwd)
LICENSE_VERSION_TXT=$SCRIPT_DIR/version.txt

OUTPUT_DIRECTORY=$SCRIPT_DIR/result
PDFLATEX_OUT=$SCRIPT_DIR/pdflatex.log.tmp
ERROR_LOG=$SCRIPT_DIR/error.log

#
# Methods
#

printUsage()
{
  echo "usage: $0 <version number>"
}

generatePdf()
{
  # $1 filename
  # $2 options (IGNORE_WARNINGS)
  
  local FILENAME=$1
  local OPTIONS=$2
  echo "generate $FILENAME ..."
  
  executePdflatex 1 ${FILENAME}
  
  checkForWarnings
  if [ $? -eq 1 ] ; then
    echo "maybe there are warnings because of missing aux, run again"
    executePdflatex 2 ${FILENAME}
  fi
  
  checkForWarnings
  RC=$?
  if [ $RC -eq 1 ] ; then
    echo "Warning: there are warnings in pdflatex log, see $PDFLATEX_OUT"
    if [ ! "$OPTIONS" = "IGNORE_WARNINGS" ] ; then
      exit 1
    fi
  fi
  
  rm $ERROR_LOG
  rm $PDFLATEX_OUT
  
  echo "successfully created ${FILENAME}.pdf"
}

generateTxt(){
 # $1 filename
 local FILENAME=$1 
 local TMP_TXT=$OUTPUT_DIRECTORY/${FILENAME}.txt.tmp
 pdftotext -enc UTF-8 -layout -nopgbrk $OUTPUT_DIRECTORY/${FILENAME}.pdf $TMP_TXT
 awk -f $SCRIPT_DIR/filter.awk $TMP_TXT > $OUTPUT_DIRECTORY/${FILENAME}.txt
 rm $TMP_TXT
}

executePdflatex()
{
  # $1 number of run 
  # $2 filename
  local FILENAME=$2
  
  echo "execute pdflatex $1" 
  test -d $OUTPUT_DIRECTORY || mkdir $OUTPUT_DIRECTORY 
  cd $SCRIPT_DIR
  pdflatex -interaction=nonstopmode -output-directory $OUTPUT_DIRECTORY $SCRIPT_DIR/${FILENAME}.tex > $PDFLATEX_OUT 2> $ERROR_LOG
  RC=$?
  cd -
  if [ $RC -eq 1 -o $(cat $ERROR_LOG | wc -l) -gt 0 ] ; then  
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

assertSameTxt()
{
  # $1 filename
  # $2 text type (e.g. license)
  local FILENAME=$1
  local TEXT_TYPE=$2
  diff -q $OUTPUT_DIRECTORY/${FILENAME}.txt ${SCRIPT_DIR}/${FILENAME}.txt 
  if [ ! $? -eq 0 ] ; then
    echo "Warning the $TEXT_TYPE text differs from the previous text in the feature.xml files!"
    echo "Please commit the new $TEXT_TYPE text '$OUTPUT_DIRECTORY/${FILENAME}.txt' into the lizenz folder and upate all feature.xml files"
    echo "(to update all feature.xml's check-in the new text and call the script: org.faktorips.build/updateAllFeatureXml.groovy)"
  fi
}

process()
{
  # $1 filename (without suffix)
  local FILENAME=$1

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

generatePdf $LICENSE_FILE_NAME
generateTxt $LICENSE_FILE_NAME
assertSameTxt $LICENSE_FILE_NAME
cp $OUTPUT_DIRECTORY/$LICENSE_FILE_NAME.pdf $OUTPUT_DIRECTORY/$LICENSE_FILE_NAME_PDF

generatePdf $DATENSCHUTZ_FILE_NAME IGNORE_WARNINGS
generateTxt $DATENSCHUTZ_FILE_NAME 
assertSameTxt $DATENSCHUTZ_FILE_NAME 
cp $OUTPUT_DIRECTORY/$DATENSCHUTZ_FILE_NAME.pdf $OUTPUT_DIRECTORY/$DATENSCHUTZ_FILE_NAME_PDF

# cleanup
rm $LICENSE_VERSION_TXT
