const fs = require('fs');
const path = require('path');

function convertSvg(svgStr) {
    const widthMatch = svgStr.match(/viewBox="0 0 ([\d\.]+) ([\d\.]+)"/);
    if (!widthMatch) return null;
    const w = widthMatch[1];
    const h = widthMatch[2];

    let result = `<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    xmlns:aapt="http://schemas.android.com/aapt"\n    android:width="${w}dp"\n    android:height="${h}dp"\n    android:viewportWidth="${w}"\n    android:viewportHeight="${h}">\n`;

    // extract gradients
    const gradients = {};
    const gradRegex = /<linearGradient id="([^"]+)" x1="([^"]+)" x2="([^"]+)" y1="([^"]+)" y2="([^"]+)"[^>]*>([\s\S]*?)<\/linearGradient>/g;
    let match;
    while ((match = gradRegex.exec(svgStr)) !== null) {
        const id = match[1];
        const stopsStr = match[6];
        const stops = [];
        const stopRegex = /<stop stop-color="([^"]+)"(?: stop-opacity="([^"]+)")? offset="([^"]+)"\/>/g;
        let sMatch;
        while ((sMatch = stopRegex.exec(stopsStr)) !== null) {
            stops.push({ color: sMatch[1], opacity: sMatch[2], offset: sMatch[3] });
        }
        gradients[id] = { id, x1: match[2], x2: match[3], y1: match[4], y2: match[5], stops };
    }

    const radRegex = /<radialGradient id="([^"]+)" cx="([^"]+)" cy="([^"]+)" r="([^"]+)"[^>]*>([\s\S]*?)<\/radialGradient>/g;
    while ((match = radRegex.exec(svgStr)) !== null) {
        // approximate radial to linear for simplicity or just use radial gradient support (available in android)
        const id = match[1];
        const stopsStr = match[5];
        const stops = [];
        const stopRegex = /<stop stop-color="([^"]+)"(?: stop-opacity="([^"]+)")? offset="([^"]+)"\/>/g;
        let sMatch;
        while ((sMatch = stopRegex.exec(stopsStr)) !== null) {
            stops.push({ color: sMatch[1], opacity: sMatch[2], offset: sMatch[3] });
        }
        gradients[id] = { id, type: 'radial', cx: match[2], cy: match[3], r: match[4], stops };
    }

    // extract styles
    const styles = {};
    const styleMatch = svgStr.match(/<style type="text\/css">([^<]+)<\/style>/);
    if (styleMatch) {
        const styleRegex = /\.([a-zA-Z0-9\-_]+)\s*{([^}]+)}/g;
        let sMatch;
        while ((sMatch = styleRegex.exec(styleMatch[1])) !== null) {
            styles[sMatch[1]] = sMatch[2];
        }
    }

    // extract paths
    const pathRegex = /<path([^>]+)(\/>|>[\s\S]*?<\/path>)/g;
    while ((match = pathRegex.exec(svgStr)) !== null) {
        const attrsStr = match[1];
        let pathData = attrsStr.match(/(?:^|\s)d="([^"]+)"/) || attrsStr.match(/(?:^|\s)android:pathData="([^"]+)"/);
        if (!pathData) continue;
        pathData = pathData[1];

        result += `    <path android:pathData="${pathData}"`;

        let fillColor, fillAlpha, strokeColor, strokeWidth, strokeOpacity;
        const fillMatch = attrsStr.match(/fill="([^"]+)"/) || attrsStr.match(/android:fillColor="([^"]+)"/);
        if (fillMatch) { fillColor = fillMatch[1]; }
        const opacityMatch = attrsStr.match(/opacity="([^"]+)"/);
        if (opacityMatch) { fillAlpha = opacityMatch[1]; }
        const fillAlphaMatch = attrsStr.match(/fill-opacity="([^"]+)"/);
        if (fillAlphaMatch) { fillAlpha = fillAlphaMatch[1]; }
        const strokeMatch = attrsStr.match(/stroke="([^"]+)"/);
        if (strokeMatch) { strokeColor = strokeMatch[1]; }
        const widthMatch = attrsStr.match(/stroke-width="([^"]+)"/);
        if (widthMatch) { strokeWidth = widthMatch[1]; }

        const classMatch = attrsStr.match(/class="([^"]+)"/);
        if (classMatch) {
            const cls = styles[classMatch[1]];
            if (cls) {
                const f = cls.match(/fill:([^;]+)/);
                if (f) fillColor = f[1];
                const s = cls.match(/stroke:([^;]+)/);
                if (s) strokeColor = s[1];
                const sw = cls.match(/stroke-width:([^;]+)/);
                if (sw) strokeWidth = sw[1];
                const o = cls.match(/opacity:([^;]+)/);
                if (o) fillAlpha = o[1];
            }
        }

        if (strokeColor && !strokeColor.includes('url')) {
            result += ` android:strokeColor="${strokeColor}"`;
        }
        if (strokeWidth) {
            result += ` android:strokeWidth="${strokeWidth}"`;
        }
        if (fillColor && !fillColor.includes('url') && fillColor !== 'none') {
            result += ` android:fillColor="${fillColor}"`;
        }
        if (fillAlpha) {
            result += ` android:fillAlpha="${fillAlpha}" android:strokeAlpha="${fillAlpha}"`;
        }

        let gradAttrs = '';
        const urlMatch = fillColor ? fillColor.match(/url\(#([^)]+)\)/) : null;
        let strokeUrlMatch = strokeColor ? strokeColor.match(/url\(#([^)]+)\)/) : null;
        let activeGradId = urlMatch ? urlMatch[1] : (strokeUrlMatch ? strokeUrlMatch[1] : null);

        if (activeGradId && gradients[activeGradId]) {
            const grad = gradients[activeGradId];
            result += `>\n        <aapt:attr name="${strokeUrlMatch ? 'android:strokeColor' : 'android:fillColor'}">\n`;
            if (grad.type === 'radial') {
                result += `            <gradient android:type="radial" android:centerX="${grad.cx}" android:centerY="${grad.cy}" android:gradientRadius="${grad.r}">\n`;
            } else {
                result += `            <gradient android:type="linear" android:startX="${grad.x1}" android:startY="${grad.y1}" android:endX="${grad.x2}" android:endY="${grad.y2}">\n`;
            }
            grad.stops.forEach(s => {
                result += `                <item android:offset="${s.offset}" android:color="${s.color}"/>\n`;
            });
            result += `            </gradient>\n        </aapt:attr>\n    </path>\n`;
        } else {
            result += `/>\n`;
        }
    }
    result += `</vector>\n`;
    return result;
}

const inputs = process.argv.slice(2);
inputs.forEach(inp => {
    const content = fs.readFileSync(inp, 'utf-8');
    const out = convertSvg(content);
    fs.writeFileSync(inp.replace('.svg', '.xml'), out);
});
