# Android memo App 

### Project Tree

    src/main/java/com.ex.realcv
     ├─ camera/          # CameraX + 일본어 OCR 모듈
     │   ├─ CameraMain   # 카메라 프리뷰 + 캡처 진입 Activity
     │   └─ OCR_Utility  # ML Kit 일본어 OCR 유틸
     │
     ├─ memoMain/        # 블록 기반 메모장(할일/텍스트 블록) 모듈
     │   ├─ MemoBase         # 메모 리스트 화면
     │   ├─ MemoBlockDialog  # 블록 편집 다이얼로그
     │   ├─ BlocksAdapter    # RecyclerView 블록 편집 어댑터
     │   ├─ FileMemoRepository # JSON 파일 기반 메모 저장소
     │   └─ ...
     │
     └─ todoMain/        # 3D 행성 Todo 실험 모듈(OpenGL)
         ├─ CustomGLSurfaceView
         ├─ BaseRenderer
         ├─ Planet / SkyboxCube / ShaderUtils
         └─ ...

### 🚀 주요 기능

#### 1. 카메라 
    📷 CameraX 기반 사진 촬영
    
    🔄 EXIF 기반 이미지 회전 보정
    
    🈶 ML Kit Japanese Text Recognizer 를 활용한 일본어 텍스트 인식
    
    ⚡ OCR 연산은 모두 비동기 처리
    
    🔌 오프라인 테스트용으로 drawable 이미지 OCR 지원
    
    🔗 결과 콜백 구조로 OCR 모듈을 재사용 가능하게 설계
    
    목표 : 온라인 시 파파고 API 등을 활용한 높은 수준의 번역
#### 2. Todo List

    기본 메모 CRUD 

    스와이프 소프트삭제/완전삭제, 복구

    체크박스를 통한 Todo 체크

    내부 edit박스에서 체크박스 구현


## 🚀 기술 목표

추후 추가 예정:

    최신 안드로이드 기술 기반의 실사용 가능한 메모 앱 개발
    
    파일 기반 저장을 Room DB 기반 아키텍처로 개선
    
    Undo/Redo, 블록 메모, 검색, 태그, 음성 + OCR 등 실전 기능 구현
    
    XML 기반 UI에서 Jetpack Compose로 리팩토링
    
    안드로이드 개발자로서의 기술 역량을 증명할 만한 독립 프로젝트

### 🧱 기능 개요 (Features)
### 1️⃣ Room DB 전환 — 로컬 DB 기반 아키텍처 완성

    기존 JSON 파일 저장 방식을 제거하고 Room Database 기반으로 전환했습니다.
    
    적용 사항
    
    Entity / Dao / Repository / ViewModel 구조 정립
    
    메모-블록 간 1:N 관계 설정
    
    수정사항 자동 반영을 위한 Flow / LiveData 기반 UI 반응형 구조
    
    마이그레이션 자동화
    
    ➡ Android 앱 개발의 핵심 구조를 이해하고 적용했다는 점에서 큰 장점이 됩니다.

### 2️⃣ Undo / Redo 시스템 — 메모 편집 히스토리 기능

    Notion처럼 편집 상태 히스토리를 DB가 아닌 메모리 레벨에서 구현했습니다.
    
    구현 방식
    
    Command 패턴 기반
    
    UndoStack, RedoStack 이용
    
    각 블록 편집을 Command 단위로 기록
    
    EditText 입력 처리 시 diff 최소 반영
    
    ➡ 실무에서 어려운 UX 기능을 직접 구현하여 문제 해결 능력을 증명합니다.

### 3️⃣ 블록 타입 확장 (멀티미디어 지원)

    Notion과 유사한 블록 구조를 확장하여 다양한 타입을 지원합니다.
    
    Supported Block Types
    
    Text Block
    
    Todo Block (Checkbox)
    
    Image Block (사진 삽입)
    
    Audio Block (음성 녹음)
    
    Quote / Divider / Tag Block (확장 예정)
    
    ➡ 블록 시스템을 직접 설계해 UI/UX 커스터마이징 능력을 보여줄 수 있습니다.

### 4️⃣ 검색 + 태그 시스템 — 고급 UX 기능

    메모 전체에서 특정 단어를 빠르게 찾을 수 있도록 Full-Text Search(FTS) 기능을 추가했습니다.
    
    기능
    
    Room FTS 기반 고속 검색
    
    태그를 추가하여 메모를 그룹핑
    
    검색 결과 하이라이트 기능
    
    ➡ 실제 서비스 앱에 필요한 경험을 학습/적용했습니다.

### 5️⃣ 음성 입력 + OCR 자동 블록화 — AI 활용

    카메라로 촬영한 이미지에서 텍스트를 자동 인식(OCR)하여
    메모 블록으로 변환하는 기능을 제공합니다.
    
    적용 기술
    
    ML Kit On-Device OCR
    
    Google SpeechRecognizer API (음성 → 텍스트 변환)
    
    OCR 결과 자동 파싱 → Block Memo List로 생성
    
    ➡ 실전 AI 기능을 앱에 통합한 사례입니다.

###6️⃣ Jetpack Compose 리팩토링 — 최신 안드로이드 개발 기술

    기존 XML 기반 RecyclerView UI를 Compose 기반으로 재구현했습니다.
    
    리팩토링 요소
    
    Compose Navigation 적용
    
    LazyColumn 기반 블록 렌더링
    
    State Hoisting 기반 단방향 데이터 흐름
    
    Material3 디자인 적용
    
    ➡ XML + Compose 모두 다루는 개발자로 어필할 수 있습니다.

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


유지보수성과 확장성을 고려한 Clean Architecture Lite 형태로 구성했습니다.











  
