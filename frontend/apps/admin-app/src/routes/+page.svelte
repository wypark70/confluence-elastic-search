<script lang="ts">
	import { Settings, Users, BarChart, Database, Shield } from 'lucide-svelte';

	let selectedSection = $state('general');

	const adminSections = [
		{
			id: 'general',
			title: 'General Settings',
			description: 'Basic configuration options',
			icon: Settings,
			color: 'bg-blue-500 dark:bg-blue-600'
		},
		{
			id: 'users',
			title: 'User Management',
			description: 'Manage user permissions and access',
			icon: Users,
			color: 'bg-green-500 dark:bg-green-600'
		},
		{
			id: 'analytics',
			title: 'Analytics',
			description: 'Search analytics and usage statistics',
			icon: BarChart,
			color: 'bg-purple-500 dark:bg-purple-600'
		},
		{
			id: 'index',
			title: 'Index Management',
			description: 'Configure search indexing options',
			icon: Database,
			color: 'bg-orange-500 dark:bg-orange-600'
		},
		{
			id: 'security',
			title: 'Security',
			description: 'Security and compliance settings',
			icon: Shield,
			color: 'bg-red-500 dark:bg-red-600'
		}
	];

	function selectSection(sectionId: string) {
		selectedSection = sectionId;
	}
</script>

<div class="min-h-screen bg-gray-50 dark:bg-[#0d1117]">
	<!-- Header -->
	<header class="bg-white shadow-sm border-b border-gray-200 dark:bg-[#161b22] dark:border-gray-800">
		<div class="px-6 py-4">
			<h1 class="text-2xl font-semibold text-gray-900 dark:text-white">
				Elastic Search Configuration
			</h1>
			<p class="mt-1 text-sm text-gray-600 dark:text-gray-400">
				Manage and configure your Confluence Elastic Search plugin
			</p>
		</div>
	</header>

	<div class="flex">
		<!-- Sidebar -->
		<aside class="w-64 min-h-screen bg-white border-r border-gray-200 dark:bg-[#161b22] dark:border-gray-800">
			<nav class="p-4 space-y-1">
				{#each adminSections as section}
					<button
						class="w-full flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors {selectedSection === section.id 
							? 'bg-blue-50 text-blue-700 dark:bg-blue-900/50 dark:text-blue-300' 
							: 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800'}"
						onclick={() => selectSection(section.id)}
					>
						<div class="flex items-center space-x-3">
							<div class="flex h-8 w-8 items-center justify-center rounded {section.color}">
								<svelte:component this={section.icon} size={16} class="text-white" />
							</div>
							<div class="flex-1 text-left">
								<div class="font-medium">{section.title}</div>
								<div class="text-xs text-gray-500 dark:text-gray-400">{section.description}</div>
							</div>
						</div>
					</button>
				{/each}
			</nav>
		</aside>

		<!-- Main Content -->
		<main class="flex-1 p-6">
			{#if selectedSection === 'general'}
				<section>
					<h2 class="text-lg font-medium text-gray-900 dark:text-white mb-4">General Settings</h2>
					<div class="bg-white dark:bg-[#161b22] rounded-lg shadow p-6">
						<p>General configuration options will go here...</p>
					</div>
				</section>
			{:else if selectedSection === 'users'}
				<section>
					<h2 class="text-lg font-medium text-gray-900 dark:text-white mb-4">User Management</h2>
					<div class="bg-white dark:bg-[#161b22] rounded-lg shadow p-6">
						<p>User management interface will go here...</p>
					</div>
				</section>
			{:else if selectedSection === 'analytics'}
				<section>
					<h2 class="text-lg font-medium text-gray-900 dark:text-white mb-4">Analytics</h2>
					<div class="bg-white dark:bg-[#161b22] rounded-lg shadow p-6">
						<p>Analytics dashboard will go here...</p>
					</div>
				</section>
			{:else if selectedSection === 'index'}
				<section>
					<h2 class="text-lg font-medium text-gray-900 dark:text-white mb-4">Index Management</h2>
					<div class="bg-white dark:bg-[#161b22] rounded-lg shadow p-6">
						<p>Index management interface will go here...</p>
					</div>
				</section>
			{:else if selectedSection === 'security'}
				<section>
					<h2 class="text-lg font-medium text-gray-900 dark:text-white mb-4">Security</h2>
					<div class="bg-white dark:bg-[#161b22] rounded-lg shadow p-6">
						<p>Security settings will go here...</p>
					</div>
				</section>
			{/if}
		</main>
	</div>
</div>