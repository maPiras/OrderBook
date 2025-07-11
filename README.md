# Implementazione di un order book in Java

## Descrizione del progetto

Il progetto rappresenta la consegna del terzo appello dell'esame di laboratorio III.
Fornisce l'implementazione di un orderbook in Java, sfruttando multithreading e network programming.

### Struttura

```text
├── Client
│   ├── MainClient.java
│   ├── NotificationReceiver.java
│   ├── RequestMaker.java
│   └── ResponseManager.java
├── Misc
│   ├── client.properties
│   ├── gson-2.11.0.jar
│   ├── MANIFEST_CLIENT.MF
│   ├── MANIFEST_SERVER.MF
│   ├── server.properties
│   └── storicoOrdini.json
├── Server
│   ├── ClientHandler.java
│   ├── MainServer.java
│   ├── NotificationSender.java
│   ├── OrderBook.java
│   ├── OrderManager.java
│   ├── RequestManager.java
│   ├── ResponseMaker.java
│   ├── User.java
│   └── UserManager.java
└── Shared
    ├── MessageSerializator.java
    ├── NotifyPort.java
    ├── OperationResponse.java
    ├── Order.java
    ├── OrderResponse.java
    ├── Pair.java
    ├── Request.java
    └── Tuple.java
  ```

## Building

Tutti i comandi vanno eseguiti all'interno della cartella del progetto  

1. Creazione delle directory di output  
mkdir -p out/client out/server  
2. Compilazione  
Client: javac -cp Misc/gson-2.11.0.jar -d out/client Client/*.java Shared/*.java  
Server: javac -cp Misc/gson-2.11.0.jar -d out/server Server/*.java Shared/*.java  
3. Creazione dei file JAR  
Client: jar cfm ClientApp.jar Misc/MANIFEST_CLIENT.MF -C out/client .  
Server: jar cfm ServerApp.jar Misc/MANIFEST_SERVER.MF -C out/server .  
4. Esecuzione  
Client: java -cp "ClientApp.jar:Misc/gson-2.11.0.jar" Client.MainClient  
Server: java -cp "ServerApp.jar:Misc/gson-2.11.0.jar" Server.MainServer  
