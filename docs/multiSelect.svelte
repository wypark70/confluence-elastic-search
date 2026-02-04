<script lang="ts">
  import { initializeApp } from 'firebase/app';
  import { getFirestore, collection, doc, setDoc, onSnapshot } from 'firebase/firestore';
  import { getAuth, signInAnonymously, onAuthStateChanged, type User } from 'firebase/auth';
  import { fade, scale } from 'svelte/transition';

  // --- Interfaces ---
  interface Option {
    id: string | number;
    label: string;
    sub?: string;
    timestamp?: number;
  }

  // --- Props (Svelte 5) ---
  let { 
    placeholder = "Search users (Simulation)...",
    value = $bindable<Option[]>([]) 
  } = $props<{ placeholder?: string; value: Option[] }>();

  // --- Simulation Data (Mock API) ---
  const MOCK_USERS: Option[] = [
    { id: 1, label: "James Wilson", sub: "james@example.com" },
    { id: 2, label: "Emma Watson", sub: "emma@example.com" },
    { id: 3, label: "Robert Downey", sub: "robert@example.com" },
    { id: 4, label: "Scarlett Johansson", sub: "scarlett@example.com" },
    { id: 5, label: "Chris Evans", sub: "chris@example.com" },
    { id: 6, label: "Benedict Cumberbatch", sub: "benedict@example.com" },
    { id: 7, label: "Elizabeth Olsen", sub: "elizabeth@example.com" },
    { id: 8, label: "Tom Holland", sub: "tom@example.com" }
  ];

  // --- Firebase Setup ---
  const firebaseConfig = JSON.parse(__firebase_config);
  const appId = typeof __app_id !== 'undefined' ? __app_id : 'default-app-id';
  const app = initializeApp(firebaseConfig);
  const db = getFirestore(app);
  const auth = getAuth(app);

  // --- States (Runes) ---
  let currentUser = $state<User | null>(null);
  let searchTerm = $state("");
  let rawApiOptions = $state<Option[]>([]);
  let rawRecentItems = $state<Option[]>([]);
  let isLoading = $state(false);
  let isOpen = $state(false);
  let containerRef = $state<HTMLDivElement | null>(null);

  // --- Derived States ---
  const filteredRecent = $derived(
    rawRecentItems.filter(item => !value.some(v => v.id === item.id))
  );
  
  const filteredApiOptions = $derived(
    rawApiOptions.filter(item => !value.some(v => v.id === item.id))
  );

  // --- Firebase Auth & Sync ---
  $effect(() => {
    signInAnonymously(auth);
    return onAuthStateChanged(auth, u => currentUser = u);
  });

  $effect(() => {
    if (!currentUser) return;
    const historyCol = collection(db, 'artifacts', appId, 'users', currentUser.uid, 'recent_history');
    return onSnapshot(historyCol, (snap) => {
      const items = snap.docs.map(d => d.data() as Option);
      rawRecentItems = items.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0)).slice(0, 10);
    });
  });

  // --- API Simulation Function ---
  async function simulateApiFetch(query: string): Promise<Option[]> {
    return new Promise((resolve) => {
      // 500ms latency simulation
      setTimeout(() => {
        const filtered = MOCK_USERS.filter(user => 
          user.label.toLowerCase().includes(query.toLowerCase()) || 
          user.sub?.toLowerCase().includes(query.toLowerCase())
        );
        resolve(filtered);
      }, 500);
    });
  }

  // --- Debounce Logic ---
  $effect(() => {
    if (searchTerm.length >= 2) {
      isLoading = true;
      const timer = setTimeout(async () => {
        rawApiOptions = await simulateApiFetch(searchTerm);
        isLoading = false;
      }, 300); // Debounce delay
      return () => clearTimeout(timer);
    } else {
      rawApiOptions = [];
      isLoading = false;
    }
  });

  // --- Handlers ---
  async function select(opt: Option) {
    if (!value.find(v => v.id === opt.id)) value = [...value, opt];
    if (currentUser) {
      const docRef = doc(db, 'artifacts', appId, 'users', currentUser.uid, 'recent_history', String(opt.id));
      await setDoc(docRef, { ...opt, timestamp: Date.now() });
    }
    searchTerm = "";
  }

  function remove(id: string | number) {
    value = value.filter(v => v.id !== id);
  }

  function handleClickOutside(e: MouseEvent) {
    if (containerRef && !containerRef.contains(e.target as Node)) {
      isOpen = false;
    }
  }
</script>

<svelte:window onclick={handleClickOutside} />

<div bind:this={containerRef} class="relative w-full max-w-lg font-sans">
  <!-- Input Container -->
  <div 
    class="min-h-[56px] w-full flex flex-wrap gap-2 p-2.5 rounded-3xl border transition-all duration-300
           bg-white dark:bg-gray-900 border-gray-200 dark:border-gray-800
           focus-within:ring-4 focus-within:ring-blue-500/10 focus-within:border-blue-500
           {isOpen ? 'shadow-xl' : 'shadow-sm'}"
  >
    {#each value as item (item.id)}
      <span in:scale={{duration: 150}} class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold rounded-xl
                   bg-blue-600 text-white shadow-sm">
        {item.label}
        <button onclick={() => remove(item.id)} class="hover:bg-blue-700 rounded-full p-0.5 transition-colors">
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
    <div in:fade={{duration: 150}} class="absolute z-50 w-full mt-3 bg-white dark:bg-gray-900 rounded-3xl shadow-2xl border border-gray-100 dark:border-gray-800 overflow-hidden">
      <div class="max-h-80 overflow-y-auto p-2 scroll-smooth">
        
        {#if searchTerm.length < 2}
          <div class="px-4 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest border-b border-gray-50 dark:border-gray-800 mb-2">
            최근 기록
          </div>
          {#if filteredRecent.length === 0}
            <div class="py-6 text-center text-sm text-gray-400 italic">표시할 기록이 없습니다.</div>
          {:else}
            {#each filteredRecent as opt (opt.id)}
              <button onclick={() => select(opt)} class="w-full flex items-center gap-4 px-4 py-3 rounded-2xl hover:bg-gray-50 dark:hover:bg-gray-800 group transition-all">
                <div class="w-10 h-10 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center text-slate-400 group-hover:bg-blue-600 group-hover:text-white transition-all">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                </div>
                <div class="text-left">
                  <div class="font-bold text-gray-900 dark:text-gray-100 text-sm">{opt.label}</div>
                  <div class="text-xs text-gray-500">{opt.sub}</div>
                </div>
              </button>
            {/each}
          {/if}

        {:else}
          <div class="px-4 py-3 text-[10px] font-black text-blue-500 uppercase tracking-widest border-b border-blue-50 dark:border-gray-800 mb-2">
            시뮬레이션 검색 결과
          </div>
          {#if filteredApiOptions.length === 0}
            <div class="py-10 text-center text-sm text-gray-400">
              {isLoading ? "검색 중..." : "결과가 없습니다."}
            </div>
          {:else}
            {#each filteredApiOptions as opt (opt.id)}
              <button onclick={() => select(opt)} class="w-full flex items-center gap-4 px-4 py-3 rounded-2xl hover:bg-blue-50 dark:hover:bg-blue-900/30 group transition-all">
                <div class="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900/40 flex items-center justify-center text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-all">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                </div>
                <div class="text-left">
                  <div class="font-bold text-gray-900 dark:text-gray-100 text-sm group-hover:text-blue-600">{opt.label}</div>
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
  ::-webkit-scrollbar { width: 4px; }
  ::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
  :global(.dark) ::-webkit-scrollbar-thumb { background: #334155; }
</style>