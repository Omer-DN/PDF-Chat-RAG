package org.handson.ragllm.controller;

import org.handson.ragllm.model.QuestionRequest;
import org.handson.ragllm.service.*; // וודא שזה השם העדכני של ה-Service שלך
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * מקבל שאלה מה-UI ומחזיר תשובה מה-PDF
     */
    @PostMapping("/ask")
    public Map<String, String> ask(
            @RequestParam Long pdfId,
            @RequestBody QuestionRequest request) {

        // קריאה ל-Gemini דרך ה-RagService
        String answer = ragService.askQuestion(pdfId, request.getQuestion(), request.getUserId());

        return Map.of("answer", answer);
    }
}