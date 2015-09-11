
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
    
    public Bolletta(int id, String tipo, String data, double importo, double quota, boolean pagata, StatoPagamento[] pagamento){
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
    
    public void setPagamenti(boolean[] pagamenti){
    }
    
    public String toCell(){     //(04)
        return data + "  " + importo + "€  ";
    }
}

/*
(00): 
La classe Bolletta viene utilizzata per creare un modello per i dati di 
ciascuna bolletta.

(02):
Costruttore utilizzato alla creazione delle bollette inserite nelle ListView<Bolletta>. 

(04):
Il metodo toCell ritorna la stringa con i dati della bolletta da inserire 
nelle celle delle ListView.

*/
