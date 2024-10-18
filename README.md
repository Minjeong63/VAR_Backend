# VIP AND RENDEZVOUS - BACKEND

## 기획 의도
VIP And Rendezvous는 워렌 버핏의 파워 런치 옥션에 영감을 받아 탄생한 VIP와의 식사 시간을 거래할 수 있는 경매 서비스입니다.  
해당 프로젝트는 단순한 만남을 넘어 VIP와 사용자가 서로 영감을 주고 받으며 성장할 수 있는 기회를 제공하는 것이 핵심입니다.  
또한 낙찰금의 일부는 사회 발전을 위한 기부금으로 사용되며 개인의 성장과 더불어 사회의 성장도 목표로 하고 있습니다.  

## 내가 한 역할
- 경매 생성
- 경매 취소
- quartz 라이브러리와 Scheduled를 이용한 자동 스케줄링(식사권 생성, 식사권 완료)
- 웹소켓을 통한 응찰과 채팅 로직

## ERD

## 사용 기술
1. 사용자 포인트와 관리자 지갑, 응찰 부분에서 동시성 문제가 발생할 가능성이 있어 비관적 락 적용
2. 식사권 생성과 완료 처리를 위해 quartz 라이브러리와 Scheduled 어노테이션을 사용하여 스케줄링 구현
3. 채팅과 입찰 과정을 실시간으로 진행하기 위해 웹소켓 사용
