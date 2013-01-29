package cmu.edu.ds.a1.Thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import cmu.edu.ds.a1.IF.MigratableProcess;
import cmu.edu.ds.a1.PM.Clients;
import cmu.edu.ds.a1.PM.Processes;
import cmu.edu.ds.a1.PM.ThreadWrapper;
import cmu.edu.ds.a1.Thread.M2CThread.ServerAction;

public class QueryStatsThread implements Runnable {
  private Clients clients;

  private Processes processes;

  private volatile ArrayList<String> stocks;

  public QueryStatsThread(Clients clients, Processes processes) throws IOException {
    this.clients = clients;
    this.processes = processes;
    this.stocks = new ArrayList<String>();
  }

  @Override
  public void run() {
    try {
      for (;;) {
        ArrayList<Thread> runningThreads = new ArrayList<Thread>();
        // System.out.println("fetching stats");
        for (M2CThread client : clients) {
          client.switchAction(ServerAction.FETCHSTATS);
          Thread newT = new Thread(client);
          runningThreads.add(newT);
          newT.start();
        }

        waitForRunningThread(runningThreads);
        runningThreads.clear();

        int curLoadLevel = clients.getLoadLevel(processes.size());
//        System.out.println("Current Load Level is " + curLoadLevel);

        if (processes.size() > curLoadLevel) {
          int numOuts = processes.size() - curLoadLevel;
          for (int i = 1; i <= numOuts; i++) {
            ThreadWrapper<MigratableProcess> mpt = processes.get(processes.size() - i);
            mpt.getTarget().suspend();
          }

          for (int i = 1; i <= numOuts; i++) {
            ThreadWrapper<MigratableProcess> mpt = processes.get(processes.size() - i);
            mpt.join();
            File curDir = new File(getClass().getResource("").getPath());
            String tmpFileName = curDir.getAbsolutePath() + File.separator
                    + new BigInteger(130, new SecureRandom()).toString(32) + ".dat";
            ObjectOutput s = new ObjectOutputStream(new FileOutputStream(tmpFileName));
            s.writeObject(mpt.getTarget());
            s.flush();
            processes.size(); // this will implicitly remove dead process
            System.out.println(tmpFileName);
            stocks.add(tmpFileName);
          }
        }

        for (M2CThread client : clients) {
          if (client.getNumProcesses() > curLoadLevel) {
            client.switchAction(ServerAction.FETCHOBJ);
            client.setFetchNum(client.getNumProcesses() - curLoadLevel);
            Thread newT = new Thread(client);
            runningThreads.add(newT);
            newT.start();
          }
        }

        waitForRunningThread(runningThreads);
        runningThreads.clear();

        for (M2CThread client : clients) {
          stocks.addAll(client.getStocks());
          client.getStocks().clear();
        }

        if (processes.size() < curLoadLevel && stocks.size() > 0) {
          for (int i = 0; i < curLoadLevel - processes.size(); i++) {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(stocks.get(i)));
            MigratableProcess mp = (MigratableProcess) in.readObject();
            ThreadWrapper<MigratableProcess> mpt = new ThreadWrapper<MigratableProcess>(mp);
            mpt.start();
            System.out.println("Resuming " + mp.toString());
            synchronized (this) {
              processes.add(mpt);
            }
            stocks.remove(i);
          }
        }

        if (stocks.size() > 0) {
          for (M2CThread client : clients) {
            if (client.getNumProcesses() < curLoadLevel) {
              ArrayList<String> offerItems = prepareOfferItems(curLoadLevel
                      - client.getNumProcesses());
              client.switchAction(ServerAction.OFFEROBJ);
              client.getStocks().addAll(offerItems);
              Thread newT = new Thread(client);
              runningThreads.add(newT);
              newT.start();
            }
          }

          waitForRunningThread(runningThreads);
          runningThreads.clear();

          stocks.clear();
        }

        // use thread join to wait all complete.
        Thread.sleep(5000);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

  }

  private ArrayList<String> prepareOfferItems(int num) {
    ArrayList<String> offerItems = new ArrayList<String>();
    for (int i = 0; i < Math.min(num, stocks.size()); i++) {
      offerItems.add(stocks.get(i));
    }
    return offerItems;
  }

  private void waitForRunningThread(ArrayList<Thread> runningThreads) throws InterruptedException {
    for (Thread t : runningThreads)
      t.join();
  }
}
