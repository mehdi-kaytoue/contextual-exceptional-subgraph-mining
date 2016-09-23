/*!
 * jQuery JavaScript Library v1.11.2
 * http://jquery.com/
 *
 * Includes Sizzle.js
 * http://sizzlejs.com/
 *
 * Copyright 2005, 2014 jQuery Foundation, Inc. and other contributors
 * Released under the MIT license
 * http://jquery.org/license
 *
 * Date: 2014-12-17T15:27Z
 */




        e = function() {
            function e(e) {
                this.mousemove = t(this.mousemove, this), this.mouseleave = t(this.mouseleave, this), this.requestAnimationFrame = t(this.requestAnimationFrame, this), this.draw = t(this.draw, this);
                var n;
                this.canvasEl = $(e), this.canvas = this.canvasEl[0], this.currentBuffer = 0, this.allSeries = [], this.width = this.canvasEl.width(), this.maxWidth = this.width, this.height = this.canvasEl.height(), this.lastMousePosition = null, n = 60, this.allSeries.push(new LineotronSeries("111, 184, 255", 1, 42, this.width, n + 105, n + 210)), this.allSeries.push(new LineotronSeries("255, 117, 85", .9, 33, this.width, n + 55, n + 255)), this.allSeries.push(new LineotronSeries("89, 206, 197", .8, 25, this.width, n + 30, n + 260)), this.allSeries.push(new LineotronSeries("136, 73, 165", .7, 18, this.width, n + 10, n + 290)), this.signupButton = new LineotronSignupButton(this), this.ctx = this.canvas.getContext("2d"), this.canvasEl.mousemove(this.mousemove), this.canvasEl.mouseleave(this.mouseleave)
            }
            return e.FONT_DIN_LIGHT = 'DINLight, "Helvetica Neue", HelveticaNeue, "Helvetica-Neue", Helvetica, "BBAlpha Sans", sans-serif', e.FONT_DIN_PRO = 'DinProRegular, "Helvetica Neue", HelveticaNeue, "Helvetica-Neue", Helvetica, "BBAlpha Sans", sans-serif', e.DATA_WIDTH_TIME_MS = 1e3, e.DATA_FADE_START_TIME_MS = 400, e.DATA_FADE_MS = 500, e.DATA_FADE_OPACITY_CHANGE = .65, e.NUM_GRIDLINES = 9, e.GRIDLINE_FADE_START_MS = 500, e.GRIDLINE_FADE_MS = 500, e.GRIDLINE_SPACING_MS = 70, e.GRIDLINE_FULL_WIDTH_MS = 250, e.GRIDLINE_BASE_COLOR = "200, 200, 200", e.GRIDLINE_BASE_OPACITY = 1, e.GRIDLINE_FADE_CHANGE = .5, e.GRIDLINE_FADE_LOW_LINES_MS = 1e3, e.GRIDLINE_FADE_LOW_THRESHOLD = 7, e.WRAP_WIDTH = 1e6, e.HEADER_TEXT_SIZE = 60, e.TITLE_START_TIME_MS = 500, e.TITLE_ANIMATION_TIME_MS = 500, e.TITLE_SLIDE_PX = 20, e.GRAY_TEXT_RGB = 183, e.TITLE_TEXT = "Type SQL, Get Charts", e.LINE_1_TEXT = "Periscope plugs directly into your databases and lets you run, save", e.LINE_2_TEXT = "and share analyses over billions of data rows in seconds.", e.prototype.draw = function(e) {
                return null == this.startTime && (this.startTime = e), this.updateLinePosition(e - this.startTime), this.drawBackground(this.ctx), this.drawGrid(this.ctx, e - this.startTime), this.drawSeries(this.ctx, e - this.startTime), this.drawTitle(this.ctx, e - this.startTime), this.signupButton.draw(this.ctx, e - this.startTime), this.requestAnimationFrame(this.draw)
            }, e.prototype.requestAnimationFrame = function(e) {
                var t;
                return (t = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame)(e)
            }, e.prototype.updateWidth = function(e) {
                var t, n, i, r, o;
                for (this.canvasEl.css("width", Math.round(e)), this.canvas.width !== e && (this.canvas.width = e), this.width = e, i = this.allSeries, r = [], t = 0, n = i.length; n > t; t++) o = i[t], r.push(o.width = e);
                return r
            }, e.prototype.updateLinePosition = function(e) {
                var t, n, i, r, o;
                for (i = this.allSeries, r = [], t = 0, n = i.length; n > t; t++) o = i[t], r.push(o.scroll(e, this.lastMousePosition));
                return r
            }, e.prototype.drawBackground = function(e) {
                return e.clearRect(0, 0, this.width, this.height)
            }, e.prototype.drawSeries = function(t, n) {
                var i, r, o, s, a, l, u, c, d, p, h, f, m, g, v, y, b;
                for (c = this.width + LineotronSeries.STRIDE, n < e.DATA_WIDTH_TIME_MS && (c *= n / e.DATA_WIDTH_TIME_MS), n < e.DATA_FADE_START_TIME_MS ? i = 1 : n < e.DATA_FADE_START_TIME_MS + e.DATA_FADE_MS ? (g = n - e.DATA_FADE_START_TIME_MS, i = 1 - this.easeOutQuad(g, 0, 1, e.DATA_FADE_MS) * e.DATA_FADE_OPACITY_CHANGE) : i = 1 - e.DATA_FADE_OPACITY_CHANGE, p = this.allSeries, f = [], o = 0, l = p.length; l > o; o++) {
                    for (m = p[o], t.beginPath(), t.strokeStyle = m.getColor(i), t.lineWidth = 2, t.moveTo(0, m.points[0]), a = m.points[0], h = m.points, r = s = 0, u = h.length; u > s; r = ++s) {
                        if (d = h[r], !(d[0] < c || n > e.DATA_WIDTH_TIME_MS)) {
                            b = d[1] - a[1], v = c - a[0], y = d[0] - a[0], t.lineTo(Math.round(c), Math.round(a[1] + b * v / y));
                            break
                        }
                        t.lineTo(Math.round(d[0]), Math.round(d[1])), a = d
                    }
                    f.push(t.stroke())
                }
                return f
            }, e.prototype.getGridlineColor = function(t, n) {
                var i, r, o;
                return n < e.GRIDLINE_FADE_START_MS ? r = 1 : n < e.GRIDLINE_FADE_START_MS + e.GRIDLINE_FADE_MS ? (o = n - e.GRIDLINE_FADE_START_MS, r = 1 - this.easeOutQuad(o, 0, 1, e.GRIDLINE_FADE_MS) * e.GRIDLINE_FADE_CHANGE) : r = 1 - e.GRIDLINE_FADE_CHANGE, t >= e.GRIDLINE_FADE_LOW_THRESHOLD && (i = (t - e.GRIDLINE_FADE_LOW_THRESHOLD) / (e.NUM_GRIDLINES - e.GRIDLINE_FADE_LOW_THRESHOLD), n < e.GRIDLINE_FADE_LOW_LINES_MS || (n < e.GRIDLINE_FADE_LOW_LINES_MS + e.GRIDLINE_FADE_MS ? (o = n - e.GRIDLINE_FADE_LOW_LINES_MS, r *= 1 - o / e.GRIDLINE_FADE_MS * i) : r *= 1 - i)), "rgba(" + e.GRIDLINE_BASE_COLOR + ", " + e.GRIDLINE_BASE_OPACITY * r + ")"
            }, e.prototype.getGridlineWidthPercent = function(t, n) {
                var i, r, o, s;
                return r = t * e.GRIDLINE_SPACING_MS, i = t * e.GRIDLINE_SPACING_MS + e.GRIDLINE_FULL_WIDTH_MS, r > n ? s = 0 : i > n ? (o = n - r, s = this.easeOutQuad(o, 0, 1, e.GRIDLINE_FULL_WIDTH_MS)) : s = 1, s
            }, e.prototype.drawGrid = function(t, n) {
                var i, r, o, s, a;
                for (t.lineWidth = .55, s = [], i = r = 0, o = e.NUM_GRIDLINES; o >= 0 ? o >= r : r >= o; i = o >= 0 ? ++r : --r) t.beginPath(), t.strokeStyle = this.getGridlineColor(i, n), a = this.height / e.NUM_GRIDLINES * i, t.moveTo(0, a), t.lineTo(this.width * this.getGridlineWidthPercent(i, n), a), s.push(t.stroke());
                return s
            }, e.prototype.easeOutQuad = function(e, t, n, i) {
                return -n * (e /= i) * (e - 2) + t
            }, e.prototype.easeInOutQuad = function(e, t, n, i) {
                return (e /= i / 2) < 1 ? n / 2 * e * e + t : -n / 2 * (--e * (e - 2) - 1) + t
            }, e.prototype.urlHas = function(e) {
                return -1 !== window.location.href.indexOf(e)
            }, e.prototype.availableWidth = function(e) {
                var t, n, i, r;
                return null == e && (e = !1), r = jQuery(window).width(), n = 960, t = 1245, i = Math.max(r, n), e || (i = Math.min(i, t)), i
            }, e.prototype.textLeftIndent = function() {
                var t, n, i;
                return i = jQuery(window).width(), t = this.availableWidth(), n = Math.max(25, Math.round((i - t) / 2)), t < e.WRAP_WIDTH && (n += 150), n
            }, e.prototype.conditionalTextLeftIndent = function(e, t, n, i) {
                var r;
                return i ? (r = e.measureText(n).width, this.leftIndentIfCentered(r)) : t
            }, e.prototype.leftIndentIfCentered = function(e) {
                var t;
                return t = this.availableWidth(!0), Math.round((t - e) / 2)
            }, e.prototype.drawTitle = function(t, n) {
                var i, r, o, s, a, l, u, c;
                return l = 1260, o = this.textLeftIndent(), a = this.optionsForTitle(n), null != a ? (c = a.top, s = a.opacity, u = this.availableWidth() < e.WRAP_WIDTH, u && (c -= 80), r = e.HEADER_TEXT_SIZE, t.fillStyle = "rgba(255,255,255," + s + ")", t.font = r + "px " + e.FONT_DIN_LIGHT, t.fillText(e.TITLE_TEXT, this.conditionalTextLeftIndent(t, o - 8, e.TITLE_TEXT, u), c), i = e.GRAY_TEXT_RGB, t.fillStyle = "rgba(" + i + ", " + i + ", " + i + ", " + s + ")", t.font = "24px " + e.FONT_DIN_LIGHT, t.fillText(e.LINE_1_TEXT, this.conditionalTextLeftIndent(t, o - 3, e.LINE_1_TEXT, u), c + 72), t.fillText(e.LINE_2_TEXT, this.conditionalTextLeftIndent(t, o - 3, e.LINE_1_TEXT, u), c + 111)) : void 0
            }, e.prototype.optionsForTitle = function(t) {
                var n, i, r;
                return r = 185, t < e.TITLE_START_TIME_MS ? void 0 : (t < e.TITLE_START_TIME_MS + e.TITLE_ANIMATION_TIME_MS ? (i = t - e.TITLE_START_TIME_MS, n = this.easeOutQuad(i, 0, 1, e.TITLE_ANIMATION_TIME_MS), r = this.easeOutQuad(i, r - e.TITLE_SLIDE_PX, e.TITLE_SLIDE_PX, e.TITLE_ANIMATION_TIME_MS)) : n = 1, {
                    "top": r,
                    "opacity": n
                })
            }, e.prototype.mouseleave = function() {
                return this.lastMousePosition = null
            }, e.prototype.mousemove = function(e) {
                var t, n, i;
                return n = this.canvasEl.offset(), t = e.clientX - n.left, i = e.clientY - n.top, this.lastMousePosition = [t, i]
            }, e
        }(), window.Lineotron = e
