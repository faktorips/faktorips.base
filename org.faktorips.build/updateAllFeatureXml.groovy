/**
 * Updates all feature.xml files.
 *   replace all copyright and license nodes with the concatenated 
 *   FaktorIps "lizenzvertrag" and "datenschutzbestimmung" text
 *   both text files must be extracted from the corresponding Pdf files before
 *
 * Note: make sure that 
 *   a) all feature projects are checkout and updated in the current workspace .. and 
 *   b) the two text files are up to date, see #lizenzvertragTextLocation and #datenschutzbestimmungTextLocation
 */
import groovy.xml.StreamingMarkupBuilder
import groovy.util.IndentPrinter
 
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

private static final String lizenzLocation = "../org.faktorips.pluginbuilder/lizenz/"

private static final String lizenzvertragTextLocation = lizenzLocation + "lizenzvertrag_fips.txt"
private static final String datenschutzbestimmungTextLocation = lizenzLocation + "datenschutzbestimmung.txt"

def String readText(File file){
  String text = ""
  file.eachLine{
    text += it + "\n"
  }
  return text
}

def String makeBlockHeader(String text){
  String hl = ""
  String header = text + "\n"
  for (int i=0;i<text.length();i++){
    hl += "-"
  }
  hl += "\n" 
  return hl + header + hl
}

def replace(String xmlFile, String name1, String textFile1, String name2, String textFile2){
  println "process " + xmlFile
  
  file1 = new File(textFile1)
  file2 = new File(textFile2)
  
  String header  = "A - " + name1 + "\n"
         header += "B - " + name2 + "\n" + "\n"
  
  String header1 = makeBlockHeader("A) " + name1 + ":")
  String header2 = makeBlockHeader("B) " + name2 + ":")
  
  String text1 = readText(file1)
  String text2 = readText(file2)

  def root = new XmlSlurper().parse(xmlFile)
  license = root.license
  if (license == null){
    throw new RuntimeException("Error node license to update not found in xml!")
  }
  copyright = root.copyright
  if (copyright == null){
    throw new RuntimeException("Error node copyright to update not found in xml!")
  }

  oldUrl = copyright.@url
  copyright.replaceNode{ node -> 
  		copyright(header + 
  		header1 +
  		text1 + "\n" + "\n" +
  		header2 +
  		text2 + "\n", 
  		url:oldUrl)
  }
  oldUrl = license.@url
  license.replaceNode{ node -> 
  		license(header + 
  		header1 +
  		text1 + "\n" + "\n" +
  		header2 +
  		text2 + "\n", 
  		url:oldUrl)
  }
    		  
  writeNewFile(xmlFile, root)
}

def writeNewFile(xmlFile, root){  
    String newFileName = xmlFile

    def outputBuilder = new StreamingMarkupBuilder()
    String result = outputBuilder.bind{ mkp.yield root }
    String resultIndent = indentXml(result);
    
    File outFile = new File(newFileName)
    if (outFile.exists()){
        outFile.delete()
        outFile = new File(newFileName)
    }
    outFile.append(resultIndent)
}


def String indentXml(xml) {
    def factory = TransformerFactory.newInstance()
    factory.setAttribute("indent-number", 4);
    
    Transformer transformer = factory.newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
    transformer.setOutputProperty(OutputKeys.ENCODING, , 'UTF-8')
    StreamResult result = new StreamResult(new StringWriter())
    transformer.transform(new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))), result)
    return result.writer.toString()
}

def handleDir(File dir, String filename1, String filename2){
	dir.eachFile { handleFile(it, filename1, filename2) }
	dir.eachDir {
	   it.eachDir { handleDir(it, filename1, filename2) } 
	   it.eachFile { handleFile(it, filename1, filename2)}
	}
}

def handleFile(File file, String filename1, String filename2){
  if (isFaktorIpsFeatureXmlFile(file)){
    replace(file.getAbsolutePath(), "Lizenzvertrag", filename1, "Datenschutzbestimmung", filename2)
  }
}

// return true if feature.xml and path contains no de.qv 
def isFaktorIpsFeatureXmlFile(file){
  if (file.getAbsolutePath() ==~ /.*de\.qv\..*/) return false;
  return file.getName() ==~ /^feature\.xml$/
}

/***************************************
 * MAIN
 ***************************************/
File dir = new File("..");
handleDir(dir, lizenzvertragTextLocation, datenschutzbestimmungTextLocation)
