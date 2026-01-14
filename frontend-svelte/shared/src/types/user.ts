export interface User {
	id: string;
	username: string;
	displayName: string;
	email: string;
	avatarUrl?: string;
}

export interface UserProfile extends User {
	department?: string;
	location?: string;
	timeZone?: string;
	preferences?: UserPreferences;
}

export interface UserPreferences {
	language: string;
	theme: 'light' | 'dark' | 'auto';
	timeFormat: '12h' | '24h';
	dateFormat: string;
}
