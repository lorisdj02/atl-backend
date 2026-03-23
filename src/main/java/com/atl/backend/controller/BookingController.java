package com.atl.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.response.BookFieldResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.BookingScreenService;
import com.atl.backend.service.BookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingScreenService bookingScreenService;
    private final AccountService accountService;

    public BookingController(BookingScreenService bookingScreenService, AccountService accountService) {
        this.bookingScreenService = bookingScreenService;
        this.accountService = accountService;
    }

    @GetMapping("/book-a-field")
    public BookFieldResponse getBookFieldResponse() {
        return bookingScreenService.getBookFieldResponse();
    }
    
    
}
