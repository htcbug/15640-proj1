package cmu.edu.ds.a1.PM;

import java.util.ArrayList;

import cmu.edu.ds.a1.Thread.M2CThread;

public class Clients extends ArrayList<M2CThread> {

  private static final long serialVersionUID = -2199238192592655497L;

  public int getLoadLevel(int masterLevel) {
    int totalLevel = masterLevel;
    for (int i = 0; i < super.size(); i++) {
      if (!get(i).isAlive())
        remove(get(i));
      else
        totalLevel += get(i).getNumProcesses();
    }
    return (int) Math.ceil(totalLevel / (size() + 1.0));
  }
}
