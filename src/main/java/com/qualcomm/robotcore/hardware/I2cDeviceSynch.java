package com.qualcomm.robotcore.hardware;

public interface I2cDeviceSynch extends I2cDeviceSynchSimple, Engagable {

    public enum ReadMode {
        REPEAT,
        BALANCED,
        ONLY_ONCE
    }

    void ensureReadWindow(ReadWindow readWindow, ReadWindow readWindow2);

    HeartbeatAction getHeartbeatAction();

    int getHeartbeatInterval();

    ReadWindow getReadWindow();

    TimestampedData readTimeStamped(int i, int i2, ReadWindow readWindow, ReadWindow readWindow2);

    void setHeartbeatAction(HeartbeatAction heartbeatAction);

    void setHeartbeatInterval(int i);

    void setReadWindow(ReadWindow readWindow);

    public static class HeartbeatAction {
        public final ReadWindow heartbeatReadWindow;
        public final boolean rereadLastRead;
        public final boolean rewriteLastWritten;

        public HeartbeatAction(boolean z, boolean z2, ReadWindow readWindow) {
            this.rereadLastRead = z;
            this.rewriteLastWritten = z2;
            this.heartbeatReadWindow = readWindow;
        }
    }

    public static class ReadWindow {
        public static final int READ_REGISTER_COUNT_MAX = 26;
        public static final int WRITE_REGISTER_COUNT_MAX = 26;
        private final int creg;
        private final int iregFirst;
        private final ReadMode readMode;
        private boolean usedForRead = false;

        public int getRegisterFirst() {
            return this.iregFirst;
        }

        public int getRegisterMax() {
            return this.iregFirst + this.creg;
        }

        public int getRegisterCount() {
            return this.creg;
        }

        public ReadMode getReadMode() {
            return this.readMode;
        }

        public boolean hasWindowBeenUsedForRead() {
            return this.usedForRead;
        }

        public void noteWindowUsedForRead() {
            this.usedForRead = true;
        }

        public boolean canBeUsedToRead() {
            return !this.usedForRead || this.readMode != ReadMode.ONLY_ONCE;
        }

        public boolean mayInitiateSwitchToReadMode() {
            return !this.usedForRead || this.readMode == ReadMode.REPEAT;
        }

        public ReadWindow(int i, int i2, ReadMode readMode2) {
            this.readMode = readMode2;
            this.iregFirst = i;
            this.creg = i2;
            if (i2 < 0 || i2 > 26) {
                throw new IllegalArgumentException(String.format("buffer length %d invalid; max is %d", new Object[]{Integer.valueOf(i2), 26}));
            }
        }

        public ReadWindow readableCopy() {
            return new ReadWindow(this.iregFirst, this.creg, this.readMode);
        }

        public boolean sameAsIncludingMode(ReadWindow readWindow) {
            if (readWindow != null && getRegisterFirst() == readWindow.getRegisterFirst() && getRegisterCount() == readWindow.getRegisterCount() && getReadMode() == readWindow.getReadMode()) {
                return true;
            }
            return false;
        }

        public boolean contains(ReadWindow readWindow) {
            return readWindow != null && getRegisterFirst() <= readWindow.getRegisterFirst() && readWindow.getRegisterMax() <= getRegisterMax();
        }

        public boolean containsWithSameMode(ReadWindow readWindow) {
            return contains(readWindow) && getReadMode() == readWindow.getReadMode();
        }

        public boolean contains(int i, int i2) {
            return containsWithSameMode(new ReadWindow(i, i2, getReadMode()));
        }
    }
}
