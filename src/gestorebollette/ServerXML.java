
package gestorebollette;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerXML {
    public static void main(String[] args){
        try{
       
        File logFile = new File("gestorebollette.xml");
        if (!logFile.exists()) {
            /*logFile.getParentFile().mkdirs();*/
            logFile.createNewFile();
        }
        
        ServerSocket servs = new ServerSocket(Integer.parseInt(args[0]));      //(01)
        while(true){           
            Socket s = servs.accept();                          //(02)
            ObjectInputStream oin = new ObjectInputStream(s.getInputStream());
            String log = (String) oin.readObject();
            System.out.println("Ricevuto: ");
            System.out.println(log);
            System.out.println();
            try{
                if(ValidazioneXML.valida(log)){
                    PrintWriter pw = new PrintWriter(new FileWriter(logFile, true));   //(03)     
                    pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    pw.println(log);
                    pw.println();
                    pw.close();
                }
            }catch (IOException e) {
                System.err.println("Errore nell' aperture del file: " + e.getMessage());                
            }
            oin.close();
            s.close();
        }   
        }catch (IOException e) {
            System.err.println("Errore nella connessione del server: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Errore nella lettura della riga di log: " + e.getMessage());
        }
    }
}

/*
(01):
ServerSockets implementa dei server sockets in attesa di richieste dalla rete.
Gli passo la porta del server da riga di comando come primo argomento.
https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html

(02):
Socket implementa client socket.Un socket è un canale di comunicazione bidirezionale 
che permette la comunicazione di programmi che girano su due host diversi connessi in rete.
https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html

(03):
Il secondo parametro del costruttore indica che il file è incrementale.
Ad ogni successiva scrittura nel file verrà appeso il contenuto della scrittura.
Uso PrintWriter per stampare e andare a capo.
*/
