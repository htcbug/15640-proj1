package cmu.edu.ds.a1.Thread;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class M2CThread implements Runnable {
  public enum ServerAction {
    FETCHSTATS, FETCHOBJ, OFFEROBJ
  }

  private Socket sock;

  private volatile ServerAction action;

  private volatile int numProcesses;

  private volatile boolean isAlive;

  private volatile int fetchNum;

  private volatile ArrayList<String> stocks;

  public M2CThread(Socket sock) {
    this.sock = sock;
    this.stocks = new ArrayList<String>();
    this.setNumProcesses(0);
    this.setAlive(true);
  }

  @Override
  public void run() {
    synchronized (this) {
      if (this.action == null) {
        System.out.println("action is null!");
        System.exit(0);
      }
      switch (action) {
        case FETCHSTATS:
          fetchStats();
          break;
        case FETCHOBJ:
          fetchObj();
          break;
        case OFFEROBJ:
          offerObj();
          break;
        default:
          System.out.println("unsupport");
          break;
      }
    }
  }

  private synchronized void offerObj() {
    try {
      DataOutputStream out = new DataOutputStream(sock.getOutputStream());
      out.writeBytes("OFFEROBJ " + stocks.size() + "\n");

      for (int i = 0; i <= stocks.size(); i++) {
        out.writeBytes(stocks.get(i) + "\n");
        stocks.remove(i);
      }
    } catch (IOException e) {
      System.out.println("IOException");
      setAlive(false);
    }
  }

  private synchronized void fetchObj() {
    try {
      DataOutputStream out = new DataOutputStream(sock.getOutputStream());
      out.writeBytes("FETCHOBJ " + getFetchNum() + "\n");
      for (int i = 0; i < getFetchNum(); i++) {
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        String msg = in.readLine();
        stocks.add(msg);
      }
    } catch (IOException e) {
      System.out.println("IOException");
      setAlive(false);
    }
  }

  private synchronized void fetchStats() {
    try {
      DataOutputStream out = new DataOutputStream(sock.getOutputStream());
      out.writeBytes("FETCHSTATS\n");
      BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      String[] msg = in.readLine().split(" ");
      if (msg[0].equals("PROCESSES"))
        setNumProcesses(Integer.parseInt(msg[1]));
    } catch (IOException e) {
      System.out.println("IOException");
      setAlive(false);
    }
  }

  protected synchronized void switchAction(final ServerAction action) {
    this.action = action;
  }

  protected synchronized Socket getSock() {
    return sock;
  }

  public synchronized int getNumProcesses() {
    return numProcesses;
  }

  public synchronized void setNumProcesses(int numProcesses) {
    this.numProcesses = numProcesses;
  }

  public synchronized boolean isAlive() {
    return isAlive;
  }

  public synchronized void setAlive(boolean isAlive) {
    this.isAlive = isAlive;
  }

  public synchronized int getFetchNum() {
    return fetchNum;
  }

  public synchronized void setFetchNum(int fetchNum) {
    this.fetchNum = fetchNum;
  }

  public synchronized ArrayList<String> getStocks() {
    return stocks;
  }
}
