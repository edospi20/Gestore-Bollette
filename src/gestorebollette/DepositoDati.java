
package gestorebollette;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepositoDati {     //(00)
    private static Connection con;      //(01)
    
    private static void getConnection(){
        try{
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/archiviobollette", "root", ""); //(02) 
            }catch (SQLException e) {
                System.err.println("Errore di connessione al DB: " + e.getMessage());
                e.printStackTrace();
            }
    }
     
    static ResultSet cercaBollette(String tipo, int limit){    //(03)
        ResultSet res = null;
        getConnection();
        try{
            PreparedStatement psta = con.prepareStatement("SELECT Id, Tipo, Data, Importo, IF(sum(Pagata) = count(*), 1, 0) as Pagata FROM Bolletta b join Pagamento p on b.Id = p.IdBolletta WHERE Tipo = ? GROUP BY b.Id ORDER BY b.Id DESC LIMIT ?");  //(04)
            psta.setString(1, tipo);
            psta.setInt(2, limit);
            res = psta.executeQuery();   //(05)    
        }catch (SQLException e) {
                   System.err.println("Errore nella ricerca di dati dal DB: " + e.getMessage());
                   e.printStackTrace();
               }
        return res;
    }
    
    static ResultSet cercaPagamenti(int id, String[] utenti){       //(06)
        ResultSet res = null;
        getConnection();
        try{
            String query = "SELECT NomeUtente, Pagata, Quota FROM Bolletta b join Pagamento p on b.Id = p.IdBolletta WHERE Id = ? AND NomeUtente IN (";
            for(int i = 0; i < utenti.length - 1; i++){
                query += "?, ";
            }
            query += "?) ORDER BY NomeUtente ASC";            
            PreparedStatement psta = con.prepareStatement(query);
            psta.setInt(1, id);
            for(int i = 0; i < utenti.length; i++){
                psta.setString(i + 2, utenti[i]);
            }
            res = psta.executeQuery();      
        }catch (SQLException e) {
                   System.err.println("Errore nella ricerca di dati dal DB: " + e.getMessage());
                   e.printStackTrace();
               }
        return res;
    }
    
    static int inserisciBolletta(String tipo, String data, double importo, double quota, String[] utenti){  //(07)
        int idBolletta = -1;
        getConnection();
        try{
            PreparedStatement psta = con.prepareStatement("INSERT INTO Bolletta (Tipo, Data, Importo, Quota) VALUES (?, ?, ?, ?)");
            psta.setString(1, tipo);
            psta.setString(2, data);
            psta.setDouble(3, importo);
            psta.setDouble(4, quota);
            psta.executeUpdate();   
            
            Statement sta = con.createStatement();        //(08)
            ResultSet res = sta.executeQuery("SELECT max(Id) as UltimoId FROM Bolletta");
            res.next();
            idBolletta = res.getInt("UltimoId");
            
            for(int i = 0; i < utenti.length; i++){
                psta = con.prepareStatement("INSERT INTO Pagamento (IdBolletta, NomeUtente) VALUES (?, ?)");
                psta.setInt(1, idBolletta);
                psta.setString(2, utenti[i]);
                psta.executeUpdate(); 
            }
        }catch (SQLException e) {
                  System.err.println("Errore nell' inserimento di dati nel DB: " + e.getMessage());
                  e.printStackTrace();
                }    
        return idBolletta;
    }
    
    static int saltaAssenti(int index, Bolletta.StatoPagamento[] statoPagamenti, String[] utenti){      //(09)
        for(int i = index; index < utenti.length; index++){
            if(statoPagamenti[i] != Bolletta.StatoPagamento.ASSENTE){
                return index;
            }
        }
        return -1;
    }
    
    static boolean modificaPagamenti(int id, Bolletta bollettaSelezionata, String[] utenti){        //(10)
        getConnection();
        try{
            PreparedStatement psta;
            int j = 0;
            for(int i = 0; i < utenti.length; i++){
                psta = con.prepareStatement("UPDATE Pagamento SET Pagata = ? WHERE IdBolletta = ? AND NomeUtente = ?");
                int indicePagamentoNonAssente = saltaAssenti(j, bollettaSelezionata.pagamentoUtenti, utenti);
                if(!(indicePagamentoNonAssente < 0)){
                    int pagata = (bollettaSelezionata.pagamentoUtenti[indicePagamentoNonAssente] == Bolletta.StatoPagamento.PAGATA) ? 1 : 0;
                    psta.setInt(1, pagata);
                    psta.setInt(2, id);
                    psta.setString(3, utenti[indicePagamentoNonAssente]);
                    psta.executeUpdate(); 
                }
                j = indicePagamentoNonAssente + 1; 
            }
        }catch (SQLException e) {
                  System.err.println("Errore nella modifica di dati del DB: " + e.getMessage());
                  e.printStackTrace();
                }
        return (bollettaSelezionata.pagataDaTutti) ? true : false;
    }
}

/*
(00):
La classe DepositoDati svolge il compito di stabilire una connessione con il 
DB e fornisce alla classe GestoreBollette tutti i ResultSet necessari.
https://docs.oracle.com/javase/tutorial/jdbc/overview/index.html

(01):
L'interfaccia Connection consente di stabilire una connessione con il 
DataBase e di eseguire statement SQL.
https://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html

(02):
Il metodo getConnection("jdbc:mysql://localhost:3306/GestoreBollette", "root", "")
della classe DriverManager ritorna un oggetto Connection ed ha come argomenti 
l'Url del DB, l'username e la password.
https://docs.oracle.com/javase/8/docs/api/java/sql/DriverManager.html

(03):
Il metodo cercaBollette ritorna come ResultSet tutte le bollette del tipo passato
per argomento.

(04):
E' uno Statement SQL precompilato

(05):
ResultSet contiente un set di risultati del DB.
Esso contiene un puntatore che inizialmente è posizionato prima della 
prima riga dei risultati. Tramite il metodo next() il puntatore si sposta
alla prossima riga e torna falso quando non ci sono più righe.
https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html

(06):
Il metodo cercaPagamenti ritorna come ResultSet i dati dei pagamenti degli 
utenti presenti nel file di configurazione, relativi alla bolletta passata per argomento.

(07):
Il metodo insericiBolletta inserisce nel DB la bolletta i cui dati sono stati 
inseriti dall'utente nell'interfaccia grafica.

(08):
Uno Statement è un'interfaccia che rappresenta uno statement SQL.
Per eseguirlo è necessaria una connessione, che viene fornita dal driver.
L'esecuzione di uno Statement può produrre come risultato un ResultSet, che 
contiene le tuple identificate dall' esecuzione dello statement sul DB.
https://docs.oracle.com/javase/8/docs/api/java/sql/Statement.html

(09):
Il metodo saltaAssenti(int index, Bolletta.StatoPagamento[] statoPagamenti, String[] utenti)
ritorna il primo indice dell'array statoPagamenti il cui StatoPagamento è diverso
da ASSENTE.

(10):
Il metodo modificaPagamenti serve per modificare gli stati di pagamento nel DB 
relativi agli utenti presenti nel file di configurazione in quel momento e 
il cui stato di pagamento è diverso da ASSENTE.

*/