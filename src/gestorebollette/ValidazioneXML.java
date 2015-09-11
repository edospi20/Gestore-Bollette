
package gestorebollette;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.w3c.dom.Document;
import org.xml.sax.*;

public class ValidazioneXML {   //(00)
    public static boolean valida(String x){
        try{
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();     //(01)
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);   //(02)
            Document d = db.parse(new InputSource(new ByteArrayInputStream(x.getBytes("utf-8"))));  //(03)
            Schema s = sf.newSchema(new StreamSource(new File("gestorebollette.xsd")));     //(04)
            s.newValidator().validate(new DOMSource(d));
            return true;
        }catch (ParserConfigurationException e){
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();          
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

/*
(01):
DocumentBuilder definisce l' API per ottenere un'instanza del documento DOM dal documento XML
https://docs.oracle.com/javase/8/docs/api/javax/xml/parsers/DocumentBuilder.html
https://docs.oracle.com/javase/8/docs/api/javax/xml/parsers/DocumentBuilderFactory.html

(02):
SchemaFactory è un compilatore di schema. Legge rappresentazioni esterne di schema e lo
prepara per la validazione.
https://docs.oracle.com/javase/8/docs/api/javax/xml/validation/SchemaFactory.html

(03):
Document è l'oggetto documento DOM vero e proprio.
https://docs.oracle.com/javase/8/docs/api/org/w3c/dom/Document.html

(04):
Schema è l'oggetto schema caricato d file XML.
https://docs.oracle.com/javase/8/docs/api/javax/xml/validation/Schema.html

(05):
Validator è l'oggetto che valida il documento xml sullo schema caricato.
In caso di errore di validazione viene generata una SAXException.

*/