# Confluence Elastic Search - Multi-SPA 아키텍처 계획

## 🎯 개요

현재 단일 SvelteKit 앱을 User App과 Admin App으로 분리하여 Confluence Data Center 환경에 최적화된 Multi-SPA 아키텍처를 구축하는 계획입니다.

## 📊 현재 vs 목표 구조

### 현재 구조
```
frontend/
├── src/routes/           # 모든 기능이 혼재된 상태
├── package.json          # 단일 앱 설정
├── vite.config.ts        # 단일 엔트리
└── svelte.config.js      # 공통 설정
```

### 목표 구조
```
frontend/
├── apps/
│   ├── user-app/         # 사용자 검색 인터페이스
│   │   ├── src/routes/   # 검색 결과, 필터링, 페이징
│   │   ├── src/lib/      # 사용자 앱 전용 컴포넌트
│   │   ├── package.json
│   │   ├── vite.config.ts
│   │   └── svelte.config.js
│   └── admin-app/        # 관리자 설정 인터페이스
│       ├── src/routes/   # 설정, 통계, 관리자 기능
│       ├── src/lib/      # 관리자 앱 전용 컴포넌트
│       ├── package.json
│       ├── vite.config.ts
│       └── svelte.config.js
├── shared/               # 공통 리소스
│   ├── src/
│   │   ├── lib/          # 공통 컴포넌트
│   │   ├── api/          # API 클라이언트
│   │   ├── types/        # TypeScript 타입
│   │   ├── utils/        # 유틸리티 함수
│   │   └── stores/       # 상태 관리
│   └── package.json
├── package.json          # 워크스페이스 루트
├── pnpm-workspace.yaml   # PNPM 워크스페이스 설정
└── vite.config.ts        # 빌드 오케스트레이터
```

## 🏗️ 기술 전략

### 1. 패키지 관리
- **pnpm workspaces** 사용 (npm 대신 성능과 디스크 효율성 우수)
- **의존성 호이스팅**으로 중복 제거
- **독립적인 버전 관리** 가능

### 2. 빌드 시스템
- **Vite multi-entry** 설정 (Webpack 대신 빠른 빌드 속도)
- **공통 vendor 청크** 생성으로 번들 크기 최적화
- **Maven frontend-maven-plugin**으로 빌드 자동화

### 3. 라우팅 전략
```
User App:  /plugins/servlet/user-app
Admin App: /plugins/servlet/admin-app
```

## 📋 구현 단계

### Phase 1: 기반 구조 (1-2일)
#### 1.1 워크스페이스 설정
- [x] pnpm-workspace.yaml 생성
- [ ] 루트 package.json 재구성
- [ ] 공유 라이브러리 스켈레톤 생성

#### 1.2 기본 앱 구조
- [ ] user-app 폴더 구조 생성
- [ ] admin-app 폴더 구조 생성
- [ ] 기본 Vite/SvelteKit 설정

### Phase 2: 공통 리소스 추출 (1-2일)
#### 2.1 공유 라이브러리 설정
- [ ] 현재 컴포넌트를 shared/로 이전
- [ ] API 클라이언트 분리
- [ ] TypeScript 타입 정리

#### 2.2 상태 관리
- [ ] 공통 stores 설정
- [ ] 테마 설정 통합
- [ ] 사용자 상태 관리

### Phase 3: 앱 분리 (2-3일)
#### 3.1 User App 구현
- [ ] 현재 검색 기능 이전
- [ ] 사용자 UI/UX 개선
- [ ] 퍼포먼스 최적화

#### 3.2 Admin App 구현
- [ ] 관리자 대시보드
- [ ] 설정 관리 인터페이스
- [ ] 통계 및 모니터링

### Phase 4: 백엔드 통합 (1-2일)
#### 4.1 Maven 빌드 통합
- [ ] frontend-maven-plugin 설정
- [ ] 다중 빌드 프로세스 구성
- [ ] 리소스 복사 자동화

#### 4.2 Confluence 설정
- [ ] Web Resource 분리
- [ ] Servlet 매핑 추가
- [ ] 권한 설정 적용

## 🔧 핵심 설정 파일

### frontend/pnpm-workspace.yaml
```yaml
packages:
  - 'apps/*'
  - 'shared'
```

### frontend/package.json
```json
{
  "name": "elastic-search-frontend",
  "private": true,
  "workspaces": [
    "apps/*",
    "shared"
  ],
  "scripts": {
    "dev": "concurrently \"pnpm --filter user-app dev\" \"pnpm --filter admin-app dev\"",
    "build": "pnpm --filter shared build && pnpm --filter user-app build && pnpm --filter admin-app build",
    "build:user": "pnpm --filter shared build && pnpm --filter user-app build",
    "build:admin": "pnpm --filter shared build && pnpm --filter admin-app build"
  }
}
```

### Maven Build Configuration
```xml
<plugin>
  <groupId>com.github.eirslett</groupId>
  <artifactId>frontend-maven-plugin</artifactId>
  <version>1.15.0</version>
  <configuration>
    <workingDirectory>frontend</workingDirectory>
  </configuration>
  <executions>
    <execution>
      <id>build user-app</id>
      <goals><goal>pnpm</goal></goals>
      <configuration>
        <arguments>build:user</arguments>
      </configuration>
    </execution>
    <execution>
      <id>build admin-app</id>
      <goals><goal>pnpm</goal></goals>
      <configuration>
        <arguments>build:admin</arguments>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## 🌐 Confluence Web Resource 설정

### User App Resources
```xml
<web-resource key="user-app-resources" name="User App">
  <dependency>com.atlassian.auiplugin:ajs</dependency>
  <resource type="download" name="user-app/" location="/user-app/"/>
  <context>elastic-search.user</context>
  <context>atl.general</context>
</web-resource>
```

### Admin App Resources
```xml
<web-resource key="admin-app-resources" name="Admin App">
  <dependency>com.atlassian.auiplugin:aui-dialog2</dependency>
  <resource type="download" name="admin-app/" location="/admin-app/"/>
  <context>elastic-search.admin</context>
  <context>atl.admin</context>
</web-resource>
```

## 🚀 개발 워크플로우

### 로컬 개발
```bash
# 전체 개발
pnpm dev

# 개별 앱 개발
pnpm dev:user     # User App만 (포트 5173)
pnpm dev:admin    # Admin App만 (포트 5174)
```

### 빌드
```bash
# 전체 빌드
mvn clean package

# 개별 빌드
mvn package -Dapp=user
mvn package -Dapp=admin
```

## 📈 기대 효과

### 1. 권한 기반 분리
- **일반 사용자**: 검색 기능에만 집중
- **관리자**: 설정과 관리 기능에만 접근

### 2. 성능 최적화
- **번들 크기 감소**: 필요한 앱만 로드
- **캐싱 효율**: 앱별 독립 캐시
- **로딩 속도**: 초기 로딩 시간 단축

### 3. 개발 생산성
- **독립 개발**: 팀별 동시 작업 가능
- **코드 재사용**: 공통 컴포넌트 공유
- **유지보수**: 관심사 분리로 유지보수 용이

### 4. 확장성
- **새로운 앱 추가**: 간단한 스켈레톤 복제
- **마이크로서비스**: 향후 서버 분리 용이
- **플러그인화**: 각 앱을 독립 플러그인으로 배포 가능

## ⚠️ 고려사항

### 1. 기술적 리스크
- 빌드 복잡성 증가
- 워크스페이스 설정 학습曲线
- Confluence 통합 테스트 필요

### 2. 해결 방안
- 철저한 테스트 자동화
- 점진적 마이그레이션 전략
- 롤백 계획 수립

### 3. 모니터링
- 앱별 성능 지표 추적
- 사용자 행동 분리 분석
- 에러 추적 및 모니터링

## 📅 마일스톤

| 단계 | 기간 | 주요 목표 | 성공 기준 |
|------|------|----------|----------|
| Phase 1 | 1-2일 | 기반 구조 | 워크스페이스 빌드 성공 |
| Phase 2 | 1-2일 | 공통 리소스 | 공통 컴포넌트 재사용 |
| Phase 3 | 2-3일 | 앱 분리 | 기능별 분리 완료 |
| Phase 4 | 1-2일 | 통합 | 배포 테스트 통과 |

## ✅ 완료 체크리스트

- [x] pnpm-workspace.yaml 설정
- [ ] 공유 라이브러리 구조 확립
- [ ] User App 기능 이전
- [ ] Admin App 신규 개발
- [ ] Maven 빌드 자동화
- [ ] Confluence Web Resource 설정
- [ ] 배포 및 테스트
- [ ] 성능 최적화
- [ ] 문서화 완료

---

이 계획은 현재 SvelteKit 기반 Confluence 플러그인을 현대적인 Multi-SPA 아키텍처로 성공적으로 전환하기 위한 로드맵입니다.