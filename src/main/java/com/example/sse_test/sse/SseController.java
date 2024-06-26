package com.example.sse_test.sse;


import com.example.sse_test.sse.service.SseTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
@CrossOrigin("*")
@RestController
@RequestMapping(value = "/sse")
@RequiredArgsConstructor
public class SseController {
    private final Map<Integer, Map<Integer, SseEmitter>> testEmitters = new HashMap<>();


    private final SseTestService sseTestService;


    @GetMapping(path = "/{topicId}/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToUpdates(
            @PathVariable int topicId,
            @PathVariable int userId
            ) {

        SseEmitter emitter = sseTestService.subscribe(topicId, userId);
        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }


    //eventPayload 를 SSE 로 연결된 모든 클라이언트에게 broadcasting 한다.
    @GetMapping(path = "/{topicId}/broadcast")
    public ResponseEntity<Void> broadcast(@PathVariable int topicId) {
        sseTestService.broadcast(topicId);
        return ResponseEntity.ok().build();
    }


    // 사용자가 SSE 구독을 종료할 때 호출될 API
    @DeleteMapping("/{topicId}/unsubscribe")
    public ResponseEntity<?> unsubscribe(@PathVariable int id, @RequestParam int userId) {
        Map<Integer, SseEmitter> userEmitters = testEmitters.get(id);
        if (userEmitters != null) {
            userEmitters.remove(userId);
        }
        return ResponseEntity.ok().build();
    }

    // 모든 사용자가 SSE 구독을 종료할 때 호출될 API
    @DeleteMapping("/{id}/unsubscribeAll")
    public ResponseEntity<?> unsubscribeAll(@PathVariable int id) {
        testEmitters.remove(id);
        return ResponseEntity.ok().build();
    }
}
