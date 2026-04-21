package ru.ifmo.project.service;

import ru.ifmo.project.util.LoggerThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggingService {
    private static LoggingService instance;
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final LoggerThread loggerThread;

    private LoggingService() {
        loggerThread = new LoggerThread(logQueue);
        loggerThread.start();
    }

    public static LoggingService getInstance() {
        if (instance == null) {
            instance = new LoggingService();
        }
        return instance;
    }

    public void log(String message) {
        try {
            logQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        loggerThread.shutdown();
    }
}