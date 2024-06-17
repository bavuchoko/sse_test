package com.example.sse_test.sse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.MediaTypes;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseTestService {


    // thread-safe 한 컬렉션 객체로 sse emitter 객체를 관리해야 한다.
    private final Map<Integer, Map<Integer, SseEmitter>>  emitterMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;



    public SseEmitter subscribe(int topicId, int userId) {
        SseEmitter emitter = createEmitter();
        //연결 세션 timeout 이벤트 핸들러 등록
        emitter.onTimeout(() -> {
            log.info("server sent event timed out : topicId={} , userId={}", topicId, userId);
            //onCompletion 핸들러 호출
            emitter.complete();
        });

        //에러 핸들러 등록
        emitter.onError(e -> {
            log.info("server sent event error occurred : topicId={}, userId={}, message={}", topicId, userId, e.getMessage());
            //onCompletion 핸들러 호출
            emitter.complete();
        });

        //SSE complete 핸들러 등록
        emitter.onCompletion(() -> {
            log.info("disconnected by completed server sent event");
        });

        Map userMap =new HashMap<>();
        emitterMap.computeIfAbsent(topicId, k -> new ConcurrentHashMap<>()).put(userId, emitter);

        //초기 연결시에 응답 데이터를 전송할 수도 있다.
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    //event 명 (event: event example)
                    .name("subscribe")
                    .id(String.valueOf("id-1"))
                    .data("SSE connected")
                    .reconnectTime(RECONNECTION_TIMEOUT);
            emitter.send(event);
        } catch (IOException e) {
            log.error("failure send media position data, topicId={}, userId={}, message={}", topicId, userId, e.getMessage());
        }
        return emitter;
    }

    public void broadcast(int topicId) {



        Map<Integer, SseEmitter> userEmitters = emitterMap.get(topicId);

        if (userEmitters != null) {
            for (SseEmitter emitter : userEmitters.values()) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("broadCast")
                            .data("broadCasting", MediaTypes.HAL_JSON));
                    log.info("sended notification, topicId={}, payload={}", topicId, "broadCasting");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private SseEmitter createEmitter() {
        return new SseEmitter(TIMEOUT);
    }
}
