import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedBuffer {

    private Queue<WorkItem> list;
    private int size;

    public AtomicInteger fullCount;
    public AtomicInteger emptyCount;

    public SharedBuffer(int size) {
        this.list = new LinkedList<>();
        this.size = size;

        this.fullCount = new AtomicInteger(0);
        this.emptyCount = new AtomicInteger(0);;
    }

    public void put(WorkItem item) throws InterruptedException {
        synchronized (this) {
            while (list.size() >= size) {
                fullCount.addAndGet(1);
                Main.writer.println("The queue is full " + Thread.currentThread().getName() + " is waiting, size: " + list.size());
                wait();
            }
            list.add(item);
            notify();
        }
    }

    public WorkItem get() throws InterruptedException {
        synchronized (this) {
            while (list.size() == 0) {
                emptyCount.addAndGet(1);
                Main.writer.println("The queue is empty " + Thread.currentThread().getName() + " is waiting");
                wait();
            }
            WorkItem item = list.poll();
            Main.writer.println("Consumer " + Thread.currentThread().getName() + " gets row  " + item.getLowA() + "-" + item.getHighA() + " of matrix A and column " +
                    item.getLowB() + "-" + item.getHighB() + " of B from buffer");
            notify();
            return item;
        }
    }

    public synchronized int getCurrentSize() {
        return list.size();
    }

    public int getFullCount() {
        return fullCount.get();
    }

    public int getEmptyCount() {
        return emptyCount.get();
    }
}