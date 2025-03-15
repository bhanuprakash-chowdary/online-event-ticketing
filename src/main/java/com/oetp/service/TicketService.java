package com.oetp.service;

import com.oetp.domain.Event;

public class TicketService {

    private final Event event;

    public TicketService(Event event){
        this.event=event;
    }

    public void bookTickets(String user,int quantity){

       if(event.getAvailableTickets()>=quantity){

           System.out.println(user +" booking "+ quantity +" for "+ event.getName()+ "( Available at the moment "+ event.getAvailableTickets()+")");

           try {
               Thread.sleep(100);
           }catch (InterruptedException e) {
               e.printStackTrace();
           }
           event.reduceTickets(quantity);
           System.out.println(user + " booked " + quantity + ". Remaining: " + event.getAvailableTickets());

       }else {
           System.out.println(user + " failed - not enough tickets.");
       }
    }
}
