const z = require('zlib');

const a_b64 = 'H4sIAAAAAAAAA83WS27CMBAA0NPMoguq+fi7bEAqgkYqSqWKJRHh/keox6FVKmIWXllCZux4THhMbMCOLyAT0MQEdO0Zg/cafROKGHaY4m7UkeHAhH5DGFPH7RhZkJlT53A8pJbcSRy+ImoKnT+3+mbu81DyjH+97k3bYUgtn6wu2juNe/006XVl0+tM2xsd77yOd0HjQW9iC1OE4PU1BRgNXK4aXCa4MEwWwhWizcENRk2mLSLp7fFZU6NAoJzq4XLTxY/56nG3/3ifJdK3QQI77fe0aoTiIlE24oURcdGIQ2jeiCuNuGiUi6YbZWlED0anr9nIt08klURSJgqZyMxE6UfYsMFiGVHg5o1MpZEpG9lsZJdGj2X0a+TbJ7KVRHaViNG7MJeRWxI92Y3INW/kKo3cehlhiPdTzS+N5Mmj1v6p5iuNfNloPtXC0siU68i2/6yFSqNQ3o5mo7g0smUjQdM8UqxEiutIHMTMG1JO/VNyRSUjzVcSYe1/yHRlAjQ/gI0N+3YLAAA=';
const b_b64 = 'H4sIAAAAAAAEAM3WS27CMBAA0NPMoguq+fi7bEAqgkYqSqWKJRHh/keox6FVKmIWXllCZux4THhMbMCOLyAT0MQEdO0Zg/cafROKGHaY4m7UkeHAhH5DGFPH7RhZkJlT53A8pJbcSRy+ImoKnT+3+mbu81DyjH+97k3bYUgtn6wu2juNe/006XVl0+tM2xsd77yOd0HjQW9iC1OE4PU1BRgNXK4aXCa4MEwWwhWizcENRk2mLSLp7fFZU6NAoJzq4XLTxY/56nG3/3ifJdK3QQI77fe0aoTiIlE24oURcdGIQ2jeiCuNuGiUi6YbZWlED0anr9nIt08klURSJgqZyMxE6UfYsMFiGVHg5o1MpZEpG9lsZJdGj2X0a+TbJ7KVRHaViNG7MJeRWxI92Y3INW/kKo3cehlhiPdTzS+N5Mmj1v6p5iuNfNloPtXC0siU68i2/6yFSqNQ3o5mo7g0smUjQdM8UqxEiutIHMTMG1JO/VNyRSUjzVcSYe1/yHRlAjQ/gI0N+3YLAAA=';

const s1 = decodeURIComponent(z.gunzipSync(Buffer.from(a_b64, 'base64')).toString());
const s2 = decodeURIComponent(z.gunzipSync(Buffer.from(b_b64, 'base64')).toString());

const GS = String.fromCharCode(29);
const RS = String.fromCharCode(30);

// Normalize the header part: [)> + RS + formatId + US
const headerEnd = s1.indexOf(GS); // first GS after header
console.log('=== data1 (去掉bzsl) ===');
console.log(s1);
console.log();
console.log('=== data2 ===');
console.log(s2);
console.log();

// Split by record separator ^HH
const recs1 = s1.split('^');
const recs2 = s2.split('^');

for (let r = 0; r < Math.min(recs1.length, recs2.length); r++) {
  const r1 = recs1[r], r2 = recs2[r];
  const f1 = r1.split(GS), f2 = r2.split(GS);
  if (f1.length !== f2.length) {
    console.log(`记录 ${r+1}: data1字段数=${f1.length}, data2字段数=${f2.length}`);
    // Show all fields
    for (let i = 0; i < Math.max(f1.length, f2.length); i++) {
      const v1 = f1[i] !== undefined ? f1[i] : '(无)';
      const v2 = f2[i] !== undefined ? f2[i] : '(无)';
      if (v1 !== v2) {
        console.log(`  字段#${i}: data1="${v1}" vs data2="${v2}"`);
      }
    }
    break; // only show first differing record
  }
}

// If same count, check values
if (recs1[0].split(GS).length === recs2[0].split(GS).length) {
  console.log('✅ 字段数一致，检查字段值...');
  for (let r = 0; r < Math.min(recs1.length, recs2.length); r++) {
    const r1 = recs1[r], r2 = recs2[r];
    const f1 = r1.split(GS), f2 = r2.split(GS);
    for (let i = 0; i < f1.length; i++) {
      if (f1[i] !== f2[i]) {
        console.log(`记录${r+1} 字段#${i}: "${f1[i]}" vs "${f2[i]}"`);
      }
    }
  }
}
