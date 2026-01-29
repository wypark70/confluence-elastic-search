<script>
  // 1. 상태 관리 (Runes)
  let searchQuery = $state(""); // 사용자 입력값
  let debouncedQuery = $state(""); // 지연 후 검색에 사용될 값
  let results = $state([]); // 검색 결과 데이터
  let isLoading = $state(false); // 로딩 상태
  let hasSearched = $state(false); // 검색 수행 여부
  let currentPage = $state(1); // 현재 페이지
  const itemsPerPage = 5;

  // 2. 디바운스 및 자동 검색 로직 (Runes $effect)
  $effect(() => {
    // 2자 미만이면 초기화
    if (searchQuery.trim().length < 2) {
      debouncedQuery = "";
      results = [];
      hasSearched = false;
      return;
    }

    // 0.7초 타이핑 대기 (Debounce)
    const timer = setTimeout(() => {
      debouncedQuery = searchQuery;
      fetchResults(debouncedQuery);
    }, 700);

    return () => clearTimeout(timer); // 다음 입력 시 이전 타이머 취소
  });

  // 3. API 호출 시뮬레이션
  async function fetchResults(query) {
    isLoading = true;
    hasSearched = true;
    currentPage = 1; // 검색어 변경 시 1페이지로 리셋

    // 네트워크 지연 시뮬레이션 (1초)
    await new Promise(resolve => setTimeout(resolve, 1000));

    // 실제로는 여기서 fetch('/api/search?q=' + query) 호출
    results = Array.from({ length: 12 }, (_, i) => ({
      id: Math.random(),
      type: i % 3 === 0 ? 'file' : (i % 3 === 1 ? 'page' : 'blog'),
      title: `<span class="font-bold text-blue-600 dark:text-blue-400">${query}</span> 관련 전략 보고서 #${i + 1}`,
      description: `이 문서는 <span class="font-bold">${query}</span> 프로젝트의 상세 분석 결과입니다. Svelte 5와 Tailwind를 활용한 고성능 UI 샘플 데이터입니다.`,
      location: "Marketing / Brand Assets",
      date: "2026-01-29"
    }));

    isLoading = false;
  }

  // 4. 페이징 계산 (Derived Runes)
  const totalPages = $derived(Math.ceil(results.length / itemsPerPage));
  const pagedResults = $derived(results.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage));

  function changePage(p) {
    isLoading = true;
    currentPage = p;
    // 페이지 전환 시 시뮬레이션
    setTimeout(() => {
      isLoading = false;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 400);
  }
</script>

{#snippet skeleton()}
  <div class="py-6 animate-pulse">
    <div class="flex items-center gap-2 mb-3">
      <div class="w-4 h-4 bg-slate-200 dark:bg-slate-700 rounded-sm"></div>
      <div class="h-5 w-1/3 bg-slate-200 dark:bg-slate-700 rounded"></div>
    </div>
    <div class="space-y-2">
      <div class="h-4 w-full bg-slate-100 dark:bg-slate-800 rounded"></div>
      <div class="h-4 w-4/5 bg-slate-100 dark:bg-slate-800 rounded"></div>
    </div>
  </div>
{/snippet}

<div class="min-h-screen bg-white dark:bg-[#1c2128] text-slate-900 dark:text-slate-100 transition-colors duration-300 font-sans">
  
  <header class="h-14 bg-[#0747A6] dark:bg-[#172B4D] flex items-center px-6 sticky top-0 z-50 shadow-lg">
    <div class="flex items-center gap-2 mr-10 cursor-pointer">
      <div class="w-7 h-7 bg-white/20 rounded-md flex items-center justify-center font-black text-xs text-white">C</div>
      <span class="font-bold text-lg tracking-tight text-white">Confluence</span>
    </div>
    <div class="flex-1 max-w-2xl relative">
      <input 
        type="text" 
        bind:value={searchQuery}
        placeholder="검색어 입력 (2자 이상)..."
        class="w-full bg-white/20 border-none rounded-md px-4 py-2 text-sm text-white focus:bg-white focus:text-slate-900 outline-none transition-all"
      />
      {#if isLoading}
        <div class="absolute right-3 top-2.5">
          <div class="w-5 h-5 border-2 border-blue-400 border-t-transparent rounded-full animate-spin"></div>
        </div>
      {/if}
    </div>
  </header>

  <main class="flex max-w-[1260px] mx-auto p-8 gap-12">
    
    <aside class="w-64 shrink-0 hidden md:block">
      <h2 class="text-2xl font-semibold mb-8">Search</h2>
      <section class="space-y-6">
        <div>
          <label class="text-[11px] font-bold text-slate-500 uppercase tracking-wider block mb-3">Filter By Type</label>
          <div class="space-y-2">
            {#each ['All Content', 'Pages', 'Blog Posts', 'Attachments'] as type}
              <label class="flex items-center text-sm text-slate-600 dark:text-slate-400 hover:text-blue-600 cursor-pointer">
                <input type="checkbox" class="mr-2 rounded border-slate-300" /> {type}
              </label>
            {/each}
          </div>
        </div>
      </section>
    </aside>

    <section class="flex-1 border-l dark:border-slate-800 pl-12">
      
      <div class="h-10 flex items-center justify-between border-b dark:border-slate-800 mb-6">
        <span class="text-sm text-slate-500">
          {#if searchQuery.length > 0 && searchQuery.length < 2}
            <span class="text-amber-500 font-medium">⚠️ 2자 이상 입력해주세요.</span>
          {:else if hasSearched}
            "{debouncedQuery}" 결과 {results.length}건 중 {pagedResults.length}건 표시
          {:else}
            검색어를 입력하세요.
          {/if}
        </span>
      </div>

      <div class="divide-y divide-slate-100 dark:divide-slate-800">
        {#if isLoading}
          {#each Array(itemsPerPage) as _} {@render skeleton()} {/each}
        {:else if results.length > 0}
          <div class="animate-in fade-in duration-500">
            {#each pagedResults as item (item.id)}
              <article class="py-6 group">
                <div class="flex items-center gap-2 mb-1.5">
                  <span class="w-4 h-4 rounded-sm text-[10px] text-white flex items-center justify-center font-bold 
                    {item.type === 'file' ? 'bg-orange-500 italic' : (item.type === 'blog' ? 'bg-green-600' : 'bg-indigo-600')}">
                    {item.type === 'file' ? 'P' : ''}
                  </span>
                  <h3 class="text-[#0052CC] dark:text-blue-400 font-semibold text-lg hover:underline cursor-pointer">
                    {@html item.title}
                  </h3>
                </div>
                <p class="text-sm text-slate-600 dark:text-slate-400 leading-relaxed max-w-4xl">
                  {@html item.description}
                </p>
                <div class="text-[12px] text-slate-500 mt-2 flex items-center gap-2 font-medium">
                  <span>{item.location}</span> • <span>{item.date}</span>
                </div>
              </article>
            {/each}
          </div>

          <nav class="flex items-center gap-1 mt-10 pt-8 border-t dark:border-slate-800">
            <button 
              onclick={() => changePage(currentPage - 1)}
              disabled={currentPage === 1 || isLoading}
              class="px-3 py-1.5 text-sm font-medium rounded hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30">
              Previous
            </button>
            {#each Array.from({length: totalPages}, (_, i) => i + 1) as page}
              <button 
                onclick={() => changePage(page)}
                class="w-8 h-8 rounded text-sm font-medium transition-all {currentPage === page ? 'bg-[#0052CC] text-white shadow-md' : 'hover:bg-slate-100 dark:hover:bg-slate-800'}">
                {page}
              </button>
            {each}
            <button 
              onclick={() => changePage(currentPage + 1)}
              disabled={currentPage === totalPages || isLoading}
              class="px-3 py-1.5 text-sm font-medium rounded hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30">
              Next
            </button>
          </nav>
        {:else if hasSearched}
          <div class="py-20 text-center text-slate-400 italic">검색 결과가 없습니다.</div>
        {:else}
          <div class="py-20 text-center border-2 border-dashed dark:border-slate-800 rounded-xl text-slate-400">
            상단 검색창에 키워드를 입력해 주세요.
          </div>
        {/if}
      </div>
    </section>
  </main>
</div>
