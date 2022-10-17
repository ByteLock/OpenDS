package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.qualcomm.ftcdriverstation.FtcDriverStationActivity;

public class PracticeTimerManager {
    private static final int AUTO_END_LENGTH = 3000;
    private static final int AUTO_LENGTH = 30000;
    private static final int DRIVER_CONTROL_LENGTH = 120000;
    private static final int ENDGAME_LENGTH = 30000;
    private static final int END_MATCH_BUZZER_LENGTH = 2000;
    private static final int GAME_ANNOUNCER_LENGTH = 5275;
    private static final int MATCH_LENGTH = 150000;
    private static final int OFFICIAL_TIMER_OFFSET = 1000;
    private static final int PICK_UP_CTRLS_LENGTH = 5000;
    private static final int TELEOP_LENGTH = 90000;
    private static final int TELE_COUNTDOWN_LENGTH = 3000;
    private static final int TIMER_TICK_PERIOD = 100;
    /* access modifiers changed from: private */
    public int PLAYING_SOUND_STREAM_ID = -1;
    /* access modifiers changed from: private */
    public int SOUND_ID_ABORT_MATCH;
    private int SOUND_ID_END_AUTO;
    private int SOUND_ID_END_MATCH;
    private int SOUND_ID_FACTWHISTLE;
    private int SOUND_ID_FIREBELL;
    private int SOUND_ID_MC_BEGIN_AUTO;
    private int SOUND_ID_PICK_UP_CTRLS;
    private int SOUND_ID_START_AUTO;
    private int SOUND_ID_TELE_COUNTDOWN;
    /* access modifiers changed from: private */
    public CountDownTimer countDownTimer;
    /* access modifiers changed from: private */
    public boolean running = false;
    /* access modifiers changed from: private */
    public SoundPool soundPool;
    /* access modifiers changed from: private */
    public ImageView startStopBtn;
    /* access modifiers changed from: private */
    public final Object syncobj = new Object();
    /* access modifiers changed from: private */
    public Context theContext;
    /* access modifiers changed from: private */
    public TextView timerView;

    public PracticeTimerManager(Context context, ImageView imageView, TextView textView) {
        this.theContext = context;
        this.startStopBtn = imageView;
        this.timerView = textView;
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                synchronized (PracticeTimerManager.this.syncobj) {
                    if (!PracticeTimerManager.this.running) {
                        PracticeTimerManager practiceTimerManager = PracticeTimerManager.this;
                        practiceTimerManager.showStartDialog(practiceTimerManager.theContext);
                    } else {
                        if (PracticeTimerManager.this.countDownTimer != null) {
                            PracticeTimerManager.this.countDownTimer.cancel();
                        }
                        boolean unused = PracticeTimerManager.this.running = false;
                        if (PracticeTimerManager.this.PLAYING_SOUND_STREAM_ID != -1) {
                            PracticeTimerManager.this.soundPool.stop(PracticeTimerManager.this.PLAYING_SOUND_STREAM_ID);
                        }
                        PracticeTimerManager practiceTimerManager2 = PracticeTimerManager.this;
                        practiceTimerManager2.playSound(practiceTimerManager2.SOUND_ID_ABORT_MATCH);
                        PracticeTimerManager.this.resetUi();
                    }
                }
            }
        });
        SoundPool soundPool2 = new SoundPool(9, 3, 0);
        this.soundPool = soundPool2;
        this.SOUND_ID_PICK_UP_CTRLS = soundPool2.load(context, C0648R.raw.lady_pick_up_ctrls, 1);
        this.SOUND_ID_TELE_COUNTDOWN = this.soundPool.load(context, C0648R.raw.lady_3_2_1, 1);
        this.SOUND_ID_FIREBELL = this.soundPool.load(context, C0648R.raw.firebell, 1);
        this.SOUND_ID_FACTWHISTLE = this.soundPool.load(context, C0648R.raw.factwhistle, 1);
        this.SOUND_ID_END_MATCH = this.soundPool.load(context, C0648R.raw.endmatch, 1);
        this.SOUND_ID_ABORT_MATCH = this.soundPool.load(context, C0648R.raw.fogblast, 1);
        this.SOUND_ID_START_AUTO = this.soundPool.load(context, C0648R.raw.charge, 1);
        this.SOUND_ID_END_AUTO = this.soundPool.load(context, C0648R.raw.endauto, 1);
        this.SOUND_ID_MC_BEGIN_AUTO = this.soundPool.load(context, C0648R.raw.mc_begin_match, 1);
    }

    public void reset() {
        synchronized (this.syncobj) {
            CountDownTimer countDownTimer2 = this.countDownTimer;
            if (countDownTimer2 != null) {
                countDownTimer2.cancel();
            }
            this.running = false;
            this.soundPool.stop(this.SOUND_ID_PICK_UP_CTRLS);
            this.soundPool.stop(this.SOUND_ID_TELE_COUNTDOWN);
            this.soundPool.stop(this.SOUND_ID_FIREBELL);
            this.soundPool.stop(this.SOUND_ID_FACTWHISTLE);
            this.soundPool.stop(this.SOUND_ID_END_MATCH);
            this.soundPool.stop(this.SOUND_ID_ABORT_MATCH);
            this.soundPool.stop(this.SOUND_ID_START_AUTO);
            this.soundPool.stop(this.SOUND_ID_END_AUTO);
            this.soundPool.stop(this.SOUND_ID_MC_BEGIN_AUTO);
            resetUi();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetUi() {
        this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_white));
        this.timerView.setText(formatTimeLeft(150000));
        this.startStopBtn.setImageDrawable(this.theContext.getResources().getDrawable(C0648R.C0649drawable.ic_play_circle_filled_24dp));
    }

    /* access modifiers changed from: package-private */
    public void showStartDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Start from...");
        builder.setItems(new String[]{"Autonomous", "Auto --> Tele-Op Transition", "Tele-Op", "Endgame"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean unused = PracticeTimerManager.this.running = true;
                PracticeTimerManager.this.startStopBtn.setImageDrawable(PracticeTimerManager.this.theContext.getResources().getDrawable(C0648R.C0649drawable.ic_stop_24dp));
                if (i == 0) {
                    PracticeTimerManager.this.gameAnnouncerTimer();
                } else if (i == 1) {
                    PracticeTimerManager.this.pickUpControllersTimer();
                } else if (i == 2) {
                    PracticeTimerManager.this.teleopTimer();
                } else if (i == 3) {
                    PracticeTimerManager.this.endgameTimer();
                }
            }
        });
        builder.create().show();
    }

    /* access modifiers changed from: private */
    public void gameAnnouncerTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_MC_BEGIN_AUTO);
                this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_yellow));
                this.timerView.setText(formatTimeLeft(150000));
                this.countDownTimer = new CountDownTimer(5275, 100) {
                    public void onTick(long j) {
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.autoTimer();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void autoTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_START_AUTO);
                this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_white));
                this.countDownTimer = new CountDownTimer(FtcDriverStationActivity.OpModeCountDownTimer.MS_COUNTDOWN_INTERVAL, 100) {
                    public void onTick(long j) {
                        PracticeTimerManager.this.timerView.setText(PracticeTimerManager.this.formatTimeLeft(j + 1000 + 120000));
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.autoEndTimer();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void autoEndTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_END_AUTO);
                this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_white));
                this.timerView.setText(formatTimeLeft(120000));
                this.countDownTimer = new CountDownTimer(3000, 100) {
                    public void onTick(long j) {
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.pickUpControllersTimer();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void pickUpControllersTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_PICK_UP_CTRLS);
                this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_yellow));
                this.countDownTimer = new CountDownTimer(5000, 100) {
                    public void onTick(long j) {
                        PracticeTimerManager.this.timerView.setText(PracticeTimerManager.this.formatTimeLeft(j + 1000));
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.teleCountDownTimer();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void teleCountDownTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_TELE_COUNTDOWN);
                this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_red));
                this.countDownTimer = new CountDownTimer(3000, 100) {
                    public void onTick(long j) {
                        PracticeTimerManager.this.timerView.setText(PracticeTimerManager.this.formatTimeLeft(j + 1000));
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.teleopTimer();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void teleopTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_FIREBELL);
                this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_white));
                this.countDownTimer = new CountDownTimer(90000, 100) {
                    public void onTick(long j) {
                        PracticeTimerManager.this.timerView.setText(PracticeTimerManager.this.formatTimeLeft(j + 1000 + FtcDriverStationActivity.OpModeCountDownTimer.MS_COUNTDOWN_INTERVAL));
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.endgameTimer();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void endgameTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_FACTWHISTLE);
                this.countDownTimer = new CountDownTimer(FtcDriverStationActivity.OpModeCountDownTimer.MS_COUNTDOWN_INTERVAL, 100) {
                    public void onTick(long j) {
                        PracticeTimerManager.this.timerView.setText(PracticeTimerManager.this.formatTimeLeft(j + 1000));
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.endMatchTimer();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public void endMatchTimer() {
        synchronized (this.syncobj) {
            if (this.running) {
                playSound(this.SOUND_ID_END_MATCH);
                this.timerView.setTextColor(this.theContext.getResources().getColor(C0648R.color.practice_timer_font_white));
                this.timerView.setText(formatTimeLeft(0));
                this.countDownTimer = new CountDownTimer(2000, 100) {
                    public void onTick(long j) {
                    }

                    public void onFinish() {
                        PracticeTimerManager.this.timerView.setText(PracticeTimerManager.this.formatTimeLeft(0));
                        boolean unused = PracticeTimerManager.this.running = false;
                        PracticeTimerManager.this.resetUi();
                    }
                }.start();
            }
        }
    }

    /* access modifiers changed from: private */
    public String formatTimeLeft(long j) {
        return ((int) ((j / 60000) % 60)) + ":" + formatSeconds(((int) (j / 1000)) % 60);
    }

    private String formatSeconds(int i) {
        if (i >= 10) {
            return Integer.toString(i);
        }
        return "0" + i;
    }

    /* access modifiers changed from: private */
    public void playSound(int i) {
        this.PLAYING_SOUND_STREAM_ID = this.soundPool.play(i, 1.0f, 1.0f, 1, 0, 1.0f);
    }
}
