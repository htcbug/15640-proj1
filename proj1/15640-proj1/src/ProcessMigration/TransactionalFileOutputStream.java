package ProcessMigration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 378282270137012604L;

  // Yang: What the hell is the boolean arg?!
  public TransactionalFileOutputStream(String string, boolean b) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void write(int tarByte) throws IOException {
    // TODO Auto-generated method stub

  }

}
