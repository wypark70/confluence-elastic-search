"use strict";
// Theme management utilities
Object.defineProperty(exports, "__esModule", { value: true });
exports.theme = void 0;
var ThemeStore = /** @class */ (function () {
    function ThemeStore() {
        this.mode = 'light';
        this.isBrowser = typeof window !== 'undefined';
        this.init();
    }
    ThemeStore.prototype.init = function () {
        if (this.isBrowser) {
            // Load from localStorage or system preference
            var saved = localStorage.getItem('theme');
            if (saved) {
                this.mode = saved;
            }
            else {
                this.mode = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
            }
            this.apply();
        }
    };
    ThemeStore.prototype.toggle = function () {
        this.mode = this.mode === 'light' ? 'dark' : 'light';
        this.save();
        this.apply();
    };
    ThemeStore.prototype.set = function (mode) {
        this.mode = mode;
        this.save();
        this.apply();
    };
    ThemeStore.prototype.save = function () {
        if (this.isBrowser) {
            localStorage.setItem('theme', this.mode);
        }
    };
    ThemeStore.prototype.apply = function () {
        if (this.isBrowser) {
            if (this.mode === 'dark') {
                document.documentElement.classList.add('dark');
            }
            else {
                document.documentElement.classList.remove('dark');
            }
        }
    };
    return ThemeStore;
}());
exports.theme = new ThemeStore();
