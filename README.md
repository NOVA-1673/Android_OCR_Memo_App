# Android memo App 

feature Readme

- 🇯🇵 일본어 학습 액티비티 기획  
  → [feat-3 브랜치 README 보기](/feat-3/wordCard/feat-3/README.md)

### 🚀 주요 기능

#### 1. 카메라 
    CameraX 기반 사진 촬영
    EXIF 기반 이미지 회전 보정
    🈶 ML Kit Japanese Text Recognizer 를 활용한 일본어 텍스트 인식
    OCR을 활용해 일본어 텍스트 추출        
    목표 : 카메라 촬영을 통한 일본어 추출 및 번역
#### 2. Todo List

    기본 메모 CRUD 
    스와이프 소프트삭제/완전삭제, 복구
    체크박스를 통한 Todo 체크
    내부 edit박스에서 체크박스 구현


## 🚀 기술 목표

추후 추가 예정:
    
    ~~파일 기반 저장을 Room DB 기반 아키텍처로 개선~~
    
    Undo/Redo, 블록 메모, 검색, 태그, 음성
    
    XML 기반 UI에서 Jetpack Compose로 리팩토링

    25.12.23 추가
    일본어 회화 학습을 위한 단어 카드 추가 -> 단순 메모 앱에서 직접 일본어 학습을 위한 앱으로 발전
    -> 이유 : 일본어 학습 시 여러 앱을 받아보고 도움이 되는 앱이 없어 주로 사용하는 언어 학습방법을 적용한 앱 개발하려함함

### 🧱 기능 개요 (Features)
### 1️⃣ Room DB 전환 — 로컬 DB 기반 아키텍처

    기존 JSON 파일 저장 방식을 제거하고 Room Database 기반으로 전환
    
    적용 사항
    
    Entity / Dao / Repository / ViewModel 구조 정립


### 3️⃣ 블록 타입 확장 

    Notion과 유사한 블록 구조를 확장하여 다양한 타입 추가 예정
    현재는 체크 박스 텍스트만 추가한 상태


### 5️⃣ OCR 일본어 인식 및 Google ML kit을 활용한 번역

    카메라로 촬영한 이미지에서 텍스트를 자동 인식(OCR)하여
    메모 블록으로 변환하는 기능
    
    적용 기술
    
    ML Kit On-Device OCR
    
    Google SpeechRecognizer API (음성 → 텍스트 변환)
    
    OCR 결과 자동 파싱 → Block Memo List로 생성(예정)
   


🏛️ 앱 아키텍처
presentation (UI)
 └─ ViewModel (AndroidX)

domain
 └─ UseCase
 └─ Model (BlockMemo, Memo)

data
 └─ Room Database
 └─ Dao
 └─ Repository (local source)

core
 └─ OCR Utility
 └─ Audio Utility
 └─ File Helper







  
