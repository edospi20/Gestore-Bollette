package gestorebollette;

import javafx.application.*; 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
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
        
        private Bolletta bollettaSelezionata; 
        
        private class HandlerPagamento implements EventHandler<ActionEvent>{
            private int indiceUtente;
            
            public HandlerPagamento(int iUtente){
                indiceUtente = iUtente;
            }
            
            @Override
            public void handle(ActionEvent event){
                if(bollettaSelezionata.pagamentoUtenti[indiceUtente] == Bolletta.StatoPagamento.PAGATA){
                    ImageView nonPagata = new ImageView("cross.png");
                    grigliaPagamenti.setConstraints(nonPagata, 1, indiceUtente);
                    grigliaPagamenti.getChildren().set(indiceUtente*3 + 1, nonPagata);
                    GridPane.setHalignment(nonPagata, HPos.CENTER);
                    bollettaSelezionata.pagamentoUtenti[indiceUtente] = Bolletta.StatoPagamento.NON_PAGATA;
                }else{
                    ImageView pagata = new ImageView("tick.png");
                    grigliaPagamenti.setConstraints(pagata, 1, indiceUtente);     
                    grigliaPagamenti.getChildren().set(indiceUtente*3 + 1, pagata);
                    GridPane.setHalignment(pagata, HPos.CENTER);
                    bollettaSelezionata.pagamentoUtenti[indiceUtente] = Bolletta.StatoPagamento.PAGATA;
                }
            }
        }
        
        private class BollettaListaVisual extends ListCell<Bolletta>{     
            @Override
            protected void updateItem(Bolletta item, boolean empty) {       //(02)
                super.updateItem(item, empty);
                
                setOnMouseClicked((MouseEvent e) -> {   //(03)                  
                    bollettaSelezionata = item;
                    if(item == null){       //(03)
                        svuotaCampi("selezione");
                        return;
                    }
                    confermaSel.setDisable(false);
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
        
        public void settaPagamenti(ResultSet res){      //(04)
            int indiceRiga = 0;
            //ToDo rimuovere errore se ultimo utente con pagamento assenti in chiusura dell'app.
            //bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.ASSENTE non inizializzata.
            try{
                do{
                   String nomeUtente = res.getString("NomeUtente");
                   while(indiceRiga < conf.utenti.length){
                       Label elem = (Label) grigliaPagamenti.getChildren().get(indiceRiga*3);
                       if(nomeUtente.equals(elem.getText())){
                            if(res.getInt("Pagata") == 1){
                                ImageView pagata = new ImageView("tick.png");
                                grigliaPagamenti.setConstraints(pagata, 1, indiceRiga);     //(05)
                                grigliaPagamenti.getChildren().set(indiceRiga*3 + 1, pagata);
                                GridPane.setHalignment(pagata, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.PAGATA;                         
                                grigliaPagamenti.getChildren().get(indiceRiga*3 + 2).setDisable(false);
                            }else{
                                ImageView nonPagata = new ImageView("cross.png");
                                grigliaPagamenti.setConstraints(nonPagata, 1, indiceRiga);
                                grigliaPagamenti.getChildren().set(indiceRiga*3 + 1, nonPagata);
                                GridPane.setHalignment(nonPagata, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.NON_PAGATA;
                                grigliaPagamenti.getChildren().get(indiceRiga*3 + 2).setDisable(false);
                            }
                            indiceRiga++;
                            break;
                       }else{
                            ImageView assente = new ImageView("square.png");
                            grigliaPagamenti.setConstraints(assente, 1, indiceRiga);
                            grigliaPagamenti.getChildren().set(indiceRiga*3 + 1, assente);
                            GridPane.setHalignment(assente, HPos.CENTER);
                            bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.ASSENTE;
                            grigliaPagamenti.getChildren().get(indiceRiga*3 + 2).setDisable(true);
                       }
                       indiceRiga++;
                   }
                }while(res.next());
                for(; indiceRiga < conf.utenti.length; indiceRiga++){
                    ImageView assente = new ImageView("square.png");
                    grigliaPagamenti.setConstraints(assente, 1, indiceRiga);
                    grigliaPagamenti.getChildren().set(indiceRiga*3 + 1, assente);
                    GridPane.setHalignment(assente, HPos.CENTER);
                    bollettaSelezionata.pagamentoUtenti[indiceRiga] = Bolletta.StatoPagamento.ASSENTE;
                    grigliaPagamenti.getChildren().get(indiceRiga*3 + 2).setDisable(true);
                }
            }catch(SQLException ex){
                System.err.println("Errore nel get del nomeUtente : " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        
        public void creaStrutture(){        //(06)
            intestazione = new Label("GESTORE BOLLETTE");
            
            contenitoreBoxBollette = new HBox(20);  //(07)
            
            for(BollettaConfig bconf : conf.bollette){
                Label tipoBollette = new Label(bconf.tipo); 
                tipoBollette.setPrefWidth(conf.larghezzaRiquadroBollette);
                tipoBollette.setAlignment(Pos.CENTER);      //(08)
                tipoBollette.setStyle("-fx-text-fill:" + bconf.colore + "; " + conf.stileLabel);
                ResultSet res = DepositoDati.cercaBollette(bconf.tipo, conf.maxNumeroBollette);
                ObservableList<Bolletta> bollette = FXCollections.observableArrayList();    
                try{
                    while(res.next()){
                        bollette.add(new Bolletta(res.getInt("Id"), res.getString("Tipo"), res.getString("Data"), res.getDouble("Importo"), res.getInt("Pagata") == 1, conf.utenti.length));
                    }
                    }catch (SQLException e){
                        e.printStackTrace();
                }
                bolletteTipo.put(bconf.tipo, bollette);
                ListView<Bolletta> listaBollette = new ListView<Bolletta>(bollette);    //(09)
                Label placeholder = new Label("Nessuna bolletta presente");
                placeholder.setStyle("-fx-text-fill: #F63526");
                listaBollette.setPlaceholder(placeholder);
                listaBollette.setCellFactory(new Callback<ListView<Bolletta>, ListCell<Bolletta>>(){    //(10)
                    @Override
                    public ListCell<Bolletta> call(ListView<Bolletta> listaBollette){
                        return new BollettaListaVisual();
                    }
                });
                
                listaBollette.setPrefWidth(conf.larghezzaRiquadroBollette);
                listaBollette.setPrefHeight(conf.altezzaRiquadroBollette);
                listaBollette.setStyle("-fx-background-color:" + bconf.colore);
                contenitoreBoxBollette.getChildren().add(new VBox(10, tipoBollette, listaBollette));  
            }
            
            inserisci = new Label("INSERISCI NUOVA BOLLETTA");
   
            contenitoreDatiInserimento = new HBox(15);
            
            tipo = new Label("Tipo:");
            ObservableList<String> tipiBollette = FXCollections.observableArrayList();  //(11)
            for(BollettaConfig bconf : conf.bollette){
                tipiBollette.add(bconf.tipo);
            }
            tipoBolletta = new ChoiceBox(tipiBollette);  //(11)
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
            campoTipoSel.setEditable(false);
            dataSel = new Label("Data:");
            campoDataSel = new TextField();
            campoDataSel.setEditable(false);
            importoSel = new Label("Importo:");
            campoImportoSel = new TextField();
            campoImportoSel.setEditable(false);
            quotaSel = new Label("Quota:");
            campoQuotaSel = new TextField();
            campoQuotaSel.setEditable(false);

            contenitoreDatiSelezione.getChildren().addAll(tipoSel, campoTipoSel, dataSel, campoDataSel, importoSel, campoImportoSel, quotaSel, campoQuotaSel);
            
            grigliaPagamenti = new GridPane();      //(12)
            /*grigliaPagamenti.setGridLinesVisible(true);*/
            Arrays.sort(conf.utenti);
            for(int i = 0; i < conf.utenti.length; i++){
                Label utente = new Label(conf.utenti[i]);
                GridPane.setHalignment(utente, HPos.CENTER);    //(13)
                ImageView pagato = new ImageView("square.png");
                GridPane.setHalignment(pagato, HPos.CENTER);
                Button paga = new Button("PAGA"); //aggiungere l'evento
                GridPane.setHalignment(paga, HPos.CENTER);
                grigliaPagamenti.setConstraints(utente, 0, i);  //(14)
                grigliaPagamenti.setConstraints(pagato, 1, i);
                grigliaPagamenti.setConstraints(paga, 2, i);
                grigliaPagamenti.getRowConstraints().add(new RowConstraints(conf.altezzaRigaGriglia)); //(15)
                grigliaPagamenti.getChildren().addAll(utente, pagato, paga);
                
                if(bollettaSelezionata == null)
                    paga.setDisable(true);    
                    
                paga.setOnAction(new HandlerPagamento(i));   //(16)
            }
            
            grigliaPagamenti.getColumnConstraints().add(new ColumnConstraints(conf.larghezzaColonnaGriglia));
            grigliaPagamenti.getColumnConstraints().add(new ColumnConstraints(conf.larghezzaColonnaGriglia));
            grigliaPagamenti.getColumnConstraints().add(new ColumnConstraints(conf.larghezzaColonnaGriglia));  
            
            confermaSel = new Button("CONFERMA");
            confermaSel.setDisable(true);
        }
        
        public void svuotaCampi(String sezione){
            if(sezione == "inserimento"){
                tipoBolletta.getSelectionModel().clearSelection();
                campoData.setText("");
                campoImporto.setText("");
            }
            else{
                bollettaSelezionata = null;
                campoTipoSel.setText("");         
                campoDataSel.setText("");        
                campoImportoSel.setText("");
                campoQuotaSel.setText("");
                for(int i = 0; i < conf.utenti.length; i++){
                    ImageView assente = new ImageView("square.png");
                    grigliaPagamenti.setConstraints(assente, 1, i);
                    grigliaPagamenti.getChildren().set(i*3 + 1, assente);
                    GridPane.setHalignment(assente, HPos.CENTER);
                    grigliaPagamenti.getChildren().get(i*3 + 2).setDisable(true);                          
                }
                confermaSel.setDisable(true);
            }
        }
        
        public void gestisciEventi(Stage stage){        
            confermaIns.setOnAction((ActionEvent event) -> {               
                String tipoIns = tipoBolletta.getValue().toString();      
                String dataIns = campoData.getText();   
                double importoIns = Double.parseDouble(campoImporto.getText());
                double quota = importoIns / conf.utenti.length;
                int idBollettaIns = DepositoDati.inserisciBolletta(tipoIns, dataIns, importoIns, quota, conf.utenti);
                Bolletta bollettaIns = new Bolletta(idBollettaIns, tipoIns, dataIns, importoIns, false, conf.utenti.length);
                if(idBollettaIns >= 0){
                    if(bolletteTipo.get(tipoIns).size() == conf.maxNumeroBollette){
                        bolletteTipo.get(tipoIns).remove(0);
                    }
                    bolletteTipo.get(tipoIns).add(bollettaIns); 
                }
                svuotaCampi("inserimento");
                LogXML inserimento = new LogXML("Inserimento Bolletta");
                inserimento.invia(conf.IPServer, conf.porta);       //(17)
            }); 
            
            confermaSel.setOnAction((ActionEvent event) ->{
                boolean pagataDaTutti = DepositoDati.modificaPagamenti(bollettaSelezionata.id, bollettaSelezionata, conf.utenti);
                svuotaCampi("selezione");         
                LogXML modifica = new LogXML("Modifica Bolletta");
                modifica.invia(conf.IPServer, conf.porta);
            });
            
            stage.setOnCloseRequest((WindowEvent event) -> chiudi());
        }
        
        public void settaStile(){       //(18)
         
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
            
            tipoBolletta.setStyle(conf.stileTextfield);
            campoData.setStyle(conf.stileTextfield);
            campoImporto.setStyle(conf.stileTextfield);
            
            confermaIns.setPrefHeight(24);
            confermaIns.setLayoutX(LARGHEZZA_FINESTRA/2 - confermaIns.getWidth()/2 - 3);
            confermaIns.setLayoutY(contenitoreDatiInserimento.getLayoutY() + contenitoreDatiInserimento.getPrefHeight() + 90);
            confermaIns.setStyle(conf.stileBottone);
            
            seleziona.setStyle(conf.stileLabel + "-fx-text-fill: #F63526;");
            seleziona.setPrefWidth(LARGHEZZA_FINESTRA);
            seleziona.setAlignment(Pos.CENTER);
            seleziona.setLayoutY(confermaIns.getLayoutY() + confermaIns.getPrefHeight() + 20);
            
            contenitoreDatiSelezione.setPadding(new Insets(40, 0, 40, 0));
            contenitoreDatiSelezione.setPrefWidth(LARGHEZZA_FINESTRA);
            contenitoreDatiSelezione.setAlignment(Pos.CENTER);
            contenitoreDatiSelezione.setLayoutY(seleziona.getLayoutY());
            
           
            campoTipoSel.setStyle(conf.stileTextfield);           
            campoDataSel.setStyle(conf.stileTextfield);            
            campoImportoSel.setStyle(conf.stileTextfield);
            campoQuotaSel.setStyle(conf.stileTextfield);
            
            grigliaPagamenti.setPrefWidth(LARGHEZZA_FINESTRA);
            grigliaPagamenti.setAlignment(Pos.CENTER);
            if(conf.utenti.length <= 4)
                grigliaPagamenti.setLayoutY(contenitoreDatiSelezione.getLayoutY() + contenitoreDatiSelezione.getHeight() + 60);
            else
                grigliaPagamenti.setLayoutY(contenitoreDatiSelezione.getLayoutY() + contenitoreDatiSelezione.getHeight() + 40);
            
            confermaSel.setPrefHeight(24);
            confermaSel.setLayoutX(LARGHEZZA_FINESTRA/2 - confermaSel.getWidth()/2 - 3);
            if(conf.utenti.length <= 4)
                confermaSel.setLayoutY(grigliaPagamenti.getLayoutY() + (conf.utenti.length * conf.altezzaRigaGriglia) + 20);
            else
                confermaSel.setLayoutY(grigliaPagamenti.getLayoutY() + (conf.utenti.length * conf.altezzaRigaGriglia));
            confermaSel.setStyle(conf.stileBottone);
        }
        
        public void ripristinaConCache(List<String> datiCache){     //(19)
            if(!datiCache.isEmpty()){
                tipoBolletta.setValue(datiCache.get(0));        
                campoData.setText(datiCache.get(1));
                campoImporto.setText(datiCache.get(2));
                if(datiCache.size() > 3){
                    
                    int numeroVecchiUtenti = Integer.parseInt(datiCache.get(9));
                    if(numeroVecchiUtenti != conf.utenti.length)
                        return;
                    for(int i = 10; i < conf.utenti.length + 10; i++){
                        if(!datiCache.get(i).equals(conf.utenti[i - 10]))
                            return;
                    }
                    
                    confermaSel.setDisable(false);
                    campoTipoSel.setText(datiCache.get(4));
                    campoDataSel.setText(datiCache.get(5));
                    campoImportoSel.setText(datiCache.get(6));
                    campoQuotaSel.setText(datiCache.get(7));
                    bollettaSelezionata = new Bolletta(Integer.parseInt(datiCache.get(3)), datiCache.get(4), datiCache.get(5), Double.parseDouble(datiCache.get(6)), (Integer.parseInt(datiCache.get(8)) == 1), conf.utenti.length);                                    
                    
                    for(int i = 10 + conf.utenti.length; i < 10 + conf.utenti.length*2; i++){        //(20)
                        int j = i - (10 + conf.utenti.length);
                        switch(datiCache.get(i)){
                            case "PAGATA":                               
                                ImageView pagata = new ImageView("tick.png");
                                grigliaPagamenti.setConstraints(pagata, 1, j);
                                grigliaPagamenti.getChildren().set(j*3 + 1, pagata);
                                GridPane.setHalignment(pagata, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[j] = Bolletta.StatoPagamento.PAGATA;
                                grigliaPagamenti.getChildren().get(j*3 + 2).setDisable(false);
                                break;
                            case "NON_PAGATA":                               
                                ImageView nonPagata = new ImageView("cross.png");
                                grigliaPagamenti.setConstraints(nonPagata, 1, j);
                                grigliaPagamenti.getChildren().set(j*3 + 1, nonPagata);
                                GridPane.setHalignment(nonPagata, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[j] = Bolletta.StatoPagamento.NON_PAGATA;
                                grigliaPagamenti.getChildren().get(j*3 + 2).setDisable(false);
                                break;
                            case "ASSENTE":                               
                                ImageView assente = new ImageView("square.png");
                                grigliaPagamenti.setConstraints(assente, 1, j);
                                grigliaPagamenti.getChildren().set(j*3 + 1, assente);
                                GridPane.setHalignment(assente, HPos.CENTER);
                                bollettaSelezionata.pagamentoUtenti[j] = Bolletta.StatoPagamento.ASSENTE;
                                grigliaPagamenti.getChildren().get(j*3 + 2).setDisable(true);
                                break;
                        }                       
                    }
                } 
            }
        }
        
        public void chiudi(){       //(21)
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
                datiCache.add(Integer.toString(conf.utenti.length));
                for(int i = 0; i < conf.utenti.length; i++)
                    datiCache.add(conf.utenti[i]);
                for(int i = 0; i < conf.utenti.length; i++)
                    datiCache.add(bollettaSelezionata.pagamentoUtenti[i].toString());
            }
            
            FormCache salvataggio = new FormCache(datiCache);
            salvataggio.salvaCache();
            
            LogXML chiusura = new LogXML("Chiusura applicazione");
            chiusura.invia(conf.IPServer, conf.porta);
        }
	
        
        @Override
	public void start(Stage stage){     //(22)
            conf = new ConfigurazioneXML();
            
            XStream xs = new XStream();     //(23)
            xs.alias("config", ConfigurazioneXML.class);    //(24)
            xs.alias("bolletta", BollettaConfig.class);
            xs.useAttributeFor(BollettaConfig.class, "colore");     //(25)
            xs.alias("nome", String.class);
            
            conf = (ConfigurazioneXML) xs.fromXML(new File("configurazione.xml"));      //(26)
            
            bolletteTipo = new HashMap<String, ObservableList<Bolletta>>(conf.bollette.length);     //(27)
            
            creaStrutture();
                     
            //(28)
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
            
            LogXML avvio = new LogXML("Avvio applicazione");
            avvio.invia(conf.IPServer, conf.porta);
	}
}

/*
(01):
Componenti grafici dell'interfaccia.
https://docs.oracle.com/javafx/2/ui_controls/jfxpub-ui_controls.htm

(02):
UpdateItem viene richiamato ogni volta che cambia il contenuto di una cella

(03):
Al click su una cella della ListView ricavo i dati della bolletta corrispondente 
e li stampo nei rispettivi campi della sezione bolletta selezionata.
Evito che il metodo prosegua al click di una lista vuota.
https://docs.oracle.com/javase/8/docs/api/java/awt/event/MouseEvent.html

(04):
Il metodo settaPagamenti visualizza nella griglia i corretti pagamenti associati
agli utenti e inizializza il campo pagamentoUtenti della Bolletta bollettaSelezionata.

(05):
Il metodo setConstraints serve per settare i vincoli di layout di una cella 
della GridPane.

(06):
Il metodo creaStrutture() crea tutti gli oggetti che saranno presenti 
nell' interfaccia grafica.

(07):
Costruttore di oggetto HBox il cui argomento indica lo spacing che intercorre
tra gli elementi contenuti nell'HBox.
In seguito aggiungerò all'oggetto HBox tutti gli oggetti VBox che a loro volta conterrano 
una Label che indica il tipo di bollette e una ListView<Bolletta> caratterizzata
dai dati delle bollette di quel tipo.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/HBox.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/VBox.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html



(08):
Il metodo setAlignment(Pos.CENTER) consente all'oggetto cui è applicato 
di posizionarsi al centro rispetto allo spazio occupato dall'oggetto stesso nella
visualizzazione.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Labeled.html#setAlignment-javafx.geometry.Pos-

(09):
ListView

(10):
Il metodo setCellFactory serve per settare una nuova formattazione
della cella da usare nella ListView.

(11):
Modificando l' ObservableList<> modifico le possibili scelte che 
appariranno nella ChoiceBox.
https://docs.oracle.com/javase/8/javafx/api/javafx/collections/ObservableList.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ChoiceBox.html


(12):
L'oggetto GridPane consente di formattare una porzione dell'interfaccia grafica 
sotto forma di griglia.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html

(13):
Il metodo setHalignment(utente, HPos.CENTER) consente di posizionare l'oggetto
passato come primo argomento centrato orizzontalmente all'intero di una cella
della GridPane.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html#setHalignment-javafx.scene.Node-javafx.geometry.HPos-

(14):
Il metodo setConstraints(utente, j, i) consente di settare i vincoli di layout 
della cella di colonna j e riga i con l'oggetto utente quando utente sarà
aggiunto nella GridPane.
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/layout/GridPane.html#setConstraints-javafx.scene.Node-int-int-

(15):
I metodi getRowConstraints() e getColumnConstraints() 
mi consentono di settare l'altezza di una riga della cella e l'ampiezza di una 
colonna.

(16):
paga

(17):
Il metodo invia, serve per inviare al server la riga di log, che verrà poi inserita 
da quest' ultimo nel file incrementale gestorebollette.xml

(18):
Il metodo settaStile() consente di settare lo stile dei vari oggetti istanziati 
dal metodo creaStrutture().
https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html

(19):
Il metodo ripristinaConCache() serve per visualizzare nell'interfaccia grafica 
i dati presenti antecedentemente all'ultima chiusura dell'applicazione.

(20):
Inizio il ciclo dall'indice i = 9 poiché sono 9 i dati presenti in datiCache prima 
di trovare gli stati di pagamento che verranno scorsi e catturati con una get(i).

(21):
Il metodo chiudi() serve per inserire i dati necessari nella cache alla 
chiusura dell'applicazione.

(22):
Il metodo start() viene chiamato dopo il metodo init che inizializza la app.



(23):
XStream è un flusso in cui possono viaggiare oggetti XML e che offre metodi 
per la serializzazione e la deserializzazione.
https://x-stream.github.io/javadoc/index.html

(24):
Il metodo alias("config", ConfigurazioneXML.class) genera un alias per la seconda 
classe passata come argomento; facendo si che l'elemento <ConfigurazioneXML> per 
esempio venga visto come <config>.

(25):
Il metodo useAttributeFor(BollettaConfig.class, "colore") fa si che il campo
colore presente nella classe BollettaConfig venga visto come un attributo.

(26):
Il metodo fromXML(new File("configurazione.xml")) deserializza il file da XML a
Java.

(27):
HashMap è una sorta di array associativo in Java.
https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html

(28):
Aggiungo gli elementi all'interfaccia tramite l'uso del Group(contenitore di oggetti osservabili)
della Scene (contenitore principale della finestra) e dello Stage(finestra vera e propria).
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Group.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Scene.html
https://docs.oracle.com/javase/8/javafx/api/javafx/scene/Stage.html
*/