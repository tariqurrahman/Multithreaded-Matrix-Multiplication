import java.util.Random;

public class Consumer implements Runnable {

    private SharedBuffer sharedBuffer;
    private int maxConsumerSleepTime;
    private int totalSleep;
    private int countSleep;
    private int totalItemsConsumed;


    public Consumer(SharedBuffer sharedBuffer, int maxConsumerSleepTime) {
        this.sharedBuffer = sharedBuffer;
        this.maxConsumerSleepTime = maxConsumerSleepTime;
        this.totalSleep = 0;
        this.countSleep = 0;
        this.totalItemsConsumed = 0;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (!Producer.isSplitFinished || sharedBuffer.getCurrentSize() > 0) {
            try {
                WorkItem item = sharedBuffer.get();
                doMultiplication(item);
                totalItemsConsumed++;
                int sleepTime = random.nextInt(maxConsumerSleepTime);
                totalSleep += sleepTime;
                countSleep++;
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doMultiplication(WorkItem workItem) {
        int rowsSubA = workItem.getSubA().length;
        int columnsSubA = workItem.getSubA()[0].length;
        int columnsSubB = workItem.getSubB()[0].length;

        for (int i = 0; i < rowsSubA; i++) {
            for (int j = 0; j < columnsSubB; j++) {
                for (int k = 0; k < columnsSubA; k++) {
                    workItem.getSubC()[i][j] += workItem.getSubA()[i][k] * workItem.getSubB()[k][j];
                }
            }
        }
        workItem.setDone(true);
        doLog(workItem);
    }

    public int getTotalSleep() {
        return totalSleep;
    }

    public int getCountSleep() {
        return countSleep;
    }

    public int getTotalItemsConsumed() {
        return totalItemsConsumed;
    }

    private void doLog(WorkItem workItem) {
        int rowsSubA = workItem.getSubA().length;
        int columnsSubA = workItem.getSubA()[0].length;
        int columnsSubB = workItem.getSubB()[0].length;
        StringBuilder log = new StringBuilder();
        log.append("Consumer ").append(Thread.currentThread().getName()).append(" finishes calculating\n");
        log.append("         [ ");

        for (int i = 0; i < rowsSubA; i++) {
            if (i == 0) {
                log.append("[");
            } else {
                log.append("           [");
            }
            for (int j = 0; j < columnsSubA; j++) {
                log.append(workItem.getSubA()[i][j]);
                if (j < columnsSubA - 1) {
                    log.append(" ");
                }
            }
            if (i < rowsSubA - 1) {
                log.append("],\n");
            } else {
                log.append("] ]\n");
            }
        }


        log.append("  *      [ ");
        for (int i = 0; i < columnsSubA; i++) {
            if (i == 0) {
                log.append("[");
            } else {
                log.append("           [");
            }
            for (int j = 0; j < columnsSubB; j++) {
                log.append(workItem.getSubB()[i][j]);
                if (j < columnsSubB - 1) {
                    log.append(" ");
                }
            }
            if (i < columnsSubA - 1) {
                log.append("],\n");
            } else {
                log.append("] ]\n");
            }
        }

        log.append("  =>     [ ");
        for (int i = 0; i < rowsSubA; i++) {
            if (i == 0) {
                log.append("[");
            } else {
                log.append("           [");
            }
            for (int j = 0; j < columnsSubB; j++) {
                log.append(workItem.getSubC()[i][j]);
                if (j < columnsSubB - 1) {
                    log.append(" ");
                }
            }
            if (i < rowsSubA - 1) {
                log.append("],\n");
            } else {
                log.append("] ]\n");
            }
        }

        Main.writer.print(log.toString());
    }
}
