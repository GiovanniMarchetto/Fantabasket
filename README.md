# Fantabasket

Applicazione android per fantabasket

## Struttura del gioco

Ogni fanta-allenatore può creare un suo team e unirsi a diversi tornei o crearne uno proprio.

Ogni giornata della stagione reale corrisponde a una giornata di torneo.

Si possono scegliere dei giocatori reali e schierare la formazione per la giornata prima che inizi la prima partita (del
campionato reale).

A giornata conclusa, l'amministratore potrà aggiornare la classifica. I punteggi dei giocatori vengono calcolati in base
alle loro statistiche di gioco. Inoltre, viene applicato un fattore in base alla posizione in cui è stato schierato
(più è vicino al campo maggiore è il rendimento).

Esempio: se prendo un giocatore che grazie alle sue statistiche ha un voto di 40, darà un contributo rispettivamente di:

- 40 punti se schierato in campo
- 30 punti se schierato panchina prima fascia
- 20 punti se schierato panchina seconda fascia
- 10 punti se schierato panchina terza fascia

La somma di tutti i punteggi dei giocatori nella mia formazione fanno il punteggio totale segnato dalla mia squadra.

Ci sono due tipologie di torneo:

- _CALENDARIO_: cioè calendario all'italiana. Come nel basket il vincitore prende due punti vittoria
  (a parità di punteggio vince la squadra di casa che prende 1 punto per il fattore campo). Per la classifica si
  valutano in ordine:
    - maggiori punti vittoria
    - maggiori punti segnati
    - minor punti subiti
- _FORMULA1_: chi segna più punti vince. Non c'è nessun altro parametro.

Quando il torneo avrà un numero adeguato di partecipanti l'amministratore potrà avviare il torneo. Per il calendario il
torneo deve essere pieno. Per la formula1 basta che siano almeno due persone per avviare la competizione.

**NB: a competizione avviata non si possono aggiungere partecipanti.**

**NB: non si può uscire dalle leghe**

## Struttura applicazione

### Access Activity

La prima attività riguarda la fase di accesso: possiamo decidere se fare il login, il che implica avere già un account
sulla piattaforma, oppure registrare un nuovo utente.

In alto c'è l'apposito pulsante per decidere l'opzione. Bisogna inserire email e password per entrambe le operazioni.

#### Login

Possiamo resettare la password se ce la siamo dimenticata.

Per farlo dobbiamo immettere la mail e premere su resetta password.

Ci verrà inviata una mail con un link per cambiare la password.

#### Registrazione

Dobbiamo immettere:

- nickname, il nome che vogliamo che abbia il nostro fanta allenatore
- teamName, il nome della nostra squadra
- teamLogo, il logo della nostra squadra

Dove aver immesso tutte le informazioni si può registrare il nuovo utente.

### Main Activity

#### Home

In questa schermata possiamo vedere il resoconto delle nostre statistiche generali.

Se abbiamo selezionato una lega di tipo formula1 vedremo di diverso solamente la posizione in classifica e i punti nella
lega.

Se abbiamo selezionato una lega di tipo calendario vedremo oltre alle info di classifica e punti vittoria, anche il
risultato dell'ultima partita e l'anteprima della prossima partita.

#### Lineup

In questa schermata possiamo immettere la formazione in base al roster di giocatori in nostro possesso. Il tempo
rimanente per inserire la formazione per la prossima giornata è indicato in alto. La posizione in formazione determina
il fattore con cui viene calcolato il punteggio.

Per salvare la formazione per questa giornata si deve premere il tasto apposito. Possiamo anche resettare l'intera
formazione.

Per cambiare il roster devo premere l'apposito bottone.

##### Roster Manager

In questo fragment possiamo cambiare il roster a nostra disposizione in base al numero di fantamilioni a nostra
disposizione (mostrato in alto). Per salvare il roster dobbiamo avere un minimo di 12 giocatori (max 16). Il budget per
il roster è di 200 fantamilioni.

#### Leaderboard

In questa sezione possiamo vedere l'attuale classifica. Se è una lega di tipo calendario, premendo sulla sezione
calendario, si aprirà il calendario completo.

Se siamo gli amministratori ed è passata una o più giornate (e non sono ancora state calcolate)
si può procedere al calcolo attraverso il bottone apposito.

#### Profile

In questa sezione si possono vedere le informazioni sulla lega selezionata e nel caso cambiare lega.

Si possono modificare le informazioni riguardanti la squadra, il nickname e il logo.

Si può chiedere l'invio di una mail per resettare la password.

Infine ci si può disconnettere, tornando alla schermata di accesso.

### Settings Activity

In questa attività si può gestire il tema selezionato.

### League Activity

In questa attività si può:

- scegliere la lega da selezionare
- unirsi ad una lega
- creare una nuova lega

Se si ha dato il consenso e si ha la localizzazione attiva si vedranno le leghe a cui ci si può unire in ordine di
vicinanza.

### No Connection Activity

Questa attività si attiva nel caso di mancanza di connessione internet (attraverso un opportuno Broadcast receiver).
