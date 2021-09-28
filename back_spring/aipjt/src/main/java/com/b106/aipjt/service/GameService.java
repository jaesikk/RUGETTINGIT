package com.b106.aipjt.service;

import com.b106.aipjt.domain.redishash.Room;
import com.b106.aipjt.domain.redishash.Round;
import com.b106.aipjt.domain.redishash.Skip;
import com.b106.aipjt.domain.redishash.User;
import com.b106.aipjt.domain.repository.RoomRedisRepository;
import com.b106.aipjt.domain.repository.RoundRedisRepository;
import com.b106.aipjt.domain.repository.SkipRedisRepository;
import com.b106.aipjt.domain.repository.UserRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final SimpMessagingTemplate template;
    private final UserRedisRepository userRedisRepository;
    private final RoundRedisRepository roundRedisRepository;
    private final RoomRedisRepository roomRedisRepository;
    private final SkipRedisRepository skipRedisRepository;

    private final String ROOM_PREFIX = "room/";


    // 에러 발생 시 캐치해서 방장한테만 알려주기 위해 동기로 동작하도록 작성
    public boolean gameExecute(String userId, String roomId) {
        log.error("========================서비스 호출 : 게임 시작===========================");
        // 게임 관련 설정은 여기서 하지 않고, 다른 요청에서 처리하도록 해준다.
        // User와 Room 객체를 가져온다.
        log.error("========================유저 & 방 조회===========================");
        Optional<User> userById = userRedisRepository.findById(userId);
        Optional<Room> roomById = roomRedisRepository.findById(roomId);
        // 검증한다.
        if (userById.isEmpty()) throw new RuntimeException("유저가 존재하지 않습니다");
        if (roomById.isEmpty()) throw new RuntimeException("방이 존재하지 않습니다");
        // 꺼낸다
        User user = userById.get();
        Room room = roomById.get();
        log.error("========================유저 & 방 검증===========================");
        // 검증한다
        if (!user.getId().equals(room.getSuperUser().getId())) throw new RuntimeException("방장만 게임을 시작할 수 있습니다");
        if (room.isStart()) throw new RuntimeException("이미 게임이 진행중입니다");
        log.error("========================게임 시작을 위한 방 설정===========================");
        // 게임 시작 적용을 위한 Room의 값 변경 후 save
        roomRedisRepository.save(room.gameStart());
        return true;
    }

    // 여기는 for 문과 Skip 객체를 사용해서 정해진 만큼의 라운드를 수행한다.
    // Skip 객체가 사라졌을 경우 쓰레드를 종료한다
    // 여기 정말 세심하게 짜야함
    @Async
    public void roundStart(String userId, String roomId) throws InterruptedException {
        log.error("========================서비스 호출 : 라운드 시작===========================");
        // 우선 방장인지부터 확인한다.
        Optional<User> userById = userRedisRepository.findById(userId);
        Optional<Room> roomById = roomRedisRepository.findById(roomId);
        // 검증 1
        if (userById.isEmpty()) throw new RuntimeException("유저가 존재하지 않습니다");
        if (roomById.isEmpty()) throw new RuntimeException("방이 존재하지 않습니다");
        // 꺼낸다
        User user = userById.get();
        Room room = roomById.get();
        log.error("========================라운드 검증 전===========================");
        log.error(user.toString());
        log.error(room.toString());
        // 검증 2
        if (!user.getId().equals(room.getSuperUser().getId())) throw new RuntimeException("방장만 라운드를 시작할 수 있습니다");
        if (!room.isStart()) throw new RuntimeException("이미 게임이 종료되었습니다");
        log.error("========================라운드 검증 후===========================");
        log.error("========================라운드 while 시작===========================");
        while (room.getCurrentRound() < room.getMaxRound()) {
            // 라운드 시작 : 라운드를 0에서 1로 변경해주고, 라운드 시작인 play를 true로 만들어준다
            room.setCurrentRound(room.getCurrentRound()+1);
            room.setPlay(true);
            log.error("========================현재 라운드 : "+room.getCurrentRound()+"===========================");
            // 질문 조회해서 Round에 넣어주기
            // 질문 조회했다고 가정하고, 라운드 객체에 값 세팅 후 저장
            Round round = roundRedisRepository.save(new Round(null, room.getRoundTime(), "문제", 1L));

            // 라운드를 저장하고 room을 저장해야 id가 null이 아니다
            room.getRound().add(round);
            room = roomRedisRepository.save(room);

            log.error("========================라운드 시작 메시지 전달===========================");
            // 방을 담아서 라운드 시작 메시지를 보낸다
            template.convertAndSend(ROOM_PREFIX+room.getId(), "방객체");
            log.error("========================라운드 시작 : 슬립===========================");
            // 라운드 진행시간동안 잔다
            Thread.sleep(room.getRoundTime()*1000);

            log.error("========================라운드 시작 : 슬립 종료===========================");
            // 라운드 종료 -> 이제부터 사이시간
            // 스킵 객체 생성
            Skip skip = skipRedisRepository.save(new Skip());
            // 사이시간이므로 false 세팅 후 room 갱신을 위해 저장
            room.setPlay(false);
            room = roomRedisRepository.save(room);

            log.error("========================사이시간 & 스킵 객체 메시지 전달===========================");
            // 방객체와 스킵 객체id를 보내준다
            template.convertAndSend(ROOM_PREFIX+room.getId(), "방객체와 슬립객체");
            log.error("========================사이시간 & 스킵 객체 메시지 전달 끝===========================");
            log.error("========================사이시간 슬립===========================");
            // 사이시간동안 잔다
            Thread.sleep(40000);
            log.error("========================사이시간 슬립 끝===========================");

            // 깨어나면 스킵 객체를 조회해서 스킵했는지 확인
            Optional<Skip> skipById = skipRedisRepository.findById(skip.getId());
            if (skipById.isEmpty()) return; // 스킵객체가 비어있으면 새 쓰레드가 생성된 것이므로 쓰레드 종료

            // 스킵 객체가 있으면 다음 라운드 진행을 위한 준비를 한다
            // 스킵객체를 삭제한다
            skipRedisRepository.delete(skipById.get()); // 스킵객체가 있으면 라운드 스킵 X -> 스킵 객체 삭제

            // 다시 while문이 돌거나, 라운드가 끝났다면 아래로 이동
            log.error("========================"+room.getCurrentRound()+"라운드 종료===========================");
        }
        // 여기까지 왔다면 쓰레드 리턴도 아니고 라운드가 다 종료되고 온 것. 따라서 isStart를 false로 만들어주고
        // currentRound도 0으로 만들어줘야 한다
        log.error("========================전체 라운드 종료===========================");
        room.setStart(false);
        room.setCurrentRound(0);
        room = roomRedisRepository.save(room);
        // 라운드 객체 비워주는게 빠짐 -> 자동삭제 되니까 괜찮은 것 같다.
    }
}