package com.oetp;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OnlineEventTicketingApplication {

	public static void main(String[] args) {

		ApplicationContext context=SpringApplication.run(OnlineEventTicketingApplication.class, args);
		TicketService service=context.getBean(TicketService.class);
		service.addEvent(new Event(1,"Cold Play",100));
		service.addEvent(new Event(2,"Pop Fest",100));

	}

}
