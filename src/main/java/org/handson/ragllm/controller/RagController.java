package org.handson.ragllm.controller;

import org.handson.ragllm.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173") // עדיף להגדיר את הכתובת של ה-React
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * עדכון: שימוש ב-@RequestParam לכל הפרמטרים כדי לתמוך ב-FormData מה-React
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(
            @RequestParam("pdfId") Long pdfId,
            @RequestParam("userId") Long userId,
            @RequestParam("question") String question) {

        try {
            // קריאה ל-Gemini דרך ה-RagService
            String answer = ragService.askQuestion(pdfId, question, userId);

            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (Exception e) {
            // במקרה של שגיאה, מחזירים הודעה מתאימה ל-UI
            return ResponseEntity.status(500).body(Map.of("answer", "שגיאה בעיבוד השאלה: " + e.getMessage()));
        }
    }
}