package com.qualcomm.ftccommon;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.TypeConversion;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.internal.android.SoundPoolIntf;
import org.firstinspires.ftc.robotcore.internal.collections.MutableReference;
import org.firstinspires.ftc.robotcore.internal.network.CallbackLooper;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;
import org.firstinspires.ftc.robotcore.internal.system.LockingRunner;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.ftc.robotcore.internal.system.RefCounted;
import org.firstinspires.ftc.robotcore.internal.system.Tracer;

public class SoundPlayer implements SoundPool.OnLoadCompleteListener, SoundPoolIntf {
    public static final String TAG = "SoundPlayer";
    public static boolean TRACE = true;
    public static final int msSoundTransmissionFreshness = 400;
    protected final LockingRunner cacheLock = new LockingRunner();
    protected SoundInfo currentlyLoadingInfo = null;
    protected CountDownLatch currentlyLoadingLatch = null;
    protected Set<CurrentlyPlaying> currentlyPlayingSounds;
    protected final boolean isRobotController = AppUtil.getInstance().isRobotController();
    protected LoadedSoundCache loadedSounds;
    protected final Object lock = new Object();
    protected float masterVolume = 1.0f;
    protected MediaPlayer mediaSizer = new MediaPlayer();
    protected ScheduledExecutorService scheduledThreadPool;
    protected SharedPreferences sharedPreferences;
    protected float soundOffVolume = 0.0f;
    protected float soundOnVolume = 1.0f;
    protected SoundPool soundPool;
    protected ExecutorService threadPool;
    protected Tracer tracer = Tracer.create(TAG, TRACE);

    protected interface SoundFromFile {
        SoundInfo apply(File file);
    }

    protected enum StopWhat {
        All,
        Loops
    }

    protected static class InstanceHolder {
        public static SoundPlayer theInstance = new SoundPlayer(3, 8);

        protected InstanceHolder() {
        }
    }

    public static SoundPlayer getInstance() {
        return InstanceHolder.theInstance;
    }

    protected static class CurrentlyPlaying {
        protected int loopControl = 0;
        protected long msFinish = Long.MAX_VALUE;
        protected Runnable runWhenFinished = null;
        protected int streamId = 0;

        protected CurrentlyPlaying() {
        }

        /* access modifiers changed from: protected */
        public boolean isLooping() {
            return this.loopControl == -1;
        }
    }

    public SoundPlayer(int i, int i2) {
        AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setUsage(1);
        builder.setContentType(4);
        AudioAttributes build = builder.build();
        SoundPool.Builder builder2 = new SoundPool.Builder();
        builder2.setAudioAttributes(build);
        builder2.setMaxStreams(i);
        this.soundPool = builder2.build();
        this.mediaSizer.setAudioAttributes(build);
        this.mediaSizer.setAudioSessionId(((AudioManager) AppUtil.getDefContext().getSystemService("audio")).generateAudioSessionId());
        this.loadedSounds = new LoadedSoundCache(i2);
        this.currentlyPlayingSounds = new HashSet();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppUtil.getDefContext());
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        this.threadPool = ThreadPool.newFixedThreadPool(1, TAG);
        this.scheduledThreadPool = ThreadPool.newScheduledExecutor(1, "SoundPlayerScheduler");
        CallbackLooper.getDefault().post(new Runnable() {
            public void run() {
                SoundPlayer.this.soundPool.setOnLoadCompleteListener(SoundPlayer.this);
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }

    public void close() {
        ExecutorService executorService = this.threadPool;
        if (executorService != null) {
            executorService.shutdownNow();
            ThreadPool.awaitTerminationOrExitApplication(this.threadPool, 5, TimeUnit.SECONDS, "SoundPool", "internal error");
            this.threadPool = null;
        }
        ScheduledExecutorService scheduledExecutorService = this.scheduledThreadPool;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            ThreadPool.awaitTerminationOrExitApplication(this.scheduledThreadPool, 3, TimeUnit.SECONDS, "SoundPool", "internal error");
        }
        MediaPlayer mediaPlayer = this.mediaSizer;
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void prefillSoundCache(int... iArr) {
        for (final int i : iArr) {
            this.threadPool.submit(new Runnable() {
                public void run() {
                    SoundPlayer.this.ensureCached((Context) AppUtil.getDefContext(), i);
                }
            });
        }
    }

    public static class PlaySoundParams {
        public int loopControl;
        public float rate;
        public float volume;
        public boolean waitForNonLoopingSoundsToFinish;

        public PlaySoundParams() {
            this.volume = 1.0f;
            this.waitForNonLoopingSoundsToFinish = true;
            this.loopControl = 0;
            this.rate = 1.0f;
        }

        public PlaySoundParams(boolean z) {
            this.volume = 1.0f;
            this.loopControl = 0;
            this.rate = 1.0f;
            this.waitForNonLoopingSoundsToFinish = z;
        }

        public PlaySoundParams(PlaySoundParams playSoundParams) {
            this.volume = 1.0f;
            this.waitForNonLoopingSoundsToFinish = true;
            this.loopControl = 0;
            this.rate = 1.0f;
            this.volume = playSoundParams.volume;
            this.waitForNonLoopingSoundsToFinish = playSoundParams.waitForNonLoopingSoundsToFinish;
            this.loopControl = playSoundParams.loopControl;
            this.rate = playSoundParams.rate;
        }

        public boolean isLooping() {
            return this.loopControl == -1;
        }
    }

    public void startPlaying(Context context, int i) {
        startPlaying(context, i, new PlaySoundParams(true), (Consumer<Integer>) null, (Runnable) null);
    }

    public void startPlaying(Context context, File file) {
        startPlaying(context, file, new PlaySoundParams(true), (Consumer<Integer>) null, (Runnable) null);
    }

    public void startPlaying(Context context, int i, PlaySoundParams playSoundParams, Consumer<Integer> consumer, Runnable runnable) {
        final Context context2 = context;
        final int i2 = i;
        final PlaySoundParams playSoundParams2 = playSoundParams;
        final Consumer<Integer> consumer2 = consumer;
        final Runnable runnable2 = runnable;
        this.threadPool.execute(new Runnable() {
            public void run() {
                SoundPlayer.this.loadAndStartPlaying(context2, i2, playSoundParams2, (Consumer<Integer>) consumer2, runnable2);
            }
        });
    }

    public void startPlaying(Context context, File file, PlaySoundParams playSoundParams, Consumer<Integer> consumer, Runnable runnable) {
        if (file != null) {
            final Context context2 = context;
            final File file2 = file;
            final PlaySoundParams playSoundParams2 = playSoundParams;
            final Consumer<Integer> consumer2 = consumer;
            final Runnable runnable2 = runnable;
            this.threadPool.execute(new Runnable() {
                public void run() {
                    SoundPlayer.this.loadAndStartPlaying(context2, file2, playSoundParams2, (Consumer<Integer>) consumer2, runnable2);
                }
            });
        }
    }

    public void stopPlayingAll() {
        internalStopPlaying(StopWhat.All);
    }

    public void stopPlayingLoops() {
        internalStopPlaying(StopWhat.Loops);
    }

    /* access modifiers changed from: protected */
    public void internalStopPlaying(StopWhat stopWhat) {
        synchronized (this.lock) {
            for (CurrentlyPlaying next : this.currentlyPlayingSounds) {
                if (stopWhat == StopWhat.All || (next.isLooping() && stopWhat == StopWhat.Loops)) {
                    next.msFinish = Long.MIN_VALUE;
                }
            }
            checkForFinishedSounds();
            if (this.isRobotController) {
                NetworkConnectionHandler.getInstance().sendCommand(new Command(CommandList.CmdPlaySound.Command, new CommandList.CmdStopPlayingSounds(stopWhat).serialize()));
            }
        }
    }

    public boolean preload(Context context, int i) {
        boolean z;
        synchronized (this.lock) {
            SoundInfo ensureLoaded = ensureLoaded(context, i);
            if (ensureLoaded != null) {
                z = true;
                releaseRef(ensureLoaded);
            } else {
                z = false;
            }
        }
        return z;
    }

    public boolean preload(Context context, File file) {
        boolean z;
        synchronized (this.lock) {
            SoundInfo ensureLoaded = ensureLoaded(context, file);
            if (ensureLoaded != null) {
                z = true;
                releaseRef(ensureLoaded);
            } else {
                z = false;
            }
        }
        return z;
    }

    public void setMasterVolume(float f) {
        synchronized (this.lock) {
            this.masterVolume = f;
        }
    }

    public float getMasterVolume() {
        return this.masterVolume;
    }

    @Deprecated
    public void play(Context context, int i) {
        startPlaying(context, i);
    }

    @Deprecated
    public void play(Context context, int i, boolean z) {
        startPlaying(context, i, new PlaySoundParams(z), (Consumer<Integer>) null, (Runnable) null);
    }

    /* access modifiers changed from: protected */
    public void loadAndStartPlaying(Context context, int i, PlaySoundParams playSoundParams, Consumer<Integer> consumer, Runnable runnable) {
        synchronized (this.lock) {
            SoundInfo ensureLoaded = ensureLoaded(context, i);
            if (ensureLoaded != null) {
                startPlayingLoadedSound(ensureLoaded, playSoundParams, consumer, runnable);
                releaseRef(ensureLoaded);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void loadAndStartPlaying(Context context, File file, PlaySoundParams playSoundParams, Consumer<Integer> consumer, Runnable runnable) {
        synchronized (this.lock) {
            SoundInfo ensureLoaded = ensureLoaded(context, file);
            if (ensureLoaded != null) {
                startPlayingLoadedSound(ensureLoaded, playSoundParams, consumer, runnable);
                releaseRef(ensureLoaded);
            }
        }
    }

    /* access modifiers changed from: protected */
    public SoundInfo ensureLoaded(Context context, int i) {
        SoundInfo resource;
        synchronized (this.lock) {
            resource = this.loadedSounds.getResource(i);
            if (resource == null) {
                int msDuration = getMsDuration(context, i);
                this.currentlyLoadingLatch = new CountDownLatch(1);
                SoundInfo soundInfo = new SoundInfo(context, i, msDuration);
                this.currentlyLoadingInfo = soundInfo;
                int load = this.soundPool.load(context, i, 1);
                if (load != 0) {
                    soundInfo.initialize(load);
                    this.loadedSounds.putResource(i, soundInfo);
                    waitForLoadCompletion();
                } else {
                    this.tracer.traceError("unable to load sound resource 0x%08x", Integer.valueOf(i));
                }
                resource = soundInfo;
            }
        }
        return resource;
    }

    /* access modifiers changed from: protected */
    public SoundInfo ensureLoaded(Context context, File file) {
        SoundInfo file2;
        synchronized (this.lock) {
            file2 = this.loadedSounds.getFile(file);
            if (file2 == null) {
                int msDuration = getMsDuration(context, file);
                this.currentlyLoadingLatch = new CountDownLatch(1);
                file2 = new SoundInfo(file, msDuration);
                this.currentlyLoadingInfo = file2;
                int load = this.soundPool.load(file.getAbsolutePath(), 1);
                if (load != 0) {
                    file2.initialize(load);
                    this.loadedSounds.putFile(file, file2);
                    waitForLoadCompletion();
                } else {
                    this.tracer.traceError("unable to load sound %s", file);
                }
            }
        }
        return file2;
    }

    public boolean isLocalSoundOn() {
        if (!this.sharedPreferences.getBoolean(AppUtil.getDefContext().getString(C0470R.string.pref_sound_on_off), true) || !this.sharedPreferences.getBoolean(AppUtil.getDefContext().getString(C0470R.string.pref_has_speaker), true)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void checkForFinishedSounds() {
        synchronized (this.lock) {
            long msNow = getMsNow();
            Iterator it = new ArrayList(this.currentlyPlayingSounds).iterator();
            while (it.hasNext()) {
                CurrentlyPlaying currentlyPlaying = (CurrentlyPlaying) it.next();
                if (currentlyPlaying.msFinish <= msNow) {
                    this.soundPool.stop(currentlyPlaying.streamId);
                    if (currentlyPlaying.runWhenFinished != null) {
                        this.threadPool.execute(currentlyPlaying.runWhenFinished);
                    }
                    this.currentlyPlayingSounds.remove(currentlyPlaying);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startPlayingLoadedSound(SoundInfo soundInfo, PlaySoundParams playSoundParams, Consumer<Integer> consumer, Runnable runnable) {
        SoundInfo soundInfo2 = soundInfo;
        PlaySoundParams playSoundParams2 = playSoundParams;
        final PlaySoundParams playSoundParams3 = playSoundParams2 == null ? new PlaySoundParams() : new PlaySoundParams(playSoundParams2);
        playSoundParams3.volume *= this.masterVolume;
        if (soundInfo2 != null) {
            synchronized (this.lock) {
                addRef(soundInfo);
                this.loadedSounds.noteSoundUsage(soundInfo2);
                final float f = (isLocalSoundOn() ? this.soundOnVolume : this.soundOffVolume) * playSoundParams3.volume;
                checkForFinishedSounds();
                long msNow = getMsNow();
                long j = Long.MIN_VALUE;
                for (CurrentlyPlaying next : this.currentlyPlayingSounds) {
                    if (!next.isLooping()) {
                        j = Math.max(j, next.msFinish);
                    }
                }
                final long max = playSoundParams3.waitForNonLoopingSoundsToFinish ? Math.max(msNow, j) : msNow;
                long j2 = max - msNow;
                final SoundInfo soundInfo3 = soundInfo;
                final Runnable runnable2 = runnable;
                final Consumer<Integer> consumer2 = consumer;
                C04795 r1 = new Runnable() {
                    public void run() {
                        synchronized (SoundPlayer.this.lock) {
                            long msNow = SoundPlayer.this.getMsNow();
                            SoundPool soundPool = SoundPlayer.this.soundPool;
                            int i = soundInfo3.sampleId;
                            float f = f;
                            final int play = soundPool.play(i, f, f, 1, playSoundParams3.loopControl, playSoundParams3.rate);
                            if (play != 0) {
                                long j = soundInfo3.msDuration * ((long) (playSoundParams3.isLooping() ? 1 : playSoundParams3.loopControl + 1));
                                CurrentlyPlaying currentlyPlaying = new CurrentlyPlaying();
                                currentlyPlaying.streamId = play;
                                currentlyPlaying.loopControl = playSoundParams3.loopControl;
                                currentlyPlaying.msFinish = playSoundParams3.isLooping() ? Long.MAX_VALUE : msNow + j;
                                currentlyPlaying.runWhenFinished = runnable2;
                                SoundPlayer.this.currentlyPlayingSounds.add(currentlyPlaying);
                                if (runnable2 != null && !playSoundParams3.isLooping()) {
                                    SoundPlayer.this.scheduledThreadPool.schedule(new Runnable() {
                                        public void run() {
                                            SoundPlayer.this.checkForFinishedSounds();
                                        }
                                    }, j + ((long) ((playSoundParams3.loopControl + 1) * 5)), TimeUnit.MILLISECONDS);
                                }
                                SoundPlayer.this.tracer.trace("playing volume=%f %s", Float.valueOf(f), soundInfo3);
                                soundInfo3.msLastPlay = msNow;
                            } else {
                                SoundPlayer.this.tracer.traceError("unable to play %s", soundInfo3);
                            }
                            SoundPlayer.releaseRef(soundInfo3);
                            if (consumer2 != null) {
                                SoundPlayer.this.threadPool.execute(new Runnable() {
                                    public void run() {
                                        consumer2.accept(Integer.valueOf(play));
                                    }
                                });
                            }
                        }
                        if (SoundPlayer.this.isRobotController) {
                            Command command = new Command(CommandList.CmdPlaySound.Command, new CommandList.CmdPlaySound(max, soundInfo3.hashString, playSoundParams3).serialize());
                            command.setTransmissionDeadline(new Deadline(400, TimeUnit.MILLISECONDS));
                            NetworkConnectionHandler.getInstance().sendCommand(command);
                        }
                    }
                };
                if (j2 > 0) {
                    this.scheduledThreadPool.schedule(r1, j2, TimeUnit.MILLISECONDS);
                } else {
                    r1.run();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getMsDuration(android.content.Context r9, int r10) {
        /*
            r8 = this;
            java.lang.Object r0 = r8.lock
            monitor-enter(r0)
            r1 = 0
            android.media.MediaPlayer r2 = r8.mediaSizer     // Catch:{ IOException -> 0x0033 }
            r2.reset()     // Catch:{ IOException -> 0x0033 }
            android.content.res.Resources r9 = r9.getResources()     // Catch:{ IOException -> 0x0033 }
            android.content.res.AssetFileDescriptor r9 = r9.openRawResourceFd(r10)     // Catch:{ IOException -> 0x0033 }
            android.media.MediaPlayer r2 = r8.mediaSizer     // Catch:{ IOException -> 0x0033 }
            java.io.FileDescriptor r3 = r9.getFileDescriptor()     // Catch:{ IOException -> 0x0033 }
            long r4 = r9.getStartOffset()     // Catch:{ IOException -> 0x0033 }
            long r6 = r9.getLength()     // Catch:{ IOException -> 0x0033 }
            r2.setDataSource(r3, r4, r6)     // Catch:{ IOException -> 0x0033 }
            r9.close()     // Catch:{ IOException -> 0x0033 }
            android.media.MediaPlayer r9 = r8.mediaSizer     // Catch:{ IOException -> 0x0033 }
            r9.prepare()     // Catch:{ IOException -> 0x0033 }
            android.media.MediaPlayer r9 = r8.mediaSizer     // Catch:{ IOException -> 0x0033 }
            int r9 = r9.getDuration()     // Catch:{ IOException -> 0x0033 }
            goto L_0x003e
        L_0x0031:
            r9 = move-exception
            goto L_0x0044
        L_0x0033:
            r9 = move-exception
            org.firstinspires.ftc.robotcore.internal.system.Tracer r10 = r8.tracer     // Catch:{ all -> 0x0031 }
            java.lang.String r2 = "exception preparing media sizer; media duration taken to be zero"
            java.lang.Object[] r3 = new java.lang.Object[r1]     // Catch:{ all -> 0x0031 }
            r10.traceError(r9, r2, r3)     // Catch:{ all -> 0x0031 }
            r9 = r1
        L_0x003e:
            monitor-exit(r0)     // Catch:{ all -> 0x0031 }
            if (r9 >= 0) goto L_0x0042
            goto L_0x0043
        L_0x0042:
            r1 = r9
        L_0x0043:
            return r1
        L_0x0044:
            monitor-exit(r0)     // Catch:{ all -> 0x0031 }
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.SoundPlayer.getMsDuration(android.content.Context, int):int");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getMsDuration(android.content.Context r5, java.io.File r6) {
        /*
            r4 = this;
            android.net.Uri r6 = android.net.Uri.fromFile(r6)
            java.lang.Object r0 = r4.lock
            monitor-enter(r0)
            r1 = 0
            android.media.MediaPlayer r2 = r4.mediaSizer     // Catch:{ IOException -> 0x0020 }
            r2.reset()     // Catch:{ IOException -> 0x0020 }
            android.media.MediaPlayer r2 = r4.mediaSizer     // Catch:{ IOException -> 0x0020 }
            r2.setDataSource(r5, r6)     // Catch:{ IOException -> 0x0020 }
            android.media.MediaPlayer r5 = r4.mediaSizer     // Catch:{ IOException -> 0x0020 }
            r5.prepare()     // Catch:{ IOException -> 0x0020 }
            android.media.MediaPlayer r5 = r4.mediaSizer     // Catch:{ IOException -> 0x0020 }
            int r5 = r5.getDuration()     // Catch:{ IOException -> 0x0020 }
            goto L_0x002b
        L_0x001e:
            r5 = move-exception
            goto L_0x0031
        L_0x0020:
            r5 = move-exception
            org.firstinspires.ftc.robotcore.internal.system.Tracer r6 = r4.tracer     // Catch:{ all -> 0x001e }
            java.lang.String r2 = "exception preparing media sizer; media duration taken to be zero"
            java.lang.Object[] r3 = new java.lang.Object[r1]     // Catch:{ all -> 0x001e }
            r6.traceError(r5, r2, r3)     // Catch:{ all -> 0x001e }
            r5 = r1
        L_0x002b:
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            if (r5 >= 0) goto L_0x002f
            goto L_0x0030
        L_0x002f:
            r1 = r5
        L_0x0030:
            return r1
        L_0x0031:
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.SoundPlayer.getMsDuration(android.content.Context, java.io.File):int");
    }

    /* access modifiers changed from: protected */
    public void waitForLoadCompletion() {
        try {
            this.currentlyLoadingLatch.await();
            this.currentlyLoadingLatch = null;
            this.currentlyLoadingInfo = null;
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }

    public void onLoadComplete(SoundPool soundPool2, int i, int i2) {
        this.tracer.trace("onLoadComplete(%s, samp=%d)=%d", this.currentlyLoadingInfo, Integer.valueOf(i), Integer.valueOf(i2));
        this.currentlyLoadingLatch.countDown();
    }

    /* access modifiers changed from: protected */
    public long getMsNow() {
        return AppUtil.getInstance().getWallClockTime();
    }

    /* access modifiers changed from: protected */
    public SoundInfo ensureLoaded(String str, SoundFromFile soundFromFile) {
        SoundInfo hash = this.loadedSounds.getHash(str);
        if (hash != null) {
            return hash;
        }
        return ensureCached(str, soundFromFile);
    }

    /* access modifiers changed from: protected */
    public void ensureCached(Context context, int i) {
        SoundInfo ensureCached;
        final SoundInfo ensureLoaded = ensureLoaded(context, i);
        if (ensureLoaded != null && (ensureCached = ensureCached(ensureLoaded.hashString, (SoundFromFile) new SoundFromFile() {
            public SoundInfo apply(File file) {
                FileOutputStream fileOutputStream;
                InputStream inputStream = ensureLoaded.getInputStream();
                FileOutputStream fileOutputStream2 = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    try {
                        SoundPlayer.copy(inputStream, fileOutputStream, ensureLoaded.cbSize);
                    } catch (IOException e) {
                        e = e;
                        try {
                            SoundPlayer.this.tracer.traceError(e, "exception caching file: %s", file);
                            SoundPlayer.this.safeClose(fileOutputStream);
                            SoundPlayer.this.safeClose(inputStream);
                            return null;
                        } catch (Throwable th) {
                            th = th;
                            fileOutputStream2 = fileOutputStream;
                        }
                    }
                } catch (IOException e2) {
                    e = e2;
                    fileOutputStream = null;
                    SoundPlayer.this.tracer.traceError(e, "exception caching file: %s", file);
                    SoundPlayer.this.safeClose(fileOutputStream);
                    SoundPlayer.this.safeClose(inputStream);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    SoundPlayer.this.safeClose(fileOutputStream2);
                    SoundPlayer.this.safeClose(inputStream);
                    throw th;
                }
                SoundPlayer.this.safeClose(fileOutputStream);
                SoundPlayer.this.safeClose(inputStream);
                return null;
            }
        })) != null) {
            releaseRef(ensureCached);
        }
    }

    /* access modifiers changed from: protected */
    public SoundInfo ensureCached(final String str, final SoundFromFile soundFromFile) {
        final MutableReference mutableReference = new MutableReference(null);
        AppUtil.getInstance().ensureDirectoryExists(AppUtil.SOUNDS_CACHE, false);
        try {
            this.cacheLock.lockWhile((Runnable) new Runnable() {
                public void run() {
                    boolean z;
                    SoundInfo ensureLoaded;
                    File file = AppUtil.SOUNDS_CACHE;
                    File file2 = new File(file, str + ".sound");
                    if (!file2.exists() || (ensureLoaded = SoundPlayer.this.ensureLoaded((Context) AppUtil.getDefContext(), file2)) == null) {
                        z = false;
                    } else {
                        mutableReference.setValue(ensureLoaded);
                        z = true;
                    }
                    if (!z) {
                        mutableReference.setValue(soundFromFile.apply(file2));
                    }
                }
            });
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        return (SoundInfo) mutableReference.getValue();
    }

    public CallbackResult handleCommandPlaySound(String str) {
        CallbackResult callbackResult = CallbackResult.HANDLED;
        final CommandList.CmdPlaySound deserialize = CommandList.CmdPlaySound.deserialize(str);
        SoundInfo ensureLoaded = ensureLoaded(deserialize.hashString, (SoundFromFile) new SoundFromFile() {
            public SoundInfo apply(File file) {
                return SoundPlayer.this.requestRemoteSound(file, deserialize.hashString);
            }
        });
        if (ensureLoaded != null) {
            RobotLog.getLocalTime(deserialize.msPresentationTime);
            getMsNow();
            startPlayingLoadedSound(ensureLoaded, deserialize.getParams(), (Consumer<Integer>) null, (Runnable) null);
            releaseRef(ensureLoaded);
        }
        return callbackResult;
    }

    public CallbackResult handleCommandStopPlayingSounds(Command command) {
        String extra = command.getExtra();
        CallbackResult callbackResult = CallbackResult.HANDLED;
        CommandList.CmdStopPlayingSounds deserialize = CommandList.CmdStopPlayingSounds.deserialize(extra);
        this.tracer.trace("handleCommandStopPlayingSounds(): what=%s", deserialize.stopWhat);
        internalStopPlaying(deserialize.stopWhat);
        return callbackResult;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v0, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v1, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v2, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v1, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v2, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v3, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v3, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v4, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v7, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v9, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v10, resolved type: java.net.ServerSocket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v5, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v9, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v8, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v10, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v9, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v10, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v11, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v12, resolved type: java.net.Socket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v11, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v12, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v13, resolved type: java.io.InputStream} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v11, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v14, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v12, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v15, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v13, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v14, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v15, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v16, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v17, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v18, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v19, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v20, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v21, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v22, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v16, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v17, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v18, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v19, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v20, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v21, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v22, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v23, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v24, resolved type: com.qualcomm.ftccommon.SoundPlayer$SoundInfo} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't wrap try/catch for region: R(5:26|25|38|39|40) */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00bc, code lost:
        if (r0 == false) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00be, code lost:
        r11.delete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c2, code lost:
        r12 = e;
        r4 = r4;
        r5 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00f7, code lost:
        if (r0 != false) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00fa, code lost:
        return r1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:38:0x00a7 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x010b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.qualcomm.ftccommon.SoundPlayer.SoundInfo requestRemoteSound(java.io.File r11, java.lang.String r12) {
        /*
            r10 = this;
            r0 = 1
            r1 = 0
            r2 = 0
            java.io.FileOutputStream r3 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x00dd, RuntimeException -> 0x00db, all -> 0x00d6 }
            r3.<init>(r11)     // Catch:{ IOException -> 0x00dd, RuntimeException -> 0x00db, all -> 0x00d6 }
            java.net.ServerSocket r4 = new java.net.ServerSocket     // Catch:{ IOException -> 0x00d3, RuntimeException -> 0x00d1, all -> 0x00ce }
            r4.<init>(r2)     // Catch:{ IOException -> 0x00d3, RuntimeException -> 0x00d1, all -> 0x00ce }
            com.qualcomm.ftccommon.CommandList$CmdRequestSound r5 = new com.qualcomm.ftccommon.CommandList$CmdRequestSound     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            int r6 = r4.getLocalPort()     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            r5.<init>(r12, r6)     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            org.firstinspires.ftc.robotcore.internal.system.Tracer r6 = r10.tracer     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            java.lang.String r7 = "handleCommandPlaySound(): requesting: port=%d hash=%s"
            r8 = 2
            java.lang.Object[] r8 = new java.lang.Object[r8]     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            int r9 = r5.port     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            r8[r2] = r9     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            java.lang.String r9 = r5.hashString     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            r8[r0] = r9     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            r6.trace((java.lang.String) r7, (java.lang.Object[]) r8)     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler r6 = org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler.getInstance()     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            com.qualcomm.robotcore.robocol.Command r7 = new com.qualcomm.robotcore.robocol.Command     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            java.lang.String r8 = "CMD_REQUEST_SOUND"
            java.lang.String r5 = r5.serialize()     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            r7.<init>(r8, r5)     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            r6.sendCommand(r7)     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            r5 = 1000(0x3e8, float:1.401E-42)
            r4.setSoTimeout(r5)     // Catch:{ IOException -> 0x00cb, RuntimeException -> 0x00c9, all -> 0x00c6 }
            java.net.Socket r6 = r4.accept()     // Catch:{ SocketTimeoutException -> 0x00a5 }
            r6.setSoTimeout(r5)     // Catch:{ SocketTimeoutException -> 0x00a3, IOException -> 0x00a0, RuntimeException -> 0x009e, all -> 0x009b }
            java.io.InputStream r5 = r6.getInputStream()     // Catch:{ SocketTimeoutException -> 0x00a3, IOException -> 0x00a0, RuntimeException -> 0x009e, all -> 0x009b }
            r7 = 4
            byte[] r8 = new byte[r7]     // Catch:{ SocketTimeoutException -> 0x00a7 }
            int r9 = r5.read(r8)     // Catch:{ SocketTimeoutException -> 0x00a7 }
            if (r7 != r9) goto L_0x0093
            int r7 = com.qualcomm.robotcore.util.TypeConversion.byteArrayToInt(r8)     // Catch:{ SocketTimeoutException -> 0x00a7 }
            if (r7 <= 0) goto L_0x0089
            copy(r5, r3, r7)     // Catch:{ SocketTimeoutException -> 0x00a7 }
            r10.safeClose(r3)     // Catch:{ SocketTimeoutException -> 0x00a7 }
            org.firstinspires.ftc.robotcore.internal.system.Tracer r3 = r10.tracer     // Catch:{ SocketTimeoutException -> 0x0086, IOException -> 0x0081, RuntimeException -> 0x007f, all -> 0x007a }
            java.lang.String r7 = "handleCommandPlaySound(): received: hash=%s"
            java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ SocketTimeoutException -> 0x0086, IOException -> 0x0081, RuntimeException -> 0x007f, all -> 0x007a }
            r0[r2] = r12     // Catch:{ SocketTimeoutException -> 0x0086, IOException -> 0x0081, RuntimeException -> 0x007f, all -> 0x007a }
            r3.trace((java.lang.String) r7, (java.lang.Object[]) r0)     // Catch:{ SocketTimeoutException -> 0x0086, IOException -> 0x0081, RuntimeException -> 0x007f, all -> 0x007a }
            android.app.Application r12 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.getDefContext()     // Catch:{ SocketTimeoutException -> 0x0086, IOException -> 0x0081, RuntimeException -> 0x007f, all -> 0x007a }
            com.qualcomm.ftccommon.SoundPlayer$SoundInfo r12 = r10.ensureLoaded((android.content.Context) r12, (java.io.File) r11)     // Catch:{ SocketTimeoutException -> 0x0086, IOException -> 0x0081, RuntimeException -> 0x007f, all -> 0x007a }
            r3 = r1
            r0 = r2
            r1 = r12
            goto L_0x00b0
        L_0x007a:
            r12 = move-exception
            r3 = r1
            r0 = r2
            goto L_0x00fc
        L_0x007f:
            r12 = move-exception
            goto L_0x0082
        L_0x0081:
            r12 = move-exception
        L_0x0082:
            r3 = r1
            r0 = r2
            goto L_0x00e2
        L_0x0086:
            r3 = r1
            r0 = r2
            goto L_0x00a7
        L_0x0089:
            org.firstinspires.ftc.robotcore.internal.system.Tracer r12 = r10.tracer     // Catch:{ SocketTimeoutException -> 0x00a7 }
            java.lang.String r7 = "handleCommandPlaySound(): client couldn't send sound"
            java.lang.Object[] r8 = new java.lang.Object[r2]     // Catch:{ SocketTimeoutException -> 0x00a7 }
            r12.traceError(r7, r8)     // Catch:{ SocketTimeoutException -> 0x00a7 }
            goto L_0x00b0
        L_0x0093:
            java.io.IOException r12 = new java.io.IOException     // Catch:{ SocketTimeoutException -> 0x00a7 }
            java.lang.String r7 = "framing error"
            r12.<init>(r7)     // Catch:{ SocketTimeoutException -> 0x00a7 }
            throw r12     // Catch:{ SocketTimeoutException -> 0x00a7 }
        L_0x009b:
            r12 = move-exception
            goto L_0x00fd
        L_0x009e:
            r12 = move-exception
            goto L_0x00a1
        L_0x00a0:
            r12 = move-exception
        L_0x00a1:
            r5 = r1
            goto L_0x00e2
        L_0x00a3:
            r5 = r1
            goto L_0x00a7
        L_0x00a5:
            r5 = r1
            r6 = r5
        L_0x00a7:
            org.firstinspires.ftc.robotcore.internal.system.Tracer r12 = r10.tracer     // Catch:{ IOException -> 0x00c4, RuntimeException -> 0x00c2 }
            java.lang.String r7 = "timed out awaiting sound file"
            java.lang.Object[] r8 = new java.lang.Object[r2]     // Catch:{ IOException -> 0x00c4, RuntimeException -> 0x00c2 }
            r12.traceError(r7, r8)     // Catch:{ IOException -> 0x00c4, RuntimeException -> 0x00c2 }
        L_0x00b0:
            r10.safeClose(r5)
            r10.safeClose(r6)
            r10.safeClose(r4)
            r10.safeClose(r3)
            if (r0 == 0) goto L_0x00fa
        L_0x00be:
            r11.delete()
            goto L_0x00fa
        L_0x00c2:
            r12 = move-exception
            goto L_0x00e2
        L_0x00c4:
            r12 = move-exception
            goto L_0x00e2
        L_0x00c6:
            r12 = move-exception
            r6 = r1
            goto L_0x00fd
        L_0x00c9:
            r12 = move-exception
            goto L_0x00cc
        L_0x00cb:
            r12 = move-exception
        L_0x00cc:
            r5 = r1
            goto L_0x00e1
        L_0x00ce:
            r12 = move-exception
            r4 = r1
            goto L_0x00d9
        L_0x00d1:
            r12 = move-exception
            goto L_0x00d4
        L_0x00d3:
            r12 = move-exception
        L_0x00d4:
            r4 = r1
            goto L_0x00e0
        L_0x00d6:
            r12 = move-exception
            r3 = r1
            r4 = r3
        L_0x00d9:
            r6 = r4
            goto L_0x00fd
        L_0x00db:
            r12 = move-exception
            goto L_0x00de
        L_0x00dd:
            r12 = move-exception
        L_0x00de:
            r3 = r1
            r4 = r3
        L_0x00e0:
            r5 = r4
        L_0x00e1:
            r6 = r5
        L_0x00e2:
            org.firstinspires.ftc.robotcore.internal.system.Tracer r7 = r10.tracer     // Catch:{ all -> 0x00fb }
            java.lang.String r8 = "handleCommandPlaySound(): exception thrown"
            java.lang.Object[] r2 = new java.lang.Object[r2]     // Catch:{ all -> 0x00fb }
            r7.traceError(r12, r8, r2)     // Catch:{ all -> 0x00fb }
            r10.safeClose(r5)
            r10.safeClose(r6)
            r10.safeClose(r4)
            r10.safeClose(r3)
            if (r0 == 0) goto L_0x00fa
            goto L_0x00be
        L_0x00fa:
            return r1
        L_0x00fb:
            r12 = move-exception
        L_0x00fc:
            r1 = r5
        L_0x00fd:
            r10.safeClose(r1)
            r10.safeClose(r6)
            r10.safeClose(r4)
            r10.safeClose(r3)
            if (r0 == 0) goto L_0x010e
            r11.delete()
        L_0x010e:
            throw r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.SoundPlayer.requestRemoteSound(java.io.File, java.lang.String):com.qualcomm.ftccommon.SoundPlayer$SoundInfo");
    }

    protected static void copy(InputStream inputStream, OutputStream outputStream, int i) throws IOException {
        if (i > 0) {
            byte[] bArr = new byte[256];
            do {
                int read = inputStream.read(bArr);
                if (read >= 0) {
                    outputStream.write(bArr, 0, read);
                    i -= read;
                } else {
                    throw new IOException("insufficient data");
                }
            } while (i > 0);
        }
    }

    public CallbackResult handleCommandRequestSound(Command command) {
        OutputStream outputStream;
        Socket socket;
        String extra = command.getExtra();
        CallbackResult callbackResult = CallbackResult.HANDLED;
        CommandList.CmdRequestSound deserialize = CommandList.CmdRequestSound.deserialize(extra);
        this.tracer.trace("handleCommandRequestSound(): hash=%s", deserialize.hashString);
        InputStream inputStream = null;
        try {
            socket = new Socket(command.getSender().getAddress(), deserialize.port);
            try {
                outputStream = socket.getOutputStream();
                try {
                    SoundInfo hash = this.loadedSounds.getHash(deserialize.hashString);
                    if (hash != null) {
                        inputStream = hash.getInputStream();
                    } else {
                        this.tracer.traceError("handleCommandRequestSound(): can't find hash=%s", deserialize.hashString);
                    }
                    if (inputStream != null) {
                        outputStream.write(TypeConversion.intToByteArray(hash.cbSize));
                        byte[] bArr = new byte[256];
                        int i = 0;
                        while (true) {
                            int read = inputStream.read(bArr);
                            if (read < 0) {
                                break;
                            }
                            outputStream.write(bArr, 0, read);
                            i += read;
                        }
                        this.tracer.trace("handleCommandRequestSound(): finished: %s cbSize=%d cbWritten=%d", hash, Integer.valueOf(hash.cbSize), Integer.valueOf(i));
                    } else {
                        outputStream.write(TypeConversion.intToByteArray(0));
                    }
                    releaseRef(hash);
                } catch (IOException | RuntimeException e) {
                    e = e;
                    try {
                        this.tracer.traceError(e, "handleCommandRequestSound(): exception thrown", new Object[0]);
                        safeClose(inputStream);
                        safeClose(outputStream);
                        safeClose(socket);
                        return callbackResult;
                    } catch (Throwable th) {
                        th = th;
                        safeClose((Object) null);
                        safeClose(outputStream);
                        safeClose(socket);
                        throw th;
                    }
                }
            } catch (IOException | RuntimeException e2) {
                e = e2;
                outputStream = null;
                this.tracer.traceError(e, "handleCommandRequestSound(): exception thrown", new Object[0]);
                safeClose(inputStream);
                safeClose(outputStream);
                safeClose(socket);
                return callbackResult;
            } catch (Throwable th2) {
                th = th2;
                outputStream = null;
                safeClose((Object) null);
                safeClose(outputStream);
                safeClose(socket);
                throw th;
            }
        } catch (IOException | RuntimeException e3) {
            e = e3;
            outputStream = null;
            socket = null;
            this.tracer.traceError(e, "handleCommandRequestSound(): exception thrown", new Object[0]);
            safeClose(inputStream);
            safeClose(outputStream);
            safeClose(socket);
            return callbackResult;
        } catch (Throwable th3) {
            th = th3;
            outputStream = null;
            socket = null;
            safeClose((Object) null);
            safeClose(outputStream);
            safeClose(socket);
            throw th;
        }
        safeClose(inputStream);
        safeClose(outputStream);
        safeClose(socket);
        return callbackResult;
    }

    /* access modifiers changed from: protected */
    public void safeClose(Object obj) {
        if (obj != null) {
            try {
                if (obj instanceof Flushable) {
                    try {
                        ((Flushable) obj).flush();
                    } catch (IOException e) {
                        this.tracer.traceError(e, "exception while flushing", new Object[0]);
                    }
                }
                if (obj instanceof Closeable) {
                    ((Closeable) obj).close();
                    return;
                }
                throw new IllegalArgumentException("Unknown object to close");
            } catch (IOException e2) {
                this.tracer.traceError(e2, "exception while closing", new Object[0]);
            }
        }
    }

    protected class SoundInfo extends RefCounted {
        public int cbSize;
        public final Context context;
        public final File file;
        public String hashString;
        public final long msDuration;
        public long msLastPlay;
        public final int resourceId;
        public int sampleId;

        public String toString() {
            return Misc.formatInvariant("samp=%d|ms=%d", Integer.valueOf(this.sampleId), Long.valueOf(this.msDuration));
        }

        public SoundInfo(Context context2, int i, int i2) {
            this.msLastPlay = 0;
            this.context = context2;
            this.resourceId = i;
            this.file = null;
            this.msDuration = (long) i2;
            this.hashString = computeHash();
        }

        public SoundInfo(File file2, int i) {
            this.msLastPlay = 0;
            this.context = null;
            this.resourceId = 0;
            this.file = file2;
            this.msDuration = (long) i;
            this.hashString = computeHash();
        }

        public void initialize(int i) {
            this.sampleId = i;
            this.hashString = computeHash();
        }

        /* access modifiers changed from: protected */
        public void destructor() {
            SoundPlayer.this.tracer.trace("unloading sound %s", this);
            SoundPlayer.this.soundPool.unload(this.sampleId);
            super.destructor();
        }

        public InputStream getInputStream() {
            try {
                if (this.resourceId != 0) {
                    return this.context.getResources().openRawResource(this.resourceId);
                }
                return new FileInputStream(this.file);
            } catch (IOException unused) {
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public String computeHash() {
            InputStream inputStream = getInputStream();
            if (inputStream != null) {
                try {
                    MessageDigest instance = MessageDigest.getInstance("MD5");
                    byte[] bArr = new byte[256];
                    this.cbSize = 0;
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read < 0) {
                            break;
                        }
                        this.cbSize += read;
                        instance.update(bArr, 0, read);
                    }
                    byte[] digest = instance.digest();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < digest.length; i++) {
                        sb.append(String.format(Locale.ROOT, "%02x", new Object[]{Byte.valueOf(digest[i])}));
                    }
                    return sb.toString();
                } catch (IOException | NoSuchAlgorithmException e) {
                    SoundPlayer.this.tracer.traceError(e, "exception computing hash", new Object[0]);
                } finally {
                    SoundPlayer.this.safeClose(inputStream);
                }
            }
            throw Misc.illegalStateException("internal error: unable to compute hash of %s", this);
        }

        public Object getKey() {
            int i = this.resourceId;
            return i == 0 ? this.file : Integer.valueOf(i);
        }
    }

    public static SoundInfo addRef(SoundInfo soundInfo) {
        if (soundInfo != null) {
            soundInfo.addRef();
        }
        return soundInfo;
    }

    public static void releaseRef(SoundInfo soundInfo) {
        if (soundInfo != null) {
            soundInfo.releaseRef();
        }
    }

    protected class LoadedSoundCache {
        /* access modifiers changed from: private */
        public final int capacity;
        private final Map<String, SoundInfo> hashMap;
        private final Map<Object, SoundInfo> keyMap;
        private final Object lock = new Object();
        /* access modifiers changed from: private */
        public boolean unloadOnRemove;

        class SoundInfoMap<K> extends LinkedHashMap<K, SoundInfo> {
            private static final float loadFactor = 0.75f;

            public SoundInfoMap(int i) {
                super(((int) Math.ceil((double) (((float) i) / loadFactor))) + 1, loadFactor, true);
            }

            /* access modifiers changed from: protected */
            public boolean removeEldestEntry(Map.Entry<K, SoundInfo> entry) {
                return size() > LoadedSoundCache.this.capacity;
            }

            public SoundInfo remove(Object obj) {
                SoundInfo soundInfo = (SoundInfo) super.remove(obj);
                if (LoadedSoundCache.this.unloadOnRemove && soundInfo != null) {
                    SoundPlayer.releaseRef(soundInfo);
                }
                return soundInfo;
            }
        }

        LoadedSoundCache(int i) {
            this.keyMap = new SoundInfoMap(i);
            this.hashMap = new SoundInfoMap(i);
            this.capacity = i;
            this.unloadOnRemove = true;
        }

        public SoundInfo getResource(int i) {
            SoundInfo addRef;
            synchronized (this.lock) {
                addRef = SoundPlayer.addRef(this.keyMap.get(Integer.valueOf(i)));
            }
            return addRef;
        }

        public SoundInfo getFile(File file) {
            SoundInfo addRef;
            synchronized (this.lock) {
                addRef = SoundPlayer.addRef(this.keyMap.get(file.getAbsoluteFile()));
            }
            return addRef;
        }

        public SoundInfo getHash(String str) {
            SoundInfo addRef;
            synchronized (this.lock) {
                addRef = SoundPlayer.addRef(this.hashMap.get(str));
            }
            return addRef;
        }

        public void putResource(int i, SoundInfo soundInfo) {
            synchronized (this.lock) {
                this.keyMap.put(Integer.valueOf(i), SoundPlayer.addRef(soundInfo));
                this.hashMap.put(soundInfo.hashString, SoundPlayer.addRef(soundInfo));
            }
        }

        public void putFile(File file, SoundInfo soundInfo) {
            synchronized (this.lock) {
                this.keyMap.put(file.getAbsoluteFile(), SoundPlayer.addRef(soundInfo));
                this.hashMap.put(soundInfo.hashString, SoundPlayer.addRef(soundInfo));
            }
        }

        public void noteSoundUsage(SoundInfo soundInfo) {
            synchronized (this.lock) {
                this.unloadOnRemove = false;
                try {
                    Object key = soundInfo.getKey();
                    this.keyMap.remove(key);
                    this.keyMap.put(key, soundInfo);
                    this.hashMap.remove(soundInfo.hashString);
                    this.hashMap.put(soundInfo.hashString, soundInfo);
                } finally {
                    this.unloadOnRemove = true;
                }
            }
        }
    }

    public void play(Context context, int i, float f, int i2, float f2) {
        PlaySoundParams playSoundParams = new PlaySoundParams(false);
        playSoundParams.volume = f;
        playSoundParams.loopControl = i2;
        playSoundParams.rate = f2;
        startPlaying(context, i, playSoundParams, (Consumer<Integer>) null, (Runnable) null);
    }

    public void play(Context context, File file, float f, int i, float f2) {
        PlaySoundParams playSoundParams = new PlaySoundParams(false);
        playSoundParams.volume = f;
        playSoundParams.loopControl = i;
        playSoundParams.rate = f2;
        startPlaying(context, file, playSoundParams, (Consumer<Integer>) null, (Runnable) null);
    }
}
