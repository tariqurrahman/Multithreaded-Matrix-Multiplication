import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static int DEFAULT_M = 10;
    public static int DEFAULT_N = 10;
    public static int DEFAULT_P = 10;
    public static int DEFAULT_MAX_BUFF_SIZE = 5;
    public static int DEFAULT_SPLIT_SIZE = 3;
    public static int DEFAULT_NUM_CONSUMER = 2;
    public static int DEFAULT_MAX_PRODUCER_SLEEP_TIME = 20;
    public static int DEFAULT_MAX_CONSUMER_SLEEP_TIME = 80;

    public static PrintWriter writer;

    public static void main(String[] args) throws InterruptedException {

        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("config.properties")) {
            properties.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int M = Integer.parseInt(properties.getProperty("M", String.valueOf(DEFAULT_M)));
        int N = Integer.parseInt(properties.getProperty("N", String.valueOf(DEFAULT_N)));
        int P = Integer.parseInt(properties.getProperty("P", String.valueOf(DEFAULT_P)));
        int maxBuffSize = Integer.parseInt(properties.getProperty("MaxBuffSize", String.valueOf(DEFAULT_MAX_BUFF_SIZE)));
        int splitSize = Integer.parseInt(properties.getProperty("SplitSize", String.valueOf(DEFAULT_SPLIT_SIZE)));
        int numConsumer = Integer.parseInt(properties.getProperty("NumConsumer", String.valueOf(DEFAULT_NUM_CONSUMER)));
        int maxProducerSleepTime = Integer.parseInt(properties.getProperty("MaxProducerSleepTime", String.valueOf(DEFAULT_MAX_PRODUCER_SLEEP_TIME)));
        int maxConsumerSleepTime = Integer.parseInt(properties.getProperty("MaxConsumerSleepTime", String.valueOf(DEFAULT_MAX_CONSUMER_SLEEP_TIME)));

        try {
            writer = new PrintWriter(new FileWriter("output.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[][] matrixA = createMatrix(M, N);
        int[][] matrixB = createMatrix(N, P);

        writer.println("First matrix:");
        printMatrix(matrixA, M, N);

        writer.println("Second matrix:");
        printMatrix(matrixB, N, P);
        writer.println("------------------------------------------------");

        SharedBuffer sharedBuffer = new SharedBuffer(maxBuffSize);
        Producer producer = new Producer(sharedBuffer, matrixA, matrixB, splitSize, maxProducerSleepTime);

        long start = Instant.now().toEpochMilli();
        Thread producerThread = new Thread(producer);

        List<Consumer> consumers = new ArrayList<>();
        for (int i = 0; i < numConsumer; i++) {
            Consumer consumer = new Consumer(sharedBuffer, maxConsumerSleepTime);
            Thread consumerThread = new Thread(consumer);
            consumers.add(consumer);
            consumerThread.setDaemon(true);
            consumerThread.start();
        }

        producerThread.start();
        producerThread.join();
        long end = Instant.now().toEpochMilli();
        doSeqMatrixMultiplication(matrixA, matrixB, M, N, P);
        logSimulationResult(sharedBuffer, maxBuffSize, 1, numConsumer, end-start, producer, consumers);

        writer.flush();
        writer.close();
    }

    public static int[][] createMatrix(int rows, int columns) {
        int[][] matrix = new int[rows][columns];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = rand.nextInt(10);
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix, int rows, int columns) {
        writer.printf("%10s", "[ ");
        for (int i = 0; i < rows; i++) {
            writer.printf("[");
            for (int j = 0; j < columns; j++) {
                writer.print(matrix[i][j]);
                if (j < columns - 1) {
                    writer.print(" ");
                }
            }
            if (i < rows - 1) {
                writer.println("],");
                writer.printf("%10s", "");
            }
        }
        writer.println("] ]");
    }

    public static void doSeqMatrixMultiplication(int[][] A, int[][] B, int M, int N, int P) {
        int[][] result = new int[M][P];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < P; j++) {
                for (int k = 0; k < N; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        writer.println("Verified result of sequential matrix multiplication: ");
        printMatrix(result, M, P);
        writer.println("------------------------------------------------");
    }

    public static void logSimulationResult(SharedBuffer buffer, int buffSize, int producers, int consumers, long duration, Producer producer, List<Consumer> consumersList) {
        AtomicInteger totalSleep = new AtomicInteger(producer.getTotalSleep());
        AtomicInteger totalSleepCount = new AtomicInteger(producer.getCountSleep());
        consumersList.forEach(
                consumer -> {
                    totalSleep.addAndGet(consumer.getTotalSleep());
                    totalSleepCount.addAndGet(consumer.getCountSleep());
                }
        );

        writer.println("PRODUCER / CONSUMER SIMULATION RESULT");
        writer.println("Simulation Time:                     " + duration + "ms");
        writer.println("Average Thread Sleep Time:           " + (double)totalSleep.get() / totalSleepCount.get());
        writer.println("Number of Producer Threads:          " + producers);
        writer.println("Number of Consumer Threads:          " + consumers);
        writer.println("Size of Buffer:                      " + buffSize);
        writer.println("Total Number of Items Produced:      " + producer.getTotalItemsProduced());
        writer.println(" Thread 0:                           " + producer.getTotalItemsProduced());
        writer.println("Total Number of Items Consumed:      " + producer.getTotalItemsProduced());
        for(int i=1; i<=consumersList.size(); i++){
            writer.println(" Thread " + i + ":                           " + consumersList.get(i-1).getTotalItemsConsumed());
        }
        writer.println("Number Of Items Remaining in Buffer: " + buffer.getCurrentSize());
        writer.println("Number Of Times Buffer Was Full:     " + buffer.getFullCount());
        writer.println("Number Of Times Buffer Was Empty:    " + buffer.getEmptyCount());
    }
}
