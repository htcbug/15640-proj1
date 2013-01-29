package cmu.edu.ds.a1.PM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import cmu.edu.ds.a1.IF.MigratableProcess;
import cmu.edu.ds.a1.Thread.ClientThread;
import cmu.edu.ds.a1.Thread.MasterThread;

public class ProcessManager {
  private static int serverPort;

  private static String hostAddr;

  private PMRole role;

  private static final String mpPrefix = "cmu.edu.ds.a1.MP.";

  private volatile Processes processes;

  public enum PMRole {
    MASTER, SLAVE
  }

  public ProcessManager(PMRole role) throws IOException, InterruptedException {
    this.processes = new Processes();
    this.role = role;
    if (this.role == PMRole.MASTER) {
      System.out.println("Entering master mode...");
      Thread mt = new Thread(new MasterThread(serverPort, processes));
      mt.start();
    } else if (this.role == PMRole.SLAVE) {
      System.out.println("Entering slave mode...");
      Thread ct = new Thread(new ClientThread(hostAddr, serverPort, processes));
      ct.start();
    }

    String curLine = ""; // Line read from standard in
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    while (!(curLine.equals("quit"))) {
      System.out.print("==> ");
      curLine = in.readLine();
      if (curLine.equals("ps")) {
        if (processes.size() == 0)
          System.out.println("no running processes");
        for (int i = 0; i < processes.size(); i ++) {
          System.out.println(processes.get(i).getTarget().toString());
        }
        continue;
      }
      
      if (!(curLine.equals("quit"))) {
        String[] inputs = curLine.split(" ");
        MigratableProcess mp = invokeProcess(inputs);
        if (mp == null)
          continue;
        ThreadWrapper<MigratableProcess> mpt = new ThreadWrapper<MigratableProcess>(mp);
        synchronized (this) {
          mpt.start();
          processes.add(mpt);
        }
      }
    }
  }

  private MigratableProcess invokeProcess(String[] inputs) {
    String processName = inputs[0];
    Object[] processArgs = { Arrays.copyOfRange(inputs, 1, inputs.length) };
    try {
      Class<?> cl = Class.forName(mpPrefix + processName);
      Constructor<?> constructor = cl.getConstructor(String[].class);
      MigratableProcess mp = (MigratableProcess) constructor.newInstance(processArgs);
      return mp;
    } catch (ClassNotFoundException e) {
      System.out.println("ClassNotFoundException");
    } catch (InstantiationException e) {
      System.out.println("InstantiationException");
    } catch (IllegalAccessException e) {
      System.out.println("IllegalAccessException");
    } catch (IllegalArgumentException e) {
      System.out.println("IllegalArgumentException");
    } catch (InvocationTargetException e) {
      System.out.println("InvocationTargetException");
    } catch (SecurityException e) {
      System.out.println("SecurityException");
    } catch (NoSuchMethodException e) {
      System.out.println("NoSuchMethodException");
    }
    return null;
  }

  /**
   * @param args
   * @throws IOException
   * @throws InterruptedException
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    if (!cmdParser(args)) {
      System.out.println("Usage: ProcessManager -c <hostname> -p <serverPort>");
      System.exit(0);
    }
    new ProcessManager(args.length == 4 ? PMRole.SLAVE : PMRole.MASTER);
    System.exit(1);
  }

  private static boolean cmdParser(String[] args) {
    if (args.length != 2 && args.length != 4)
      return false;
    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-p")) {
          serverPort = Integer.parseInt(args[++i]);
        } else if (args[i].equals("-c")) {
          hostAddr = args[++i];
        } else {
          return false;
        }
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}
