package io.github.bluelhf.dapper;

import io.github.bluelhf.dapper.exception.DapperException;
import javazoom.jl.decoder.*;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

public class DapperPlayer implements AutoCloseable {
    private final Bitstream bitstream;
    private final Decoder decoder;
    private AudioDevice audio;
    private boolean complete;

    private Runnable onComplete;

    private boolean paused;

    public DapperPlayer(File file) throws FileNotFoundException {
        this(new BufferedInputStream(new FileInputStream(file)), null);
    }

    public DapperPlayer(InputStream stream) {
        this(stream, null);
    }

    public DapperPlayer(InputStream stream, AudioDevice device) {
        try {
            this.complete = false;
            this.bitstream = new Bitstream(stream);
            this.decoder = new Decoder();
            if (device != null) {
                this.audio = device;
            } else {
                FactoryRegistry r = FactoryRegistry.systemRegistry();

                this.audio = r.createAudioDevice();

            }
            this.audio.open(this.decoder);

        } catch (Exception e) {
            throw new DapperException(e);
        }
    }

    public DapperPlayer setOnComplete(Runnable r) {
        onComplete = r;
        return this;
    }

    public CompletableFuture<Void> play() {
        return CompletableFuture.runAsync(() -> {
            while (decodeFrame()) {
                while (paused) {
                    LockSupport.parkNanos(1000000L);
                }
            }
        }).thenRun(() -> {
            AudioDevice out = this.audio;
            if (out != null) {
                out.flush();
                synchronized (this) {
                    this.complete = true;
                    this.close();
                }
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean togglePause() {
        return this.paused = !this.paused;
    }

    @Override
    public synchronized void close() {
        AudioDevice out = this.audio;
        if (out != null) {
            this.audio = null;
            out.close();

            try {
                this.bitstream.close();
            } catch (BitstreamException ignored) {
            }
        }

    }

    public synchronized boolean isComplete() {
        return this.complete;
    }

    protected boolean decodeFrame() {
        try {
            AudioDevice out = this.audio;
            if (out == null) {
                return false;
            } else {
                Header h = this.bitstream.readFrame();
                if (h == null) {
                    return false;
                } else {
                    SampleBuffer output = (SampleBuffer) this.decoder.decodeFrame(h, this.bitstream);
                    synchronized (this) {
                        out = this.audio;
                        if (out != null) {
                            out.write(output.getBuffer(), 0, output.getBufferLength());
                        }
                    }

                    this.bitstream.closeFrame();
                    return true;
                }
            }
        } catch (Exception e) {
            throw new DapperException(e);
        }
    }
}
