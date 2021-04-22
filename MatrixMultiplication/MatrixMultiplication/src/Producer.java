import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Producer implements Runnable {

    private SharedBuffer sharedBuffer;
    private int splitSize;
    private int[][] matrixA;
    private int[][] matrixB;

    private List<WorkItem> workItems;

    private int matrixARows;
    private int matrixAColumns;
    private int matrixBRows;
    private int matrixBColumns;

    private int maxProducerSleepTime;
    private int totalSleep;
    private int countSleep;
    private int itemsProduced;

    public static volatile boolean isSplitFinished;


    public Producer(SharedBuffer sharedBuffer, int[][] matrixA, int[][] matrixB, int splitSize, int maxProducerSleepTime) {
        this.sharedBuffer = sharedBuffer;
        this.splitSize = splitSize;
        this.matrixA = matrixA;
        this.matrixB = matrixB;
        this.workItems = new ArrayList<>();

        this.matrixARows = matrixA.length;
        this.matrixAColumns = matrixA[0].length;
        this.matrixBRows = matrixB.length;
        this.matrixBColumns = matrixB[0].length;

        this.maxProducerSleepTime = maxProducerSleepTime;

        isSplitFinished = false;
    }

    @Override
    public void run() {
        try {
            doSplit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isSplitFinished = true;

        try {
            createResultMatrixC();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doSplit() throws InterruptedException {
        Random random = new Random();
        for (int lowA = 0; lowA < matrixARows; lowA += splitSize) {
            int highA = Math.min(lowA + splitSize, matrixARows);
            for (int lowB = 0; lowB < matrixBColumns; lowB += splitSize) {
                int highB = Math.min(lowB + splitSize, matrixBColumns);

                WorkItem workItem = new WorkItem();
                workItem.setSubA(createSubA(lowA, highA));
                workItem.setSubB(createSubB(lowB, highB));
                workItem.setSubC(createSubC(highA - lowA, highB - lowB));
                workItem.setLowA(lowA);
                workItem.setHighA(highA);
                workItem.setLowB(lowB);
                workItem.setHighB(highB);
                workItems.add(workItem);
                writeLog(workItem);
                sharedBuffer.put(workItem);
                int sleepTime = random.nextInt(maxProducerSleepTime);
                totalSleep += sleepTime;
                countSleep++;
                Thread.sleep(sleepTime);
            }
        }
    }

    private int[][] createSubA(int lowA, int highA) {
        int[][] subA = new int[highA - lowA][matrixAColumns];
        for (int i = 0; i < highA - lowA; i++) {
            for (int j = 0; j < matrixAColumns; j++) {
                subA[i][j] = matrixA[i + lowA][j];
            }
        }
        return subA;
    }

    private int[][] createSubB(int lowB, int highB) {
        int[][] subB = new int[matrixBRows][highB - lowB];
        for (int i = 0; i < matrixBRows; i++) {
            for (int j = 0; j < highB - lowB; j++) {
                subB[i][j] = matrixB[i][j + lowB];
            }
        }
        return subB;
    }

    private int[][] createSubC(int rows, int columns) {
        return new int[rows][columns];
    }

    private void createResultMatrixC() throws InterruptedException {
        int[][] matrixC = new int[matrixARows][matrixBColumns];
        while (!workItems.stream().allMatch(WorkItem::isDone)) {
            Thread.sleep(1);
        }
        workItems.forEach(
                workItem -> {
                    int[][] subC = workItem.getSubC();
                    int rowsSubC = subC.length;
                    int columnSubC = subC[0].length;
                    for (int i = 0; i < rowsSubC; i++) {
                        for (int j = 0; j < columnSubC; j++) {
                            matrixC[i + workItem.getLowA()][j + workItem.getLowB()] = subC[i][j];
                        }
                    }
                }
        );
        writeResultToLog(matrixC);
    }

    public int getTotalSleep() {
        return totalSleep;
    }

    public int getCountSleep() {
        return countSleep;
    }

    private void writeResultToLog(int[][] matrixC) {
        StringBuilder log = new StringBuilder();
        log.append("Producer successfully assembly all the results from consumer threads\n");
        log.append("------------------------------------------------\n");


        log.append("Final result of parallel matrix multiplication:\n");
        log.append("         [ ");
        for (int i = 0; i < matrixARows; i++) {
            if (i == 0) {
                log.append("[");
            } else {
                log.append("           [");
            }
            for (int j = 0; j < matrixBColumns; j++) {
                log.append(matrixC[i][j]);
                if (j < matrixBColumns - 1) {
                    log.append(" ");
                }
            }
            if (i < matrixARows - 1) {
                log.append("],\n");
            } else {
                log.append("] ]\n");
            }
        }
        Main.writer.print(log.toString());
    }

    private void writeLog(WorkItem item) {
        Main.writer.println("Producer puts row " + item.getLowA() + "-" + item.getHighA() + " of matrix A and column " +
                item.getLowB() + "-" + item.getHighB() + " of B to buffer");
    }

    public int getTotalItemsProduced() {
        return workItems.size();
    }
}