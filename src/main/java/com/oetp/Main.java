package com.oetp;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;

public class Main {
    public static void main(String[] args) {

        Event concert = new Event(1, "ColdPlay Concert", 10);

        TicketService service = new TicketService(concert);

        Thread thread1 = new Thread(() -> service.bookTickets("User 1", 3));

        Thread thread2 = new Thread() {
            @Override
            public void run() {
                service.bookTickets("User 2", 3);
            }
        };

        thread1.start();
        thread2.start();
    }
}