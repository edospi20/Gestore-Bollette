
package gestorebollette;

public class Bolletta {     //(00)
    int id;    
    String tipo;
    String data;
    double importo;
    double quota;
    boolean pagataDaTutti;
    StatoPagamento[] pagamentoUtenti;
    
    enum StatoPagamento{PAGATA, NON_PAGATA, ASSENTE}
    
    public Bolletta(int id, String tipo, String data, double importo, double quota, boolean pagata, StatoPagamento[] pagamento){    //(01)
        this.id = id;
        this.tipo = tipo;
        this.data = data;
        this.importo = importo;
        this.quota = quota;
        this.pagataDaTutti = pagata;
        this.pagamentoUtenti = pagamento;
    }
    
    public Bolletta(int id, String tipo, String data, double importo, boolean pagata, int numUtenti){   //(02)
        this.id = id;
        this.tipo = tipo;
        this.data = data;
        this.importo = importo;
        this.pagataDaTutti = pagata;
        pagamentoUtenti = new StatoPagamento[numUtenti];
    }
 
    public String toCell(){     //(03)
        return data + "  " + importo + "€ ";
    }
}

/*
(00): 
La classe Bolletta viene utilizzata per creare un modello per i dati di 
ciascuna bolletta.

(01):
Costruttore che inizializza anche lo StatoPagamento dei singoli utenti.

(02):
Costruttore utilizzato alla creazione delle bollette inserite nelle ListView<Bolletta>. 

(03):
Il metodo toCell ritorna la stringa con i dati della bolletta da inserire 
nelle celle delle ListView.

*/
