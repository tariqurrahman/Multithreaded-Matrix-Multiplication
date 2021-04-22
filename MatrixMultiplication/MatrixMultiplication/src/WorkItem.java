public class WorkItem {
    private int[][] subA;
    private int[][] subB;
    private int[][] subC;

    private int lowA;
    private int highA;

    private int lowB;
    private int highB;

    private boolean done;

    public int[][] getSubA() {
        return subA;
    }

    public void setSubA(int[][] subA) {
        this.subA = subA;
    }

    public int[][] getSubB() {
        return subB;
    }

    public void setSubB(int[][] subB) {
        this.subB = subB;
    }

    public int[][] getSubC() {
        return subC;
    }

    public void setSubC(int[][] subC) {
        this.subC = subC;
    }

    public int getLowA() {
        return lowA;
    }

    public void setLowA(int lowA) {
        this.lowA = lowA;
    }

    public int getHighA() {
        return highA;
    }

    public void setHighA(int highA) {
        this.highA = highA;
    }

    public int getLowB() {
        return lowB;
    }

    public void setLowB(int lowB) {
        this.lowB = lowB;
    }

    public int getHighB() {
        return highB;
    }

    public void setHighB(int highB) {
        this.highB = highB;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
