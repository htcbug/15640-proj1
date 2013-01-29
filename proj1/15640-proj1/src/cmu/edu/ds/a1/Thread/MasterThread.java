package cmu.edu.ds.a1.Thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import cmu.edu.ds.a1.PM.Clients;
import cmu.edu.ds.a1.PM.Processes;

public class MasterThread implements Runnable {
  private int serverPort;

  private volatile Clients clients;

  private Processes processes;

  public MasterThread(int serverPort, Processes processes) {
    this.serverPort = serverPort;
    this.clients = new Clients();
    this.processes = processes;
  }

  @Override
  public void run() {
    try {
      ServerSocket sock = new ServerSocket(serverPort);
      Thread qst = new Thread(new QueryStatsThread(clients, processes));
      qst.start();
      for (;;) {
        Socket connSock = sock.accept();
        // System.out.println("received new incoming message");
        // TODO: deal with incoming request
        BufferedReader in = new BufferedReader(new InputStreamReader(connSock.getInputStream()));
        if (in.readLine().equals("REGISTER")) {
          synchronized (this) {
            clients.add(new M2CThread(connSock));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
