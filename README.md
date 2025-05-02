# 🍽️ Table-Now 

실시간 식당 예약 및 방문 관리 시스템

<img src="https://github.com/user-attachments/assets/e35037b0-1ebd-47d9-8f68-0e7fbf5c5185" width="1200"/>

Notion으로 보기: <a href="https://www.notion.so/teamsparta/7-Table-Now-1e32dc3ef514806f9160e0e3f16041fd#1e52dc3ef51480d28048de0d958e1f2f"><code>🍀 Table-Now</code></a>

---

## 📚 목차

- [🚀 프로젝트 소개](#-프로젝트-소개)
- [🎯 핵심 목표](#-핵심-목표)
- [📌 주요 기능](#-주요-기능)
- [🛠️ 사용 기술](#️-사용-기술)
- [🏗️ 아키텍처](#️-아키텍처)
- [🧠 기술적 의사결정](#-기술적-의사결정)
- [🧯 트러블 슈팅](#-트러블-슈팅)
- [🗂️ API 명세서](#-api-명세서)
- [🗂️ ERD](#-erd)
- [🧑‍💻 팀원 소개](#-팀원-소개)

---

## 🚀 프로젝트 소개

TableNow는 사용자가 식당을 예약하고 방문할 수 있도록 지원하는 실시간 예약 관리 시스템입니다.  
고객, 사장님, 관리자 각각의 요구를 충족하는 다양한 기능을 제공합니다.

---

## 🎯 핵심 목표

### ✅ 안정적인 인증 및 보안 체계 구축
- Spring Security + JWT
- Redis: Refresh Token 저장, Access Token 블랙리스트 등록
- OAuth2.0 소셜 로그인 연동

### ✅ 고속 검색 및 캐싱 성능 강화
- Elasticsearch: 검색 기능 고도화
  - 역색인 → 빠른 검색 속도 보장
  - 형태소 분석, 유사도 기반 랭킹 → 효율적인 검색 엔진 구현
- Redis: 시간대별 인기 검색어 캐싱

### ✅ 동시성 제어 및 비동기 아키텍처 구현
- Redis 기반 분산 락(Redisson)
- Rabbit MQ 사용

---

## 📌 주요 기능

![Image](https://github.com/user-attachments/assets/90b1eddb-37f4-4c96-bdcf-6b1c343f6d2b)

### 👤 사용자 / 고객 관리
- 회원 탈퇴, 프로필 이미지 등록 및 수정
- 프로필 및 가게 이미지 업로드

### 🏪 가게 운영 기능
- 가게 정보 등록/수정
- 음식 카테고리 등록 및 관리
- 사용자 평점 등록

### 📅 예약 및 알림
- 실시간 예약 등록
- 이벤트 신청 및 알림
- 빈자리 대기 등록 및 알림

### 💰 결제 및 정산
- 예약금 결제 처리
- 가게별 매출 정산 및 자동 집계

### 💬 실시간 채팅
- 예약자 ↔ 사장 1:1 채팅

---

## 🛠️ 사용 기술
#### Backend

<table>
  <tr>
    <td><img src="https://img.shields.io/badge/Java_17-007396?style=flat-square&logo=java&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white"/></td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/Web_Socket-FF6C37?style=flat-square&logo=websocket&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=flat-square&logo=spring&logoColor=white"/></td>
  </tr>
</table>

#### Security

<table>
  <tr>
    <td><img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=spring-security&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=json-web-tokens&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/OAuth2-6DB33F?style=flat-square&logo=oauth&logoColor=white"/></td>
  </tr>
</table>

#### Etc

<table>
  <tr>
    <td><img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Elasticsearch-005571?style=flat-square&logo=elasticsearch&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Kibana-E8478B?style=flat-square&logo=kibana&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Logstash-005571?style=flat-square&logo=logstash&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/TossPayments-1D7CF2?style=flat-square&logoColor=white"/></td>
  </tr>
</table>

#### Test

<table>
  <tr>
    <td><img src="https://img.shields.io/badge/JMeter-D22128?style=flat-square&logo=apache-jmeter&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Gatling-FF6600?style=flat-square&logo=gatling&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Postman-FF6C37?style=flat-square&logo=postman&logoColor=white"/></td>
  </tr>
</table>

#### DataBase & Caching

<table>
  <tr>
    <td><img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/></td>
  </tr>
</table>

#### Infra

<table>
  <tr>
    <td><img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=flat-square&logo=amazon-aws&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazon-s3&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/AWS_RDS-527FFF?style=flat-square&logo=amazon-rds&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white"/></td>
  </tr>
</table>

#### Collaborative Tool

<table>
  <tr>
    <td><img src="https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/></td>
    <td><img src="https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=slack&logoColor=white"/></td>
  </tr>
</table>

---

## 🏗️ 아키텍처

추가 예정

---

## 🧠 기술적 의사결정

<a href="https://www.notion.so/teamsparta/DB-Lock-vs-Redisson-Lock-1e52dc3ef51480d9be1bceede6bc30a1?pvs=4">🔐 동시성 제어를 위한 DB Lock vs Redisson Lock 비교</a>
<br>
<a href="https://www.notion.so/teamsparta/RDBMS-LIKE-VS-Elastic-Search-1e52dc3ef5148085af70f52d0366e2c1?pvs=4">🔎 RDBMS LIKE 검색 VS Elastic Search 의 키워드 검색</a>
<br>
<a href="https://www.notion.so/teamsparta/Kakao-Naver-WebClient-1e52dc3ef514804db2bad50ea0db2553?pvs=4">👤 소셜 로그인(Kakao, Naver)을 WebClient를 사용해서 구현한 이유</a>
<br>
<a href="https://www.notion.so/teamsparta/1-1-RabbitMQ-Relay-1e52dc3ef5148051be9fd024f97e32e5?pvs=4">💬 1:1(예약자:가게) 채팅 기능 고도화를 위해 RabbitMQ Relay를 적용한 이유</a>
<br>
<a href="https://www.notion.so/teamsparta/Waitlist-1e52dc3ef51480998e1bd348338c043b?pvs=4">🔒 기술적 의사 결정: Waitlist 등록 동시성 제어를 위한 락 방식 선택 배경</a>
<br>
<a href="https://www.notion.so/teamsparta/Spring-Batch-Reddison-Lock-1e52dc3ef51480a0a56dd53541b645bf?pvs=4">📗 Spring Batch와 Reddison Lock을 선택한 이유</a>
<br>
<a href="https://www.notion.so/teamsparta/Redis-RabbitMQ-1e52dc3ef51480629e0acf60363ee4d3?pvs=4">📙 Redis + RabbitMQ 기반 이벤트 오픈 구조 도입</a>

---

## 🧯 트러블 슈팅

<a href="https://www.notion.so/teamsparta/Redis-ES-1e52dc3ef51480739b47cb1ce00be803?pvs=4">⚠️ 데이터 정합성 오류: 데이터 변경 시 Redis + ES 반영</a>
<br>
<a href="https://www.notion.so/teamsparta/RabbitMQ-1e52dc3ef5148002af99c8cb2b8c9900?pvs=4">⚠️ 빈자리 알림 개선 : 스케줄러 → RabbitMQ</a>
<br>
<a href="https://www.notion.so/teamsparta/RepositoryItemReader-1e52dc3ef51480e8b68bd2ddcbff8744?pvs=4">⚠️ RepositoryItemReader의 페이징 처리 방식에 따른 데이터 변경 시 페이지 밀림 현상</a>
<br>
<a href="https://www.notion.so/teamsparta/WebSocket-Handshake-Authorization-1e52dc3ef514808aa166d3aaf58b69d2?pvs=4">⚠️ WebSocket Handshake 시 Authorization 헤더가 누락되는 이유와 테스트 환경의 한계</a>
<br>
<a href="https://www.notion.so/teamsparta/Redisson-self-invocation-1e52dc3ef51480fa898feb215960d334?pvs=4">⚠️ Redisson 락 적용 시 데이터 미반영 이슈와 self-invocation 문제</a>

---

## 🗂️ API 명세서

[API 명세서 보기](https://www.notion.so/teamsparta/1ce2dc3ef5148199b5c6ddb96bbfd911?v=1ce2dc3ef51481fb920d000c22c52449)

---

## 🗂️ ERD

[ERD 보기](https://www.erdcloud.com/p/G6gdPGhrWgMmHC4fh)
![Image](https://github.com/user-attachments/assets/3161b362-3dab-4974-8aa2-a04fd3d4d4a3)

---

## 🧑‍💻 팀원 소개

<table align="center">
  <tr>
    <td align="center" style="padding: 10px;">
      <img src="https://avatars.githubusercontent.com/u/190332955?v=4" width="100px"/><br />
      <sub>👑 리더</sub><br />
      <b>최유리</b><br />
      가게 / 카테고리<br />
      <a href="https://github.com/3uomlkh">GitHub</a>
    </td>
    <td align="center" style="padding: 10px;">
      <img src="https://avatars.githubusercontent.com/u/81231593?v=4" width="100px"/><br />
      <sub>👑 부리더</sub><br />
      <b>김한나</b><br />
      인증 / 유저 / 채팅<br />
      <a href="https://github.com/SuhyeonB">GitHub</a>
    </td>
    <td align="center" style="padding: 10px;">
      <img src="https://avatars.githubusercontent.com/u/44752186?v=4" width="100px"/><br />
      <sub>🫅 멤버</sub><br />
      <b>박성현</b><br />
      결제 / 정산<br />
      <a href="https://github.com/queenriwon">GitHub</a>
    </td>
    <td align="center" style="padding: 10px;">
      <img src="https://avatars.githubusercontent.com/u/192585473?v=4" width="100px"/><br />
      <sub>🫅 멤버</sub><br />
      <b>박성호</b><br />
      알림 / 대기목록<br />
      <a href="https://github.com/ijieun0123">GitHub</a>
    </td>
    <td align="center" style="padding: 10px;">
      <img src="https://avatars.githubusercontent.com/u/86907076?v=4" width="100px" alt="이채원"/><br />
      <sub>🫅 멤버</sub><br />
      <b>이채원</b><br />
      예약 / 이벤트<br />
      <a href="https://github.com/3uomlkh">GitHub</a><br />
    </td>
  </tr>
</table>