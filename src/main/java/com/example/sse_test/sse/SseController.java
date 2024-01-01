package com.example.sse_test.sse;


import com.example.sse_test.sse.entity.SseTest;
import com.example.sse_test.sse.service.SseTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/sse")
@RequiredArgsConstructor
public class SseController {
    private final Map<Integer, Map<Integer, SseEmitter>> testEmitters = new HashMap<>();


    private final SseTestService sseTestService;

    @PostMapping("data")
    public void createData() {
        SseTest sse1= SseTest.builder().id(1).build();
        sseTestService.insertData(sse1);
        SseTest sse2= SseTest.builder().id(2).build();
        sseTestService.insertData(sse2);
    }
    @GetMapping(path = "/{id}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToUpdates(
            @PathVariable int id) {

        SseEmitter emitter = sseTestService.subscribe(id, 1);

        // 게임마다 사용자와 SSEEmitter를 매핑
        testEmitters.computeIfAbsent(id, k -> new HashMap<>()).put(1, emitter);

        // 클라이언트 연결 종료 시 처리
        emitter.onCompletion(() -> {
            Map<Integer, SseEmitter> userEmitters = testEmitters.get(id);
            if (userEmitters != null) {
                userEmitters.remove(1, emitter);
            }
        });

        emitter.onTimeout(() -> {
            Map<Integer, SseEmitter> userEmitters = testEmitters.get(id);
            if (userEmitters != null) {
                userEmitters.remove(1, emitter);
            }
        });
        return ResponseEntity.ok(emitter);
    }


    //eventPayload 를 SSE 로 연결된 모든 클라이언트에게 broadcasting 한다.
    @GetMapping(path = "/{id}/broadcast")
    public ResponseEntity<Void> broadcast(@PathVariable int id) {
        sseTestService.broadcast(id);
        return ResponseEntity.ok().build();
    }


    // 사용자가 SSE 구독을 종료할 때 호출될 API
    @DeleteMapping("/{id}/unsubscribe")
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
