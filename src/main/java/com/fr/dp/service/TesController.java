package com.fr.dp.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fr.dp.service.dto.ApiInfoDTO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@AllArgsConstructor
public class TesController {

    private ApiRegistry apiRegistry;

    ChatModel chatModel;

    @PostMapping("/register")
    public String registerMethod(@RequestBody ApiInfoDTO apiInfoDTO) {
        apiRegistry.register(apiInfoDTO.getPath(), apiInfoDTO);
        log.info("registerMethod: {}", apiInfoDTO);
        return "注册成功";
    }

    @PostMapping("/unregister")
    public String unregisterMethod(@RequestBody ApiInfoDTO apiInfoDTO) {
        apiRegistry.unregister(apiInfoDTO.getPath());
        log.info("unregisterMethod: {}", apiInfoDTO);
        return "注销成功";
    }

    @GetMapping("chat")
    public String chat(@RequestParam String prompt) {
        ChatClient chatClient = ChatClient.create(chatModel);
        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }

    private final ObjectMapper objectMapper;

    @GetMapping(value = "chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestParam String prompt) {
        return ChatClient.create(chatModel)
                .prompt(prompt)
                .stream()
                .chatResponse().map(
                        chatResponse -> ServerSentEvent.builder(
                                        toJson(chatResponse)
                                ).event("message")
                                .build()
                );
    }

    @SneakyThrows
    public String toJson(ChatResponse chatResponse) {
        return objectMapper.writeValueAsString(chatResponse);
    }
}
