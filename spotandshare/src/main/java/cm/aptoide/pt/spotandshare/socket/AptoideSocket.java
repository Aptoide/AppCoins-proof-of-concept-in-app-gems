package cm.aptoide.pt.spotandshare.socket;

import cm.aptoide.pt.spotandshare.socket.interfaces.OnError;
import cm.aptoide.pt.spotandshare.socket.interfaces.ProgressAccumulator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AptoideSocket {

  protected final ExecutorService executorService;
  protected final int bufferSize;
  protected OnError<IOException> onError = Throwable::printStackTrace;

  public AptoideSocket() {
    this(Executors.newCachedThreadPool(), 8192);
  }

  public AptoideSocket(ExecutorService executorService, int bufferSize) {
    this.executorService = executorService;
    this.bufferSize = bufferSize;
  }

  public AptoideSocket(int bufferSize) {
    this(Executors.newCachedThreadPool(), bufferSize);
  }

  public AptoideSocket(ExecutorService executorService) {
    this(executorService, 8192);
  }

  public AptoideSocket startAsync() {
    new Thread(this::start).start();
    System.out.println("ShareApps: Started " + getClass().getSimpleName() + " AptoideSocket.");
    return this;
  }

  public abstract AptoideSocket start();

  public void shutdown() {
    onError = null;
    executorService.shutdown();
  }
  
  public void shutdownExecutorService() {
    executorService.shutdown();
  }

  public void forceShutdownExecutorService() {
    executorService.shutdownNow();
  }

  protected void copy(InputStream in, OutputStream out, ProgressAccumulator progressAccumulator)
      throws IOException {
    copy(in, out, Long.MAX_VALUE, progressAccumulator);
  }

  protected void copy(InputStream in, OutputStream out, long len,
      ProgressAccumulator progressAccumulator) throws IOException {
    byte[] buf = new byte[bufferSize];

    int totalBytesRead = 0;
    int bytesRead;
    while ((totalBytesRead) != len
        && (bytesRead = in.read(buf, 0, (int) Math.min(buf.length, len - totalBytesRead))) != -1) {
      out.write(buf, 0, bytesRead);
      if (progressAccumulator != null) {
        progressAccumulator.addProgress(bytesRead);
      }
      totalBytesRead += bytesRead;
    }
  }

  public void shutdown(Runnable onDisconnect) {
    shutdown();

    try {
      executorService.awaitTermination(5, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    onDisconnect.run();
  }
}