package ru.ifmo.project.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

public class LoggerThread extends Thread {
    private final BlockingQueue<String> queue;
    private volatile boolean running = true;

    public LoggerThread(BlockingQueue<String> queue) {
        this.queue = queue;
        setDaemon(true); // поток-демон, чтобы JVM могла завершиться
    }

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("app.log", true))) {
            while (running || !queue.isEmpty()) {
                String message = queue.take(); // блокируется, если очередь пуста
                writer.write(LocalDateTime.now() + " - " + message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}