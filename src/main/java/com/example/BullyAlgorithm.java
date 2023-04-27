package com.example;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class Process {
    int id;
    boolean active;

    public Process(int id, boolean active) {
        this.id = id;
        this.active = active;
    }
}

class Message {
    int from;
    String type;

    public Message(int from, String type) {
        this.from = from;
        this.type = type;
    }
}

public class BullyAlgorithm {
    
    public static AtomicInteger messageCount;

    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        messageCount = new AtomicInteger(0);
        int numProcesses = 20; // Set the number of processes here
    
        // Create processes and add them to the list
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, true));
        }
    
        // Simulate a coordinator failure
        processes.get(numProcesses - 1).active = false;
        System.out.println("Coordinator (Process " + (numProcesses - 1) + ") has failed.");
    
        int numGroups = 4; // You can set the number of groups here
        int groupSize = (int) Math.ceil((double) processes.size() / numGroups);
        List<Integer> groupLeaders = new ArrayList<>();
    
        // Elect group leaders
        for (int i = 0; i < numGroups; i++) {
            int groupStart = i * groupSize;
            int groupEnd = Math.min((i + 1) * groupSize - 1, processes.size() - 1);
            int groupLeader = startElection(processes, groupStart, groupEnd);
            if (groupLeader != -1) {
                groupLeaders.add(groupLeader);
            }
        }
    
        // Elect overall leader from group leaders
        int newCoordinator = -1;
        if (!groupLeaders.isEmpty()) {
            int overallGroupStart = processes.indexOf(processes.stream().filter(p -> p.id == groupLeaders.get(0)).findFirst().orElse(null));
            int overallGroupEnd = processes.indexOf(processes.stream().filter(p -> p.id == groupLeaders.get(groupLeaders.size() - 1)).findFirst().orElse(null));
            newCoordinator = startElection(processes, overallGroupStart, overallGroupEnd);
        }
        System.out.println("New coordinator is: Process " + newCoordinator);
        System.out.println("Total messages sent: " + messageCount.get());
    }

    public static int startElection(List<Process> processes, int start, int end) {
        System.out.println("Election started by Process " + start);

        int maxId = -1;
        boolean receivedHigherId = false;

        for (int i = start + 1; i <= end; i++) {
            Process process = processes.get(i);
            if (process.active) {
                Message electionMessage = new Message(processes.get(start).id, "ELECTION");
                Message responseMessage = sendMessage(process, electionMessage);

                if (responseMessage != null && "OK".equals(responseMessage.type)) {
                    receivedHigherId = true;
                    if (responseMessage.from > maxId) {
                        maxId = responseMessage.from;
                    }
                }
            }
        }

        if (!receivedHigherId) {
            System.out.println("Process " + processes.get(start).id + " becomes the new coordinator.");
            for (int i = start - 1; i >= 0; i--) {
                Process process = processes.get(i);
                if (process.active) {
                    Message coordinatorMessage = new Message(processes.get(start).id, "COORDINATOR");
                    sendMessage(process, coordinatorMessage);
                }
            }
            return processes.get(start).id;
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
