# VIP AND RENDEZVOUS - BACKEND
<br>

## 프로젝트 개요
VIP And Rendezvous는 워렌 버핏의 파워 런치 옥션에 영감을 받아 탄생한 유명인과의 식사 시간을 거래할 수 있는 경매 서비스입니다.  
해당 프로젝트는 단순한 만남을 넘어 유명인과 사용자가 서로 영감을 주고 받으며 성장할 수 있는 기회를 제공하는 것이 핵심입니다.  
또한 낙찰금의 일부는 사회 발전을 위한 기부금으로 사용되며 개인의 성장과 더불어 사회의 성장도 목표로 하고 있습니다.
<br>
<br>

## 사용한 기술 스택
- Java 17
- Spring Boot
- JPA
- MySQL
- QueryDSL
- Redis
- Quartz
- Spring Security, OAuth2, JWT
- Thymeleaf
- Gradle
<br>

## 프로젝트 아키텍처
<img width="416" alt="스크린샷 2024-10-22 오후 7 54 41" src="https://github.com/user-attachments/assets/b49da97e-04b4-4f00-9a74-e593b5d9a90f">
<br>
<br>

## 내가 한 역할
### 1. 경매 & 식사권 스케줄링

   quartz 라이브러리를 사용한 동적 스케줄링
   - 경매가 마감할 때마다 최고 입찰자와 경매 주최자에게 자동으로 식사권이 생성하기 위해 사용  
   
   
  
   Scheduled 어노테이션 사용하여 스케줄링
   - 매일 자정마다 식사권 완료 처리를 하기 위해 사용  
   
   
  
   EventListener 어노테이션
   - 서버가 재시작 되더라도 예정된 스케줄링 job들을 수행하기 위해 사용  
   
        
  
### 2. 포인트 증가 & 감소 처리
   Lock 어노테이션을 통해 비관적 락 적용
   - 사용자 포인트와 관리자 지갑, 응찰 부분에서 동시성 문제가 발생할 가능성이 있어서 사용
     <br>
     
### 3. 실시간 응찰과 채팅 처리
   WebSocket
   - 실시간 경매, 채팅 로직을 구현하기 위해 사용 
<br>

## ERD
![VAR](https://github.com/user-attachments/assets/97b186e4-0870-413e-a4eb-6f9fa4b9ad4f)
<br>
<br>

## 와이어프레임
https://excalidraw.com/
<br>
<br>

## 기능 개발 프로세스
<img width="686" alt="스크린샷 2024-10-22 오후 7 45 03" src="https://github.com/user-attachments/assets/6e4f7be1-ad69-4735-a5d9-cd67780a3b76">
<br>
