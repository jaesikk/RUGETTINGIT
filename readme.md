# INTRO

## 👩‍👩‍👦‍👦 팀원 소개

### 👾 한건(팀장)

FE, BE 고민 중입니다. 이번 프로젝트에서 FE, 배포를 맡게 됐습니다.

### 🐫 김준영

Backend를 지망하고 있습니다.  Spring 혹은 Django를 지망하고 있으며 Django, VueJS를 이용해 프로젝트를 진행한 경험이 있습니다. Docker, Jenkins, Nginx 리버스 프록시를 사용한 클라우드 서버 배포 및 배포자동화 경험이 있습니다. 이번 프로젝트에서는 BE를 담당하게 되었습니다. 맡은 부분 구현 후에 FE를 함께 구현 할 예정입니다. 

### 💃 이정준

BE 지망하고 있습니다. Springboot, JPA, Django, vueJS 프로젝트 경험 있습니다.

이번 프로젝트에서는 BE를 담당하고, 맡은 부분 구현 후에 FE를 함께 할 예정입니다. 

### 🧞‍♂️ 허진녕

Frontend를 지망하고 있습니다. 이번 프로젝트에서 FE, AI, 배포를 맡게 됐습니다.

### 🤓 엄재식

BE 지망하고 있습니다. 이번 프로젝트에서 BE/AI를 담당하게 되었습니다. 

---

## 🕵️ 프로젝트 소개

이미지 캡셔닝, TTS로 함께 하는 그림 그리기 놀이! (두둥 탁!)

AI가 본 이미지와 내가 그린 그림은 얼마나 다를까~~^^?

---

## 🐕‍🦺 서비스플로우

1. 닉네임 설정, 아바타 선택 (보기중에서 선택하게 하는 것이 현실적→ 색, 머리, 입, 눈, 코)
    1. 소켓 sender의 이름이 됨. 아바타는 어떻게 만들어야 할 지 고민
2. 대기방 입장 or 방 생성 (비밀방, 랜덤방을 어떻게 구현할지에 대한 고민이 필요)
3. 게임 설정 (문제 수, 인원 수, 커스텀 사진, 랜덤사진, 라운드, 라운드당 시간)
4. 게임 시작 (버튼클릭 하면 5, 4, 3, 2, 1과 같은 애니메이션 고려)
5. 게임 진행
    1. AI가 이미지 캡셔닝한 문장 전달
    2. 받은 문장을 보고 유저가 그림 그리기(페인팅 기능 상세 명세가 필요)
    3. 그린 그림을 AI가 이미지 캡셔닝
    4. 유사도 판별(재미 목적)
        1. 유사도 판별하는 API 찾아보기 (문장 유사도)
        2. 투표 : 그림을 누가그렸는지 안알려주고 투표하고, 그 다음에 누가그렸는지 알려주기
    5. 캡셔닝한 결과물 읽어주기 (TTS) || 점수 환산
    6. 채팅기능 구현 필요(말풍선, 채팅창)
6. 게임이 끝나면 다시 대기방으로 이동

---

## 📋 요구사항 명세서

🔗 [**https://recondite-sweatshirt-73b.notion.site/f3886584b50c4f7ebdff4718e4be2b51?v=cc6444ef92d044dea2911300983f1128**](https://www.notion.so/f3886584b50c4f7ebdff4718e4be2b51)

---

