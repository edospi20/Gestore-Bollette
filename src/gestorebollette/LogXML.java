
package gestorebollette;

import com.thoughtworks.xstream.XStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogXML {
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
            localAddress = InetAddress.getLocalHost().getHostAddress();
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
    
    public void invia(String ipServer, int porta){
        
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
