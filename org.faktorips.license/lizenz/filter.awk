# Filtern des Lizenzvertrag Textes
###################################################
# als Input dient der Lizenzvertrag Text des PDF's 
# 1. Ignoriere Zeile mit Versionsangabe
# 2. Ignoriere alle Fusszeilen (mehr als drei Leerzeilen und Nummer in einer Zeile)
BEGIN {
i=0
}
{
if (length($1)==0) 
{
  i++
}else{
  if (/.*Version [0-9]\.[0-9]\.[0-9].*/) {  
  } else if (!(i>=3 && /^ *[0-9] *$/ )){
    while (i>0){ print ""; i--}
    print
  } else {
    i=0
    print ""
    print ""
  }
}
}
END {
}
