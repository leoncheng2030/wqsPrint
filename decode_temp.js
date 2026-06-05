const zlib = require('zlib');

console.log('=== 完整解码流程分析 ===\n');

const base64Data = 'H4sIAAAAAAAEAM3WS27CMBAA0NPMoguq+fi7bEAqgkYqSqWKJRHh/keox6FVKmIWXllCZux4THhMbMCOLyAT0MQEdO0Zg/cafROKGHaY4m7UkeHAhH5DGFPH7RhZkJlT53A8pJbcSRy+ImoKnT+3+mbu81DyjH+97k3bYUgtn6wu2juNe/006XVl0+tM2xsd77yOd0HjQW9iC1OE4PU1BRgNXK4aXCa4MEwWwhWizcENRk2mLSLp7fFZU6NAoJzq4XLTxY/56nG3/3ifJdK3QQI77fe0aoTiIlE24oURcdGIQ2jeiCuNuGiUi6YbZWlED0anr9nIt08klURSJgqZyMxE6UfYsMFiGVHg5o1MpZEpG9lsZJdGj2X0a+TbJ7KVRHaViNG7MJeRWxI92Y3INW/kKo3cehlhiPdTzS+N5Mmj1v6p5iuNfNloPtXC0siU68i2/6yFSqNQ3o5mo7g0smUjQdM8UqxEiutIHMTMG1JO/VNyRSUjzVcSYe1/yHRlAjQ/gI0N+3YLAAA=';

// 第1步: Base64 解码
console.log('【步骤1】Base64 → Buffer');
const buffer = Buffer.from(base64Data, 'base64');
console.log(`  Buffer大小: ${buffer.length} 字节\n`);

// 第2步: Gzip 解压
console.log('【步骤2】Gzip 解压缩');
const rawDecoded = zlib.gunzipSync(buffer).toString('utf8');
console.log(`  解压后长度: ${rawDecoded.length} 字符`);
console.log(`  前200字符: ${rawDecoded}\n`);

// 第3步: URL 解码
console.log('【步骤3】URL 解码');
const decoded = decodeURIComponent(rawDecoded);
console.log(`  解码后长度: ${decoded.length} 字符`);
console.log(`  前300字符: ${decoded}\n`);

// 第4步: 拆分记录
console.log('【步骤4】按 RS(\\x1e) 分隔符拆分记录');
const allRecords = decoded.split('\x1e');
const header = allRecords[0]; // 报文头 [)>
const dataRecords = allRecords.slice(1).filter(r => r.trim()); // 数据记录
console.log(`  报文头: "${header}"`);
console.log(`  数据记录: ${dataRecords.length} 条\n`);

// 第5步: 分析第一条数据记录的字段
console.log('【步骤5】详细分析记录1(HH1)的字段结构');
const record1 = dataRecords[0];
const fields = record1.split('\x1d');
console.log(`  记录1包含 ${fields.length} 个字段(GS分隔):\n`);

fields.forEach((field, index) => {
  if (field) {
    let description = '';
    // 识别字段含义
    if (field === '21') description = '[报文头标识]';
    else if (field.startsWith('M')) description = '[AI(21) 序列号]';
    else if (field.startsWith('W')) description = '[AI(10) 批次号]';
    else if (field.startsWith('Bb')) description = '[箱号]';
    else if (field.startsWith('SJ')) description = '[产品型号]';
    else if (field.startsWith('6D')) description = '[AI(11) 生产日期]';
    else if (['JKJ', 'JQT'].includes(field)) description = '[供应商代码]';
    else if (field.startsWith('16Q')) description = '[AI(16Q) 数量]';
    else if (field.startsWith('1Y')) description = '[单位]';
    else if (field.startsWith('14D')) description = '[AI(14D) 到期日]';
    else if (field.startsWith('16D')) description = '[AI(16D) 生产日期]';
    else if (field.startsWith('1BA')) description = '[AI(1BA) 内部批次]';
    else if (field.startsWith('1SS')) description = '[AI(1SS) 序列号]';
    else if (field.startsWith('2Q')) description = '[AI(2Q) 数量]';
    else if (field.startsWith('6M')) description = '[AI(6M) 制造日期]';
    else if (/^[2-5]M[1-9]$/.test(field)) description = '[AI自定义字段]';
    else if (/^[2-3]B[7-9]$/.test(field)) description = '[AI自定义字段]';
    else if (field.startsWith('2S')) description = '[AI(2S9) 自定义]';
    else if (field === 'C采购订单号') description = '[AI(C) 采购订单号标识]';
    else if (field.startsWith('1C')) description = `[采购订单号值: ${field.substring(2)}]`;
    else if (field.startsWith('2Y')) description = '[AI(2Y) 行业]';
    else if (field.startsWith('3K')) description = '[AI(3K) 客户代码]';
    else if (field.startsWith('KDHLG')) description = '[完整追溯码]';
    else if (field.startsWith('^HH')) description = '[条码序号]';
    
    console.log(`  字段${String(index + 1).padStart(2, ' ')}: "${field.padEnd(25, ' ')}" ${description}`);
  }
});

// 第6步: 提取所有采购订单号
console.log('\n\n【步骤6】提取所有采购订单号');
const regex = /C采购订单号\x1d1C(\d+)/g;
let match;
let count = 1;

while ((match = regex.exec(decoded)) !== null) {
  console.log(`  记录 ${String(count).padStart(2, ' ')}: 采购订单号 = ${match[1]}`);
  count++;
}

console.log(`\n  总结: 共找到 ${count - 1} 条采购订单号`);
console.log(`  列表: 0010, 0020, 0030, 0040, 0050, 0060, 0070, 0080, 0090, 0100`);
