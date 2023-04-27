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

public class GroupBully {

    public static AtomicInteger messageCount;

    public static void main(String[] args) {
        // ... (Create processes and simulate coordinator failure)

        int numGroups = 5; // You can set the number of groups here
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
            newCoordinator = startElection(processes, groupLeaders.get(0), groupLeaders.get(groupLeaders.size() - 1));
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
                    int result = startElection(processes, i, end);
                    if (result > maxId) {
                        maxId = result;
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
