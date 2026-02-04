<script>
  import { initializeApp } from 'firebase/app';
  import { getFirestore, collection, doc, setDoc, onSnapshot } from 'firebase/firestore';
  import { getAuth, signInAnonymously, onAuthStateChanged } from 'firebase/auth';

  // --- Props ---
  let { 
    placeholder = "사용자 검색 (2자 이상)...",
    value = $bindable([]) // Selected items
  } = $props();

  // --- Firebase Setup ---
  const firebaseConfig = JSON.parse(__firebase_config);
  const appId = typeof __app_id !== 'undefined' ? __app_id : 'default-app-id';
  const app = initializeApp(firebaseConfig);
  const db = getFirestore(app);
  const auth = getAuth(app);

  // --- Internal State ---
  let user = $state(null);
  let searchTerm = $state("");
  let rawApiOptions = $state([]);
  let rawRecentItems = $state([]);
  let isLoading = $state(false);
  let isOpen = $state(false);
  let containerRef = $state(null);

  // --- Derived State (Exclusion Logic) ---
  // Filter out already selected items from Recent History
  const filteredRecent = $derived(
    rawRecentItems.filter(item => !value.some(v => v.id === item.id))
  );

  // Filter out already selected items from Search Results
  const filteredApiOptions = $derived(
    rawApiOptions.filter(item => !value.some(v => v.id === item.id))
  );

  const showRecent = $derived(searchTerm.length < 2);

  // --- Firebase Auth ---
  $effect(() => {
    signInAnonymously(auth);
    const unsubscribe = onAuthStateChanged(auth, u => user = u);
    return () => unsubscribe();
  });

  // --- Sync History ---
  $effect(() => {
    if (!user) return;
    const historyCol = collection(db, 'artifacts', appId, 'users', user.uid, 'recent_history');
    const unsubscribe = onSnapshot(historyCol, (snapshot) => {
      let items = snapshot.docs.map(doc => doc.data());
      rawRecentItems = items.sort((a, b) => b.timestamp - a.timestamp).slice(0, 10);
    });
    return () => unsubscribe();
  });

  // --- API Fetch with Debounce ---
  $effect(() => {
    if (searchTerm.length >= 2) {
      isLoading = true;
      const timeoutId = setTimeout(async () => {
        try {
          const res = await fetch(`https://jsonplaceholder.typicode.com/users?q=${encodeURIComponent(searchTerm)}`);
          const data = await res.json();
          rawApiOptions = data.map(u => ({ id: u.id, label: u.name, sub: u.email }));
        } catch (e) {
          rawApiOptions = [];
        } finally {
          isLoading = false;
        }
      }, 500);
      return () => clearTimeout(timeoutId);
    } else {
      rawApiOptions = [];
      isLoading = false;
    }
  });

  // --- Handlers ---
  async function selectOption(opt) {
    if (!value.find(v => v.id === opt.id)) {
      value = [...value, opt];
    }
    if (user) {
      const docRef = doc(db, 'artifacts', appId, 'users', user.uid, 'recent_history', String(opt.id));
      await setDoc(docRef, { ...opt, timestamp: Date.now() });
    }
    searchTerm = "";
  }

  function removeOption(id) {
    value = value.filter(v => v.id !== id);
  }

  function handleClickOutside(e) {
    if (containerRef && !containerRef.contains(e.target)) isOpen = false;
  }
</script>

<svelte:window onclick={handleClickOutside} />

<div bind:this={containerRef} class="relative w-full max-w-lg font-sans">
  <!-- Multi-Select Input Box -->
  <div 
    class="min-h-[56px] w-full flex flex-wrap gap-2 p-2.5 rounded-2xl border transition-all duration-300
           bg-white dark:bg-gray-900 border-gray-200 dark:border-gray-800
           focus-within:ring-4 focus-within:ring-blue-500/10 focus-within:border-blue-500
           {isOpen ? 'shadow-xl border-blue-400' : 'shadow-sm'}"
  >
    {#each value as item}
      <span class="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-bold rounded-xl
                   bg-blue-600 text-white shadow-sm animate-in zoom-in-95">
        {item.label}
        <button onclick={() => removeOption(item.id)} class="hover:bg-blue-700 rounded-full p-0.5">
          <svg class="w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="4"><path d="M18 6 6 18M6 6l12 12"/></svg>
        </button>
      </span>
    {/each}

    <input
      type="text"
      bind:value={searchTerm}
      onfocus={() => isOpen = true}
      placeholder={value.length === 0 ? placeholder : ""}
      class="flex-1 min-w-[140px] bg-transparent border-none outline-none p-1 text-gray-900 dark:text-gray-100 placeholder-gray-400"
    />

    <div class="flex items-center pr-1">
      {#if isLoading}
        <div class="animate-spin h-4 w-4 border-2 border-blue-500 border-t-transparent rounded-full mr-2"></div>
      {/if}
      <svg class="w-5 h-5 text-gray-400 transition-transform {isOpen ? 'rotate-180' : ''}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg>
    </div>
  </div>

  <!-- Dropdown Menu -->
  {#if isOpen}
    <div class="absolute z-50 w-full mt-3 bg-white dark:bg-gray-900 rounded-3xl shadow-[0_20px_60px_rgba(0,0,0,0.15)] border border-gray-100 dark:border-gray-800 overflow-hidden">
      <div class="max-h-80 overflow-y-auto p-2">
        
        {#if showRecent}
          <div class="px-4 py-3 text-[11px] font-black text-gray-400 uppercase tracking-widest border-b border-gray-50 dark:border-gray-800 mb-2">
            최근 기록 (선택된 항목 제외)
          </div>
          {#if filteredRecent.length === 0}
            <div class="py-6 text-center text-sm text-gray-400">표시할 최근 기록이 없습니다.</div>
          {:else}
            {#each filteredRecent as opt}
              <button onclick={() => selectOption(opt)} class="w-full flex items-center gap-4 px-4 py-3 rounded-2xl hover:bg-gray-50 dark:hover:bg-gray-800 group transition-all">
                <div class="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center text-gray-400 group-hover:bg-blue-500 group-hover:text-white transition-all">
                  <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                </div>
                <div class="text-left">
                  <div class="font-bold text-gray-900 dark:text-gray-100">{opt.label}</div>
                  <div class="text-xs text-gray-500">{opt.sub}</div>
                </div>
              </button>
            {/each}
          {/if}

        {:else}
          <div class="px-4 py-3 text-[11px] font-black text-blue-500 uppercase tracking-widest border-b border-blue-50 dark:border-gray-800 mb-2">
            검색 결과 (선택된 항목 제외)
          </div>
          {#if filteredApiOptions.length === 0}
            <div class="py-10 text-center text-sm text-gray-400">
              {isLoading ? "검색 중..." : "사용 가능한 결과가 없습니다."}
            </div>
          {:else}
            {#each filteredApiOptions as opt}
              <button onclick={() => selectOption(opt)} class="w-full flex items-center gap-4 px-4 py-3 rounded-2xl hover:bg-blue-50 dark:hover:bg-blue-900/30 group transition-all">
                <div class="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900/40 flex items-center justify-center text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-all">
                  <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <div class="text-left">
                  <div class="font-bold text-gray-900 dark:text-gray-100 group-hover:text-blue-600">{opt.label}</div>
                  <div class="text-xs text-gray-500">{opt.sub}</div>
                </div>
              </button>
            {/each}
          {/if}
        {/if}
      </div>
    </div>
  {/if}
</div>

<style>
  ::-webkit-scrollbar { width: 5px; }
  ::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
  :global(.dark) ::-webkit-scrollbar-thumb { background: #374151; }
</style>

