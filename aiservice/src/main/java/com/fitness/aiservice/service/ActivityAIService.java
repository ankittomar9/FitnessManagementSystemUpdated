package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

        private final GeminiService geminiService;

        public String generateRecommendation(Activity activity) {
            String prompt= createPromptForActivity(activity);
            String aiResponse = geminiService.getAnswer(prompt);
            log.info("AI Response: {}", aiResponse);
            return  aiResponse;
        }

        private String createPromptForActivity(Activity activity) {

        }

}
