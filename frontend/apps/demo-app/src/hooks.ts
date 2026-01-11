import { deLocalizeUrl } from '@elastic-search/shared/i18n/runtime';

export const reroute = (request) => deLocalizeUrl(request.url).pathname;
