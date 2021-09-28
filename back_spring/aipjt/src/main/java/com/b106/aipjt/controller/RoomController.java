package com.b106.aipjt.controller;


import com.b106.aipjt.domain.dto.ResponseDto;
import com.b106.aipjt.domain.dto.room.RoomResponseDto;
import com.b106.aipjt.domain.dto.room.RoomUserResponseDto;
import com.b106.aipjt.domain.redishash.Room;
import com.b106.aipjt.service.RoomRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/room")
public class RoomController {
    private final RoomRedisService roomRedisService;
    private final SimpMessagingTemplate template; // socket 메시지 주고받기용

    // 방 생성
    @PostMapping("")
    public ResponseDto<RoomResponseDto> createRoom(@RequestHeader(value="User-Id") String userId){
        Room result = roomRedisService.createRoom(userId);
        RoomResponseDto build = RoomResponseDto.builder().id(result.getId()).build();
        System.out.println(build.getUserList());
        return new ResponseDto<>(HttpStatus.CREATED.value(), "방 생성 성공", build);
    }

    // 유저의 방 입장 : 여기서 id만 보내는게 아니라 아바타랑 닉네임도 보내야 함
    // RoomEnterRequestDto를 수정할 것. 실제로 백에 보내는 요청은 굳이 Param으로 보낼 필요 없으니 Body로 빼도 될듯
    @PostMapping("/user")
    public ResponseDto<RoomResponseDto> joinRoom(@RequestHeader(value="User-Id") String userId,
                                                 @RequestParam String roomId) {
        Room room = roomRedisService.joinRoom(userId, roomId);

        // 1.  User 리스트를 RoomUserResponseDto 변환
        List<RoomUserResponseDto> roomUsers = new ArrayList<>();
        room.getUserList().forEach(u -> {
            roomUsers.add(new RoomUserResponseDto(0, u.getAvatar(), u.getNickname(), room.getSuperUser().getId().equals(u.getId())));
        });

        // 2. Room 객체를 RoomResponseDto 변환
        RoomResponseDto build = RoomResponseDto.toRoom(room, roomUsers);

        // 방에 있는 다른 사람들에게 다시 방객체 정보 publish
        template.convertAndSend("/sub/chat/room/" + build.getId(), build);
        // 3. RoomResponseDto를 리턴
        return new ResponseDto<>(HttpStatus.OK.value(), "방 입장 성공", build);
    }

    // 유저의 방 퇴장
    @DeleteMapping("/user")
    public ResponseDto<RoomResponseDto> leaveRoom(@RequestHeader(value="User-Id") String userId,
                                                 @RequestParam String roomId) {
        Room room = roomRedisService.leaveRoom(userId, roomId);
        // 유저 퇴장했음을 채팅 창에 알림

        // UserList 새로 작성
        List<RoomUserResponseDto> roomUsers = new ArrayList<>();
        room.getUserList().forEach(user -> {
            roomUsers.add(new RoomUserResponseDto(
                user.getScore(), user.getAvatar(), user.getNickname(), room.getSuperUser().getId().equals(user.getId())
            ));
        });
        RoomResponseDto build = RoomResponseDto.toRoom(room, roomUsers);

        // 방에 있는 다른 사람들에게 다시 방객체 정보 publish
        template.convertAndSend("/sub/chat/room/" + room.getId(), build);
        return new ResponseDto<>(HttpStatus.OK.value(), "멤버 퇴장", build);
    }
}
