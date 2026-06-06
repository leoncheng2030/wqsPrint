const fs = require('fs');
const zlib = require('zlib');

/**
 * 解码 GZip + Base64 编码的字符串
 */
function decode(encodedStr) {
    const buffer = Buffer.from(encodedStr, 'base64');
    const decoded = zlib.gunzipSync(buffer);
    return decoded.toString('utf-8');
}

/**
 * 解码并保存到文件
 */
function decodeToFile(encodedStr, outputPath) {
    const result = decode(encodedStr);
    fs.writeFileSync(outputPath, result, 'utf-8');
    console.log(`>>> 解码完成，结果已保存至：${outputPath}`);
}

// ============ 主逻辑 ============

const args = process.argv.slice(2);

if (args.length > 0) {
    // 从文件读取
    const inputFile = args[0];
    const outputFile = args[1] || inputFile + '.decoded.txt';
    const encoded = fs.readFileSync(inputFile, 'utf-8').trim();
    decodeToFile(encoded, outputFile);
} else {
    // 交互模式：从控制台读取
    console.log('请输入待解码的 Base64 字符串（粘贴后按 Ctrl+D 或 Ctrl+Z 结束）：');
    let input = '';
    process.stdin.setEncoding('utf-8');
    process.stdin.on('data', chunk => (input += chunk));
    process.stdin.on('end', () => {
        const encoded = input.trim().replace(/\s+/g, '');
        try {
            const result = decode(encoded);
            console.log('\n========================================');
            console.log('>>> 解码结果：');
            console.log(result);
            console.log('========================================');
        } catch (e) {
            console.error('>>> 解码失败：', e.message);
        }
    });
}
