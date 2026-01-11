"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.formatDate = formatDate;
exports.truncateText = truncateText;
exports.highlightText = highlightText;
exports.debounce = debounce;
function formatDate(dateString) {
    var date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}
function truncateText(text, maxLength) {
    if (maxLength === void 0) { maxLength = 200; }
    if (text.length <= maxLength)
        return text;
    return text.substring(0, maxLength).trim() + '...';
}
function highlightText(text, queries) {
    var highlighted = text;
    queries.forEach(function (query) {
        var regex = new RegExp("(".concat(query, ")"), 'gi');
        highlighted = highlighted.replace(regex, '<mark>$1</mark>');
    });
    return highlighted;
}
function debounce(func, wait) {
    var timeout;
    return function () {
        var args = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            args[_i] = arguments[_i];
        }
        clearTimeout(timeout);
        timeout = setTimeout(function () { return func.apply(void 0, args); }, wait);
    };
}
