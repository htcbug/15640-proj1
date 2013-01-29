package cmu.edu.ds.a1.PM;

import java.util.ArrayList;

import cmu.edu.ds.a1.IF.MigratableProcess;

public class Processes extends ArrayList<ThreadWrapper<MigratableProcess>> {

  private static final long serialVersionUID = -7664995909224665317L;

  @Override
  public int size() {
    for (int i = 0; i < super.size(); i++) {
      if (!get(i).isAlive()) {
        System.out.println("Process \"" + get(i).getTarget().toString() + "\" was terminated");
        remove(i);
      }
    }
    return super.size();
  }

}
