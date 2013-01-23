package ProcessMigration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 8525596152925121720L;

  private int offset;

  private FileInputStream tarInputStream;

  public TransactionalFileInputStream(String filename) throws FileNotFoundException {
    this.offset = 0;
    try {
      this.tarInputStream = new FileInputStream(filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public int read() throws IOException {
    byte[] buf = new byte[1];
    int len = this.tarInputStream.read(buf, this.offset++, 1);
    return len >= 0 ? buf[0] : -1;
  }

  @Override
  public void close() throws IOException {
    this.tarInputStream.close();
  }

}
