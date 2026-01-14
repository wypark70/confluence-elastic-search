import descriptions from './SampleDescriptions';

const baseResults = generateRandomIssues('', 1000);

function getSampleResponse(query = '', currentPage = 1) {
	const searchResults = baseResults.filter((issue) => {
		return (
			issue.title.toLowerCase().includes(query.toLowerCase()) ||
			issue.snippet.toLowerCase().includes(query.toLowerCase()) ||
			issue.space.toLowerCase().includes(query.toLowerCase())
		);
	});
	const totalResults = searchResults.length;
	const pageSize = 10;
	const totalPages = Math.ceil(totalResults / pageSize);
	const sliceResults = searchResults.slice((currentPage - 1) * pageSize, currentPage * pageSize);
	const results = sliceResults.map((issue) => {
		return {
			...issue,
			highlightValues: [query]
		};
	});

	return {
		results,
		totalResults,
		currentPage,
		pageSize,
		totalPages
	};
}

function generateRandomIssueTitle() {
	// 1. 단어장 정의 (프로젝트 성격에 맞춰 수정 가능)
	const prefixes = ['[FE]', '[BE]', '[Design]', '[Hotfix]', '[Refactor]', '[Docs]'];

	const targets = [
		'로그인 페이지',
		'결제 모듈',
		'네비게이션 바',
		'메인 대시보드',
		'회원가입 폼',
		'API 응답 속도',
		'검색 필터',
		'유저 프로필 이미지',
		'다크 모드',
		'알림 설정 창',
		'푸시 알림'
	];

	const problems = [
		'반응형 깨짐 현상',
		'클릭 시 무반응',
		'데이터 로딩 지연',
		'404 에러 발생',
		'오타 수정',
		'레이아웃 정렬 불량',
		'유효성 검사 실패',
		'간헐적 크래시',
		'토큰 만료 문제'
	];

	const actions = ['수정', '개선', '추가', '삭제', '리팩토링', '원인 파악 및 해결', '업데이트'];

	// 2. 랜덤 선택 헬퍼 함수
	const pick = (arr: string[]) => arr[Math.floor(Math.random() * arr.length)];

	// 3. 패턴 조합 (랜덤하게 패턴을 섞어서 더 자연스럽게 만듭니다)
	const patternType = Math.random();

	let title = '';

	if (patternType < 0.33) {
		// 패턴 A: [대상] + [문제] + [행동] (예: 로그인 페이지 404 에러 발생 수정)
		title = `${pick(targets)} ${pick(problems)} ${pick(actions)}`;
	} else if (patternType < 0.66) {
		// 패턴 B: [대상] + [행동] (예: 결제 모듈 리팩토링)
		title = `${pick(targets)} ${pick(actions)}`;
	} else {
		// 패턴 C: [행동] + [대상] (예: 추가 다크 모드 -> 다크 모드 추가 *조사 처리가 복잡하므로 단순 나열*)
		// 한국어 어순상 자연스럽게: [대상] 관련 [행동]
		title = `${pick(targets)} 관련 ${pick(actions)} 작업`;
	}

	// 4. 말머리(Prefix) 붙이기
	return `${pick(prefixes)} ${title}`;
}

function generateRandomDescription(query = '') {
	// 1. 문구 데이터베이스
	const contexts = [
		'현재 운영 환경에서 간헐적으로 발생하고 있습니다.',
		'QA 테스트 도중 발견된 이슈입니다.',
		'사용자로부터 접수된 CS 문의 내용을 바탕으로 작성했습니다.',
		'기존 레거시 코드 리팩토링 중 사이드 이펙트로 추정됩니다.',
		'배포 직후 모니터링 툴에서 감지되었습니다.'
	];

	const stepsPool = [
		'메인 페이지 접속',
		'로그인 버튼 클릭',
		'잘못된 비밀번호 3회 입력',
		'네트워크 연결 끊기',
		'뒤로가기 버튼 클릭',
		'결제 모달 창 열기',
		'프로필 이미지 업로드 시도',
		'새로고침(F5) 수행',
		'다크 모드 토글 스위치 클릭'
	];

	const expectations = [
		'정상적으로 페이지가 로드되어야 함.',
		'에러 메시지가 붉은색으로 노출되어야 함.',
		'로딩 스피너가 사라지고 데이터가 표시되어야 함.',
		'토스트 알림이 우측 상단에 떠야 함.'
	];

	const realities = [
		'화면이 하얗게 변함 (White Screen).',
		"콘솔에 'undefined is not a function' 에러 출력.",
		'아무런 반응이 없음 (무응답).',
		'404 Not Found 페이지로 리다이렉트됨.',
		'레이아웃이 깨져서 버튼을 누를 수 없음.'
	];

	const osList = ['macOS 14.0', 'Windows 11', 'iOS 17', 'Android 14', 'Ubuntu 22.04'];
	const browserList = ['Chrome 120', 'Safari 17', 'Firefox 118', 'Edge', 'Whale Browser'];

	// 2. 헬퍼 함수
	const pick = (arr: string[]) => arr[Math.floor(Math.random() * arr.length)];

	// 재현 경로를 2~4개 랜덤하게 뽑기
	const getRandomSteps = () => {
		const stepCount = Math.floor(Math.random() * 3) + 2; // 2~4
		let steps = [];
		for (let i = 0; i < stepCount; i++) {
			steps.push(`1. ${pick(stepsPool)}`);
		}
		return steps.join('\n');
	};

	// 3. 마크다운 템플릿 조합
	// 실제 깃허브나 지라(Jira) 템플릿과 유사하게 구성
	const description = descriptions[Math.floor(Math.random() * descriptions.length)];

	return description;
}

function generateRandomIssue(query: string) {
	const types = ['page', 'issue', 'document', 'blog', 'wiki'];
	const spaces = ['Demonstration Space', 'Test Space', 'Demo Space'];
	return {
		id: 5,
		type: types[Math.floor(Math.random() * types.length)],
		title: generateRandomIssueTitle(),
		space: spaces[Math.floor(Math.random() * spaces.length)],
		date: new Date().toISOString(),
		snippet: generateRandomDescription(query),
		highlightValues: [query]
	};
}

function generateRandomIssues(query: string, size: number) {
	const issues = [];
	for (let i = 0; i < size; i++) {
		issues.push(generateRandomIssue(query));
	}
	return issues;
}

function getRandomInt(min: number, max: number) {
	const minCeiled = Math.ceil(min);
	const maxFloored = Math.floor(max);
	return Math.floor(Math.random() * (maxFloored - minCeiled + 1)) + minCeiled;
}

export default getSampleResponse;
