package cmu.edu.ds.a1.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable {

  private static final long serialVersionUID = 8525596152925121720L;

  private long offset;

  private String filename;

  public TransactionalFileInputStream(String filename) {
    this.filename = filename;
    this.offset = 0L;
  }

  @Override
  public int read() throws IOException {
    RandomAccessFile raf = new RandomAccessFile(filename, "rws");
    raf.seek(offset++);
    int nextByte = raf.read();
    raf.close();
    return nextByte;
  }
}
