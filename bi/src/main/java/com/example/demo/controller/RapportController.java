package com.example.demo.controller;

import com.example.demo.model.RapportRequest;
import com.example.demo.model.RapportResponse;
import com.example.demo.services.RapportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rapport")
@CrossOrigin(origins = "http://localhost:4200")
public class RapportController {

    private final RapportService service;

    public RapportController(RapportService service) {
        this.service = service;
    }

    @PostMapping("/generer")
    public RapportResponse generer(@RequestBody RapportRequest request) {
        return service.generer(request);
    }
}