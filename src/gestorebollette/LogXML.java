
package gestorebollette;

import com.thoughtworks.xstream.XStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogXML {       //(00)
    //(01)
    private static String localAddress;
    private String ipClient;
    private String data;
    private String ora;
    private String evento;
    private static final SimpleDateFormat formattatoreData = new SimpleDateFormat("dd-MM-YYYY");   
    private static final SimpleDateFormat formattatoreOra = new SimpleDateFormat("HH:mm:ss");
    private static XStream xs = new XStream();
    
    static {
        try {
            localAddress = InetAddress.getLocalHost().getHostAddress();     //(02)
        } catch (UnknownHostException e) {
            System.out.println("Errore nel get dell' ipClient: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public LogXML(String evento){
        ipClient = localAddress;
        Date dataCorrente = new Date();
        data = formattatoreData.format(dataCorrente);   
        ora = formattatoreOra.format(dataCorrente);
        this.evento = evento;
    }
    
    public void invia(String ipServer, int porta){      //(03)
        
        try {
            String xml = xs.toXML(this);
            Socket server = new Socket(ipServer, porta);
            ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
            oos.writeObject(xml);
            oos.close();
            server.close();
            System.out.println("Invia stato: ");
            System.out.println(xml);
        } catch (IOException e) {
            System.out.println("Errore nella connessione al server: " + e.getMessage());
        }
        
    }
}


/*
(00):
La classe LogXML serve per inviare al server stringhe xml che raprresentano 
righe di log.

(01):
Alcuni membri sono statici poiché vengono usati gli stessi finché l'app rimane 
in esecuzione.

(02):
InetAddress con i suoi metodi consente di catturare l'Ip locale del client.
https://docs.oracle.com/javase/8/docs/api/java/net/InetAddress.html

(03):
Il metodo invia(String ipServer, int porta) invia alla
porta porta del server di ip ipServer l'oggetto LogXML serializzato e lo
stampa a video. 

*/