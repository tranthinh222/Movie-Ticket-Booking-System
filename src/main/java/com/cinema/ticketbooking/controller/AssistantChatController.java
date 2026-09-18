package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.request.ReqAssistantChatDto;
import com.cinema.ticketbooking.domain.response.ResAssistantChatDto;
import com.cinema.ticketbooking.service.AssistantChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantChatController {
    private final AssistantChatService service;

    public AssistantChatController(AssistantChatService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    public ResAssistantChatDto chat(@Valid @RequestBody ReqAssistantChatDto request) {
        return service.chat(request);
    }
}
