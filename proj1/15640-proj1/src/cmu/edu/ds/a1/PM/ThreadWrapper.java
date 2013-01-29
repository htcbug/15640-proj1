package cmu.edu.ds.a1.PM;

import cmu.edu.ds.a1.IF.MigratableProcess;

public class ThreadWrapper<T extends MigratableProcess> extends Thread {

  private T target;

  public ThreadWrapper(T target) {
    super(target);
    this.setTarget(target);
  }

  public T getTarget() {
    return target;
  }

  public void setTarget(T target) {
    this.target = target;
  }

}
