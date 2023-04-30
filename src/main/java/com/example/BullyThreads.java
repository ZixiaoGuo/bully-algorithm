package com.example;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BullyThreads {
    public static AtomicInteger messageCount;
    public static ExecutorService executor;

    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        messageCount = new AtomicInteger(0);
        int numProcesses = 5;
        
        // Create an ExecutorService to handle message sending concurrently
        executor = Executors.newFixedThreadPool(numProcesses);
        
        // Create processes and add them to the list
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, true));
        }

        // Simulate a coordinator failure
        processes.get(numProcesses - 1).active = false;
        System.out.println("Coordinator (Process " + (numProcesses - 1) + ") has failed.");

        // Start the election process
        int newCoordinator = startElection(processes, 1);
        System.out.println("New coordinator is: Process " + newCoordinator);
        System.out.println("Total messages sent: " + messageCount.get());
        
        // Shutdown the executor service
        executor.shutdown();
    }

    public static int startElection(List<Process> processes, int initiator) {
        System.out.println("Election started by Process " + initiator);

        int maxId = -1;
        boolean receivedHigherId = false;

        List<Future<Message>> futures = new ArrayList<>();
        
        for (Process process : processes) {
            if (process.id > initiator && process.active) {
                Message electionMessage = new Message(initiator, "ELECTION");
                futures.add(executor.submit(() -> sendMessage(process, electionMessage)));
            }
        }

        for (Future<Message> future : futures) {
            try {
                Message responseMessage = future.get();
                if (responseMessage != null && "OK".equals(responseMessage.type)) {
                    receivedHigherId = true;
                    int result = startElection(processes, responseMessage.from);
                    if (result > maxId) {
                        maxId = result;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (!receivedHigherId) {
            System.out.println("Process " + initiator + " becomes the new coordinator.");
            for (Process process : processes) {
                if (process.id < initiator && process.active) {
                    Message coordinatorMessage = new Message(initiator, "COORDINATOR");
                    executor.submit(() -> sendMessage(process, coordinatorMessage));
                }
            }
            return initiator;
        }

        return maxId;
    }

    public static Message sendMessage(Process receiver, Message message) {
        messageCount.incrementAndGet();
        if (!receiver.active) {
            return null;
        }

        if ("ELECTION".equals(message.type)) {
            System.out.println("OK message send from process: " + receiver.id);
            return new Message(receiver.id, "OK");
        } else if ("COORDINATOR".equals(message.type)) {
            System.out.println("Process " + receiver.id + " acknowledges new coordinator: Process " + message.from);
        }

        return null;
    }
}

