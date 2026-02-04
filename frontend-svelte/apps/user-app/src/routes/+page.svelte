<script lang="ts">
	/**
	 * Confluence Search - Single Page Implementation
	 * @description 모든 기능을 하나의 페이지에 통합
	 */

	// --- 다크모드 상태 ---
	let isDarkMode = $state(false);

	// --- 검색 상태 ---
	let searchQuery = $state('marketing strategy');
	let hasSearched = $state(true);

	// --- 타입 정의 ---
	interface SearchResult {
		id: string;
		title: string;
		excerpt: string;
		space: string;
		date: string;
		type: 'page' | 'blog' | 'attachment' | 'space' | 'excel' | 'powerpoint';
	}

	// --- 모의 결과 데이터 ---
	const results: SearchResult[] = [
		{
			id: '1',
			title: 'Strategy FY19',
			excerpt:
				'This page outlines the Product Marketing strategy for FY19, including the OKRs and strategic initiatives for each quarter.',
			space: 'Product Marketing',
			date: 'Jan 07, 2019',
			type: 'page'
		},
		{
			id: '2',
			title: 'Channel Marketing Budget.xlsx',
			excerpt:
				'Channel Marketing budget Channel marketing budget Anticipated sales total ($AUD) 750 200 500 1600 1200 1500',
			space: 'Product Marketing / ... / Comms and marketing',
			date: 'Jan 2019',
			type: 'excel'
		},
		{
			id: '3',
			title: 'Marketing brand refresh - project overview.pptx',
			excerpt:
				'Brand refresh – project plan Brand and marketing team Last year, the marketing team launched a new project to refresh our brand',
			space: 'Comms and Marketing / ... / Brand guidelines',
			date: 'Jan 03, 2019',
			type: 'powerpoint'
		},
		{
			id: '4',
			title: 'Marketing assets',
			excerpt:
				'Find all the marketing assets you need. Download hi res logo files, branded slide decks and illustrations.',
			space: 'Product Marketing',
			date: 'Jan 03, 2019',
			type: 'page'
		},
		{
			id: '5',
			title: 'Comms and Marketing',
			excerpt:
				'The Communications and Marketing team looks after our brand guidelines, including logos, typefaces and other assets.',
			space: 'Space',
			date: '',
			type: 'space'
		}
	];

	// Initialize theme from localStorage
	$effect(() => {
		const savedTheme = localStorage.getItem('theme');
		if (savedTheme === 'dark') {
			isDarkMode = true;
		} else if (savedTheme === 'light') {
			isDarkMode = false;
		} else {
			// Auto-detect system preference
			isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;
		}
	});

	// Apply theme using AUI Design Tokens API
	$effect(() => {
		// Wait for AUI to be loaded
		if (isDarkMode) {
			// Fallback: use data-color-mode attribute
			document.documentElement.setAttribute('data-color-mode', 'dark');
			localStorage.setItem('theme', 'dark');
		} else {
			document.documentElement.setAttribute('data-color-mode', 'light');
			localStorage.setItem('theme', 'light');
		}
	});

	// --- AUI Select2 Initialization ---
	$effect(() => {
		if (typeof window !== 'undefined' && (window as any).AJS && (window as any).AJS.$) {
			const jq = (window as any).AJS.$;
			// Initialize once
			jq(document).ready(function () {
				jq('#select2-example').auiSelect2();
			});
		}
	});

	function toggleTheme() {
		isDarkMode = !isDarkMode;
	}

	function handleSearch(e: Event) {
		e.preventDefault();
		hasSearched = true;
	}

	function clearSearch() {
		searchQuery = '';
	}
</script>

<svelte:head>
	<title>CQL Search - Confluence</title>
</svelte:head>

<!-- App Container -->
<div id="app">
	<!-- AUI Header -->
	<header id="header" class="aui-header aui-dropdown2-trigger-group">
		<div class="aui-header-inner">
			<div class="aui-header-primary">
				<h1 class="aui-header-logo aui-header-logo-textonly">
					<a href="/">CQL Search</a>
				</h1>
				<form class="aui-form" onsubmit={handleSearch}>
					<div class="field-group">
						<input
							type="text"
							class="text long-field search-input-large"
							placeholder="Search"
							bind:value={searchQuery}
						/>
						{#if searchQuery}
							<button
								type="button"
								class="aui-button aui-button-link clear-button"
								onclick={clearSearch}
							>
								✕
							</button>
						{/if}
					</div>
				</form>
			</div>
			<div class="aui-header-secondary">
				<button
					class="aui-button aui-button-subtle"
					onclick={toggleTheme}
					title={isDarkMode ? 'Switch to light mode' : 'Switch to dark mode'}
				>
					{#if isDarkMode}
						☀️
					{:else}
						🌙
					{/if}
				</button>
			</div>
		</div>
	</header>

	<!-- Main Content -->
	<main>
		<!-- AUI Page Panel with Sidebar -->
		<div class="aui-page-panel">
			<div class="aui-page-panel-inner">
				<!-- Left Sidebar - AUI Navigation -->
				<aside class="aui-page-panel-sidebar sidebar-fixed">
					<div class="aui-navgroup aui-navgroup-vertical">
						<div class="aui-navgroup-inner">
							<div class="aui-nav-heading"><strong>FILTER BY</strong></div>
						</div>

						<!-- Space Filter -->
						<div class="aui-navgroup-inner">
							<ul class="aui-nav">
								<select class="aui-select select-full-width" id="select2-example" multiple>
									<option value="CONF">Confluence</option>
									<option value="JIRA">JIRA</option>
									<option value="BAM">Bamboo</option>
									<option value="JAG">JIRA Agile</option>
									<option value="CAP">JIRA Capture</option>
									<option value="AUI">AUI</option>
								</select>
							</ul>
						</div>

						<!-- Contributor Filter -->
						<div class="aui-navgroup-inner">
							<ul class="aui-nav">
								<li>
									<button class="aui-button aui-button-subtle filter-button">
										<span class="aui-icon aui-icon-small aui-iconfont-person"></span>
										Contributor
										<span class="aui-icon aui-icon-small aui-iconfont-chevron-down chevron-right"
										></span>
									</button>
								</li>
							</ul>
						</div>

						<!-- Type Filter -->
						<div class="aui-navgroup-inner">
							<ul class="aui-nav">
								<li>
									<button class="aui-button aui-button-subtle filter-button">
										<span class="aui-icon aui-icon-small aui-iconfont-blogroll"></span>
										Type
										<span class="aui-icon aui-icon-small aui-iconfont-chevron-down chevron-right"
										></span>
									</button>
								</li>
							</ul>
						</div>

						<!-- Date Filter -->
						<div class="aui-navgroup-inner">
							<ul class="aui-nav">
								<li>
									<button class="aui-button aui-button-subtle filter-button">
										<span class="aui-icon aui-icon-small aui-iconfont-calendar"></span>
										Date
										<span
											class="aui-icon aui-icon-small aui-iconfont-chevron-down"
											style="float: right;"
										></span>
									</button>
								</li>
							</ul>
						</div>

						<!-- Label Filter -->
						<div class="aui-navgroup-inner">
							<ul class="aui-nav">
								<li>
									<button class="aui-button aui-button-subtle filter-button">
										<span class="aui-icon aui-icon-small aui-iconfont-label"></span>
										Label
										<span
											class="aui-icon aui-icon-small aui-iconfont-chevron-down"
											style="float: right;"
										></span>
									</button>
								</li>
							</ul>
						</div>

						<!-- Space Category Filter -->
						<div class="aui-navgroup-inner">
							<ul class="aui-nav">
								<li>
									<button class="aui-button aui-button-subtle filter-button">
										<span class="aui-icon aui-icon-small aui-iconfont-component"></span>
										Space category
										<span
											class="aui-icon aui-icon-small aui-iconfont-chevron-down"
											style="float: right;"
										></span>
									</button>
								</li>
							</ul>
						</div>

						<!-- Advanced Search -->
						<div class="aui-navgroup-inner">
							<ul class="aui-nav">
								<li>
									<a href="#advanced" class="aui-nav-item">
										<span class="aui-icon aui-icon-small aui-iconfont-search"></span>
										Advanced search
									</a>
								</li>
							</ul>
						</div>
					</div>
				</aside>

				<!-- Main Content -->
				<section class="aui-page-panel-content">
					<!-- Search Input -->
					<div class="aui-page-header">
						<div class="aui-page-header-inner">
							<div class="aui-page-header-main">
								<form class="aui-form" onsubmit={handleSearch}>
									<div class="field-group">
										<input
											type="text"
											class="text long-field"
											placeholder="Search"
											bind:value={searchQuery}
											style="font-size: 16px;"
										/>
										{#if searchQuery}
											<button
												type="button"
												class="aui-button aui-button-link"
												onclick={clearSearch}
												style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%);"
											>
												✕
											</button>
										{/if}
									</div>
								</form>
							</div>
						</div>
					</div>

					<!-- Results Header -->
					<div class="aui-page-header">
						<div class="aui-page-header-inner">
							<div class="aui-page-header-main">
								<p class="result-count">18 search results</p>
							</div>
							<div class="aui-page-header-actions">
								<a href="#tips" class="aui-button aui-button-link">Search tips</a>
							</div>
						</div>
					</div>

					<!-- Search Results -->
					<div class="results-container">
						{#each results as result}
							<a href="#result-{result.id}" class="result-item">
								<div class="result-icon">
									<span class="aui-avatar aui-avatar-large">
										<span class="aui-avatar-inner">
											<span
												class="aui-icon aui-icon-large aui-iconfont-{result.type === 'page'
													? 'page-default'
													: result.type === 'excel'
														? 'table'
														: result.type === 'powerpoint'
															? 'blogroll'
															: result.type === 'space'
																? 'space-default'
																: 'page-default'}"
											></span>
										</span>
									</span>
								</div>
								<div class="result-content">
									<h3 class="result-title">
										{result.title}
									</h3>
									<p class="result-meta">
										<strong>{result.space}</strong>
										{#if result.date}
											<span class="result-meta-date">{result.date}</span>
										{/if}
									</p>
									<p class="result-excerpt">
										{result.excerpt}
									</p>
								</div>
							</a>
						{/each}
					</div>
				</section>
			</div>
		</div>
	</main>
</div>

<style>
	/* Layout */
	.field-group {
		position: relative;
	}

	.sidebar-fixed {
		width: 18rem;
		flex-shrink: 0;
	}

	/* Search Input */
	.search-input-large {
		font-size: 16px;
	}

	.clear-button {
		position: absolute;
		right: 10px;
		top: 50%;
		transform: translateY(-50%);
	}

	/* Filter Buttons */
	.filter-button {
		width: 100%;
		text-align: left;
	}

	.chevron-right {
		float: right;
	}

	/* Select */
	.select-full-width {
		width: 100%;
	}

	/* Select2 Dark Mode Global Overrides */
	:global([data-color-mode='dark'] .select2-container-multi .select2-choices) {
		background-color: #1d2125; /* Dark background */
		border-color: #454f59; /* Dark border */
		color: #b6c2cf; /* Light text */
	}

	:global(
		[data-color-mode='dark'] .select2-container-multi .select2-choices .select2-search-choice
	) {
		background-color: #2c333a;
		border-color: #454f59;
		color: #b6c2cf;
	}

	:global([data-color-mode='dark'] .select2-dropdown-open .select2-choices) {
		background-color: #1d2125;
		border-color: #579dff; /* Focus color */
	}

	:global([data-color-mode='dark'] .select2-drop) {
		background-color: #1d2125;
		border-color: #454f59;
		color: #b6c2cf;
	}

	:global([data-color-mode='dark'] .select2-results .select2-result-label) {
		color: #b6c2cf;
	}

	:global([data-color-mode='dark'] .select2-results .select2-highlighted) {
		background-color: #2c333a;
		color: #fff;
	}

	/* Search Results */
	.results-container {
		display: flex;
		flex-direction: column;
		gap: 12px;
	}

	.result-item {
		display: flex;
		gap: 16px;
		padding: 12px;
		border-radius: 3px;
		text-decoration: none;
		color: inherit;
		transition: background 0.2s;
	}

	.result-icon {
		flex-shrink: 0;
		width: 40px;
		height: 40px;
	}

	.result-content {
		flex: 1;
		min-width: 0;
	}

	.result-title {
		margin: 0 0 4px 0;
		font-size: 16px;
		font-weight: 600;
	}

	.result-meta {
		color: #6b778c;
		font-size: 12px;
		margin: 0 0 8px 0;
	}

	.result-meta-date {
		margin-left: 8px;
	}

	.result-excerpt {
		color: #42526e;
		font-size: 14px;
		margin: 0;
		line-height: 1.5;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		-webkit-box-orient: vertical;
	}

	.result-count {
		color: #6b778c;
		font-size: 12px;
		margin: 0;
	}
</style>
