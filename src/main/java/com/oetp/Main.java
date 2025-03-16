package com.oetp;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        TicketService service= new TicketService();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        service.addEvent(new Event(1,"ColdPlay", 100));
        Scanner scanner = new Scanner(System.in);

        System.out.println("OETP CLI Started - type 'help' for commands");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+");

            try {
                if (parts[0].equals("exit")) {
                    pool.shutdown();
                    pool.awaitTermination(2, TimeUnit.SECONDS); // Graceful shutdown
                    System.out.println("Final status - Queued: " + service.getQueuedBookings() +
                            ", Completed: " + service.getCompletedBookings());
                    break;
                } else if (parts[0].equals("help")) {
                    System.out.println("Commands:");
                    System.out.println("  list - Show events with tickets");
                    System.out.println("  add <id> <name> <tickets> - Add event");
                    System.out.println("  book <user> <eventId> <tickets> - Book tickets");
                    System.out.println("  remove <id> - Remove event");
                    System.out.println("  find <name> - Find event by name");
                    System.out.println("  status - Show booking status");
                    System.out.println("  exit - Quit");
                } else if (parts[0].equals("list")) {
                    service.listEvents();
                } else if (parts[0].equals("add") && parts.length == 4) {
                    int id = Integer.parseInt(parts[1]);
                    String name = parts[2];
                    int tickets = Integer.parseInt(parts[3]);
                    if (tickets < 0) throw new NumberFormatException("Tickets can't be negative");
                    if(service.addEvent(new Event(id, name, tickets))){
                        System.out.println("Added: " + name);
                    }

                } else if (parts[0].equals("book") && parts.length == 4) {
                    String user = parts[1];
                    int eventId = Integer.parseInt(parts[2]);
                    int tickets = Integer.parseInt(parts[3]);
                    if (tickets <= 0) throw new NumberFormatException("Tickets must be positive");
                    service.bookTicket(user, eventId, tickets)
                            .thenAccept(System.out::println);
                    System.out.println(user + " booking queued...");
                } else if (parts[0].equals("remove") && parts.length == 2) {
                    int id = Integer.parseInt(parts[1]);
                    if (service.removeEvent(id)) {
                        System.out.println("Removed event ID: " + id);
                    } else {
                        System.out.println("Event not found: " + id);
                    }
                } else if (parts[0].equals("find") && parts.length == 2) {
                    String name = parts[1];
                    Event event = service.findEventByName(name);
                    if (event != null) {
                        System.out.println("Found: ID " + event.getId() + ", Tickets: " + event.getAvailableTickets());
                    } else {
                        System.out.println("Event not found: " + name);
                    }
                } else if (parts[0].equals("status")) {
                    System.out.println("Bookings - Queued: " + service.getQueuedBookings() +
                            ", Completed: " + service.getCompletedBookings());
                } else if (parts[0].equals("stats")){
                    service.printStats();
                }
                else {
                    System.out.println("Unknown command - type 'help'");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: " + (e.getMessage() != null ? e.getMessage() : "Invalid number"));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}