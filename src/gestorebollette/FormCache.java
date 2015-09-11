
package gestorebollette;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FormCache implements Serializable{     //(00)
    private List<String> campi= new ArrayList<String>();    //(01)
    
    FormCache(List<String> campi){
        this.campi = campi;
    }
    
    List<String> getCampi(){
        return campi;
    }
    
    void salvaCache(){  //(02)
        try{
            File cache = new File("formcache.bin");
            if (!cache.exists()) {
                /*logFile.getParentFile().mkdirs();*/
                cache.createNewFile();
            }
                
            FileOutputStream fout = new FileOutputStream(cache); //(04)
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(this.campi);
            oout.close();
            fout.close();
        }catch (FileNotFoundException e){
            System.out.println("Errore nell' apertura del file" + e.getMessage());
            e.printStackTrace();
        }catch (IOException e) {
            System.out.println("Errore durante la scrittura su file" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    void caricaCache(){     //(03)
       
            try{
                FileInputStream fin = new FileInputStream("formcache.bin"); //(04)
                ObjectInputStream oin = new ObjectInputStream(fin);
                this.campi = (List<String>) oin.readObject();
                oin.close();
                fin.close();
            }catch (FileNotFoundException e){
                System.out.println("File di cache non esistente " + e.getMessage());
            }catch (IOException e) {
                System.out.println("Errore durante la lettura da file " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("Classe dell'oggetto serializzato non trovata " + e.getMessage());
                e.printStackTrace();
            }
    }
}


/*
(00):
La classe FormCache viene utilizzata per leggere e scrivere i dati 
nel file formcache.bin, tramite dei flussi oggetto.

(01):
Il membro privato campi verrà inizializzato con le stringhe presenti nei 
campi dell'interfaccia grafica.

La classe ArrayList implementa l'interfaccia List. Viene usato un array per contenere
gli elementi della lista che ha la stessa dimensione della lista.
https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html

(02):
Il metodo salvaCache(), richiamato alla chiusura dell'app 
salva sul file binario il membro campi.

(03):
Il metodo caricaCache, richiamato all'avvio dell'app 
inizializza il membro campi con i dati presenti nel file binario. 

(04):
I flussi file FileOutputStream e FileInputStream generano un collegamento con 
il file passato per argomento.
https://docs.oracle.com/javase/8/docs/api/java/io/FileOutputStream.html
https://docs.oracle.com/javase/8/docs/api/java/io/FileInputStream.html

I flussi oggetto ObjectOutputStream e ObjectInputStream scrivono o leggono 
dati primitivi di oggetti java nell' ObjectOutputStream o dall' ObjectInputStream corrispondente.
https://docs.oracle.com/javase/8/docs/api/java/io/ObjectInputStream.html
https://docs.oracle.com/javase/8/docs/api/java/io/ObjectOutputStream.html

*/