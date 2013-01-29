package cmu.edu.ds.a1.Thread;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import cmu.edu.ds.a1.IF.MigratableProcess;
import cmu.edu.ds.a1.PM.Processes;
import cmu.edu.ds.a1.PM.ThreadWrapper;

public class ClientThread implements Runnable {

  private int serverPort;

  private String hostAddr;

  private Processes processes;

  public ClientThread(String hostAddr, int serverPort, Processes processes) {
    this.serverPort = serverPort;
    this.hostAddr = hostAddr;
    this.processes = processes;
  }

  @Override
  public void run() {
    try {
      Socket client = new Socket(hostAddr, serverPort);
      DataOutputStream out = new DataOutputStream(client.getOutputStream());
      out.writeBytes("REGISTER\n");
      for (;;) {
        processIncomingMsg(out, client);
      }
    } catch (UnknownHostException e) {
      System.out.println("Unknown host, exit...");
      System.exit(0);
    } catch (IOException e) {
      System.out.println("Connection refused, exit...");
      System.exit(0);
    } catch (InterruptedException e) {
      System.out.println("InterruptedException, exit...");
      System.exit(0);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void processIncomingMsg(DataOutputStream out, Socket client) throws IOException,
          InterruptedException, ClassNotFoundException {
    BufferedReader bufferIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
    String msg = bufferIn.readLine();
    if (msg.equals("FETCHSTATS")) {
      out.writeBytes("PROCESSES " + processes.size() + "\n");
    } else if (msg.startsWith("FETCHOBJ")) {
      int numMoves = Integer.parseInt(msg.split(" ")[1]);
      for (int i = 1; i <= numMoves; i++) {
        ThreadWrapper<MigratableProcess> mpt = processes.get(processes.size() - i);
        mpt.getTarget().suspend();
      }

      for (int i = 1; i <= numMoves; i++) {
        ThreadWrapper<MigratableProcess> mpt = processes.get(processes.size() - i);
        mpt.join();
        File curDir = new File(getClass().getResource("").getPath());
        String tmpFileName = curDir.getAbsolutePath() + File.separator
                + new BigInteger(130, new SecureRandom()).toString(32) + ".dat";
        ObjectOutput s = new ObjectOutputStream(new FileOutputStream(tmpFileName));
        s.writeObject(mpt.getTarget());
        s.flush();
        processes.size(); // this will implicitly remove dead process
        out.writeBytes(tmpFileName + "\n");
      }
    } else if (msg.startsWith("OFFEROBJ")) {
      int numIns = Integer.parseInt(msg.split(" ")[1]);
      for (int i = 0; i < numIns; i++) {
        String fileName = new BufferedReader(new InputStreamReader(client.getInputStream()))
                .readLine();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        MigratableProcess mp = (MigratableProcess) in.readObject();
        ThreadWrapper<MigratableProcess> mpt = new ThreadWrapper<MigratableProcess>(mp);
        mpt.start();
        System.out.println("Resuming " + mp.toString());
        synchronized (this) {
          processes.add(mpt);
        }
      }
    }
  }
}
