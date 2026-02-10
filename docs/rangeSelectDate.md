```html
<script>
  /**
   * Svelte 5 Date Range Picker
   * Features: Range selection, Hover preview, Presets, Dark mode
   */
  import { fade, slide } from 'svelte/transition';

  // Props
  let {
    startDate = $bindable(null),
    endDate = $bindable(null),
    onSelect = null
  } = $props();

  // State
  let viewDate = $state(new Date());
  let hoverDate = $state(null);
  let isOpen = $state(false);
  let containerRef = $state(null);

  // Constants
  const daysOfWeek = ['일', '월', '화', '수', '목', '금', '토'];
  const months = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];

  // Derived: Calendar Data
  const calendarDays = $derived.by(() => {
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const days = [];
    // Prev month padding
    const prevMonthLastDay = new Date(year, month, 0).getDate();
    for (let i = firstDay - 1; i >= 0; i--) {
      days.push({ day: prevMonthLastDay - i, month: month - 1, year, current: false });
    }
    // Current month
    for (let i = 1; i <= daysInMonth; i++) {
      days.push({ day: i, month, year, current: true });
    }
    // Next month padding
    const remaining = 42 - days.length;
    for (let i = 1; i <= remaining; i++) {
      days.push({ day: i, month: month + 1, year, current: false });
    }
    return days;
  });

  // Helper Functions
  function isSameDate(d1, d2) {
    if (!d1 || !d2) return false;
    return d1.getFullYear() === d2.getFullYear() &&
           d1.getMonth() === d2.getMonth() &&
           d1.getDate() === d2.getDate();
  }

  function isInRange(date, start, end) {
    if (!start || !end) return false;
    const d = new Date(date.year, date.month, date.day).getTime();
    const s = start.getTime();
    const e = end.getTime();
    return d > Math.min(s, e) && d < Math.max(s, e);
  }

  function handleDateClick(d) {
    const selected = new Date(d.year, d.month, d.day);

    if (!startDate || (startDate && endDate)) {
      startDate = selected;
      endDate = null;
    } else {
      if (selected < startDate) {
        startDate = selected;
      } else {
        endDate = selected;
        if (onSelect) onSelect({ start: startDate, end: endDate });
        setTimeout(() => isOpen = false, 300);
      }
    }
  }

  function applyPreset(type) {
    const now = new Date();
    now.setHours(0,0,0,0);
    if (type === 'today') {
      startDate = now;
      endDate = now;
    } else if (type === 'week') {
      const first = now.getDate() - now.getDay();
      startDate = new Date(now.setDate(first));
      endDate = new Date(now.setDate(first + 6));
    } else if (type === 'month') {
      startDate = new Date(now.getFullYear(), now.getMonth(), 1);
      endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    }
    isOpen = false;
  }

  function formatDate(d) {
    if (!d) return '';
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  function changeMonth(offset) {
    viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() + offset, 1);
  }

  function handleClickOutside(e) {
    if (containerRef && !containerRef.contains(e.target)) isOpen = false;
  }
</script>

<svelte:window onclick={handleClickOutside} />

<div bind:this={containerRef} class="relative w-full max-w-sm font-sans">
  <!-- Display Field -->
  <button
    onclick={() => isOpen = !isOpen}
    class="w-full h-14 flex items-center justify-between px-4 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl shadow-sm hover:border-blue-500 transition-all group"
  >
    <div class="flex items-center gap-3">
      <div class="p-2 bg-blue-50 dark:bg-blue-900/30 rounded-lg text-blue-600 dark:text-blue-400">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
      </div>
      <div class="text-left">
        <span class="block text-[10px] font-bold text-gray-400 dark:text-gray-500 uppercase">기간 선택</span>
        <span class="text-sm font-semibold text-gray-700 dark:text-gray-200">
          {#if startDate}
            {formatDate(startDate)} {endDate ? '~ ' + formatDate(endDate) : '(종료일 선택)'}
          {:else}
            시작일 ~ 종료일
          {/if}
        </span>
      </div>
    </div>
    <svg class="w-5 h-5 text-gray-300 group-hover:text-blue-500 transition-transform {isOpen ? 'rotate-180' : ''}" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="m6 9 6 6 6-6"/></svg>
  </button>

  <!-- Calendar Dropdown -->
  {#if isOpen}
    <div
      transition:slide={{ duration: 200 }}
      class="absolute z-50 mt-3 w-[340px] bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-3xl shadow-2xl p-4 overflow-hidden"
    >
      <!-- Header -->
      <div class="flex items-center justify-between mb-4 px-1">
        <button onclick={() => changeMonth(-1)} class="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-xl transition-colors text-gray-500">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="m15 19-7-7 7-7"/></svg>
        </button>
        <span class="text-sm font-black text-gray-900 dark:text-white uppercase tracking-widest">
          {viewDate.getFullYear()}년 {months[viewDate.getMonth()]}
        </span>
        <button onclick={() => changeMonth(1)} class="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-xl transition-colors text-gray-500">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="m9 5 7 7-7 7"/></svg>
        </button>
      </div>

      <!-- Days Header -->
      <div class="grid grid-cols-7 mb-2">
        {#each daysOfWeek as day}
          <div class="text-center text-[10px] font-bold text-gray-400 dark:text-gray-500 py-1 uppercase">{day}</div>
        {/each}
      </div>

      <!-- Calendar Grid -->
      <div class="grid grid-cols-7 gap-y-1">
        {#each calendarDays as d}
          {@const fullDate = new Date(d.year, d.month, d.day)}
          {@const isSelected = isSameDate(fullDate, startDate) || isSameDate(fullDate, endDate)}
          {@const range = isInRange(d, startDate, endDate || hoverDate)}
          {@const isStart = isSameDate(fullDate, startDate)}
          {@const isEnd = isSameDate(fullDate, endDate)}

          <div class="relative py-1 flex justify-center items-center">
            <!-- Range background -->
            {#if range}
              <div class="absolute inset-y-1 left-0 right-0 bg-blue-50 dark:bg-blue-900/20"></div>
            {/if}
            {#if isStart && endDate || (isStart && hoverDate && hoverDate > startDate)}
              <div class="absolute inset-y-1 left-1/2 right-0 bg-blue-50 dark:bg-blue-900/20"></div>
            {/if}
            {#if isEnd && startDate}
              <div class="absolute inset-y-1 left-0 right-1/2 bg-blue-50 dark:bg-blue-900/20"></div>
            {/if}

            <button
              onclick={() => handleDateClick(d)}
              onmouseenter={() => startDate && !endDate ? hoverDate = fullDate : null}
              onmouseleave={() => hoverDate = null}
              class="relative w-9 h-9 flex items-center justify-center rounded-xl text-sm font-medium transition-all
                     {d.current ? 'text-gray-700 dark:text-gray-200' : 'text-gray-300 dark:text-gray-600'}
                     {isSelected ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/30' : 'hover:bg-gray-100 dark:hover:bg-gray-800'}
                     {range ? 'text-blue-600 dark:text-blue-400' : ''}"
            >
              {d.day}
            </button>
          </div>
        {/each}
      </div>

      <!-- Presets -->
      <div class="mt-4 pt-4 border-t border-gray-50 dark:border-gray-800 grid grid-cols-3 gap-2">
        <button onclick={() => applyPreset('today')} class="py-2 text-[11px] font-bold rounded-xl bg-gray-50 dark:bg-gray-800 text-gray-500 hover:bg-blue-50 hover:text-blue-600 transition-colors">오늘</button>
        <button onclick={() => applyPreset('week')} class="py-2 text-[11px] font-bold rounded-xl bg-gray-50 dark:bg-gray-800 text-gray-500 hover:bg-blue-50 hover:text-blue-600 transition-colors">이번 주</button>
        <button onclick={() => applyPreset('month')} class="py-2 text-[11px] font-bold rounded-xl bg-gray-50 dark:bg-gray-800 text-gray-500 hover:bg-blue-50 hover:text-blue-600 transition-colors">이번 달</button>
      </div>
    </div>
  {/if}
</div>
```
