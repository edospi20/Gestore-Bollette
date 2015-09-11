package gestorebollette;

import javafx.application.*; 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import com.thoughtworks.xstream.converters.basic.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import static javafx.scene.control.ContentDisplay.RIGHT;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Callback;

public class GestoreBollette extends Application{       //(00)
        private final int LARGHEZZA_FINESTRA = 850;
        private final int ALTEZZA_FINESTRA = 700;
	private ConfigurazioneXML conf;
	//(01)
	private Label intestazione; 
        private HBox contenitoreBoxBollette;
        private HashMap<String, ObservableList<Bolletta>> bolletteTipo;
        private Label inserisci;
        private HBox contenitoreDatiInserimento;
        private Label tipo;
        private ChoiceBox tipoBolletta;
        private Label data;
        private TextField campoData;
        private Label importo;
        private TextField campoImporto;
        private Button confermaIns;
        private Label seleziona;
        private HBox contenitoreDatiSelezione;
        private Label tipoSel;
        private TextField campoTipoSel;
        private Label dataSel;
        private TextField campoDataSel;
        private Label importoSel;
        private TextField campoImportoSel;
        private Label quotaSel;
        private TextField campoQuotaSel;
        private GridPane grigliaPagamenti;
        private Button confermaSel;
        
        private int[] indiciUtenti;
        
        private Bolletta bollettaSelezionata;       //serve per la cache
        
        private class BollettaListaVisual extends ListCell<Bolletta>{
            
            @Override
            protected void updateItem(Bolletta item, boolean empty) {       //(002)
                super.updateItem(item, empty);
                
                setOnMouseClicked((MouseEvent e) -> {   //(003)
                    bollettaSelezionata = item;
                    ResultSet res = DepositoDati.cercaPagamenti(item.id, conf.utenti);
                    
                    campoTipoSel.setText(item.tipo);
                    campoDataSel.setText(item.data);
                    campoImportoSel.setText(Double.toString(item.importo));
                    try {
                        res.next();
                        campoQuotaSel.setText(Double.toString(res.getDouble("Quota")));
                        bollettaSelezionata.quota = res.getDouble("Quota");
                    } catch(SQLException ex) {
                        System.err.println("Errore nel get della quota dal DB: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    
                    settaPagamenti(res);
                }); 
                
                if(!empty){
                    setText(item.toCell());
                    setGraphic(item.pagataDaTutti ? new ImageView("tick.png") : new ImageView("cross.png"));
                    setContentDisplay(RIGHT);
                }
                else{
                    setText(null);
                }
            }
        }
        
        public void settaPagamenti(ResultSet res){      //(004)
            int indiceRiga = 0;
            try{
                do{
                   String nomeUtente = res.getString("NomeUtente");
                   while(indiceRiga < conf.utenti.length){
                       Label elem = (Label) grigliaPagamenti.getChildren().get(indiceRiga*3);
                       if(nomeUtente.equals(elem.getText())){
                            if(res.getInt("Pagata") == 1){
                                ImageView a = new ImageView("tick.png");
                                grigliaPagamenti.setConstraints(a, 1, indiceRiga);
                                grigliaPagamenti.getChildren().set(indiceRiga*3 + 1, a);
                                GridPane.setHalignment(a, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.PAGATA;
                            }else{
                                ImageView b = new ImageView("cross.png");
                                grigliaPagamenti.setConstraints(b, 1, indiceRiga);
                                grigliaPagamenti.getChildren().set(indiceRiga*3 + 1, b);
                                GridPane.setHalignment(b, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.NON_PAGATA;
                            }
                            indiceRiga++;
                            break;
                       }else{
                            ImageView c = new ImageView("square.png");
                            grigliaPagamenti.setConstraints(c, 1, indiceRiga);
                            grigliaPagamenti.getChildren().set(indiceRiga*3 + 1, c);
                            GridPane.setHalignment(c, HPos.CENTER);
                            bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.ASSENTE;
                       }
                       indiceRiga++;
                   }
                }while(res.next());
            }catch(SQLException ex){
                System.err.println("Errore nel get del nomeUtente : " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        
        public void creaStrutture(){        //(02)
            intestazione = new Label("GESTORE BOLLETTE");
            
            contenitoreBoxBollette = new HBox(20);  //(03)
            
            for(BollettaConfig bconf : conf.bollette){
                Label tipoBollette = new Label(bconf.tipo); 
                tipoBollette.setPrefWidth(conf.larghezzaRiquadroBollette);
                tipoBollette.setAlignment(Pos.CENTER);      //(04)
                tipoBollette.setStyle("-fx-text-fill:" + bconf.colore + "; " + conf.stileLabel);
                ResultSet res = DepositoDati.cercaBollette(bconf.tipo);
                ObservableList<Bolletta> bollette = FXCollections.observableArrayList();
                try{
                    while(res.next()){
                        bollette.add(new Bolletta(res.getInt("Id"), res.getString("Tipo"), res.getString("Data"), res.getDouble("Importo"), res.getInt("Pagata") == 1, conf.utenti.length));
                    }
                    }catch (SQLException e){
                        e.printStackTrace();
                }
                bolletteTipo.put(bconf.tipo, bollette);
                //To Do ObservableView con db 
                ListView<Bolletta> listaBollette = new ListView<Bolletta>(bollette);    //(03)
                Label placeholder = new Label("Nessuna bolletta presente");
                placeholder.setStyle("-fx-text-fill: #F63526");
                listaBollette.setPlaceholder(placeholder);
                listaBollette.setCellFactory(new Callback<ListView<Bolletta>, ListCell<Bolletta>>(){    //(007)
                    @Override
                    public ListCell<Bolletta> call(ListView<Bolletta> listaBollette){
                        return new BollettaListaVisual();
                    }
                });
                
                listaBollette.setPrefWidth(conf.larghezzaRiquadroBollette);
                listaBollette.setPrefHeight(conf.altezzaRiquadroBollette);
                listaBollette.setStyle("-fx-background-color:" + bconf.colore);
                contenitoreBoxBollette.getChildren().add(new VBox(10, tipoBollette, listaBollette));  //(03)
            }
            
            inserisci = new Label("INSERISCI NUOVA BOLLETTA");
   
            contenitoreDatiInserimento = new HBox(15);
            
            tipo = new Label("Tipo:");
            ObservableList<String> tipiBollette = FXCollections.observableArrayList();  //(05)
            for(BollettaConfig bconf : conf.bollette){
                tipiBollette.add(bconf.tipo);
            }
            tipoBolletta = new ChoiceBox(tipiBollette);  //(05)
            data = new Label("Data:");
            campoData = new TextField();
            importo = new Label("Importo:");
            campoImporto = new TextField();
            
            contenitoreDatiInserimento.getChildren().addAll(tipo, tipoBolletta, data, campoData, importo, campoImporto);
 
            confermaIns = new Button("CONFERMA");
 
            seleziona = new Label("BOLLETTA SELEZIONATA");
            
            contenitoreDatiSelezione = new HBox(15);

            tipoSel = new Label("Tipo:");
            campoTipoSel = new TextField();
            dataSel = new Label("Data:");
            campoDataSel = new TextField();
            importoSel = new Label("Importo:");
            campoImportoSel = new TextField();
            quotaSel = new Label("Quota:");
            campoQuotaSel = new TextField();

            contenitoreDatiSelezione.getChildren().addAll(tipoSel, campoTipoSel, dataSel, campoDataSel, importoSel, campoImportoSel, quotaSel, campoQuotaSel);
            
            grigliaPagamenti = new GridPane();      //(06)
            /*grigliaPagamenti.setGridLinesVisible(true);*/
            Arrays.sort(conf.utenti);
            indiciUtenti = new int[conf.utenti.length];
            for(int i = 0; i < conf.utenti.length; i++){
                Label utente = new Label(conf.utenti[i]);
                GridPane.setHalignment(utente, HPos.CENTER);    //(07)
                ImageView pagato = new ImageView("square.png");
                GridPane.setHalignment(pagato, HPos.CENTER);
                Button paga = new Button("PAGA"); //aggiungere l'evento
                GridPane.setHalignment(paga, HPos.CENTER);
                grigliaPagamenti.setConstraints(utente, 0, i);  //(08)
                grigliaPagamenti.setConstraints(pagato, 1, i);
                grigliaPagamenti.setConstraints(paga, 2, i);
                grigliaPagamenti.getRowConstraints().add(new RowConstraints(conf.altezzaRigaGriglia)); //(09)
                grigliaPagamenti.getChildren().addAll(utente, pagato, paga);
                /*if(bollettaSelezionata.pagamentoUtenti[i] == Bolletta.StatoPagamento.ASSENTE){
                    paga.setDisable(true);
                }else{
                    paga.setOnAction((ActionEvent event) ->{
                        if(bollettaSelezionata.pagamentoUtenti[i] == Bolletta.StatoPagamento.PAGATA){
                            
                        }else{
                            
                        }

                    });
                }*/
            }
            
            grigliaPagamenti.getColumnConstraints().add(new ColumnConstraints(conf.larghezzaColonnaGriglia));
            grigliaPagamenti.getColumnConstraints().add(new ColumnConstraints(conf.larghezzaColonnaGriglia));
            grigliaPagamenti.getColumnConstraints().add(new ColumnConstraints(conf.larghezzaColonnaGriglia));  
            
            confermaSel = new Button("CONFERMA");
        }
        
        public void gestisciEventi(Stage stage){        
            confermaIns.setOnAction((ActionEvent event) -> {
                String tipoIns = (String) tipoBolletta.getValue();      
                String dataIns = campoData.getText();   
                double importoIns = Double.parseDouble(campoImporto.getText());
                double quota = importoIns / conf.utenti.length;
                int idBollettaIns = DepositoDati.inserisciBolletta(tipoIns, dataIns, importoIns, quota, conf.utenti);
                Bolletta bollettaIns = new Bolletta(idBollettaIns, tipoIns, dataIns, importoIns, false, conf.utenti.length);
                if(idBollettaIns >= 0){
                    bolletteTipo.get(tipoIns).add(bollettaIns);
                }
            }); 
            
            confermaSel.setOnAction((ActionEvent event) ->{
                DepositoDati.modificaPagamenti(bollettaSelezionata.id, bollettaSelezionata, conf.utenti);
                //cambiare con tic nella ListView se tutti hanno pagato
            });
            
            stage.setOnCloseRequest((WindowEvent event) -> chiudi());
        }
        
        public void settaStile(){       //(10)
         
            intestazione.setStyle(conf.stileIntestazione);
            intestazione.setPrefWidth(LARGHEZZA_FINESTRA);
            intestazione.setAlignment(Pos.CENTER);
            
            contenitoreBoxBollette.setPadding(new Insets(50, 0, 30, 0));
            contenitoreBoxBollette.setPrefWidth(LARGHEZZA_FINESTRA);
            contenitoreBoxBollette.setAlignment(Pos.CENTER);
            
            inserisci.setStyle(conf.stileLabel + "-fx-text-fill: #F63526;");
            inserisci.setPrefWidth(LARGHEZZA_FINESTRA);
            inserisci.setAlignment(Pos.CENTER);
            inserisci.setLayoutY(contenitoreBoxBollette.getLayoutY() + conf.altezzaRiquadroBollette + 100);
            
            contenitoreDatiInserimento.setPadding(new Insets(40, 0, 40, 0));
            contenitoreDatiInserimento.setPrefWidth(LARGHEZZA_FINESTRA - 30);
            contenitoreDatiInserimento.setAlignment(Pos.CENTER);
            contenitoreDatiInserimento.setLayoutY(inserisci.getLayoutY());
            
            //tipo.setStyle(conf.stileLabel);
            tipoBolletta.setPrefWidth(80);
            //data.setStyle(conf.stileLabel);
            campoData.setPrefWidth(80);
            //importo.setStyle(conf.stileLabel);
            campoImporto.setPrefWidth(80);
            
            confermaIns.setPrefHeight(24);
            confermaIns.setLayoutX(LARGHEZZA_FINESTRA/2 - confermaIns.getWidth()/2);
            confermaIns.setLayoutY(contenitoreDatiInserimento.getLayoutY() + contenitoreDatiInserimento.getPrefHeight() + 90);
            
            seleziona.setStyle(conf.stileLabel + "-fx-text-fill: #F63526;");
            seleziona.setPrefWidth(LARGHEZZA_FINESTRA);
            seleziona.setAlignment(Pos.CENTER);
            seleziona.setLayoutY(confermaIns.getLayoutY() + confermaIns.getPrefHeight() + 20);
            
            contenitoreDatiSelezione.setPadding(new Insets(40, 0, 40, 0));
            contenitoreDatiSelezione.setPrefWidth(LARGHEZZA_FINESTRA);
            contenitoreDatiSelezione.setAlignment(Pos.CENTER);
            contenitoreDatiSelezione.setLayoutY(seleziona.getLayoutY());
            
            //tipoSel.setStyle(conf.stileLabel);
            campoTipoSel.setPrefWidth(80);
            //dataSel.setStyle(conf.stileLabel);
            campoDataSel.setPrefWidth(80);
            //importoSel.setStyle(conf.stileLabel);
            campoImportoSel.setPrefWidth(80);
            //quotaSel.setStyle(conf.stileLabel);
            campoQuotaSel.setPrefWidth(80);
            
            grigliaPagamenti.setPrefWidth(LARGHEZZA_FINESTRA);
            grigliaPagamenti.setAlignment(Pos.CENTER);
            grigliaPagamenti.setLayoutY(contenitoreDatiSelezione.getLayoutY() + contenitoreDatiSelezione.getHeight() + 60);
            
            confermaSel.setPrefHeight(24);
            confermaSel.setLayoutX(LARGHEZZA_FINESTRA/2 - confermaSel.getWidth()/2);
            confermaSel.setLayoutY(grigliaPagamenti.getLayoutY() + (conf.utenti.length * conf.altezzaRigaGriglia) + 20);
        }
        
        public void ripristinaConCache(List<String> datiCache){
            if(!datiCache.isEmpty()){
                tipoBolletta.setValue(datiCache.get(0));        //potrebbe dare errore con stringa vuota 
                campoData.setText(datiCache.get(1));
                campoImporto.setText(datiCache.get(2));
                if(datiCache.size() > 3){
                    campoTipoSel.setText(datiCache.get(4));
                    campoDataSel.setText(datiCache.get(5));
                    campoImportoSel.setText(datiCache.get(6));
                    campoQuotaSel.setText(datiCache.get(7));
                    bollettaSelezionata = new Bolletta(Integer.parseInt(datiCache.get(3)), datiCache.get(4), datiCache.get(5), Double.parseDouble(datiCache.get(6)), (Integer.parseInt(datiCache.get(8)) == 1), conf.utenti.length);
                    for(int i = 9; i < conf.utenti.length + 9; i++){
                        int j = i - 9;
                        switch(datiCache.get(i)){
                            case "PAGATA":                               
                                ImageView pagata = new ImageView("tick.png");
                                grigliaPagamenti.setConstraints(pagata, 1, j);
                                grigliaPagamenti.getChildren().set(j*3 + 1, pagata);
                                GridPane.setHalignment(pagata, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[j] = Bolletta.StatoPagamento.PAGATA;
                                break;
                            case "NON_PAGATA":                               
                                ImageView nonPagata = new ImageView("cross.png");
                                grigliaPagamenti.setConstraints(nonPagata, 1, j);
                                grigliaPagamenti.getChildren().set(j*3 + 1, nonPagata);
                                GridPane.setHalignment(nonPagata, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[j] = Bolletta.StatoPagamento.NON_PAGATA;
                                break;
                            case "ASSENTE":                               
                                ImageView assente = new ImageView("square.png");
                                grigliaPagamenti.setConstraints(assente, 1, j);
                                grigliaPagamenti.getChildren().set(j*3 + 1, assente);
                                GridPane.setHalignment(assente, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[j] = Bolletta.StatoPagamento.ASSENTE;
                                break;
                        }                       
                    }
                } 
            }
        }
        
        public void chiudi(){
            List<String> datiCache = new ArrayList<String>();
            if(tipoBolletta.getValue() != null)
                datiCache.add(tipoBolletta.getValue().toString());
            else 
                datiCache.add(" ");
            datiCache.add(campoData.getText());
            datiCache.add(campoImporto.getText());
            if(bollettaSelezionata != null){
                datiCache.add(Integer.toString(bollettaSelezionata.id));
                datiCache.add(campoTipoSel.getText().toString());
                datiCache.add(campoDataSel.getText().toString());
                datiCache.add(campoImportoSel.getText().toString());
                datiCache.add(campoQuotaSel.getText().toString());
                datiCache.add(Integer.toString((bollettaSelezionata.pagataDaTutti) ? 1 : 0));
                for(int i = 0; i < conf.utenti.length; i++){
                    datiCache.add(bollettaSelezionata.pagamentoUtenti[i].toString());
                }
            }
            
            FormCache salvataggio = new FormCache(datiCache);
            salvataggio.salvaCache();
        }
	
        
        @Override
	public void start(Stage stage){
            conf = new ConfigurazioneXML();
            
            XStream xs = new XStream();     //(11)
            xs.alias("config", ConfigurazioneXML.class);    //(12)
            xs.alias("bolletta", BollettaConfig.class);
            xs.useAttributeFor(BollettaConfig.class, "colore");     //(13)
            xs.alias("nome", String.class);
            
            conf = (ConfigurazioneXML) xs.fromXML(new File("configurazione.xml"));      //(14)
            
            bolletteTipo = new HashMap<String, ObservableList<Bolletta>>(conf.bollette.length);
            
            creaStrutture();
                     
            //(15)
            Group root;
            root = new Group(intestazione, contenitoreBoxBollette, inserisci ,contenitoreDatiInserimento, confermaIns, seleziona, contenitoreDatiSelezione, grigliaPagamenti, confermaSel);
            Scene scene = new Scene(root, LARGHEZZA_FINESTRA, ALTEZZA_FINESTRA); 
            stage.setTitle("Gestore Bollette");          
            stage.setScene(scene);                          
            stage.show();   
          
            gestisciEventi(stage);
              
            settaStile();
           
            List<String> datiCache = new ArrayList<String>();
            FormCache caricamento = new FormCache(datiCache);
            caricamento.caricaCache();
            
            datiCache = caricamento.getCampi();
            ripristinaConCache(datiCache);
	}
}

/*
(01):
Componenti grafici dell'interfaccia.
https://docs.oracle.com/javafx/2/ui_controls/jfxpub-ui_controls.htm

(002):
UpdateItem viene richiamato ogni volta che cambia il contenuto di una cella

(003):
Al click su una cella della ListView ricavo i dati della bolletta corrispondente 
e li stampo nei rispettivi campi della sezione bolletta selezionata.
https://docs.oracle.com/javase/8/docs/api/java/awt/event/MouseEvent.html

(004):
Il metodo settaPagamenti visualizza nella griglia i corretti pagamenti associati
agli utenti e inizializza il campo pagamentoUtenti della Bolletta bollettaSelezionata.

(02):
Il metodo creaStrutture() crea tutti gli oggetti che saranno presenti 
nell' interfaccia grafica.

(03):
Costruttore di oggetto HBox il cui argomento indica lo spacing che intercorre
tra gli elementi contenuti nell'HBox.
In seguito aggiungerò all'oggetto HBox tutti gli oggetti VBox che a loro volta conterrano 
una Label che indica il tipo di bollette e una ListView<Bolletta> caratterizzata
dai dati delle bollette di quel tipo.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/HBox.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/VBox.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html

(007):
Il metodo setCellFactory serve per settare una nuova formattazione
della cella da usare nella ListView.

(04):
Il metodo setAlignment(Pos.CENTER) consente all'oggetto cui è applicato 
di posizionarsi al centro rispetto allo spazio occupato dall'oggetto stesso nella
visualizzazione.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Labeled.html#setAlignment-javafx.geometry.Pos-

(05):
Modificando l' ObservableList<> modifico le possibili scelte che 
appariranno nella ChoiceBox.
https://docs.oracle.com/javase/8/javafx/api/javafx/collections/ObservableList.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ChoiceBox.html

(06):
L'oggetto GridPane consente di formattare una porzione dell'interfaccia grafica 
sotto forma di griglia.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html

(07):
Il metodo setHalignment(utente, HPos.CENTER) consente di posizionare l'oggetto
passato come primo argomento centrato orizzontalmente all'intero di una cella
della GridPane.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html#setHalignment-javafx.scene.Node-javafx.geometry.HPos-

(08):
Il metodo setConstraints(utente, j, i) consente di settare il contenuto 
della cella di colonna j e riga i con l'oggetto utente quando utente sarà
aggiunto nella GridPane.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html#setConstraints-javafx.scene.Node-int-int-

(09):
I metodi getRowConstraints() e getColumnConstraints() 
mi consentono di settare l'altezza di una riga della cella e l'ampiezza di una 
colonna.


(10):
Il metodo settaStile() consente di settare lo stile dei vari oggetti istanziati 
dal metodo creaStrutture().
https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html

(11):
XStream è un flusso in cui possono viaggiare oggetti XML e che offre metodi 
per la serializzazione e la deserializzazione.
https://x-stream.github.io/javadoc/index.html

(12):
Il metodo alias("config", ConfigurazioneXML.class) genera un alias per la seconda 
classe passata come argomento; facendo si che l'elemento <ConfigurazioneXML> per 
esempio venga visto come <config>.

(13):
Il metodo useAttributeFor(BollettaConfig.class, "colore") fa si che il campo
colore presente nella classe BollettaConfig venga visto come un attributo.

(14):
Il metodo fromXML(new File("configurazione.xml")) deserializza il file da XML a
Java.

(15):
Aggiungo gli elementi all'interfaccia tramite l'uso del Group(contenitore di oggetti osservabili)
della Scene (contenitore principale della finestra) e dello Stage(finestra vera e propria).
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Group.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Scene.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Stage.html
*/